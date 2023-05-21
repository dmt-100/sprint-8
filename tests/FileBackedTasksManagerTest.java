import main.java.managers.FileBackedTasksManager;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    private static File file = new File(saveTasksFilePath);

    List<UUID> subtasks = new ArrayList<>();

    UUID testUuid = UUID.fromString("00000000-0000-48c2-bb4a-f4cf88f18e23");
    UUID wrongUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID randomUuid = UUID.randomUUID();

    LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
    LocalDateTime dateTimeTestTask2 = LocalDateTime.parse("2000-01-01T02:00:00");
    LocalDateTime dateTimeTestTask3 = LocalDateTime.parse("2000-01-01T03:00:00");
    LocalDateTime dateTimeTestTask4 = LocalDateTime.parse("2000-01-01T04:00:00");
    LocalDateTime dateTimeTestEpic1 = LocalDateTime.parse("2000-01-01T05:00:00");
    LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
    LocalDateTime dateTimeTestSubtask2 = LocalDateTime.parse("2000-01-01T07:40:00");
    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");
    Task task1;
    Epic epic1;
    Subtask subtask1;
    Subtask subtask2;
    FileBackedTasksManager fileBackedTasksManager;
    @Override
    void setManager() {
        fileBackedTasksManager = new FileBackedTasksManager(file);
    }

    @Override
    void init() {
        setManager();

        task1 = new Task(
                TaskType.TASK,
                "Task1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestTask1,
                50
        );

        List<UUID> subtasksList = new ArrayList<>();
        epic1 = new Epic(
                epicUuid,
                TaskType.EPIC,
                "Epic1",
                "Relocation",
                Status.NEW,
                dateTimeTestEpic1,
                0,
                subtasksList
        );

        subtask1 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestSubtask1,
                50,
                epicUuid
        );

        subtask2 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Pack the cat",
                Status.NEW,
                dateTimeTestSubtask2,
                15,
                epicUuid
        );

    }

    @Override
    @BeforeEach
    void beforeEachAddTasks() {
        init();
        fileBackedTasksManager.addTask(task1);
        fileBackedTasksManager.addTask(epic1);
        fileBackedTasksManager.addTask(subtask1);
        fileBackedTasksManager.addTask(subtask2);
    }

    @Override
    @AfterEach
    void afterEachRemoveTasks() {

        if (fileBackedTasksManager.getTasks().containsKey(task1.getId())) {
            fileBackedTasksManager.removeTaskById(task1.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(subtask1.getId())) {
            fileBackedTasksManager.removeTaskById(subtask1.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(subtask2.getId())) {
            fileBackedTasksManager.removeTaskById(subtask2.getId());
        }
        if (fileBackedTasksManager.getTasks().containsKey(epic1.getId())) {
            fileBackedTasksManager.removeTaskById(epic1.getId());
        }
    }
}