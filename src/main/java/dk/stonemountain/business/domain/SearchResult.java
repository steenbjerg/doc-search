package dk.stonemountain.business.domain;

import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;

public class SearchResult {
    public String id;
    @JsonbProperty("site-id")
    public String siteId;
    public String url;

    public SearchResult() {
    }

    public SearchResult(String id, String url, String siteId) {
        this.id = id;
        this.url = url;
        this.siteId = siteId;
    }

    @Override
    public String toString() {
        return "SearchResult [id=" + id + ", url=" + url + ", siteId=" + siteId + "]";
    }
}