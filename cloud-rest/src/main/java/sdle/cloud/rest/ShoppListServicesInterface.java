package sdle.cloud.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.util.Map;

@Path("/api/shopp")
public interface ShoppListServicesInterface extends AutoCloseable {

    @GET
    @Path("/list/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Integer> getList(@PathParam String hash);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list")
    String getNewList();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}")
    String putList(@PathParam String hash, Map<String, Integer> shoppList);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}/{name}/{quantity}")
    String putItem(@PathParam String hash, @PathParam String name, @PathParam Integer quantity);
}
