package dk.stonemountain.business.dto;

import java.time.ZonedDateTime;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

public record Sites (
    @JsonbProperty("no-of-sites")
    long noOfSites,
    @JsonbProperty("retrieval-time")
    ZonedDateTime retrievalTime,
    @JsonbProperty("sites")
    List<Site> sites) {
}