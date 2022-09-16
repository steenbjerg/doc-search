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
import dk.stonemountain.business.domain.Hit;
import dk.stonemountain.business.domain.Page;
import dk.stonemountain.business.domain.SearchResult;
import dk.stonemountain.business.dto.Site;
import dk.stonemountain.business.dto.Sites;
import io.smallrye.common.constraint.NotNull;

@Path("/sites")
public class SiteResource {
    private static final Logger log = LoggerFactory.getLogger(SiteResource.class);
    private static final String SITES_INDEX = "sites";
    @Inject
    ElasticSearchService service;
    @Inject
    CrawlService crawlService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Sites getSites() {
        List<Hit<Site>> result = service.searchAll(SITES_INDEX, ElasticSearchService.siteSourceReader);

        var list = result.stream().map(h -> h.source).toList();
        return new Sites(list.size(), ZonedDateTime.now(), list);
    }

    @GET
    @Path("/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Site getSite(@PathParam("siteName") String siteName) {
        return service.getById(ElasticSearchService.siteSourceReader, SITES_INDEX, siteName).orElseThrow(() -> new RuntimeException("Not Found"));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Site create(@NotNull @Valid Site site) {
        service.addToIndex(SITES_INDEX, site.name, site);
        return getSite(site.name);
    }

    @PUT()
    @Path("/{siteName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Site startCrawl(@PathParam("siteName") String siteName) {
        Site site = getSite(siteName);
        LocalDateTime startTime = LocalDateTime.now();
        crawlService.crawl(site, (s, p, title, text) -> service.addToIndex(
            site.name,
            URLEncoder.encode(Base64.getEncoder().encodeToString((s.name + "#" + p).getBytes()), StandardCharsets.UTF_8), 
            new Page(
                URLEncoder.encode(Base64.getEncoder().encodeToString((s.name + "#" + p).getBytes()), StandardCharsets.UTF_8), 
                p,
                title.orElse("No Title Available"),
                text, 
                s.name))
        );
        site.lastSuccessfulCrawl = ZonedDateTime.now();
        site.lastSuccessfulCrawlDuration = Duration.between(startTime, site.lastSuccessfulCrawl).toString();
        create(site);
        log.info("Site {} crawled in {}", siteName, site.lastSuccessfulCrawlDuration);
        return site;
    }

    @GET
    @Path("/{siteName}/pages/search-by-text")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResult> searchByText(@PathParam("siteName") @NotBlank String siteName, @QueryParam("match") String match) {
        Site site = getSite(siteName);
        List<Hit<Page>> result;
        if (match == null) {
            result = service.searchAll(site.name, ElasticSearchService.pageSourceReader);
        } else {
            result = service.searchByTerms(site.name, ElasticSearchService.pageSourceReader, new ElasticSearchService.TermMatchPair(ElasticSearchService.TermMatchPair.QueryType.TERM, "site-id", siteName), new ElasticSearchService.TermMatchPair(ElasticSearchService.TermMatchPair.QueryType.WILDCARD, "page-text", match));
        }
        return result.stream()
            .map(SearchResult::new)
            .toList();
    }

    @GET
    @Path("/{siteName}/pages/full-text-search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResult> fullTextSearch(@PathParam("siteName") @NotBlank String siteName, @NotBlank @QueryParam("query") String query) {
        Site site = getSite(siteName);
        List<Hit<Page>> result = service.searchByFullTextSearch(site.name, ElasticSearchService.pageSourceReader, query, "page-text", "title");
        return result.stream()
            .map(SearchResult::new)
            .toList();
    }

    @GET
    @Path("/{siteName}/term-search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResult> searchByTerm(@PathParam("siteName") @NotBlank String siteName, @QueryParam("term") String term, @QueryParam("match") String match) {
        Site site = getSite(siteName);
        List<Hit<Page>> result;
        if (term == null) {
            result = service.searchAll(site.name, ElasticSearchService.pageSourceReader);
        } else {
            result = service.search(site.name, ElasticSearchService.pageSourceReader, term, match);
        }
        return result.stream()
            .map(SearchResult::new)
            .toList();
    }
}