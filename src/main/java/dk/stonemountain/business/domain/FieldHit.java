package dk.stonemountain.business.domain;

import java.util.List;

import dk.stonemountain.business.domain.Hit.Pair;

public record FieldHit(
    String fieldName,
    List<String> highlights) {

    public FieldHit(Pair<String, List<String>> fieldHighlights) {
        this(fieldHighlights.first, fieldHighlights.second.stream().map(s -> s).toList());
    }
}
