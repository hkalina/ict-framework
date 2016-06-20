package maf.isenframework.isenframeworkapplication;

import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcDatabaseConnection;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.BaseConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseConnectionProxyFactory;

import SQLite.JDBCDataSource;
import SQLite.JDBCDriver;

/**
 * Implementation of the ConnectionSource interface that supports what is needed
 * by ORMLite. This is not thread-safe nor synchronized. For other dataSources,
 * see the {@link DataSourceConnectionSource} class.
 *
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java,
 * {@link #initialize} should be called after all of the set methods. In Spring
 * XML, init-method="initialize" should be used.
 * </p>
 *
 * @author Serge Leontiev/Oracle AEG
 */
public class MafSQLiteConnectionSource extends BaseConnectionSource implements ConnectionSource {

    private static Logger logger = LoggerFactory.getLogger(MafSQLiteConnectionSource.class);

    private JDBCDataSource dataSource;
    private String password;
    private String url;
    protected boolean initialized = false;
    private DatabaseConnection connection;
    protected DatabaseType databaseType;
    private static DatabaseConnectionProxyFactory connectionProxyFactory;

    /**
     * The main constructor for the MAFSQLiteConnectionSource class
     *
     * @param url
     *            JDBC connection URL
     *
     * @throws SQLException
     */
    public MafSQLiteConnectionSource(String url) throws SQLException {
        this(url, null);
    }

    public MafSQLiteConnectionSource(String url, String password) throws SQLException {
        dataSource = new JDBCDataSource(url);
        this.url = url;
        this.password = password;
        initialize();
    }

    /**
     * Initialize the class after the setters have been called. If you are using
     * the no-arg constructor and Spring type wiring, this should be called
     * after all of the set methods.
     *
     * @throws SQLException
     *             If the driver associated with the database URL is not found
     *             in the classpath.
     */
    public void initialize() throws SQLException {
        if (initialized) {
            return;
        }
        if (url == null) {
            throw new SQLException("url was never set on " + getClass().getSimpleName());
        }
        databaseType = new SqliteDatabaseType();
        databaseType.setDriver(new JDBCDriver());
        initialized = true;
    }

    @Override
    public DatabaseConnection getReadOnlyConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException(getClass().getSimpleName() + " was not initialized properly");
        }
        return getReadWriteConnection();
    }

    @Override
    public DatabaseConnection getReadWriteConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException(getClass().getSimpleName() + " was not initialized properly");
        }
        if (connection != null) {
            if (connection.isClosed()) {
                throw new SQLException("Connection has already been closed");
            } else {
                return connection;
            }
        }
        connection = makeConnection(logger);
        return connection;
    }

    @Override
    public void releaseConnection(DatabaseConnection databaseConnection) throws SQLException {
        if (!initialized) {
            throw new SQLException(getClass().getSimpleName() + " was not initialized properly");
        }
        // noop right now
    }

    @SuppressWarnings("unused")
    @Override
    public boolean saveSpecialConnection(DatabaseConnection databaseConnection) throws SQLException {
        // TODO Implement this method
        return true;
    }

    @Override
    public void clearSpecialConnection(DatabaseConnection databaseConnection) {
        // TODO Implement this method
    }

    @Override
    public void close() throws SQLException {
        if (!initialized) {
            throw new SQLException(getClass().getSimpleName() + " was not initialized properly");
        }
        if (connection != null) {
            connection.close();
            logger.debug("closed connection #{}", connection.hashCode());
            connection = null;
        }
    }

    @Override
    public void closeQuietly() {
        try {
            close();
        } catch (SQLException e) {
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        if (!initialized) {
            throw new IllegalStateException(getClass().getSimpleName() + " was not initialized properly");
        }
        return databaseType;
    }

    @Override
    public boolean isOpen() {
        return connection != null;
    }

    /**
     * Make a connection to the database.
     *
     * @param logger
     *            This is here so we can use the right logger associated with
     *            the sub-class.
     */
    @SuppressWarnings("resource")
    protected DatabaseConnection makeConnection(Logger logger) throws SQLException {
        DatabaseConnection connection;
        if (password != null) {
            logger.debug("encrypted connection");
            connection = new JdbcDatabaseConnection(dataSource.getConnection(null, password));
        } else {
            connection = new JdbcDatabaseConnection(dataSource.getConnection());
        }
        // by default auto-commit is set to true
        connection.setAutoCommit(true);
        if (connectionProxyFactory != null) {
            connection = connectionProxyFactory.createProxy(connection);
        }
        logger.debug("opened connection to {} got #{}", url, connection.hashCode());
        return connection;
    }

}