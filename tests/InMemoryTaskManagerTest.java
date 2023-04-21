import main.java.managers.InMemoryTaskManager;
import main.java.service.Status;
import main.java.service.TaskType;
import main.java.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();

    @Override
    @BeforeEach
    void setTaskManager() {
        inMemoryTaskManager = fileBackedTasksManager;
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
        inMemoryTaskManager.getTasks().clear();

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
        assertEquals(1, statusDoneOrNew);
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
        assertEquals(1, statusInprogress);
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
        inMemoryTaskManager.getTasks().clear();
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
        clearHistory();
        // Redirect standard output stream to a ByteArrayOutputStream
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Call the void method that contains System.out.println
        inMemoryTaskManager.prioritizeTasks();

        // Get the output from the ByteArrayOutputStream
        String expectedOutput = "Нужно ещё больше задач";
        String actualOutput = outContent.toString().trim();

        // Assert that the output matches the expected output
        assertEquals(expectedOutput, actualOutput);
    }
}