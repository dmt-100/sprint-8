package main.java.intefaces;

import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TaskManager {
    void addNewTask(Task task);

    List<Task> getAllTasksByTaskType(TaskType taskType);

    void removeTasksByTasktype(TaskType taskType);

    Task getTask(UUID taskId);

    void updateTask(Task task);

    void removeTaskById(UUID id);

    void changeStatusTask(UUID id, Status status);

    List<Task> getSubtasksFromEpic(UUID epicId);

    void updateEpicStatus(UUID epicId);

    List<Task> getHistory();

    Map<UUID, Task> getTasks();

    void prioritizeTasks();

    List<Task> getPrioritizedTasks();

}