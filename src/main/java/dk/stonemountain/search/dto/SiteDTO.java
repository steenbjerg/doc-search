package dk.stonemountain.search.dto;

import java.time.ZonedDateTime;

import dk.stonemountain.search.domain.Site;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SiteDTO {
    public Long id;
    @NotNull
    @Size(min=7)
    @JsonbProperty("start-url")
    public String startUrl;
    @NotNull
    @Size(min=7)
    @JsonbProperty("main-url")
    public String mainUrl;
    @NotNull
    @Size(min=7)
    @JsonbProperty("inclusion-url")
    public String inclusionUrl;
    @NotNull
    @Size(min=1)
    public String name;
    @NotNull
    @Size(min=1)
    @JsonbProperty("display-name")
    public String displayName;
    @JsonbProperty("last-successful-crawl")
    public ZonedDateTime lastSuccessfulCrawl;
    @JsonbProperty("last-successful-crawl-duration")
    public String lastSuccessfulCrawlDuration;
    @Size(min=7)
    public String icon;

    public SiteDTO() {
    }

    public SiteDTO(String name, String startUrl, String inclusionUrl, String icon) {
        this.name = name;
        this.startUrl = startUrl;
        this.inclusionUrl = inclusionUrl;
        this.icon = icon;
    }

    public SiteDTO(Site s) {
        this.id = s.id;
        this.displayName = s.displayName;
        this.icon = s.icon;
        this.inclusionUrl = s.inclusionUrl;
        this.lastSuccessfulCrawl = s.lastSuccessfulCrawl;
        this.lastSuccessfulCrawlDuration = s.lastSuccessfulCrawlDuration == null ? null : s.lastSuccessfulCrawlDuration.toString();
        this.mainUrl = s.mainUrl;
        this.name = s.name;
        this.startUrl = s.startUrl;
    }

    @Override
    public String toString() {
        return "Site [displayName=" + displayName + ", icon=" + icon + ", inclusionUrl=" + inclusionUrl
                + ", lastSuccessfulCrawl=" + lastSuccessfulCrawl + ", lastSuccessfulCrawlDuration="
                + lastSuccessfulCrawlDuration + ", mainUrl=" + mainUrl + ", name=" + name + ", startUrl=" + startUrl
                + "]";
    }

    public Site toDomain(Site site) {
        site.displayName = displayName;
        site.icon = icon;
        site.inclusionUrl = inclusionUrl;
        site.lastSuccessfulCrawl = lastSuccessfulCrawl;
        site.lastSuccessfulCrawlDuration = lastSuccessfulCrawlDuration;
        site.mainUrl = mainUrl;
        site.name = name;
        site.startUrl = startUrl;
        return site;
    }
}
