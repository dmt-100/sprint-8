import main.java.intefaces.TasksManager;
import main.java.managers.FileBackedTasksManager;
import main.java.managers.HttpTaskManager;
import main.java.server.KVServer;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    private static File file = new File(saveTasksFilePath);
    TasksManager fBTM;
    List<UUID> subtasks = new ArrayList<>();

    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");

    @Override
    void setTaskManager() {
        fBTM = new FileBackedTasksManager(file);
    }


    Task task = new Task(
            TaskType.TASK,
            "Task1",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T00:00:00"),
            50
    );

    Task epic = new Epic(
            TaskType.EPIC,
            "Эпик1",
            "Переезд",
            Status.NEW,
            subtasks
    );

    Task subtask = new Subtask(
            TaskType.SUBTASK,
            "Subtask1",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T04:00:00"),
            50,
            epicUuid
    );

    Task subtask2 = new Subtask(
            TaskType.SUBTASK,
            "Subtask2",
            "Собрать коробки",
            Status.NEW,
            LocalDateTime.parse("2000-01-01T04:00:00"),
            50,
            epicUuid
    );

    @BeforeEach
    void init() {
        setTaskManager();
        fBTM.addTask(task);
        fBTM.addTask(epic);
        subtask.setEpicId(epic.getId());
        subtask2.setEpicId(epic.getId());
        fBTM.addTask(subtask);
        fBTM.addTask(subtask2);
        get();
    }

    @AfterEach
    void clearHistory() {
        if (fBTM.getTasks().containsKey(task.getId())) {
            fBTM.removeTaskById(task.getId());
        }
        if (fBTM.getTasks().containsKey(subtask.getId())) {
            fBTM.removeTaskById(subtask.getId());
        }
        if (fBTM.getTasks().containsKey(subtask2.getId())) {
            fBTM.removeTaskById(subtask2.getId());
        }
        if (fBTM.getTasks().containsKey(epic.getId())) {
            fBTM.removeTaskById(epic.getId());
        }
        inMemoryTaskManager.getTasks().clear();
    }

    @Test
    void getAddedTasksFromFile() {
        List<Task> expected = fBTM.getTasks().values().stream().toList();
        expected = expected.stream().sorted(Comparator.comparing(Task::getName)).collect(Collectors.toList());
        List<Task> actual = fBTM.getAddedTasksFromFile();
        actual = actual.stream().sorted(Comparator.comparing(Task::getName)).collect(Collectors.toList());
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void getHistoryFromFile() {
        List<UUID> expected = fBTM.getHistory().stream().map(t ->t.getId()).collect(Collectors.toList());
        List<UUID> actual = fBTM.loadHistoryFromFile();
        assertEquals(expected, actual);
    }

}