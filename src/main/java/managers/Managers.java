package main.java.managers;

import com.google.gson.Gson;
import main.java.intefaces.HistoryManager;
import main.java.intefaces.TasksManager;
import main.java.server.GsonManager;

import java.io.File;

public abstract class Managers {
    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    public static File file = new File(saveTasksFilePath);

    public static  HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static TasksManager getDefaultFileBacked() {
        return new FileBackedTasksManager(file);
    }

    public static Gson getGson() {
        return GsonManager.getGson();
    }
}
