package com.kani.realcomercialproject.api.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerDto {
    Boolean answer;
    public static AnswerDto answerDto(Boolean answer){
        return builder()
                .answer(answer)
                .build();
    }
}
