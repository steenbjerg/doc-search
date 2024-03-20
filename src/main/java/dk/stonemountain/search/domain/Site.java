package dk.stonemountain.search.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity()
@Table(uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = "name"),
    @jakarta.persistence.UniqueConstraint(columnNames = "displayName")
})
@Indexed
public class Site extends PanacheEntity {
    @NotNull
    @Size(min=7, max=300)
    public String startUrl;
    @NotNull
    @Size(min=7, max=300)
    public String mainUrl;
    @NotNull
    @Size(min=7, max=300)
    public String inclusionUrl;
    @NotNull
    @Size(min=1, max=300)
    @FullTextField(analyzer = "english")
    public String name;
    @NotNull
    @Size(min=1, max=300)
    @FullTextField(analyzer = "english")
    public String displayName;
    public ZonedDateTime lastSuccessfulCrawl;
    public String lastSuccessfulCrawlDuration;
    @Size(min=7, max = 300)
    public String icon;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) 
    public List<Page> pages = new ArrayList<>();


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
        return "Site [startUrl=" + startUrl + ", mainUrl=" + mainUrl + ", inclusionUrl=" + inclusionUrl + ", name="
                + name + ", displayName=" + displayName + ", lastSuccessfulCrawl=" + lastSuccessfulCrawl
                + ", lastSuccessfulCrawlDuration=" + lastSuccessfulCrawlDuration + ", icon=" + icon + ", pages=" + pages
                + "]";
    }
}
