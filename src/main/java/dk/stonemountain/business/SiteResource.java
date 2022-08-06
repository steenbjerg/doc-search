package dk.stonemountain.business;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.business.crawl.CrawlService;
import dk.stonemountain.business.domain.ElasticSearchService;
import dk.stonemountain.business.domain.ElasticSearchService.Index;
import dk.stonemountain.business.domain.Page;
import dk.stonemountain.business.domain.SearchResult;
import dk.stonemountain.business.dto.Site;
import io.smallrye.common.constraint.NotNull;

@Path("/sites")
public class SiteResource {
    private static final Logger log = LoggerFactory.getLogger(SiteResource.class);
    
    @Inject
    ElasticSearchService service;
    @Inject
    CrawlService crawlService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Site> getSite() {
        return service.searchAll(Index.SITES, ElasticSearchService.siteSourceReader);
    }

    @GET
    @Path("/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Site getSite(@PathParam("siteName") String siteName) {
        return service.getById(Site.class, ElasticSearchService.siteSourceReader, Index.SITES, siteName).orElseThrow(() -> new RuntimeException("Not Found"));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Site create(@NotNull @Valid Site site) {
        service.addToIndex(Index.SITES, site.name, site);
        return getSite(site.name);
    }

    @PUT()
    @Path("/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Site startCrawl(@PathParam("siteName") String siteName) {
        Site site = getSite(siteName);
        LocalDateTime startTime = LocalDateTime.now();
        crawlService.crawl(site, (s, p, t) -> service.addToIndex(Index.PAGES, URLEncoder.encode(Base64.getEncoder().encodeToString((s.name + "#" + p).getBytes()), StandardCharsets.UTF_8), new Page(URLEncoder.encode(Base64.getEncoder().encodeToString((s.name + "#" + p).getBytes()), StandardCharsets.UTF_8), p, t, s.name)));
        site.lastSuccessfulCrawl = ZonedDateTime.now();
        site.lastSuccessfulCrawlDuration = Duration.between(startTime, site.lastSuccessfulCrawl).toString();
        create(site);
        log.info("Site {} crawled in {}", siteName, site.lastSuccessfulCrawlDuration);
        return site;
    }

    @GET
    @Path("/pages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResult> search(@QueryParam("term") String term, @QueryParam("match") String match) {
        List<Page> result;
        if (term == null) {
            result = service.searchAll(Index.PAGES, ElasticSearchService.pageSourceReader);
        } else {
            result = service.search(Index.PAGES, ElasticSearchService.pageSourceReader, term, match);
        }
        return result.stream()
            .map(p -> new SearchResult(p.id, p.url, p.siteId))
            .toList();
    }

    @GET
    @Path("/{siteName}/pages/search-by-text")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResult> searchByText(@PathParam("siteName") @NotBlank String siteName, @QueryParam("match") String match) {
        List<Page> result;
        if (match == null) {
            result = service.searchAll(Index.PAGES, ElasticSearchService.pageSourceReader);
        } else {
            result = service.searchByTerms(Index.PAGES, ElasticSearchService.pageSourceReader, new ElasticSearchService.TermMatchPair(ElasticSearchService.TermMatchPair.QueryType.TERM, "site-id", siteName), new ElasticSearchService.TermMatchPair(ElasticSearchService.TermMatchPair.QueryType.WILDCARD, "page-text", match));
        }
        return result.stream()
            .map(p -> new SearchResult(p.id, p.url, p.siteId))
            .toList();
    }

    @GET
    @Path("/{siteName}/pages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResult> search(@PathParam("siteName") @NotBlank String siteName, @QueryParam("term") String term, @QueryParam("match") String match) {
        List<Page> result;
        if (term == null) {
            result = service.searchAll(Index.PAGES, ElasticSearchService.pageSourceReader);
        } else {
            result = service.search(Index.PAGES, ElasticSearchService.pageSourceReader, term, match);
        }
        return result.stream()
            .map(p -> new SearchResult(p.id, p.url, p.siteId))
            .toList();
    }
}