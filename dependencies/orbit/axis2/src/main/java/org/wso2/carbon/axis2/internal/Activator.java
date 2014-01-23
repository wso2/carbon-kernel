/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.axis2.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.neethi.AssertionBuilderFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.xml.namespace.QName;

/*
* 
*/
public class Activator implements BundleActivator {

    private static Log log = LogFactory.getLog(Activator.class);

    public void start(BundleContext context) throws Exception {

        //Alternative to service provider
        String[] builders = new String[]{"org.apache.axis2.policy.builders.MTOM10AssertionBuilder"};
        try {
            for (String buildeName : builders) {
                Class aClass = context.getBundle().loadClass(buildeName.trim());
                AssertionBuilder builder = (AssertionBuilder) aClass.newInstance();
                QName[] knownElements = builder.getKnownElements();
                for (QName knownElement : knownElements) {
                    AssertionBuilderFactory.registerBuilder(knownElement, builder);
                }
            }
        } catch (ClassNotFoundException e) {
            String msg = "Required builder is not found.";
            log.error(msg, e);
            throw new Exception(e);
        } catch (InstantiationException e) {
            String msg = "Builder cannot be intiated.";
            log.error(msg, e);
            throw new Exception(e);
        } catch (IllegalAccessException e) {
            String msg = "Builder cannot be accessed";
            log.error(msg, e);
            throw new Exception(e);
        }
    }

    public void stop(BundleContext context) throws Exception {
       //TODO, is it necessary to remove the added builder?
    }
}
