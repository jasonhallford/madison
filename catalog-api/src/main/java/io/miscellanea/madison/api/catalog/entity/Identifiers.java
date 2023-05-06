package io.miscellanea.madison.api.catalog.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "identifiers")
public class Identifiers {
    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "isbn_10", length = 10, columnDefinition = "BPCHAR(10)")
    private String isbn10;
    @Column(name = "isbn_13", length = 13, columnDefinition = "BPCHAR(13)")
    private String isbn13;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id")
    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    // Constructors
    public Identifiers() {
    }

    // Properties
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    // Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifiers that = (Identifiers) o;
        return Objects.equals(id, that.id) && Objects.equals(isbn10, that.isbn10) && Objects.equals(isbn13, that.isbn13);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isbn10, isbn13);
    }
}
