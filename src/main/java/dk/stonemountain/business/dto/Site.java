package dk.stonemountain.business.dto;

import java.time.ZonedDateTime;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Site {
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

    public Site() {
    }

    public Site(String name, String startUrl, String inclusionUrl, String icon) {
        this.name = name;
        this.startUrl = startUrl;
        this.inclusionUrl = inclusionUrl;
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Site [displayName=" + displayName + ", icon=" + icon + ", inclusionUrl=" + inclusionUrl
                + ", lastSuccessfulCrawl=" + lastSuccessfulCrawl + ", lastSuccessfulCrawlDuration="
                + lastSuccessfulCrawlDuration + ", mainUrl=" + mainUrl + ", name=" + name + ", startUrl=" + startUrl
                + "]";
    }
}
