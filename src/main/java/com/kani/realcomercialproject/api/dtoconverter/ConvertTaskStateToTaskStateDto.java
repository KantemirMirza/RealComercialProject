package com.kani.realcomercialproject.api.dtoconverter;

import com.kani.realcomercialproject.api.dto.TaskStateDto;
import com.kani.realcomercialproject.entity.TaskState;
import org.springframework.stereotype.Component;

@Component
public class ConvertTaskStateToTaskStateDto {

    public TaskStateDto convertToTaskStateDto(TaskState taskState){
        return TaskStateDto.builder()
                .id(taskState.getId())
                .name(taskState.getName())
                .ordinal(taskState.getOrdinal())
                .createAt(taskState.getCreateAt())
                .description(taskState.getDescription())
                .build();
    }
}
