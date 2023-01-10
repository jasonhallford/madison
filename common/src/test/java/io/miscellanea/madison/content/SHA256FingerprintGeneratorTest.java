package io.miscellanea.madison.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class SHA256FingerprintGeneratorTest {
    @Test
    @DisplayName("A null URL throws an IllegalArgumentException")
    void nullUrlThrowsException() {
        var gen = new SHA256FingerprintGenerator();

        assertThatThrownBy(() -> gen.fromUrl(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A null byte array throws an IllegalArgumentException")
    void nullBytesThrowsException() {
        var gen = new SHA256FingerprintGenerator();

        assertThatThrownBy(() -> gen.fromBytes(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("UTF-8 bytes return SHA256 hash ID")
    void identityFromBytes() {
        var gen = new SHA256FingerprintGenerator();

        var id = gen.fromBytes("This is my test byte array".getBytes(StandardCharsets.UTF_8));
        assertThat(id).isNotBlank().isEqualToIgnoringCase("64b461d25d08f69c2de3cb34907357286ecb2b41c49c48993b1534ccaf33ec00");
    }

    @Test
    @DisplayName("File URL returns SHA256 hash ID")
    void identityFromFile() {
        var gen = new SHA256FingerprintGenerator();

        URL url = this.getClass().getResource("/id-test-file.txt");
        assertThat(url).isNotNull();

        String id = gen.fromUrl(url);
        assertThat(id).isNotNull().isEqualToIgnoringCase("3904e22b428679bb5e8350279bae67e033a15a62145231f8a2bb58bcb8dbb96a");

    }
}