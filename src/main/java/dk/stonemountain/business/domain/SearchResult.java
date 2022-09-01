package dk.stonemountain.business.domain;

import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

public record SearchResult(
    String id,
    @JsonbProperty("site-id")
    String siteId,
    String url,
    String title,
    List<FieldHit> highlights) {
 
    public SearchResult(Hit<Page> hit) {
        this(hit.source.id, hit.source.siteId, hit.source.url, hit.source.title, hit.highlights.stream().map(FieldHit::new).toList());
    }
}