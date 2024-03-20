package dk.stonemountain.search;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path("/clients")
public class ClientsResource {
    private static final Logger log = LoggerFactory.getLogger(ClientsResource.class);

    public enum OS {
        WINDOWS("windows"), MAC("mac"), LINUX("linux");

        private String name;

        private OS(String name) {
            this.name = name;
        }

        public static OS fromString(String name) {
            for (OS os : OS.values()) {
                if (os.name.equals(name)) {
                    return os;
                }
            }
            return null;
        }
    }

    @ConfigProperty(name = "clients.folder", defaultValue = "/tmp/clients")
    String clientsFolder;
    @ConfigProperty(name = "clients.latest.released.version", defaultValue = "0.0.1")
    String latestReleasedVersion;
    @ConfigProperty(name = "clients.oldest.compatible.version", defaultValue = "0.0.1")
    String oldestCompatibleVersion;


    public static record VersionsDTO(@JsonbProperty("latest-released-version") String latestReleasedVersion, @JsonbProperty("oldest-compatible-version") String oldestCompatibleVersion) {
    }

    public static record ClientDTO(String version, String user, @JsonbProperty("ip-address") String ipAddress, String hostname, String os) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/versions/{os}")
    public VersionsDTO getVersionsInfo(@NotNull @PathParam("os") OS os) {
        return new VersionsDTO(latestReleasedVersion, oldestCompatibleVersion);
    }


    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/versions/{os}/{version}")
    public StreamingOutput getNewClient(@NotNull @PathParam("os") OS os, @PathParam("version") String version) {
        return s -> {
            boolean found = false;
            var dir = Paths.get(clientsFolder, os.name().toLowerCase(), version).toFile();
            if (dir.exists() && dir.canRead() && dir.isDirectory()) {
                var file = getFileInDirectory(dir);
                if (file.exists() && file.canRead() && file.isFile()) {
                    found = true;
                    try (var in = new FileInputStream(file)) {
                        in.transferTo(s);
                    }
                }
            }

            if (!found) {
                throw new RuntimeException("No client for os " + os + " and version " + version);
            }
        };
    }

    private File getFileInDirectory(File dir) {
        return dir.listFiles()[0];
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registered")
    public void registered(ClientDTO client) {
        log.info("Registered client: {}", client);
    }

}
