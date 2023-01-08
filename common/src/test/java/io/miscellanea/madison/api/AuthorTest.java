package io.miscellanea.madison.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AuthorTest {
    @Test
    @DisplayName("Validate that full name is correctly generated")
    void validateFullName(){
        var author = new Author("Jane","Smith","Doe","");
        assertThat(author.fullName()).isEqualTo("Jane Smith Doe");

        author = new Author("John","", "Smith", "");
        assertThat(author.fullName()).isEqualTo("John Smith");

        author = new Author("Jacob","","Jones","Jr.");
        assertThat(author.fullName()).isEqualTo("Jacob Jones, Jr.");
    }
}