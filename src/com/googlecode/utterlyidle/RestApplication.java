package com.googlecode.utterlyidle;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.utterlyidle.handlers.AuditHandler;
import com.googlecode.utterlyidle.handlers.ContentLengthHandler;
import com.googlecode.utterlyidle.handlers.DateHandler;
import com.googlecode.utterlyidle.handlers.ExceptionHandler;
import com.googlecode.utterlyidle.modules.CoreModule;
import com.googlecode.utterlyidle.modules.Module;
import com.googlecode.utterlyidle.modules.Modules;
import com.googlecode.utterlyidle.rendering.exceptions.LastExceptionsModule;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.closeable.CloseableContainer;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.utterlyidle.RequestBuilder.get;

public class RestApplication implements Application {
    private final CloseableContainer applicationScope = CloseableContainer.closeableContainer();
    private final Modules modules;

    public RestApplication(BasePath basePath) {
        this(basePath, new UtterlyIdleProperties());
    }

    public RestApplication(BasePath basePath, UtterlyIdleProperties properties) {
        this(basePath, properties, new Module[0]);
    }

    public RestApplication(BasePath basePath, Module... modules) {
        this(basePath, new UtterlyIdleProperties(), modules);
    }

    public RestApplication(BasePath basePath, UtterlyIdleProperties properties, Module... modules) {
        applicationScope.addInstance(BasePath.class, basePath);
        applicationScope.addInstance(UtterlyIdleProperties.class, properties);
        applicationScope.addInstance(Application.class, this);
        applicationScope.removeCloseable(Application.class);
        this.modules = new Modules(properties);
        this.modules.setupApplicationScope(applicationScope);
        add(new CoreModule());
        add(new LastExceptionsModule());
        for (Module module : modules) {
            add(module);
        }
    }

    public Application add(Module module) {
        modules.activateApplicationModule(module, applicationScope);
        Container requestScope = createRequestScope();
        requestScope.addInstance(Request.class, get("/dummy/request/to/allow/starting/application").build());
        modules.activateStartupModule(module, requestScope);
        return this;
    }

    public Container applicationScope() {
        return applicationScope;
    }

    public Response handle(final Request request) throws Exception {
        return usingRequestScope(handleRequest(request));
    }

    public <T> T usingRequestScope(Callable1<Container, T> callable) {
        return using(createRequestScope(), callable);
    }

    private CloseableContainer createRequestScope() {
        final CloseableContainer requestScope = CloseableContainer.closeableContainer(applicationScope);
        requestScope.add(HttpHandler.class, BaseHandler.class);
        requestScope.decorate(HttpHandler.class, DateHandler.class);
        modules.activateRequestModules(requestScope);
        requestScope.decorate(HttpHandler.class, BasePathHandler.class);
        requestScope.decorate(HttpHandler.class, ExceptionHandler.class);
        requestScope.decorate(HttpHandler.class, ContentLengthHandler.class);
        requestScope.decorate(HttpHandler.class, AuditHandler.class);
        addResourcesIfNeeded(requestScope);
        return requestScope;
    }

    private void addResourcesIfNeeded(Container requestScope) {
        Bindings bindings = requestScope.get(Bindings.class);
        sequence(bindings).fold(requestScope, new Callable2<Container, Binding, Container>() {
            public Container call(Container container, Binding binding) throws Exception {
                Class<?> aClass = binding.method().getDeclaringClass();
                if (!container.contains(aClass)) {
                    container.add(aClass);
                }
                return container;
            }
        });
    }

    private void addBasePathIfNeeded(Container requestScope) {
        if (!requestScope.contains(BasePath.class)) {
            requestScope.addInstance(BasePath.class, BasePath.basePath("/"));
        }
    }

    public <T> T usingParameterScope(Request request, Callable1<Container, T> callable) {
        return using(createArgumentScope(request), callable);
    }

    private Container createArgumentScope(Request request) {
        final Container argumentScope = new SimpleContainer();
        argumentScope.addInstance(Request.class, request);
        modules.activateArgumentModules(argumentScope);
        return argumentScope;
    }

    public static Callable1<Container, Response> handleRequest(final Request request) {
        return new Callable1<Container, Response>() {
            public Response call(Container container) throws Exception {
                return container.addInstance(Request.class, request).
                        get(HttpHandler.class).handle(request);
            }
        };
    }

    public static <T, R> Callable1<Container, R> inject(final T instance, final Callable1<Container, R> handler) {
        return new Callable1<Container, R>() {
            public R call(Container container) throws Exception {
                if (container.contains(instance.getClass())) {
                    container.remove(instance.getClass());
                }
                return handler.call(container.addInstance((Class) instance.getClass(), instance));
            }
        };
    }

    public void close() throws IOException {
        applicationScope.close();
    }
}