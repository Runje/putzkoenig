package database;


import com.example.communication.model.CleanTask;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Thomas on 17.01.2017.
 */
public interface PutzDatabase
{
    void addCleanTasks(List<CleanTask> cleanTasks) throws SQLException;

    boolean checkIfCleanTaskIdExists(int id) throws SQLException;

    void startTransaction() throws SQLException;

    void rollback();
}
