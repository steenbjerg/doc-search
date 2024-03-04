package dk.stonemountain.business.dto;

import java.util.List;

import dk.stonemountain.business.SiteResource.PageHit;
import jakarta.json.bind.annotation.JsonbProperty;

public record SearchResultDTO(
    Long id,
    @JsonbProperty("site-id")
    Long siteId,
    String url,
    String title,
    List<String> highlights) {
 
    public SearchResultDTO(PageHit hit) {
        this(hit.page().id, hit.page().owner.id, hit.page().url, hit.page().title, hit.pageText());
    }
}