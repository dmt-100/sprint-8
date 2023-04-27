package main.java.managers;

import com.google.gson.Gson;
import main.java.intefaces.HistoryManager;
import main.java.intefaces.TasksManager;
import main.java.server.GsonManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class Managers {
    private static final String sep = File.separator;
    private static final String saveTasksFilePath = String.join(sep, "src", "main", "java", "resources", "taskSaves" + ".csv");
    public static File file = new File(saveTasksFilePath);
    private static final URI BASE_URL = URI.create("http://localhost:8078/");

    public static  HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static TasksManager getDefaultFileBacked() {
        return new FileBackedTasksManager(file);
    }

    public static Gson getGson() {
        return GsonManager.getGson();
    }

    public static HttpTaskManager getDefault() throws IOException {
        return new HttpTaskManager(BASE_URL);
    }
}
