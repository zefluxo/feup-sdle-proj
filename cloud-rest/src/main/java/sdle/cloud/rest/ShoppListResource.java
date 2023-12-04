package sdle.cloud.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import sdle.cloud.service.ShoppListService;
import sdle.cloud.utils.HashUtils;

import java.util.HashMap;
import java.util.Map;

@Path("/api/shopp")
public class ShoppListResource implements AutoCloseable {

    @Inject
    public ShoppListService shoppListService;

    @GET
    @Path("/list/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Integer> getList(@PathParam String hash) {
        return shoppListService.processGetList(hash);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list")
    public String newList() {
        return shoppListService.processPutList(HashUtils.getRandomHash(), new HashMap<>());
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}")
    public String putEmptyList(@PathParam String hash) {
        return shoppListService.processPutList(hash, new HashMap<>());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}")
    public String putList(@PathParam String hash, Map<String, Integer> shoppList) {
        return shoppListService.processPutList(hash, shoppList);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/replicate/{hash}")
    public String replicateList(@PathParam String hash, Map<String, Integer> shoppList) {
        return shoppListService.processReplicateList(hash, shoppList);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}/{name}/{quantity}")
    public String putItem(@PathParam String hash, @PathParam String name, @PathParam Integer quantity) {
        return shoppListService.processPutItem(hash, name, quantity);
    }

    @Override
    public void close() throws Exception {
        //
    }
}
