import main.java.managers.FileBackedTasksManager;
import main.java.managers.HttpTaskManager;
import main.java.managers.Managers;
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
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    private KVServer kvServer;

    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    public static File file = new File(saveTasksFilePath);
    URI uri = URI.create("http://localhost:8078/");

    LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
    LocalDateTime dateTimeTestTask2 = LocalDateTime.parse("2000-01-01T02:00:00");
    LocalDateTime dateTimeTestTask3 = LocalDateTime.parse("2000-01-01T03:00:00");
    LocalDateTime dateTimeTestTask4 = LocalDateTime.parse("2000-01-01T04:00:00");

    LocalDateTime dateTimeTestEpic1 = LocalDateTime.parse("2000-01-01T05:00:00");
    LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
    LocalDateTime dateTimeTestSubtask2 = LocalDateTime.parse("2000-01-01T07:40:00");

    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");

    FileBackedTasksManager fileBackedTasksManager;

    HttpTaskManager httpTaskManager1;
    HttpTaskManager httpTaskManager2;
    HttpTaskManagerTest httpTaskManagerTest;

    @BeforeEach
    void init()  throws IOException {

        kvServer = new KVServer();
        kvServer.start();
        setTaskManager();
// ----------------------------------------
        fileBackedTasksManager = new FileBackedTasksManager(file);

        Task task1 = new Task(
                TaskType.TASK,
                "Task1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestTask1,
                50
        );
        fileBackedTasksManager.addTask(task1);

        Task task2 = new Task(
                TaskType.TASK,
                "Task2",
                "Pack the cat",
                Status.NEW,
                dateTimeTestTask2,
                5
        );
        fileBackedTasksManager.addTask(task2);

        List<UUID> subtasksList = new ArrayList<>();
        Epic epic1 = new Epic(
                epicUuid,
                TaskType.EPIC,
                "Epic1",
                "Relocation",
                Status.NEW,
                dateTimeTestEpic1,
                0,
                subtasksList
        );
        fileBackedTasksManager.addTask(epic1);

        Subtask subtask1 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestSubtask1,
                50,
                fileBackedTasksManager.getTasks().get(epic1.getId()).getId()
        );
        fileBackedTasksManager.addTask(subtask1);

        Subtask subtask2 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Pack the cat",
                Status.NEW,
                dateTimeTestSubtask2,
                15,
                fileBackedTasksManager.getTasks().get(epic1.getId()).getId()
        );
        fileBackedTasksManager.addTask(subtask2);

        //Не могу понять почему падает
//        httpTaskManager1 = new HttpTaskManager(uri, false);
        httpTaskManager1 = Managers.getDefault();
        httpTaskManager1.save();
        httpTaskManager2 = new HttpTaskManager(uri, true);

    }

    @AfterEach
    void after() {
        kvServer.stop();
    }
    @Test
    void checkTasks() {
        HttpTaskManager load = new HttpTaskManager(URI.create("http://localhost:8078/"), true);
        assertEquals(taskManager.getAllTasks(), load.getAllTasks(),
                "Задачи после выгрузки не совпадают");
        assertEquals(taskManager.prioritizeTasks(), load.prioritizeTasks(),
                "Отсортированный список не совпадает");
        assertEquals(taskManager.getHistory(), load.getHistory(),
                "Список задач в истории не совпадает");
    }

    @Override
    void setTaskManager() {
        taskManager = new HttpTaskManager(URI.create("http://localhost:8078/"), false);
    }
}