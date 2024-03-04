package dk.stonemountain.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/clients")
public class ClientsResource {
    private static final Logger log = LoggerFactory.getLogger(ClientsResource.class);
    private static final String NEWEST_VERSION = "0.0.1";
    private static final String OLDEST_VERSION = "0.0.1";

    public static record VersionDTO(String newest, String oldest) {
    }

    public static record ClientDTO(String version, String user, @JsonbProperty("ip-address") String ipAddress, String hostname, String os) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/versions")
    public VersionDTO getSearchInfo(@PathParam("version") String uiVersion) {
        return new VersionDTO(NEWEST_VERSION, OLDEST_VERSION);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registered")
    public void registered(ClientDTO client) {
        log.info("Registered client: {}", client);
    }

}
