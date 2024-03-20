package dk.stonemountain.search.dto;

import java.util.List;

import dk.stonemountain.search.SiteResource.PageHit;
import jakarta.json.bind.annotation.JsonbProperty;

public record SearchResultDTO(
    Long id,
    @JsonbProperty("site-id")
    Long siteId,
    @JsonbProperty("site-name")
    String siteName,
    String url,
    String title,
    List<String> highlights) {
 
    public SearchResultDTO(PageHit hit) {
        this(hit.page().id, hit.page().owner.id, hit.page().owner.displayName, hit.page().url, hit.page().title, hit.pageText());
    }
}