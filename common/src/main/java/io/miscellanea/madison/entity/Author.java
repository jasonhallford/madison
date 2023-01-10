package io.miscellanea.madison.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Author {
    // Fields
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;

    // Constructors

    public Author(@NotNull String firstName, String middleName, @NotNull String lastName, String suffix) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.suffix = suffix;
    }

    public Author(String firstName, String lastName) {

        this(firstName, null, lastName, null);
    }

    // Static methods
    public static Author fromString(String fullName) {
        return null;
    }

    // Properties
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

    public String getFullName() {
        var builder = new StringBuilder();

        builder.append(this.firstName).append(" ");
        if (!this.middleName.isEmpty()) {
            builder.append(this.middleName).append(" ");
        }
        builder.append(this.lastName);
        if (!this.suffix.isEmpty()) {
            builder.append(", ").append(this.suffix);
        }

        return builder.toString();
    }

    // Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return firstName.equals(author.firstName) && Objects.equals(middleName, author.middleName) && lastName.equals(author.lastName) && Objects.equals(suffix, author.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleName, lastName, suffix);
    }
}
