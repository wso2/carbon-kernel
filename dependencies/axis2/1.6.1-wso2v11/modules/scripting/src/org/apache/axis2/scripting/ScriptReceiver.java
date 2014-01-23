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

package org.apache.axis2.scripting;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.scripting.convertors.ConvertorFactory;
import org.apache.axis2.scripting.convertors.OMElementConvertor;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

/**
 * An Axis2 MessageReceiver for invoking script language functions.
 * 
 * The scripting support uses the Apache Bean Scripting Framework (BSF) 
 * so the script may be written in any language supported by BSF. 
 * 
 * There are two ways of defining the script, either in a seperate file
 * or embedded in-line within the services.xml file.
 * 
 * This example shows a services.xml using a seperate script file:
 * <code>
 *   <serviceGroup>
 *     <service ...>
 *       ...
 *       <parameter name="script">scripts/myScript.js</parameter>
 *     </service>
 *   </serviceGroup>
 * </code>
 * 
 * This example shows a JavaScript function embedded within a services.xml file:
 * <code>
 *   <serviceGroup>
 *     <service ...>
 *       ...
 *       <parameter name="script.js"><![CDATA[
 *          function invoke(inMC, outMC) {
 *             ...
 *          }
 *        ]]></parameter>
 *     </service>
 *   </serviceGroup>
 * </code>
 * 
 * The script language is determined by the file name suffix when using scripts
 * in seperate files or the script parameter name suffix when using inline scripts.
 */
public class ScriptReceiver extends AbstractInOutSyncMessageReceiver {

    public static final String SCRIPT_ATTR = "script";
    public static final String FUNCTION_ATTR = "function";
    public static final String DEFAULT_FUNCTION = "invoke";
    public static final String CONVERTOR_ATTR = "convertor";

    protected static final String BSFENGINE_PROP = ScriptReceiver.class.getName() + "BSFEngine";
    protected static final String CONVERTOR_PROP = ScriptReceiver.class.getName() + "OMElementConvertor";
    public static final String SCRIPT_SRC_PROP = ScriptReceiver.class.getName() + "ScriptSrc";

    private static final Log log = LogFactory.getLog(ScriptModule.class);

    public ScriptReceiver() {
    }

    /**
     * Invokes the service by calling the script function
     */
    public void invokeBusinessLogic(MessageContext inMC, MessageContext outMC) throws AxisFault {
        try {

            log.debug("invoking script service");

            outMC.setEnvelope(getSOAPFactory(inMC).getDefaultEnvelope());

            BSFEngine engine = getBSFEngine(inMC);
            OMElementConvertor convertor = (OMElementConvertor) inMC.getServiceContext().getProperty(CONVERTOR_PROP);

            Parameter scriptFunctionParam = inMC.getAxisService().getParameter(FUNCTION_ATTR);
            String scriptFunction = scriptFunctionParam == null ? DEFAULT_FUNCTION : (String) scriptFunctionParam.getValue();

            ScriptMessageContext inScriptMC = new ScriptMessageContext(inMC, convertor);
            ScriptMessageContext outScriptMC = new ScriptMessageContext(outMC, convertor);
            Object[] args = new Object[] { inScriptMC, outScriptMC };

            engine.call(null, scriptFunction, args);

        } catch (BSFException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Gets the BSFEngine for the script service.
     * 
     * The first service invocation creates the BSFEngine and caches
     * it in the Axis2 ServiceContext for reuse by subsequent requests.
     */
    protected BSFEngine getBSFEngine(MessageContext mc) throws AxisFault {
        BSFEngine bsfEngine;
        ServiceContext serviceContext = mc.getServiceContext();
        synchronized (serviceContext) {
            bsfEngine = (BSFEngine) serviceContext.getProperty(BSFENGINE_PROP);
            if (bsfEngine == null) {
                bsfEngine = initScript(mc);
            }
        }
        return bsfEngine;
    }

    /**
     * Initializes the script service by finding the script source code,
     * compiling it in a BSFEngine, and creating an OMElementConvertor
     * for the script.
     */
    protected BSFEngine initScript(MessageContext mc) throws AxisFault {
        log.debug("initializing script service");

        AxisService axisService = mc.getAxisService();

        String scriptName = null;
        String scriptSrc = null;
        Parameter scriptFileParam = axisService.getParameter(SCRIPT_ATTR);
        if (scriptFileParam != null) {
            // the script is defined in a seperate file
            scriptName = ((String) scriptFileParam.getValue()).trim();
            Parameter scriptSrcParam = axisService.getParameter(SCRIPT_SRC_PROP);
            if (scriptSrcParam != null) {
                scriptSrc = (String) scriptSrcParam.getValue();
            } else {
                scriptSrc = readScript(axisService.getClassLoader(), scriptName);
            }
        } else {
            // the script is defined inline within the services.xml
            ArrayList parameters = axisService.getParameters();
            for (int i=0; scriptFileParam == null && i<parameters.size(); i++) {
                Parameter p = (Parameter) parameters.get(i);
                if (p.getName().startsWith("script.")) {
                    scriptFileParam = p;
                    scriptName = p.getName();
                    scriptSrc = (String) p.getValue();
                }
            }
        }
        if (scriptName == null) {
            throw new AxisFault("Missing script parameter");
        }
        
        try {

            String scriptLanguage = BSFManager.getLangFromFilename(scriptName);
            BSFManager bsfManager = new BSFManager();
            bsfManager.setClassLoader(BSFManager.class.getClassLoader());
            bsfManager.declareBean("_AxisService", axisService, AxisService.class);

            BSFEngine bsfEngine = bsfManager.loadScriptingEngine(scriptLanguage);
            bsfEngine.exec(scriptName, 0, 0, scriptSrc);

            ServiceContext serviceContext = mc.getServiceContext();
            serviceContext.setProperty(BSFENGINE_PROP, bsfEngine);

            OMElementConvertor convertor = ConvertorFactory.createOMElementConvertor(axisService, scriptName);
            serviceContext.setProperty(CONVERTOR_PROP, convertor);

            return bsfEngine;

        } catch (BSFException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Reads the complete script source code into a String
     */
    protected String readScript(ClassLoader cl, String scriptName) throws AxisFault {
        URL url = cl.getResource(scriptName);
        if (url == null) {
            throw new AxisFault("Script not found: " + scriptName);
        }
        InputStream is;
        try {
            is = url.openStream();
        } catch (IOException e) {
            throw new AxisFault("IOException opening script: " + scriptName, e);
        }
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1024];
            StringBuffer source = new StringBuffer();
            int count;
            while ((count = reader.read(buffer)) > 0) {
                source.append(buffer, 0, count);
            }
            return source.toString();
        } catch (IOException e) {
            throw new AxisFault("IOException reading script: " + scriptName, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new AxisFault("IOException closing script: " + scriptName, e);
            }
        }
    }
}
