package com.kani.realcomercialproject.repository;

import com.kani.realcomercialproject.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface IProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);

    Stream<Project> streamAll();
    Stream<Project> streamAllByNameStartsWithIgnoreCase(String prefixName);
}
