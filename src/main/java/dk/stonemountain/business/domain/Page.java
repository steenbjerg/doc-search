package dk.stonemountain.business.domain;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Indexed
public class Page extends PanacheEntity {
    @Size(min=7, max=500)
    @NotNull
    public String url;
    @FullTextField(analyzer = "english")
    @Size(min=1, max=500)
    @NotNull
    public String title;
    @FullTextField(analyzer = "english")
    @NotNull
    @Lob
    public String pageText;
    @ManyToOne
    @NotNull
    public Site owner;

    public Page() {
    }

    public Page(String url, String title, String pageText, Site site) {
        this.url = url;
        this.pageText = pageText;
        this.title = title;
        this.owner = site;
    }

    @Override
    public String toString() {
        return "Page [" + super.toString() + ", site=" + owner.id + ", url=" + url + ", title=" + title + ", pageText=" + pageText + "]";
    }
}