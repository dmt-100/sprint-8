package main.java.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import main.java.intefaces.TasksManager;
import main.java.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskManagerAdapter extends TypeAdapter<TasksManager> {
    Gson gson = new Gson();

    @Override
    public void write(JsonWriter jsonWriter, TasksManager manager) throws IOException {
        List<UUID> history = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            history.add(task.getId());
        }

        jsonWriter.beginObject();
        jsonWriter.name("history").jsonValue(gson.toJson(history));

        jsonWriter.name("tasks").jsonValue(gson.toJson(manager.getAllTasks()));
        jsonWriter.endObject();
        jsonWriter.close();
    }

    @Override
    public TasksManager read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
