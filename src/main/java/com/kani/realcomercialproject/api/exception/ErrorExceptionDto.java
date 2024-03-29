package com.kani.realcomercialproject.api.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorExceptionDto {

    String error;

    @JsonProperty("error_description")
    String errorDescription;
}
