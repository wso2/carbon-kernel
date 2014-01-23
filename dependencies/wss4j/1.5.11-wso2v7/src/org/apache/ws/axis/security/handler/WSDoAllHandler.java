/**
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
package org.apache.ws.axis.security.handler;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.utils.LockableHashtable;
import org.apache.commons.logging.Log;
import org.apache.ws.security.handler.WSHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * <code>WSDoAllHandler</code> is a utility class which implements simple
 * property setting/getting behavior, and stubs out a lot of the Handler
 * methods.  Extend this class to make writing your Handlers easier, and
 * then override what you need to.
 */
public abstract class WSDoAllHandler extends WSHandler implements Handler {
    private static Log log =
            LogFactory.getLog(WSDoAllHandler.class.getName());

    protected boolean makeLockable = false;
    protected Hashtable options;
    protected String name;

    /**
     * Should this Handler use a LockableHashtable for options?
     * Default is 'false'.
     */
    protected void setOptionsLockable(boolean makeLockable) {
        this.makeLockable = makeLockable;
    }

    protected void initHashtable() {
        if (makeLockable) {
            options = new LockableHashtable();
        } else {
            options = new Hashtable();
        }
    }

    /**
     * Stubbed-out methods.  Override in your child class to implement
     * any real behavior.  Note that there is NOT a stub for invoke(), since
     * we require any Handler derivative to implement that.
     */
    public void init() {
    }

    public void cleanup() {
    }

    public boolean canHandleBlock(QName qname) {
        return false;
    }

    public void onFault(MessageContext msgContext) {
    }

    /**
     * Set the given option (name/value) in this handler's bag of options
     */
    public void setOption(String name, Object value) {
        if (options == null) initHashtable();
        options.put(name, value);
    }

    /**
     * Set a default value for the given option:
     * if the option is not already set, then set it.
     * if the option is already set, then do not set it.
     * <p/>
     * If this is called multiple times, the first with a non-null value
     * if 'value' will set the default, remaining calls will be ignored.
     * <p/>
     * Returns true if value set (by this call), otherwise false;
     */
    public boolean setOptionDefault(String name, Object value) {
        boolean val = (options == null || options.get(name) == null) && value != null;
        if (val) {
            setOption(name, value);
        }
        return val;
    }

    /**
     * Returns the option corresponding to <code>name</code>.
     *
     * @param name the non-null name of the option.
     * @return the option on <code>name</code> if <code>name</code>
     *  exists; otherwise null.
     */
    public Object getOption(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (options == null) {
            return null;
        }
        return options.get(name);
    }

    /**
     * Return the entire list of options
     */
    public Hashtable getOptions() {
        return (options);
    }

    public void setOptions(Hashtable opts) {
        options = opts;
    }

    /**
     * Set the name (i.e. registry key) of this Handler
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the name (i.e. registry key) for this Handler
     */
    public String getName() {
        return name;
    }

    public Element getDeploymentData(Document doc) {
        log.debug("Enter: BasicHandler::getDeploymentData");

        Element root = doc.createElementNS("", "handler");

        root.setAttribute("class", this.getClass().getName());
        options = this.getOptions();
        if (options != null) {
            Enumeration e = options.keys();
            while (e.hasMoreElements()) {
                String k = (String) e.nextElement();
                Object v = options.get(k);
                Element e1 = doc.createElementNS("", "option");
                e1.setAttribute("name", k);
                e1.setAttribute("value", v.toString());
                root.appendChild(e1);
            }
        }
        log.debug("Exit: WSDoAllHandler::getDeploymentData");
        return (root);
    }

    public void generateWSDL(MessageContext msgContext) throws AxisFault {
    }

    /**
     * Return a list of QNames which this Handler understands.  By returning
     * a particular QName here, we are committing to fulfilling any contracts
     * defined in the specification of the SOAP header with that QName.
     */
    public List getUnderstoodHeaders() {
        return null;
    }

    public Object getProperty(Object msgContext, String key) {
        return ((MessageContext)msgContext).getProperty(key);
    }
    
    public void setProperty(Object msgContext, String key, Object value) {
        ((MessageContext)msgContext).setProperty(key, value);
    }

    public String getPassword(Object msgContext) {
        return ((MessageContext)msgContext).getPassword();
    }

    public void setPassword(Object msgContext, String password) {
        ((MessageContext)msgContext).setPassword(password);
    }
}
