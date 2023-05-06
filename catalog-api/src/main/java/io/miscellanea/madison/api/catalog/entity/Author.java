package io.miscellanea.madison.api.catalog.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "author")
public class Author {
    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name", length = 50)
    private String firstName;
    @Column(name = "middle_name", length = 50)
    private String middleName;
    @Column(name = "last_name", length = 50)
    private String lastName;
    @Column(name = "suffix", length = 10)
    private String suffix;
    @Column(name = "code", length = 50)
    private String code;
    @Column(name = "full_name", updatable = false, insertable = false)
    private String fullName;
    @JoinTable(
            name = "author_document",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    @OneToMany
    private List<Document> documents;

    // Constructors
    public Author() {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
    }

    // Properties
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void addDocument(Document document) {
        this.documents.add(document);
    }

    // Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id) && Objects.equals(firstName, author.firstName)
                && Objects.equals(middleName, author.middleName) && Objects.equals(lastName, author.lastName)
                && Objects.equals(suffix, author.suffix) && Objects.equals(code, author.code)
                && Objects.equals(fullName, author.fullName) && Objects.equals(documents, author.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, middleName, lastName, suffix, code, fullName, documents);
    }
}
