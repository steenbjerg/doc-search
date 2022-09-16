package dk.stonemountain.business;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;


@Path("/info")
public class InformationResource {
    @Inject
    Template searchInfo; 

    @Path("/search/{version}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getSearchInfo(@PathParam("version") String uiVersion) {
        return  searchInfo.data("version", uiVersion);
    }
}
