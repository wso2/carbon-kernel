package org.wso2.carbon.user.core.util;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.wso2.carbon.user.api.RealmConfiguration;

import javax.sql.DataSource;
import java.sql.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class DatabaseUtilTest {


    @Mock DataSource datasource;
    @Mock Connection conn;
    @Mock PreparedStatement preparedStatement;
    @Mock Statement statement;
    @Mock ResultSet results;
    @Mock RealmConfiguration realmConfig;
    @Mock PoolProperties poolProperties;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(datasource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(statement.getResultSet()).thenReturn(results);
    }

    @Test
    public void getDBConnection() throws Exception {
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
    public void testCloseConnection() throws SQLException {
        Mockito.doAnswer(RETURNS_MOCKS).when(datasource).getConnection();
        DatabaseUtil.closeConnection(conn);
        assertNotNull(conn);
    }

    @Test
    public void testCloseConnectionHandlesSQLRecoverableException() throws Exception {
        try {
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(conn).close();
            DatabaseUtil.closeConnection(conn);
        } catch (SQLRecoverableException e) {
            Assert.assertNull(conn);
            Assert.assertEquals(SQLRecoverableException.class, e.getClass());
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
            e.printStackTrace();
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
            Mockito.doThrow(new SQLRecoverableException("throw SQLRecoverableException")).when(results).close();
            DatabaseUtil.closeAllConnections(conn, results, preparedStatement);

        } catch (SQLException e) {
            e.printStackTrace();
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

}
