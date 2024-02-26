package com.kani.realcomercialproject.api.controller;

import com.kani.realcomercialproject.api.dto.AnswerDto;
import com.kani.realcomercialproject.api.dto.ProjectDto;
import com.kani.realcomercialproject.api.dtoconverter.ConvertProjectToProjectDto;
import com.kani.realcomercialproject.api.exception.BadRequestException;
import com.kani.realcomercialproject.api.exception.ProjectNotFoundException;
import com.kani.realcomercialproject.entity.Project;
import com.kani.realcomercialproject.repository.IProjectRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class ProjectController {
    IProjectRepository projectRepository;
    ConvertProjectToProjectDto projectDto;
    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/project";
    public static final String EDIT_PROJECT = "/api/project/{id}/edit";
    public static final String DELETE_PROJECT = "/api/project/{id}/delete";
    //public static final String CREATE_OR_UPDATE_PROJECT = "/api/project/create_or_update";

    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> allProjects(@RequestParam(value = "prefix_name", required = false)
                                                        Optional<String> optionalPrefixName){
        optionalPrefixName = optionalPrefixName.filter(prefix -> !prefix.trim().isEmpty());

        Stream<Project> projects = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAll);
        return projects
                .map(projectDto::convertToProjectDto).collect(Collectors.toList());
    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam(value = "project_name") String projectName) throws BadRequestException {
        if(projectName.trim().isEmpty()){
            throw new BadRequestException("Name can not be empty!!!");
        }
        projectRepository.findByName(projectName).ifPresent(project ->{
            try {
                throw new BadRequestException("Project already exists: " + projectName);
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        });
        Project project = projectRepository.saveAndFlush(
                Project.builder()
                        .name(projectName)
                        .build()
        );
        return projectDto.convertToProjectDto(project);
    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(@PathVariable("id") Long id, @RequestParam String name) throws BadRequestException {
        if(name.trim().isEmpty()){
            throw new BadRequestException("Name can not be empty!!!");
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(()-> new ProjectNotFoundException("Project with the Id not found: " + id));

        Project finalProject = project;
        projectRepository.findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), finalProject.getId()))
                .ifPresent(anotherProject ->{
            try {
                throw new BadRequestException("Project already exists: " + name);
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        });
        project.setName(name);
        project = projectRepository.saveAndFlush(project);
        return projectDto.convertToProjectDto(project);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AnswerDto deleteProject(@PathVariable("id") Long id) throws ProjectNotFoundException {
        projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
        projectRepository.deleteById(id);
        return AnswerDto.answerDto(true);
    }


    //    @PutMapping(CREATE_OR_UPDATE_PROJECT)
//    public ProjectDto createOrUpdateProject(
//            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
//            @RequestParam(value = "project_name") Optional<String> optionalProjectName) throws BadRequestException {
//
//       optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());
//       boolean isCreated = !optionalProjectId.isPresent();
//
//        if(isCreated && !optionalProjectName.isPresent()){
//            throw new BadRequestException("Project name can not be empty!!!");
//        }
//
//       Project project = optionalProjectId
//               .map(this::getProjectOrThrowException)
//               .orElseGet(()-> Project.builder().build());
//
//       optionalProjectName.ifPresent(projectName ->{
//           projectRepository.findByName(projectName)
//                   .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
//                   .ifPresent(anotherProject ->{
//                       try {
//                           throw new BadRequestException("Project already exists: " + projectName);
//                       } catch (BadRequestException e) {
//                           throw new RuntimeException(e);
//                       }
//        });
//           project.setName(projectName);
//    });
//        final Project saveProject = projectRepository.save(project);
//        return projectDto.convertToProjectDto(saveProject);
//    }


}
