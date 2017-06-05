package database;

import com.example.communication.model.CleanTask;
import com.example.communication.model.Frequency;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 21.01.2017.
 */
public class CleanTasks
{
    public static CleanTask createOne() {
        return createWithName("One");
    }

    public static CleanTask createWithName(String name) {
        return new CleanTask(name, "Description", "Responsible", 1, 2, DateTime.now());
    }

    public static CleanTask createWithIndex(int i) {
        return new CleanTask("Name " + i, "Description " + i, "Responsible " + i, i, i + 1, DateTime.now().plusSeconds(i), Frequency.daily, i, i,DateTime.now().plusSeconds(i), DateTime.now().plusSeconds(i), false, "Id" + i, "Id" + i);
    }

    public static List<CleanTask> createList(int i) {
        List<CleanTask> cleanTasks = new ArrayList<>(i);
        for (int j = 0; j < i; j++)
        {
            cleanTasks.add(createWithIndex(j));
        }

        return cleanTasks;
    }
}
