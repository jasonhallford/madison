package io.miscellanea.madison.api.catalog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "document")
public class Document {
    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", length = 100)
    private String title;
    @Column(name = "fingerprint", length = 100)
    private String fingerprint;
    @Column(name = "content_type", length = 100)
    private String contentType;
    @Column(name = "page_count")
    private Integer pageCount;
    @OneToOne(mappedBy = "document")
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    private Identifiers identifiers;
    @JoinTable(
            name = "author_document",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @OneToMany
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    private List<Author> authors;

    // Constructors
    public Document() {
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
    }

    // Properties
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Identifiers getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Identifiers identifiers) {
        this.identifiers = identifiers;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void addAuthor(Author author) {
        this.authors.add(author);
    }

    // Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) && Objects.equals(title, document.title) &&
                Objects.equals(fingerprint, document.fingerprint) && Objects.equals(contentType, document.contentType)
                && Objects.equals(pageCount, document.pageCount) && Objects.equals(identifiers, document.identifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, fingerprint, contentType, pageCount, identifiers);
    }
}
