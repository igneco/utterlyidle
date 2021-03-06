package com.googlecode.utterlyidle.handlers;

import com.googlecode.totallylazy.concurrent.NamedExecutors;
import com.googlecode.utterlyidle.Request;
import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Status;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.googlecode.utterlyidle.handlers.ClientHttpHandler.errorResponse;

public class TimeoutClient implements HttpClient {
    private static final ExecutorService service = NamedExecutors.newCachedThreadPool(TimeoutClient.class);
    private final HttpClient client;
    private final int timeout;

    public TimeoutClient(int timeout, final HttpClient client) {
        this.client = client;
        this.timeout = timeout;
    }

    @Override
    public Response handle(final Request request) throws Exception {
        try {
            return service.submit(() -> client.handle(request)).get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return errorResponse(Status.CLIENT_TIMEOUT, e);
        }
    }
}
