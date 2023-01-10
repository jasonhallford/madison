package io.miscellanea.madison.entity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Document extends AbstractEntity {
    // Fields
    private String title;
    private String fingerPrint;
    private int pageCount;
    private String contentType;
    private List<Author> authors;
    private String isbn10;
    private String isbn13;

    // Constructors
    public Document() {
    }

    public Document(@NotNull String fingerPrint, @NotNull String contentType) {
        this(null, null, fingerPrint, 0, contentType, null, null, null);
    }

    public Document(Long id, String title, @NotNull String fingerPrint, int pageCount,
                    @NotNull String contentType, String isbn10, String isbn13, List<Author> authors) {
        super(id);

        this.title = title;
        this.fingerPrint = fingerPrint;
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

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
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
                fingerPrint.equals(document.fingerPrint) && contentType.equals(document.contentType) &&
                Objects.equals(authors, document.authors) && Objects.equals(isbn10, document.isbn10) &&
                Objects.equals(isbn13, document.isbn13);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, fingerPrint, pageCount, contentType, authors, isbn10, isbn13);
    }
}
