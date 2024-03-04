package dk.stonemountain.business;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.EntityProjection;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.HighlightProjection;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ProjectionConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.business.crawl.CrawlService;
import dk.stonemountain.business.domain.Page;
import dk.stonemountain.business.domain.Site;
import dk.stonemountain.business.dto.SearchResultDTO;
import dk.stonemountain.business.dto.SiteDTO;
import dk.stonemountain.business.dto.SitesDTO;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/sites")
@Transactional
public class SiteResource {
    private static final Logger log = LoggerFactory.getLogger(SiteResource.class);
    
    @ProjectionConstructor
    public record PageHit(
        @EntityProjection
        Page page,
        @HighlightProjection
        List<String> pageText
    ) {
    }
    
    @Inject
    SearchSession searchSession;
    @Inject
    CrawlService crawlService;

    void onStart(@Observes StartupEvent ev) throws InterruptedException { 
        // only reindex if we imported some content
        if (Page.count() > 0) {
            searchSession.massIndexer()
                    .startAndWait();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SitesDTO getSites() {
        var sites = Site.<Site>streamAll().map(SiteDTO::new).toList();
        return new SitesDTO(sites.size(), ZonedDateTime.now(), sites);
    }

    @GET
    @Path("/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    public SiteDTO getSite(@PathParam("siteName") String siteName) {
        var s = Site.<Site>find("name", siteName).firstResult();
        return new SiteDTO(s);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public SiteDTO create(@NotNull @Valid SiteDTO site) {
        var s = site.toDomain(new Site());
        Site.persist(s);
        return new SiteDTO(s);
    }

    @PUT()
    @Path("/{siteName}/crawl")
    @Produces(MediaType.APPLICATION_JSON)
    public SiteDTO startCrawl(@PathParam("siteName") String siteName) {
        var startTime = LocalDateTime.now();
        var site = Site.<Site>find("name", siteName).singleResult();
        site.pages.clear();

        crawlService.crawl(site, (s, l, title, text) -> { 
            log.info("Crawled page {}", l);
            var p = new Page(l, title.orElse("No Title Available"), text, s);
            s.pages.add(p);
        });

        site.lastSuccessfulCrawl = ZonedDateTime.now();
        site.lastSuccessfulCrawlDuration = Duration.between(startTime, site.lastSuccessfulCrawl).toString();
        log.info("Site {} crawled in {}", siteName, site.lastSuccessfulCrawlDuration);

        return new SiteDTO(site);
    }

    @GET
    @Path("/{siteName}/pages/search-by-text")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResultDTO> searchByText(@PathParam("siteName") @NotBlank String siteName, @QueryParam("pattern") String pattern, @QueryParam("elements") @DefaultValue("10") Integer elements) {
        var result = searchSession.search(Page.class)
            .select(PageHit.class)
            .where(f ->
                pattern == null || pattern.trim().isEmpty() ? f.matchAll() : f.simpleQueryString().fields("pageText", "title").matching(pattern)
            )
            .highlighter(f -> f.plain().noMatchSize( 100 ))
            .fetchHits(elements); 

        return result.stream()
            .map(SearchResultDTO::new)
            .toList();
    }
}