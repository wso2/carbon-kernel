package org.wso2.carbon.datasource;

import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.core.DataSourceManager;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class HikariCPRunner {
    //HikariCP sample
//    public static void main(String[] args) throws SQLException {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl("jdbc:mysql://localhost:3306/test");
//        config.setUsername("root");
//        config.setPassword("root");
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//
//        HikariDataSource ds = new HikariDataSource(config);
//        Connection con = ds.getConnection();
//        Statement st = con.createStatement();
//        ResultSet rs = st.executeQuery("select * from pet");
//        int cnt = 1;
//        while (rs.next()) {
//            System.out.println((cnt++)+". Host:" +rs.getString("name")+
//                    " User:"+rs.getString("owner")+" Password:"+rs.getString("birth"));
////                System.out.println((cnt++)+". Host:" +rs.getString("Host")+
////                        " User:"+rs.getString("User")+" Password:"+rs.getString("Password"));
//        }
//        rs.close();
//        st.close();
//    }


    public static void main(String[] args) {
        DataSourceManager manager = DataSourceManager.getInstance();

        /*
            Since this is a POC, the configuration directory needs to be set. However poc will collect data source files
            as per the file naming convention used in earlier versions of carbon.
            i.e: file with the name master-datasources.xml is taken as the configuration for master data and any file
                        that it's name ends with "-datasources.xml" will be processed.
          */
        manager.setConfigDir(Paths.get("src", "main", "resources").toString());
        try {
            manager.initSystemDataSources();
            Hashtable<String, String> map = new Hashtable<>();
            map.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
            map.put(Context.PROVIDER_URL, "file:///home/dinushab/Carbon/POC/data-sources/carbon-kernel/modules/carbon-datasources/org.wso2.carbon.datasource.core/src/test");
            InitialContext context = new InitialContext(map);
            DataSource dataSource = (DataSource)context.lookup("jdbc/WSO2CarbonDB/test");

            Connection con = null;
            try {
                con = dataSource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from pet");
                int cnt = 1;
                while (rs.next()) {
                    System.out.println((cnt++)+". Host:" +rs.getString("name")+
                            " User:"+rs.getString("owner")+" Password:"+rs.getString("birth"));
                }
                rs.close();
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (con!=null) try {con.close();}catch (Exception ignore) {}
            }

        } catch (DataSourceException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
