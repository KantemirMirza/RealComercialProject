package com.kani.realcomercialproject.api.controller;

import com.kani.realcomercialproject.api.dto.AnswerDto;
import com.kani.realcomercialproject.api.dto.TaskStateDto;
import com.kani.realcomercialproject.api.dtoconverter.ConvertTaskStateToTaskStateDto;
import com.kani.realcomercialproject.api.exception.BadRequestException;
import com.kani.realcomercialproject.api.exception.ProjectNotFoundException;
import com.kani.realcomercialproject.entity.Project;
import com.kani.realcomercialproject.entity.TaskState;
import com.kani.realcomercialproject.repository.ITaskStateRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class TaskStateController {
    ITaskStateRepository taskStateRepository;
    ConvertTaskStateToTaskStateDto convertTaskStateToTaskStateDto;
    ControllerHelper controllerHelper;
    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";
    public static final String CHANGE_TASK_STATE_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/task-states/{task_state_id}";

    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable(name = "project_id") Long projectId) {

        Project project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStateList()
                .stream()
                .map(convertTaskStateToTaskStateDto::convertToTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskSate(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(name = "task_state_name") String taskStateName) throws BadRequestException {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name can't be empty.");
        }

        Project project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskState> optionalAnotherTaskState = Optional.empty();

        for (TaskState taskState: project.getTaskStateList()) {

            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName));
            }

            if (!taskState.getRightTaskState().isPresent()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }
        }

        TaskState taskState = taskStateRepository.saveAndFlush(
                TaskState.builder()
                        .name(taskStateName)
                        .project(project)
                        .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {

                    taskState.setLeftTaskState(anotherTaskState);

                    anotherTaskState.setRightTaskState(taskState);

                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskState savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return convertTaskStateToTaskStateDto.convertToTaskStateDto(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_state_name") String taskStateName) throws BadRequestException {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name can't be empty.");
        }

        TaskState taskState = getTaskStateOrThrowException(taskStateId);

        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName
                )
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    try {
                        throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName));
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    }
                });

        taskState.setName(taskStateName);

        taskState = taskStateRepository.saveAndFlush(taskState);

        return convertTaskStateToTaskStateDto.convertToTaskStateDto(taskState);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public TaskStateDto changeTaskStatePosition(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "left_task_state_id", required = false) Optional<Long> optionalLeftTaskStateId) {

        TaskState changeTaskState = getTaskStateOrThrowException(taskStateId);

        Project project = changeTaskState.getProject();

        Optional<Long> optionalOldLeftTaskStateId = changeTaskState
                .getLeftTaskState()
                .map(TaskState::getId);
        if (optionalOldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return convertTaskStateToTaskStateDto.convertToTaskStateDto(changeTaskState);
        }

        Optional<TaskState> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {

                    if (taskStateId.equals(leftTaskStateId)) {
                        try {
                            throw new BadRequestException("Left task state id equals changed task state.");
                        } catch (BadRequestException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    TaskState leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

                    if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        try {
                            throw new BadRequestException("Task state position can be changed within the same project.");
                        } catch (BadRequestException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return leftTaskStateEntity;
                });

        Optional<TaskState> optionalNewRightTaskState;
        if (!optionalNewLeftTaskState.isPresent()) {

            optionalNewRightTaskState = project
                    .getTaskStateList()
                    .stream()
                    .filter(anotherTaskState -> !anotherTaskState.getLeftTaskState().isPresent())
                    .findAny();
        } else {

            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStatePosition(changeTaskState);

        if (optionalNewLeftTaskState.isPresent()) {

            TaskState newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changeTaskState.setLeftTaskState(null);
        }

        if (optionalNewRightTaskState.isPresent()) {

            TaskState newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);
        } else {
            changeTaskState.setRightTaskState(null);
        }

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);
        return convertTaskStateToTaskStateDto.convertToTaskStateDto(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AnswerDto deleteTaskState(@PathVariable(name = "task_state_id") Long taskStateId) {

        TaskState changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStatePosition(changeTaskState);

        taskStateRepository.delete(changeTaskState);

        return AnswerDto.builder().answer(true).build();
    }

    private void replaceOldTaskStatePosition(TaskState changeTaskState) {

        Optional<TaskState> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskState> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it -> {

                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });

        optionalOldRightTaskState
                .ifPresent(it -> {

                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });
    }

    private TaskState getTaskStateOrThrowException(Long taskStateId) {

        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                String.format(
                                        "Task state with \"%s\" id doesn't exist.",
                                        taskStateId
                                )
                        )
                );
    }

}
