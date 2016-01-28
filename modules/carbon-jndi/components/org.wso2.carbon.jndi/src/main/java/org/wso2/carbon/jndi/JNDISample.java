/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public class JNDISample {

//    public static void main(String[] args) {
////        initNaming();
//
//        try {
//            Hashtable hashtableEnvironment = new Hashtable();
//
//            hashtableEnvironment.put(
//                    Context.INITIAL_CONTEXT_FACTORY,
//                    "org.apache.naming.java.javaURLContextFactory"
//            );
//
//            // Create the initial context
////            Context ctx = new InitialContext(hashtableEnvironment);
//            Context ctx = new InitialContext();
//
//            ctx.bind("sameera", "test");
//
//            // Look up an object
////            Object obj = ctx.lookup("sameera");
//
//            Context envCtx = (Context) ctx.lookup("java:comp/env/");
//
//            DataSource dataSource = (DataSource) envCtx.lookup("jdbc/USERDB");
////            String value = (String) ctx.lookup("sameera");
//
//            // Print it
////            System.out.println("sameera" + " is bound to: " + value);
//
//        } catch (NamingException e) {
//            System.err.println("Problem looking up " + "sameera" + ": " + e);
//        }
//    }


    protected static void initNaming() {
        // Setting additional variables
//        System.setProperty("catalina.useNaming", "true");
//        String value = "org.apache.naming";
//        String oldValue =
//                System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
//        if (oldValue != null) {
//            value = value + ":" + oldValue;
//        }
//        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
//        if (log.isDebugEnabled()) {
//            log.debug("Setting naming prefix=" + value);
//        }
//        value = System.getProperty
//                (javax.naming.Context.INITIAL_CONTEXT_FACTORY);
//        if (value == null) {
        System.setProperty
                (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                        "org.apache.naming.java.javaURLContextFactory");
//        } else {
//            log.debug("INITIAL_CONTEXT_FACTORY already set " + value);
//        }

    }
}
