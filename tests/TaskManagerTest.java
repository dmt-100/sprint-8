import main.java.intefaces.TasksManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

abstract class TaskManagerTest<T extends TasksManager> {

    protected T taskManager; // не пойму как пользоваться этим полем

    abstract void setManager();


    abstract void init() throws IOException;

    abstract void beforeEachAddTasks() throws IOException;

    abstract void afterEachRemoveTasks();
}