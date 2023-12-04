package sdle.cloud.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.util.Map;

@Path("/api/cluster")
public interface ClusterServicesInterface extends AutoCloseable {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/joining/{id}/{ip}")
    String joining(@PathParam String id, @PathParam String ip);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/leaving/{id}")
    String leaving(@PathParam String id);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/leave")
    String leave();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/update")
    String updateCluster(Map<String, String> cluster);
}
