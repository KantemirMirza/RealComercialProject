package com.kani.realcomercialproject.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDto {
    @NonNull
    Long id;
    @NonNull
    String name;
    @NonNull
    Instant createAt;
    @NonNull
    String description;
}
