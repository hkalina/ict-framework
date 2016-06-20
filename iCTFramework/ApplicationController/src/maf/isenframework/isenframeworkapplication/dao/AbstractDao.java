package maf.isenframework.isenframeworkapplication.dao;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import maf.isenframework.isenframeworkapplication.MafSQLiteConnectionSource;
import maf.isenframework.isenframeworkapplication.data.Entity;
import maf.isenframework.isenframeworkapplication.data.User;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.Utility;

/**
 * Abstract superclass of all data access object (DAO) classes.
 */
public abstract class AbstractDao<T extends Entity> {

    protected Class<T> type;
    protected static ConnectionSource connectionSource = null;
    protected Dao<T, String> dao;

    protected AbstractDao(Class<T> type) {
        this.type = type;
        try {
            if (connectionSource == null) {
                String dir = AdfmfJavaUtilities.getDirectoryPathRoot(AdfmfJavaUtilities.ApplicationDirectory);
                String jdbc = "jdbc:sqlite:" + dir + "/framework.db";
                connectionSource = new MafSQLiteConnectionSource(jdbc, "newPassword");
            }
            dao = DaoManager.createDao(connectionSource, type);
        } catch (SQLException e) {
            e.printStackTrace();
            Utility.ApplicationLogger.severe(e.getMessage());
            throw new RuntimeException(e.getMessage() + e.getCause().getMessage(), e);
        }
    }

    public boolean tableExists() throws SQLException {
        return dao.isTableExists();
    }

    public void createTable() throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, type);
    }

    public List<T> getAll() throws SQLException {
        return dao.queryForAll();
    }
    
    public List<T> getById(int id) throws SQLException {
        QueryBuilder<T, String> builder = dao.queryBuilder();
        builder.where().eq("id", id);
        PreparedQuery<T> query = builder.prepare();
        return dao.query(query);
    }
    
    public List<T> getByIds(List<Integer> ids) throws SQLException {
        QueryBuilder<T, String> builder = dao.queryBuilder();
        builder.where().in("id", ids);
        PreparedQuery<T> query = builder.prepare();
        return dao.query(query);
    }

    protected int nextId() throws SQLException {
        Utility.ApplicationLogger.info("Generating new ID...");

        QueryBuilder<T, String> builder = dao.queryBuilder();
        builder.orderBy("id", false); // every entity has to have "id" !
        builder.limit(new Long(1));
        PreparedQuery<T> query = builder.prepare();
        T max = dao.queryForFirst(query);

        int nextId = max != null ? max.getId() + 1 : 1;

        Utility.ApplicationLogger.info("Generated ID: "+ nextId);
        return nextId;
    }

    public void add(T item) throws SQLException {
        if(item.getId() == 0){
            item.setId(nextId()); // generate id
        }
        dao.createIfNotExists(item);
    }

    public void update(T item) throws SQLException {
        dao.update(item);
    }
    
    public void delete(T item) throws SQLException {
        dao.delete(item);
    }
}
