package com.kani.realcomercialproject.api.dtoconverter;

import com.kani.realcomercialproject.api.dto.TaskDto;
import com.kani.realcomercialproject.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class ConvertTaskToTaskDto {

    public TaskDto convertToTaskDto(Task task){
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .createAt(task.getCreateAt())
                .description(task.getDescription())
                .build();
    }
}
