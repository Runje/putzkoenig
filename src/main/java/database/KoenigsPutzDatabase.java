package database;

import com.example.communication.model.CleanTask;
import com.example.communication.model.Frequency;
import com.example.communication.model.database.CleanTaskEntry;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 17.01.2017.
 */
public class KoenigsPutzDatabase implements PutzDatabase
{

    private Connection connection;

    public KoenigsPutzDatabase(Connection connection)
    {
        this.connection = connection;
    }

    public void start() throws SQLException
    {

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.
        if (!cleanTaskTableExists()) {
            statement.executeUpdate(CleanTaskEntry.CREATE);
        }
    }

    public void stop() throws SQLException
    {
        if(connection != null)
            connection.close();
    }

    public boolean cleanTaskTableExists() throws SQLException
    {
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, CleanTaskEntry.TABLE, null);
        if (tables.next()) {
            // Table exists
            return true;
        }
        else {
            // Table does not exist
            return false;
        }
    }

    @Override
    public void addCleanTasks(List<CleanTask> cleanTasks) throws SQLException
    {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("insert into " + CleanTaskEntry.TABLE + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        for (int i = 0; i < cleanTasks.size(); i++)
        {
            CleanTask cleanTask = cleanTasks.get(i);
            insertValuesToPs(cleanTask, ps);
            ps.addBatch();
        }

        ps.executeBatch();
        ps.close();
    }

    @Override
    public boolean checkIfCleanTaskIdExists(int id) throws SQLException
    {
        Statement statement = connection.createStatement();
        String query = "SELECT 1 FROM " + CleanTaskEntry.TABLE + " WHERE " + CleanTaskEntry._ID + " = " + id;
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void startTransaction() throws SQLException
    {
        connection.setAutoCommit(false);
    }

    @Override
    public void rollback()
    {
        try
        {
            connection.rollback();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public int addCleanTask(CleanTask cleanTask) throws SQLException
    {
        PreparedStatement ps = connection.prepareStatement("insert into " + CleanTaskEntry.TABLE + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        insertValuesToPs(cleanTask, ps);
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        int id = -1;
        if (generatedKeys.next()) {
            id = generatedKeys.getInt(1);
        }
        ps.close();
        return id;
    }

    private static void insertValuesToPs(CleanTask cleanTask, PreparedStatement ps) throws SQLException
    {
        int id = cleanTask.getId();
        if (id > -1)
        {
            ps.setInt(1, id);
        }

        ps.setString(2, cleanTask.getName());
        ps.setString(3, cleanTask.getDescription());
        ps.setString(4, cleanTask.getResponsible());
        ps.setInt(5, cleanTask.getDifficulty());
        ps.setInt(6, cleanTask.getDurationInMin());
        ps.setLong(7, cleanTask.getFirstDate().getMillis());
        Frequency frequency = cleanTask.getFrequency();
        ps.setString(8, frequency == null ? null : frequency.toString());
        ps.setInt(9, cleanTask.getFrequencyNumber());
        ps.setInt(10, cleanTask.isDeleted() ? 0 : 1);
        ps.setLong(11, cleanTask.getInsertDate().getMillis());
        ps.setString(12, cleanTask.getCreatedFrom().toString());
        ps.setLong(13, cleanTask.getLastModifiedDate().getMillis());
        ps.setString(14, cleanTask.getLastChangeFrom().toString());
    }

    public List<CleanTask> getAllCleanTasks() throws SQLException
    {
        List<CleanTask> cleanTasks = new ArrayList<>();
        String query = "SELECT * FROM " + CleanTaskEntry.TABLE;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while(rs.next()) {
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String description = rs.getString(3);
            String responsible = rs.getString(4);
            int difficulty = rs.getInt(5);
            int duration = rs.getInt(6);
            DateTime firstDate = new DateTime(rs.getLong(7));
            String frequency = rs.getString(8);
            int frequencyNumber = rs.getInt(9);
            boolean deleted = rs.getInt(10) != 0;
            DateTime insertDate = new DateTime(rs.getLong(11));
            String insertId = rs.getString(12);
            DateTime modifiedDate = new DateTime(rs.getLong(13));
            String modifiedId = rs.getString(14);
            cleanTasks.add(new CleanTask(name, description,responsible, difficulty, duration, firstDate, frequency == null ? null : Frequency.valueOf(frequency), frequencyNumber, id, insertDate, modifiedDate, deleted, insertId, modifiedId));
        }

        return cleanTasks;
    }

    public void commit() throws SQLException
    {
        connection.commit();
        connection.setAutoCommit(true);
    }
}
