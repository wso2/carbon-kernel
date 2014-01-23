/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WSDLBasedPolicyProcessor {
    private HashMap ns2modules = new HashMap();

    public WSDLBasedPolicyProcessor(ConfigurationContext configctx) {
        AxisConfiguration axisConfiguration = configctx.getAxisConfiguration();
        for (Iterator iterator = axisConfiguration.getModules().values()
                .iterator(); iterator.hasNext();) {
            AxisModule axisModule = (AxisModule) iterator.next();
            String[] namespaces = axisModule.getSupportedPolicyNamespaces();

            if (namespaces == null) {
                continue;
            }

            for (int i = 0; i < namespaces.length; i++) {
                ArrayList moduleList = null;
                Object obj = ns2modules.get(namespaces[i]);
                if (obj == null) {
                    moduleList = new ArrayList(5);
                    ns2modules.put(namespaces[i], moduleList);
                } else {
                    moduleList = (ArrayList) obj;
                }
                moduleList.add(axisModule);

            }
        }

    }

    public void configureServicePolices(AxisService axisService)
            throws AxisFault {
        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOp = (AxisOperation) operations.next();
            // TODO we support only operation level Policy now
            configureOperationPolices(axisOp);
        }
    }

    public void configureOperationPolices(AxisOperation op) throws AxisFault {
        PolicyInclude policyInclude = op.getPolicyInclude();
        Policy policy = policyInclude.getEffectivePolicy();
        if (policy != null) {

            policy = (Policy) policy.normalize(policyInclude
                    .getPolicyRegistry(), false);

            for (Iterator iterator = policy.getAlternatives(); iterator
                    .hasNext();) {

                List namespaceList = new ArrayList();
                Assertion assertion;

                /*
                 * Fist we compute the set of distinct namespaces of assertions
                 * of this particular policy alternative.
                 */
                for (Iterator assertions = ((List) iterator.next()).iterator(); assertions
                        .hasNext();) {

                    assertion = (Assertion) iterator.next();
                    QName name = assertion.getName();
                    String namespaceURI = name.getNamespaceURI();

                    if (!namespaceList.contains(namespaceURI)) {
                        namespaceList.add(namespaceURI);
                    }
                }

                /*
                 * Now we compute all the modules that are are involved in
                 * process assertions that belongs to any of the namespaces of
                 * list.
                 */
                List modulesToEngage;

                for (Iterator namespaces = namespaceList.iterator(); iterator
                        .hasNext();) {
                    String namespace = (String) namespaces.next();
                    modulesToEngage = (List) ns2modules.get(namespace);

                    if (modulesToEngage == null) {
                        /*
                         * If there isn't a single module that is not interested
                         * of assertions that belongs to a particular namespace,
                         * we simply ignore it.
                         */
                        System.err
                                .println("cannot find any modules to process "
                                        + namespace + "type assertions");
                        // TODO: Log this ..
                        continue;

                    } else {
                        engageModulesToAxisDescription(modulesToEngage, op);
                    }
                }

                /*
                * We only pick the first policy alternative. Other policy
                * alternatives are ignored.
                */
                break;
            }
        }
    }

    /**
     * Engages the list of Modules to the specified AxisDescription.
     */
    private void engageModulesToAxisDescription(List modulesToEngage,
                                                AxisDescription axisDescription) throws AxisFault {
        AxisModule axisModule;
        String moduleName;

        for (Iterator iterator = modulesToEngage.iterator(); iterator.hasNext();) {
            axisModule = (AxisModule) iterator.next();
            moduleName = axisModule.getName();

            if (!axisDescription.isEngaged(moduleName)) {
                axisDescription.engageModule(axisModule);
                (axisModule.getModule()).engageNotify(axisDescription);
            }
        }
    }
}
