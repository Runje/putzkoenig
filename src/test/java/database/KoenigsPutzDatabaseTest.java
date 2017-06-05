package database;

import com.example.communication.model.CleanTask;
import com.example.communication.model.database.CleanTaskEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.List;

/**
 * Created by Thomas on 20.01.2017.
 */
public class KoenigsPutzDatabaseTest
{
    private KoenigsPutzDatabase database;
    private String DB_TEST_NAME = "Test.db";

    @Before
    public void setup() throws SQLException
    {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_TEST_NAME);
        database = new KoenigsPutzDatabase(connection);
        DatabaseMetaData metaData = connection.getMetaData();
        Assert.assertTrue(metaData.supportsBatchUpdates());
        Assert.assertTrue(metaData.supportsTransactions());



        if (!database.cleanTaskTableExists()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(CleanTaskEntry.CREATE);
        }

        Assert.assertTrue(database.cleanTaskTableExists());

        String query = "DELETE FROM " + CleanTaskEntry.TABLE;
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    @After
    public void teardown() throws SQLException
    {
        database.stop();
    }

    @Test
    public void add() throws SQLException
    {
        List<CleanTask> expCleanTasks = CleanTasks.createList(100);
        database.addCleanTasks(expCleanTasks);

        List<CleanTask> cleanTasks = database.getAllCleanTasks();
        Assert.assertEquals(expCleanTasks.toString(), cleanTasks.toString());
    }

    @Test
    public void exists() throws SQLException
    {
        database.addCleanTask(CleanTasks.createWithIndex(2));
        Assert.assertTrue(database.checkIfCleanTaskIdExists(2));
        Assert.assertFalse(database.checkIfCleanTaskIdExists(1));
    }

    @Test
    public void generateNewId() throws SQLException
    {
        CleanTask cleanTask = CleanTasks.createWithIndex(-1);
        database.addCleanTask(cleanTask);
        Assert.assertFalse(database.checkIfCleanTaskIdExists(-1));
        database.addCleanTask(cleanTask);
        Assert.assertFalse(database.checkIfCleanTaskIdExists(-1));
    }


    @Test
    public void transaction() throws SQLException
    {
        database.startTransaction();
        int id = 5;
        CleanTask cleanTask = CleanTasks.createWithIndex(id);
        Assert.assertFalse(database.checkIfCleanTaskIdExists(id));
        database.addCleanTask(cleanTask);
        database.rollback();
        Assert.assertFalse(database.checkIfCleanTaskIdExists(id));

        database.startTransaction();
        database.addCleanTask(cleanTask);
        database.commit();
        Assert.assertTrue(database.checkIfCleanTaskIdExists(id));
    }
}