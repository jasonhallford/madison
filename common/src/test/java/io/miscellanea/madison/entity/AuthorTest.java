package io.miscellanea.madison.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthorTest {
    @Test
    @DisplayName("Validate that full name is correctly generated")
    void validateFullName() {
        var author = new Author("Jane", "Smith", "Doe", "");
        assertThat(author.getFullName()).isEqualTo("Jane Smith Doe");

        author = new Author("John", "", "Smith", "");
        assertThat(author.getFullName()).isEqualTo("John Smith");

        author = new Author("Jacob", "", "Jones", "Jr.");
        assertThat(author.getFullName()).isEqualTo("Jacob Jones, Jr.");
    }

    @Test
    @DisplayName("Validate name parsing from a string")
    void validateFromName() {
        var authors = Author.fromString("John Smith");
        assertThat(authors.size()).isEqualTo(1);
        assertThat(authors.get(0)).hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Smith");

        authors = Author.fromString("John Smith and Jane Doe");
        assertThat(authors.size()).isEqualTo(2);
        assertThat(authors.get(0)).hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Smith");
        assertThat(authors.get(1)).hasFieldOrPropertyWithValue("firstName", "Jane")
                .hasFieldOrPropertyWithValue("lastName", "Doe");

        authors = Author.fromString("John Smith, Jim Jones, and Jane Doe");
        assertThat(authors.size()).isEqualTo(3);
        assertThat(authors.get(0)).hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Smith");
        assertThat(authors.get(1)).hasFieldOrPropertyWithValue("firstName", "Jim")
                .hasFieldOrPropertyWithValue("lastName", "Jones");
        assertThat(authors.get(2)).hasFieldOrPropertyWithValue("firstName", "Jane")
                .hasFieldOrPropertyWithValue("lastName", "Doe");

    }
}