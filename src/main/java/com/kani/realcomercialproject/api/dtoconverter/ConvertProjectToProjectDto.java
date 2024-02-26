package com.kani.realcomercialproject.api.dtoconverter;

import com.kani.realcomercialproject.api.dto.ProjectDto;
import com.kani.realcomercialproject.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ConvertProjectToProjectDto {

    public ProjectDto convertToProjectDto(Project project){
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .createAt(project.getCreateAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

}
