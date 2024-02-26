package com.kani.realcomercialproject.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class TaskState {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;
    @Column(unique = true)
    String name;
    @OneToOne
    TaskState leftTaskState;
    @OneToOne
    TaskState rightTaskState;

    @ManyToOne
    Project project;

    @Builder.Default
    Instant createAt = Instant.now();
    String description;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany
    @JoinColumn(name = "task_state_id", referencedColumnName = "id")
    List<Task> taskList = new ArrayList<>();

    public Optional<TaskState> getLeftTaskState() {
        return Optional.ofNullable(leftTaskState);
    }

    public Optional<TaskState> getRightTaskState() {
        return Optional.ofNullable(rightTaskState);
    }
}
