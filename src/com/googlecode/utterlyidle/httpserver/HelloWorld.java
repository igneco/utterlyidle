package com.googlecode.utterlyidle.httpserver;

import com.googlecode.utterlyidle.Response;
import com.googlecode.utterlyidle.Responses;
import com.googlecode.utterlyidle.Status;
import com.googlecode.utterlyidle.HeaderParameters;
import static com.googlecode.utterlyidle.HeaderParameters.headerParameters;
import com.googlecode.totallylazy.Pair;
import static com.googlecode.totallylazy.Pair.pair;

import javax.ws.rs.*;

public class HelloWorld {
    @GET
    @Path("helloworld/inresponseheaders")
    public Response getx(@QueryParam("name") String name){
        return Responses.response(Status.OK, headerParameters(pair("greeting",hello(name))), "");
    }

    @GET
    @Path("helloworld/queryparam")
    public String get(@QueryParam("name") String name){
        return hello(name);
    }

    @GET
    @Path("helloworld/headerparam")
    public String header(@HeaderParam("name") String name){
        return hello(name);
    }

    @POST
    @Path("helloworld/formparam")
    public String post(@FormParam("name") String name){
        return hello(name);
    }



    private String hello(String name) {
        return "Hello " + name;
    }
}
