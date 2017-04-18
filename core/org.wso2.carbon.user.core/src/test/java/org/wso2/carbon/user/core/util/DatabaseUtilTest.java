package org.wso2.carbon.user.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class DatabaseUtilTest {

    private static Log log = LogFactory.getLog(DatabaseUtil.class);

    @Mock DataSource datasource;
    @Mock Connection conn;
    @Mock PreparedStatement preparedStatement;
    @Mock Statement statement;
    @Mock ResultSet resultSetMock;
    @Mock RealmConfiguration realmConfig;
    @Mock PoolProperties poolProperties;
    @Mock CallableStatement callableStatementMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(datasource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(statement.getResultSet()).thenReturn(resultSetMock);
    }

    @Test
    public void getDBConnectionShouldNotBeNull() throws Exception {
        // not a true unit test.
        String url = "jdbc:h2:target/ReadOnlyTest/CARBON_TEST";
        String driverName = "org.h2.Driver";
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(url);
        poolProperties.setDriverClassName(driverName);
        DataSource fake = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);

        Assert.assertNotNull(DatabaseUtil.getDBConnection(fake));
    }

    @Test
    public void closeConnectionShouldHandleSQLRecoverableException() throws Exception {
        boolean exceptionNotHandled = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(conn).close();
            DatabaseUtil.closeConnection(conn);
        } catch (SQLRecoverableException e) {
            log.info("Unhandled SQLRecoverableException");
            exceptionNotHandled = true;
        } catch (SQLException e) {
            // this shouldn't happen
            Assert.fail();
        } finally {
            Assert.assertFalse(exceptionNotHandled);
        }
    }

    @Test
    public void closeWithPreparedStatementShouldHandleSQLRecoverableException() {

        boolean exceptionNotHandled = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(preparedStatement).close();
            DatabaseUtil.close(conn, preparedStatement);
        } catch (SQLRecoverableException e) {
            log.info("Unhandled SQLRecoverableException");
            exceptionNotHandled = true;
        } catch (SQLException e) {
            // this shouldn't happen
            Assert.fail();
        } finally {
            Assert.assertFalse(exceptionNotHandled);
        }
    }

    @Test
    public void closeWithResultSetShouldHandleSQLRecoverableException() {
        boolean exceptionNotHandled = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(resultSetMock).close();
            DatabaseUtil.close(conn, resultSetMock, preparedStatement);

        } catch (SQLRecoverableException e) {
            log.info("Unhandled SQLRecoverableException");
            exceptionNotHandled = true;
        } catch (SQLException e) {
            // this shouldn't happen
            Assert.fail();
        } finally {
            Assert.assertFalse(exceptionNotHandled);
        }
    }

    // regression test legacy code
    @Test
    public void legacyCloseAllConnectionWithPreparedStatementHandlesSQLRecoverableException() {

        boolean exceptionNotHandled = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(preparedStatement).close();
            DatabaseUtil.closeAllConnections(conn, preparedStatement);
        } catch (SQLRecoverableException e) {
            log.info("Unhandled SQLRecoverableException");
            exceptionNotHandled = true;
        } catch (SQLException e) {
            // this shouldn't happen
            Assert.fail();
        } finally {
            Assert.assertFalse(exceptionNotHandled);
        }
    }

    // regression test legacy code
    @Test
    public void legacyCloseAllConnectionWithResultSetHandlesSQLRecoverableException() {
        boolean exceptionNotHandled = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(resultSetMock).close();
            DatabaseUtil.closeAllConnections(conn, resultSetMock, preparedStatement);
        } catch (SQLRecoverableException e) {
        log.info("Unhandled SQLRecoverableException");
        exceptionNotHandled = true;
        } catch (SQLException e) {
        // this shouldn't happen
        Assert.fail();
        } finally {
            Assert.assertFalse(exceptionNotHandled);
        }
    }
    
    @Test
    public void getStringValuesFromDatabaseShouldReturnValues() throws Exception{

        ResultSetMocker resultSetMocker = new ResultSetMocker().invoke();
        String sqlStmt = resultSetMocker.getSqlStmt();
        List<Object> params = resultSetMocker.getParams();
        List<Object> resultSetAnswers = resultSetMocker.getResultSetAnswers();

        String[] results =   DatabaseUtil.getStringValuesFromDatabase(conn, sqlStmt, params.get(0), params.get(1), params.get(2));

        Assert.assertEquals( resultSetAnswers.get(0), results[0]);
        Assert.assertEquals(resultSetAnswers.get(1), results[1]);
        Assert.assertEquals(resultSetAnswers.get(2), results[2]);
    }


    @Test (expected = UserStoreException.class)
    public void getStringValuesFromDatabaseShouldHandleSQLRecoverableException() throws Exception{

        // cannot handle sqlrecoverableexception for resultsets
        ResultSetMocker resultSetMocker = new ResultSetMocker().invoke();
        String sqlStmt = resultSetMocker.getSqlStmt();
        List<Object> params = resultSetMocker.getParams();
        Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(resultSetMocker.get()).next();

        String[] results =   DatabaseUtil.getStringValuesFromDatabase(conn, sqlStmt, params.get(0), params.get(1), params.get(2));
        Assert.fail("Cannot handle SQLRecoverableError" );

    }

    public DataSource getDBConnection() throws Exception {

        // not a true unit test.
        String url = "jdbc:h2:target/ReadOnlyTest/CARBON_TEST";
        String driverName = "org.h2.Driver";
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(url);
        poolProperties.setDriverClassName(driverName);
        return new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
    }

    private class ResultSetMocker {

        private String sqlStmt;
        private List<Object> params;
        private List<Object> resultSetAnswers;

        public String getSqlStmt() {
            return sqlStmt;
        }

        public List<Object> getParams() {
            return params;
        }

        public List<Object> getResultSetAnswers() {
            return resultSetAnswers;
        }

        public ResultSet get(){
            return resultSetMock;
        }

        public ResultSetMocker invoke() throws SQLException {

            sqlStmt = "SELECT  * FROM people WHHERE firstname=? AND lastname=? AND id=?";

            params = new ArrayList<>();
            params.add("Jack");
            params.add("Smith");
            params.add(100);

            resultSetAnswers = new ArrayList<>();
            resultSetAnswers.add("Value1");
            resultSetAnswers.add("Value2");
            resultSetAnswers.add("100");

            when(conn.prepareStatement(sqlStmt)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSetMock);
            when(resultSetMock.getString(1)).thenReturn( (String) resultSetAnswers.get(0), (String) resultSetAnswers.get(1), (String) resultSetAnswers.get(2));

            Mockito.when(resultSetMock.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
            Mockito.doReturn(resultSetMock).when(callableStatementMock).getResultSet();
            return this;
        }
    }
}
