/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.datasource;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.wso2.carbon.datasource.core.common.DataSourceException;
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

public class Runner {

    public static void main(String[] args) {
        DataSourceManager manager = DataSourceManager.getInstance();

        /*
            Since this is a POC, the configuration directory needs to be set. However poc will collect data source files
            as per the file naming convention used in earlier versions of carbon.
            i.e: file with the name master-datasources.xml is taken as the configuration for master data and any file
                        that it's name ends with "-datasources.xml" will be processed.
          */
        manager.setConfigDir(Paths.get("component", "src", "main", "resources").toString());
        try {
            manager.initSystemDataSources();
            Hashtable<String, String> map = new Hashtable<>();
            map.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
            map.put(Context.PROVIDER_URL, "file:///home/dinushab/Carbon/POC/data-sources/carbon-kernel/modules/" +
                    "carbon-datasources/component/src/test");
            InitialContext context = new InitialContext(map);

            DataSource dataSource = (DataSource) context.lookup("jdbc/WSO2CarbonDB/test");
            Connection con = null;
            try {
                con = dataSource.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from pet");

                int cnt = 1;
                while (rs.next()) {
                    System.out.println((cnt++) + ". First name: " + rs.getString("name") +
                            ", Last name: " + rs.getString("owner") + ", DOB:" + rs.getString("birth"));
                }
                rs.close();
                st.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (con != null) try {
                    con.close();
                } catch (Exception ignore) {
                }
            }


        } catch (DataSourceException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


}
