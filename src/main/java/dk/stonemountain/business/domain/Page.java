package dk.stonemountain.business.domain;

import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;

public class Page {
    public String id;
    @JsonbProperty("site-id")
    public String siteId;
    public String url;
    public String title;
    @JsonbProperty("page-text")
    public String pageText;

    public Page() {
    }

    public Page(String id, String url, String title, String pageText, String siteId) {
        this.id = id;
        this.url = url;
        this.pageText = pageText;
        this.title = title;
        this.siteId = siteId;
    }

    @Override
    public String toString() {
        return "Page [id=" + id + ", siteId=" + siteId + ", title=" + title + ", pageText (length)=" + (pageText != null ? pageText.length() : null) + ", url=" + url + "]";
    }
}