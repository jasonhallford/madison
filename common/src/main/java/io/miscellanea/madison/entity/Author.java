package io.miscellanea.madison.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Author extends AbstractEntity {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(Author.class);

    private static final Pattern nameAndPattern = Pattern.compile(",*\\s*(and|&)\\s*");
    private static final Pattern nameWhitespacePattern = Pattern.compile(",\\s+");

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
    public static List<Author> fromString(String fullName) {
        List<Author> authors = new ArrayList<>();

        // Step 1: Remove 'and' and '&' from multi-author names and replace them with '|'.
        var compoundNames = nameAndPattern.matcher(fullName).replaceAll("|");
        logger.debug("compoundNames with 'and' removed = {}", compoundNames);

        // Step 2: Replace all commas followed by whitespace with '|'.
        compoundNames = nameWhitespacePattern.matcher(compoundNames).replaceAll("|");
        logger.debug("compoundNames with ', ' removed = {}", compoundNames);

        // Step 2: Split into individual names based on the presence of '|'
        var splitNames = compoundNames.split("\\|");

        // Step 3: Process each name, breaking it into a series of components.
        for (String name : splitNames) {
            logger.debug("Processing name: {}", name);

            var components = name.split(" ");
            if (components.length == 2) {
                var author = new Author(components[0].trim(), components[1].trim());
                authors.add(author);
                logger.debug("Added new author to list: {}", author);
            }
        }

        return authors;
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
        if (this.middleName != null && !this.middleName.isEmpty()) {
            builder.append(this.middleName).append(" ");
        }
        builder.append(this.lastName);
        if (this.suffix != null && !this.suffix.isEmpty()) {
            builder.append(", ").append(this.suffix);
        }

        return builder.toString();
    }

    public String getCode() {
        return DigestUtils.md5Hex(this.getFullName().replaceAll(" ", "").toLowerCase());
    }

    // Methods
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("lastName", lastName)
                .append("suffix", suffix)
                .toString();
    }

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
