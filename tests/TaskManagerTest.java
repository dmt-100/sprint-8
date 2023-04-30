import main.java.intefaces.TasksManager;

abstract class TaskManagerTest<T extends TasksManager> {

    protected T taskManager;

    abstract void setManager();

}