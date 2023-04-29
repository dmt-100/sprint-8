package main.java.tasks;

import main.java.service.Status;
import main.java.service.TaskType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Epic extends Task {
    /*
    из пачки
Всем привет! а у вас у всех классы Task, Subtask и Epic не имеют ссылок друг на друга? Вчера жестко потратил 2ч на то что gson пытался сделать бесконечный паровозик из моих классов Subtask и Epic, так как у Epic есть массив Subtask'ов и у Subtask есть ссылка на Epic. Как я понял, gson пытался сериализовать Epic, в котором есть Subtask'и, а у него в свою очередь ссылка Epic, и тут начинается бесконечность ) только слово transient разорвало паровоз
 */

    private final transient List<UUID> subtasks;

    public Epic(
            TaskType taskType,
            String name,
            String description,
            Status status,
            List<UUID> subtasks
    ) {
        super(taskType, name, description, status);
        this.subtasks = subtasks;
    }

    public Epic(
            UUID id,
            TaskType taskType,
            String name,
            String description,
            Status status,
            LocalDateTime startTime,
            int duration,
            List<UUID> subtasks
    ) {
        super(id, taskType, name, description, status, startTime, duration);
        this.subtasks = subtasks;
    }

    public Epic(
            UUID id,
            TaskType taskType,
            String name,
            String description,
            Status status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int duration,
            List<UUID> subtasks
            ) {
        super(id, taskType, name, description, status, startTime, endTime, duration);
        this.subtasks = subtasks;
    }

    public Epic(
                 UUID id,
                 TaskType taskType,
                 String name,
                 String description,
                 Status status,
                 List<UUID> subtasks
    ) {
        super(id, taskType, name, description, status);
        this.subtasks = subtasks;
    }
//
//    public Epic( // конструктор для восстановления taskfromString() создания задачи из строки
//    ) {
//        super(id, taskType, name, description, status);
//        this.startTime = startTime;
//        this.endTime = endTime;
//        this.subtasks = subtasks;
//        this.duration = duration;
//    }



    @Override
    public String cleanSubtaskIds() {
        subtasks.clear();
        return "Список Эпика от подзадач очищен."; // ТЗ - 7
    }

    @Override
    public void removeSubtask(UUID id) {
        subtasks.remove(id);
    }

    @Override
    public List<UUID> getSubtasks() {
        return subtasks;
    }

    @Override
    public void setSubtasks(UUID subtask) {
        subtasks.add(subtask);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    @Override
    public String toString() {
        return "Epic{" + "id=" + getId() +
                ", taskType=" + getTaskType() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", startTime='" + getStartTime() + '\'' +
                ", endTime='" + getEndTime() + '\'' +
                ", duration='" + getDuration() + '\'' +
                ", subtasksList=" + subtasks + '}';
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
                getDuration();
        return result;
    }
}
