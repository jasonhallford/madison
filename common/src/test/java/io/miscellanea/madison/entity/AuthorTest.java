package io.miscellanea.madison.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AuthorTest {
    @Test
    @DisplayName("Validate that full name is correctly generated")
    void validateFullName(){
        var author = new Author("Jane","Smith","Doe","");
        assertThat(author.getFullName()).isEqualTo("Jane Smith Doe");

        author = new Author("John","", "Smith", "");
        assertThat(author.getFullName()).isEqualTo("John Smith");

        author = new Author("Jacob","","Jones","Jr.");
        assertThat(author.getFullName()).isEqualTo("Jacob Jones, Jr.");
    }
}