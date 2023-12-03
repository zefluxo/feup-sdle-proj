package sdle.cloud.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import sdle.cloud.service.ClusterService;

import java.util.Map;

@Path("/api/cluster")
public class ClusterResource implements AutoCloseable {
    @Inject
    ClusterService clusterService;


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/joining/{id}/{ip}")
    public String joining(@PathParam String id, @PathParam String ip) {
        return clusterService.processJoining(id, ip);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/leaving/{id}")
    public String leaving(@PathParam String id) {
        return clusterService.processLeaving(id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/leave")
    public String leave() {
        return clusterService.processLeave();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/update")
    public String updateCluster(Map<String, String> newClusterNodes) {
        return clusterService.processUpdate(newClusterNodes);
    }


    @Override
    public void close() {
        //
    }
}
