package main.java.tasks;

import main.java.service.Status;
import main.java.service.TaskType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Subtask extends Task {
    private UUID epicId;

    public Subtask(
            TaskType taskType,
            String name,
            String description,
            Status status,
            LocalDateTime startTime,
            int duration,
            UUID epicId
    ) {
        super(taskType, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(
            UUID id,
            TaskType taskType,
            String name,
            String description,
            Status status,
            LocalDateTime startTime,
            int duration,
            UUID epicId
    ) {
        super(id, taskType, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask( // конструктор для восстановления taskfromString()
            UUID id,
            TaskType taskType,
            String name,
            String description,
            Status status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int duration,
            UUID epicId
    ) {
        super(id, taskType, name, description, status, startTime, endTime, duration);
        this.epicId = epicId;
    }

    @Override
    public UUID getEpicId() {
        return epicId;
    }

    @Override
    public int getDuration() {
        return super.getDuration();
    }

    @Override
    public void setEpicId(UUID epicId) {
        this.epicId = epicId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return "Subtask{" + "id=" + getId() +
                ", taskType=" + getTaskType() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", startTime='" + getStartTime() + '\'' +
                ", endTime='" + getEndTime() + '\'' +
                ", duration='" + getDuration() + '\'' +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toCsvFormat() {
        String result;
        result = getId() + "," +
                getTaskType() + "," +
                getName() + "," +
                getDescription() + "," +
                getStatus() + "," +
                getStartTime() + "," +
                getEndTime() + "," +
                getDuration() + "," +
                epicId;
        return result;
    }
}
