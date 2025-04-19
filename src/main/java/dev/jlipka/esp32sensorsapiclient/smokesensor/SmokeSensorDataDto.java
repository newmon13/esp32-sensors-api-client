package dev.jlipka.esp32sensorsapiclient.smokesensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SmokeSensorDataDto(
        @JsonProperty("raw_value") int rawValue,
        @JsonProperty("smoke_presence") boolean smokePresence,
        @JsonProperty("normalized_value") int normalizedValue,
        @JsonProperty("severity") SmokeLevelSeverity severity
) {
}
