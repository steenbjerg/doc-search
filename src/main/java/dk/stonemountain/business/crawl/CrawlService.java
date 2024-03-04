package dk.stonemountain.business.crawl;

import java.io.IOException;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.business.domain.Site;
import io.quarkus.arc.Lock;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Lock
public class CrawlService {
    private static final Logger log = LoggerFactory.getLogger(CrawlService.class);

    @FunctionalInterface
    public static interface CrawlObserver {
        void update(Site site, String link, Optional<String> title, String text);
    }

    private CrawlObserver observer;
    private TreeSet<String> visitedLinks = new TreeSet<>();
    private Site site;

    public void crawl(Site site, CrawlObserver observer) {
        this.observer = observer;
        visitedLinks.clear();
        this.site = site;

        crawl(site.startUrl);
        log.debug("Visted links: {}", visitedLinks);
    }

    private void crawl(String currentLink) {
        visitedLinks.add(currentLink);
        log.trace("Crawling: {}", currentLink);

        Document doc;
        try {
            doc = Jsoup.connect(currentLink).get();
        } catch (IOException e) {
            log.warn("Failed to access link {} : {}", currentLink, e.getMessage());
            return;
        }

        // extract data
        Element titleElement = doc.select("title").first();
        Optional<String> title = Optional.empty();
        if (titleElement != null) {
            title = Optional.of(titleElement.text());
        }
        String textInBody = clean(doc.body().text());

        observer.update(site, currentLink, title, textInBody);

        // extract links
        Elements links = doc.select("a[href]");
        log.trace("Found links: {}", links);
        links.stream()
            .map(l -> l.attr("href"))
            .filter(l -> l != null && !l.isBlank())
            .map(this::normalizeLink)
            .filter(l -> !l.isBlank())
            .filter(l -> !alreadyVisited(l, visitedLinks))
            .filter(l -> l.startsWith(site.inclusionUrl))
            .peek(l -> log.trace("Accepted link {} at page {}", l, currentLink))
            .forEach(this::crawl);
    }

    private String clean(String text) {
        if (text != null) {
            text = text.replace("\n", "").replace("\t", "");
            text = text.replace("\\", "");
            text = text.trim();
        }
        return text;
    }

    private boolean alreadyVisited(String link, SortedSet<String> visitedLinks) {
        return visitedLinks.contains(link);
    }

    private String normalizeLink(String link) {
        String normalizedLink = link.contains("?") ? link.substring(0, link.indexOf("?")) : link;
        normalizedLink = normalizedLink.contains("#") ? link.substring(0, link.indexOf("#")) : link;
        normalizedLink = normalizedLink.startsWith("/") ? site.mainUrl + normalizedLink : normalizedLink;
        normalizedLink = normalizedLink.endsWith("/") ? normalizedLink.substring(0, normalizedLink.length() - 1) : normalizedLink;
        return normalizedLink;
    }
}
