import main.java.managers.FileBackedTasksManager;
import main.java.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    private static final File file = new File(saveTasksFilePath);

    FileBackedTasksManager fManager = new FileBackedTasksManager(file);

    @Override
    @BeforeEach
    void setTaskManager() {
        fManager = fileBackedTasksManager;
    }
    @Test
    void testGetHistoryTasksAndCheckWithMapOnMatchingWithStandardCondition() {
        boolean flag = false;
        get();
        List<Task> tasks;
        tasks = fManager.getHistoryTasks();
        List<Task> tasksTest = new ArrayList<>();
        for (Task task : tasks) {
            if (fileBackedTasksManager.getTasks().containsKey(task.getId())) {
                flag = true;
            }
        }
        assertTrue(flag);
    }

    @Test
    void testGetHistoryTasksAndCheckWithMapOnMatchingWhenEmptyMap() {
        clearHistory();
        boolean flag = false;
        get();
        List<Task> tasks;
        tasks = fManager.getHistoryTasks();
        assertEquals(new ArrayList<>(), tasks);
    }
 }