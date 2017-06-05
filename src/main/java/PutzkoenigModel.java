import com.example.communication.model.CleanTask;
import com.example.communication.model.communication.ConnectUtils;
import com.example.communication.model.communication.messages.CleanTasksAnsMessage;
import com.example.communication.model.communication.messages.CleanTasksMessage;
import com.example.communication.model.communication.messages.IdentifyMessage;
import com.example.communication.model.communication.messages.KoenigsputzMessage;
import communication.OnReceiveMessageListener;
import communication.Server;
import database.KoenigsPutzDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Thomas on 17.01.2017.
 */
public class PutzkoenigModel implements OnReceiveMessageListener
{
    private Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private static String DB_NAME = "koenigsputz.db";
    private KoenigsPutzDatabase database;
    private Server server;

    public PutzkoenigModel() {

    }

    public void start() throws SQLException
    {
        // create a database connection
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
        database = new KoenigsPutzDatabase(connection);
        database.start();
        server = new Server(ConnectUtils.PORT, this);
        server.start();
    }

    @Override
    public void onReceiveMessage(KoenigsputzMessage message)
    {
        switch(message.getName()) {
            case IdentifyMessage.NAME:
                IdentifyMessage identifyMessage = (IdentifyMessage) message;
                logger.info("Hello " + identifyMessage.getUsername());
                break;
            case CleanTasksMessage.NAME:
                CleanTasksMessage cleanTasksMessage = (CleanTasksMessage) message;
                addCleanTasksFromUser(cleanTasksMessage.getCleanTasks(), message.getFromId());
                break;
            default:
                logger.error("Unknown Message: " + message.getName());
        }
    }

    private void addCleanTasksFromUser(List<CleanTask> cleanTasks, String answerTo)
    {
        try
        {
            database.startTransaction();
            int[] oldIds = new int[cleanTasks.size()];
            int[] newIds = new int[cleanTasks.size()];
            // TODO: Check for collisions, give new ids on collision, send answer, make transaction with commit
            for (int i = 0; i < cleanTasks.size(); i++)
            {
                CleanTask cleanTask = cleanTasks.get(i);
                int id = cleanTask.getId();
                oldIds[i] = id;
                boolean exists = database.checkIfCleanTaskIdExists(id);
                if (exists) {
                    // force to generate new ID
                    cleanTask.setId(-1);
                }

                int newId = database.addCleanTask(cleanTask);
                newIds[i] = newId;

            }

            database.commit();

            server.sendMessage(new CleanTasksAnsMessage(oldIds, newIds), answerTo);
        } catch (SQLException e) {
            logger.error(e.toString());
            database.rollback();
        }
    }
}
