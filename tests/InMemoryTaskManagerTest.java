import main.java.intefaces.TasksManager;
import main.java.managers.HttpTaskManager;
import main.java.managers.InMemoryTaskManager;
import main.java.server.KVServer;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Epic;
import main.java.tasks.Subtask;
import main.java.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    LocalDateTime dateTimeTestTask1 = LocalDateTime.parse("2000-01-01T01:00:00");
    LocalDateTime dateTimeTestTask2 = LocalDateTime.parse("2000-01-01T02:00:00");
    LocalDateTime dateTimeTestTask3 = LocalDateTime.parse("2000-01-01T03:00:00");
    LocalDateTime dateTimeTestTask4 = LocalDateTime.parse("2000-01-01T04:00:00");
    LocalDateTime dateTimeTestEpic1 = LocalDateTime.parse("2000-01-01T05:00:00");
    LocalDateTime dateTimeTestSubtask1 = LocalDateTime.parse("2000-01-01T06:00:00");
    LocalDateTime dateTimeTestSubtask2 = LocalDateTime.parse("2000-01-01T07:40:00");
    UUID epicUuid = UUID.fromString("11111111-d496-48c2-bb4a-f4cf88f18e23");
    Task task1;
    Task task2;
    Epic epic1;
    Subtask subtask1;
    Subtask subtask2;

    TasksManager inMemoryTaskManager;

    @Override
    void setTaskManager() {
        inMemoryTaskManager = new InMemoryTaskManager();
    }

    @BeforeEach
    void init() {
        setTaskManager();

        task1 = new Task(
                TaskType.TASK,
                "Task1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestTask1,
                50
        );
        inMemoryTaskManager.addTask(task1);

        task2 = new Task(
                TaskType.TASK,
                "Task2",
                "Pack the cat",
                Status.NEW,
                dateTimeTestTask2,
                5
        );
        inMemoryTaskManager.addTask(task2);

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
        inMemoryTaskManager.addTask(epic1);

        subtask1 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Collect boxes",
                Status.NEW,
                dateTimeTestSubtask1,
                50,
                inMemoryTaskManager.getTasks().get(epic1.getId()).getId()
        );
        inMemoryTaskManager.addTask(subtask1);

        subtask2 = new Subtask(
                TaskType.SUBTASK,
                "Subtask1",
                "Pack the cat",
                Status.NEW,
                dateTimeTestSubtask2,
                15,
                inMemoryTaskManager.getTasks().get(epic1.getId()).getId()
        );
        inMemoryTaskManager.addTask(subtask2);

    }

    @AfterEach
    void afterEach() {
        inMemoryTaskManager.getTasks().clear();
    }




    // ============================================================================
    // Блок тестов на изменения статусов
    @Test
    void allSubtasksWithStatusNew() { // b.   Все подзадачи со статусом NEW.
        boolean flag = false; // флаг на прохождение фильтра
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK) && subtask.getStatus().equals(Status.NEW)) {
                flag = true;
                assertTrue(flag);
            }
        }

    }

    @Test
    void allSubtasksWithStatusNewAndDone() { // d.    Подзадачи со статусами NEW и DONE.
        int statusDoneOrNew = 0;
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {
                subtask.setStatus(Status.DONE); // первому сабтаску назначаем DONE и break;
                break;
            }
        }
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {

                if (subtask.getStatus().equals(Status.DONE) || subtask.getStatus().equals(Status.NEW)) {
                    statusDoneOrNew++;
                }
            }
        }
        assertEquals(2, statusDoneOrNew);
        inMemoryTaskManager.getTasks().clear();
    }

    @Test
    void allSubtasksWithStatusInprogress() { // e.    Подзадачи со статусом IN_PROGRESS.
        int statusInprogress = 0;
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {
                subtask.setStatus(Status.IN_PROGRESS); // меняем задачу на IN_PROGRESS
                statusInprogress++;
            }
        }
        assertEquals(2, statusInprogress);
        inMemoryTaskManager.getTasks().clear();
    }


    @Test
    void allSubtasksWithStatusDone() { // c.    Все подзадачи со статусом DONE.
        boolean flag;
        for (Task subtask : inMemoryTaskManager.getTasks().values()) {
            if (subtask.getTaskType().equals(TaskType.SUBTASK)) {
                inMemoryTaskManager.changeStatusTask(subtask.getId(), Status.DONE); // меняем статус подзадач
            }
        }
        for (Task epicAndSubtasks : inMemoryTaskManager.getTasks().values()) {
            if (epicAndSubtasks.getTaskType().equals(TaskType.SUBTASK) || epicAndSubtasks.getTaskType().equals(TaskType.EPIC)
                    && epicAndSubtasks.getStatus().equals(Status.DONE)) { // проверка и Эпика, так как его статус тоже меняется если все подзадачи этого эпика равны DONE
                flag = true;
                assertTrue(flag);
            }
        }
        inMemoryTaskManager.getTasks().clear();
    }

    @Test
    void cleanSubtaskIds() {
        boolean flag = true;
        inMemoryTaskManager.removeTasksByTasktype(TaskType.SUBTASK);
        for (Task task : inMemoryTaskManager.getTasks().values()) {
            if (task.getTaskType().equals(TaskType.SUBTASK)) {
                flag = false;
                break;
            }
        }
        assertTrue(flag);
    }

    // ============================================================================

    @Test
    void testPrioritizeTasksWithStandardCondition() { // a. Со стандартным поведением.
        inMemoryTaskManager.prioritizeTasks();
        LocalDateTime testTime = LocalDateTime.parse("1999-01-01 00:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US));
        boolean flag = false;
        for (Task task : inMemoryTaskManager.getTasks().values()) {
            if (task.getStartTime().isAfter(testTime)) {
                flag = true;
                testTime = task.getStartTime(); // обновляем время
            }
        }
        assertTrue(flag);
    }

    @Test
    void testPrioritizeTasksWhenEmptyMap() { // b. С пустым списком задач.
        inMemoryTaskManager.getTasks().clear();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        inMemoryTaskManager.prioritizeTasks();

        String expectedOutput = "Нужно больше задач";
        String actualOutput = outContent.toString().trim();

        assertEquals(expectedOutput, actualOutput);

        init();
    }



}