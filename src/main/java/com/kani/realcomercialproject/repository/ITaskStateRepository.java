package com.kani.realcomercialproject.repository;

import com.kani.realcomercialproject.entity.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITaskStateRepository extends JpaRepository<TaskState, Long> {
}
