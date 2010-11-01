package com.googlecode.utterlyidle;

import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;

import java.util.ArrayList;
import java.util.List;

public class RestApplication implements Application {
    private final Container applicationScope = new SimpleContainer();
    private final List<Module> modules = new ArrayList<Module>();

    public RestApplication() {
        add(new CoreModule());
    }

    public Container createRequestScope(Request request) {
        Container requestScope = new SimpleContainer(applicationScope);
        requestScope.addInstance(Request.class, request);
        for (Module module : modules) {
            module.addPerRequestObjects(requestScope);
        }
        return requestScope;
    }

    public Application add(Module module) {
        module.addPerApplicationObjects(applicationScope);
        module.addResources(engine());
        modules.add(module);
        return this;
    }

    public Container applicationScope() {
        return applicationScope;
    }

    public void handle(Request request, Response response) {
        engine().handle(createRequestScope(request), request, response);
    }

    public Engine engine() {
        return applicationScope.get(Engine.class);
    }
}