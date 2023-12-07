package sdle.cloud.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import sdle.cloud.service.ShoppListService;
import sdle.cloud.utils.HashUtils;
import sdle.crdt.implementations.ORMap;

import java.util.UUID;

@Path("/api/shopp")
public class ShoppListResource implements AutoCloseable {

    @Inject
    public ShoppListService shoppListService;

    @GET
    @Path("/list/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public ORMap getList(@PathParam String hash) {
        return shoppListService.processGetList(hash);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list")
    public String newList() {
        return shoppListService.processPutList(HashUtils.getRandomHash(), new ORMap(UUID.randomUUID().toString()));
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}")
    public String putEmptyList(@PathParam String hash) {
        return shoppListService.processPutList(hash, new ORMap());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}")
    public String putList(@PathParam String hash, ORMap shoppList) {
        return shoppListService.processPutList(hash, shoppList);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/replicate/{hash}")
    public String replicateList(@PathParam String hash, ORMap shoppList) {
        return shoppListService.processReplicateList(hash, shoppList);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}/inc/{name}/{quantity}")
    public String incItem(@PathParam String hash, @PathParam String name, @PathParam Integer quantity) {
        return shoppListService.processPutItem(hash, name, quantity, true);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/list/{hash}/dec/{name}/{quantity}")
    public String decItem(@PathParam String hash, @PathParam String name, @PathParam Integer quantity) {
        return shoppListService.processPutItem(hash, name, quantity, false);
    }

    @Override
    public void close() {
        //
    }
}
