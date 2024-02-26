package com.kani.realcomercialproject.api.dtoconverter;

import com.kani.realcomercialproject.api.dto.TaskStateDto;
import com.kani.realcomercialproject.entity.TaskState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class ConvertTaskStateToTaskStateDto {
    ConvertTaskToTaskDto convertTaskToTaskDto;

    public TaskStateDto convertToTaskStateDto(TaskState entity) {

        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreateAt())
                .leftTaskStateId(entity.getLeftTaskState().map(TaskState::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskState::getId).orElse(null))
                .tasks(
                        entity.getTaskList()
                                .stream()
                                .map(convertTaskToTaskDto::convertToTaskDto)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
