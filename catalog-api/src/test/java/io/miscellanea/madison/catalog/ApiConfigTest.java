package io.miscellanea.madison.catalog;

import io.miscellanea.madison.catalog.config.ApiConfig;
import io.miscellanea.madison.catalog.config.BrokerConfig;
import io.miscellanea.madison.catalog.config.RestConfig;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ApiConfigTest {
    @Test
    @DisplayName("Config can be serialized to JSON")
    void serializableToJson() {

        var config = new ApiConfig( new RestConfig(8080,"/var/lib/madison/import"),
                new BrokerConfig("localhost",5672,"",""));

        var json = JsonObject.mapFrom(config);
        assertThat(json).isNotNull();
        assertThat(json.toString()).isEqualTo("{\"restConfig\":{\"port\":8080}}");
    }
}