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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

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
    public void closeConnectionShouldNotBeNull() throws SQLException {

        Mockito.doAnswer(RETURNS_MOCKS).when(datasource).getConnection();
        DatabaseUtil.closeConnection(conn);
        assertNotNull(conn);
    }

    @Test
    public void closeConnectionShouldHandlesSQLRecoverableException() throws Exception {

            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(conn).close();
            DatabaseUtil.closeConnection(conn);
            System.out.println("Expected error thrown in sqlerecoverable handler");
            Assert.assertNotNull(conn);
    }

    @Test
    public void newCloseStatementsShouldBBeSameResult() throws SQLException {
        when(conn.isClosed()).thenReturn(false);
        DatabaseUtil.close(conn, preparedStatement);
    }

    @Test
    public void newClosePreparedStatementShouldHandlesSQLRecoverableException() {

        boolean isThrown = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(preparedStatement).close();
            DatabaseUtil.close(conn, preparedStatement);
        } catch (SQLRecoverableException e) {
            isThrown = true;
            Assert.assertTrue(isThrown);
        } catch (SQLException e) {
            // this shouldn't happen
            Assert.assertTrue(false);
        }
        Assert.assertNotNull(preparedStatement);
    }

    @Test
    public void testNewCloseResultSetHandlesSQLRecoverableException() {

        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(resultSetMock).close();
            DatabaseUtil.close(conn, resultSetMock, preparedStatement);

        } catch (SQLException e) {
            log.error("Unexpected error thrown in sqlerecoverable handler" +  e.getMessage(), e);
        }
    }
    @Test
    public void testCloseStatements() throws SQLException {

        when(conn.isClosed()).thenReturn(false);
        DatabaseUtil.closeAllConnections(conn, preparedStatement);
    }

    @Test
    public void testClosePreparedStatementHandlesSQLRecoverableException() {

        boolean isThrown = false;
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(preparedStatement).close();
            DatabaseUtil.closeAllConnections(conn, preparedStatement);
        } catch (SQLRecoverableException e) {
            // this is expected
            isThrown = true;
           Assert.assertTrue(isThrown);

        } catch (SQLException e) {
            e.printStackTrace();
            // this shouldn't happen
            Assert.assertTrue(false);
        }
       Assert.assertNotNull(preparedStatement);
    }

    @Test
    public void testCloseResultSetHandlesSQLRecoverableException() {

        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(resultSetMock).close();
            DatabaseUtil.closeAllConnections(conn, resultSetMock, preparedStatement);

        } catch (SQLException e) {
            log.error("Unexpected error thrown in sqlerecoverable handler" +  e.getMessage(), e);
        }
    }

    @Test
    public void testCreateUserStoreDataSource() throws Exception {

        when(realmConfig.isLogAbandoned()).thenReturn(true);
        when(realmConfig.isRemoveAbandoned()).thenReturn(true);
        when(realmConfig.getRemoveAbandonedTimeout()).thenReturn(100);
        DatabaseUtil.createUserStoreDataSource(realmConfig);

        Assert.assertTrue(realmConfig.isLogAbandoned());
        Assert.assertTrue(realmConfig.isRemoveAbandoned());
        Assert.assertEquals(100, realmConfig.getRemoveAbandonedTimeout());
        Assert.assertNotNull(datasource.getConnection());
    }
    @Test
    public void testNullCreateUserStoreDataSource() throws Exception {

        DatabaseUtil.createUserStoreDataSource(realmConfig);

        Assert.assertFalse(realmConfig.isLogAbandoned());
        Assert.assertFalse(realmConfig.isRemoveAbandoned());
        Assert.assertEquals(0, realmConfig.getRemoveAbandonedTimeout());

        Assert.assertEquals(realmConfig.isLogAbandoned(), poolProperties.isLogAbandoned());
        Assert.assertEquals(realmConfig.isRemoveAbandoned(), poolProperties.isRemoveAbandoned());
        Assert.assertEquals(realmConfig.getRemoveAbandonedTimeout(), poolProperties.getRemoveAbandonedTimeout());
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

        // cannot handle sqlerecoverable for resultsets
        ResultSetMocker resultSetMocker = new ResultSetMocker().invoke();
        String sqlStmt = resultSetMocker.getSqlStmt();
        List<Object> params = resultSetMocker.getParams();
        Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(resultSetMocker.get()).next();

        String[] results =   DatabaseUtil.getStringValuesFromDatabase(conn, sqlStmt, params.get(0), params.get(1), params.get(2));
        Assert.fail("Can not handle SQLRecoverableError" );
    }

    @Test
    public void getCurrentRuntime(){

        Assert.assertTrue(System.getProperties().getProperty("java.version").contains("1.7"));
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
