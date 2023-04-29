package io.miscellanea.madison.document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.miscellanea.madison.entity.AbstractEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Document extends AbstractEntity {
    // Fields
    @JsonProperty("title")
    private String title;
    @JsonProperty("fingerprint")
    private Fingerprint fingerprint;
    @JsonProperty("pages")
    private int pageCount;
    @JsonProperty("content-type")
    private String contentType;
    @JsonProperty("authors")
    private List<Author> authors;
    @JsonProperty("isbn10")
    private String isbn10;
    @JsonProperty("isbn13")
    private String isbn13;

    // Constructors
    public Document() {
    }

    public Document(@NotNull Fingerprint fingerPrint, @NotNull String contentType) {
        this(null, null, fingerPrint, 0, contentType, null, null, null);
    }

    @JsonCreator
    public Document(@JsonProperty("id") Long id, @JsonProperty("title") String title,
                    @JsonProperty("fingerprint") @NotNull Fingerprint fingerprint,
                    @JsonProperty("pages") int pageCount,
                    @JsonProperty("content-type") @NotNull String contentType,
                    @JsonProperty("isbn10") String isbn10,
                    @JsonProperty("isbn13") String isbn13,
                    @JsonProperty("authors") List<Author> authors) {
        super(id);

        this.title = title;
        this.fingerprint = fingerprint;
        this.pageCount = pageCount;
        this.contentType = contentType;
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
        this.authors = authors != null ? new ArrayList<>(authors) : new ArrayList<>();
    }

    // Properties
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    // Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Document document = (Document) o;
        return pageCount == document.pageCount && title.equals(document.title) &&
                fingerprint.equals(document.fingerprint) && contentType.equals(document.contentType) &&
                Objects.equals(authors, document.authors) && Objects.equals(isbn10, document.isbn10) &&
                Objects.equals(isbn13, document.isbn13);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, fingerprint, pageCount, contentType, authors, isbn10, isbn13);
    }
}
