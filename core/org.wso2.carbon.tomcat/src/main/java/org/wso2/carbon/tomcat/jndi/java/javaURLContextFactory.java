package org.wso2.carbon.tomcat.jndi.java;
/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import org.apache.naming.ContextBindings;
import org.apache.naming.NamingContext;
import org.wso2.carbon.tomcat.jndi.CarbonSelectorContext;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Hashtable;

public class javaURLContextFactory extends org.apache.naming.java.javaURLContextFactory {

    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable)
            throws NamingException {
        if (ContextBindings.isClassLoaderBound()) {
            return new CarbonSelectorContext((Hashtable<String, Object>) hashtable, false, context);
        }
        return null;
    }

    public Context getInitialContext(Hashtable hashtable) throws NamingException {

        // We check wether the intiCtx request is coming from webapps
        if (ContextBindings.isClassLoaderBound()) {
            return new CarbonSelectorContext(hashtable, true, initialContext);
        }

        if (initialContext == null) {
            initialContext = new NamingContext(hashtable, MAIN);
        }
        return initialContext;
    }
}
