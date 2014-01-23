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

package org.apache.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The OnDemandLogger will defer the creation of the
 * actual Log object until it is needed.  This may be necessary
 * to ensure that the Log implementation is consistent with the Log
 * interface in the current class loader.
 */
public class OnDemandLogger  {

    private final Class c;
    private Log _log = null;
    
    /**
     * Create an on-demand logger for the given class.
     * @param c
     */
    public OnDemandLogger(Class c){
        this.c = c;
    }
    
    
    /** Get or create Log on demand
     * @return
     */
    private Log getLog(){
        if(c==null){
            throw new RuntimeException("Unable to load Logging, Logging class not found");
        }
        if (_log == null) {
            _log = LogFactory.getLog(c);
            if (_log.isDebugEnabled()) {
                _log.debug("OnDemandLogger initialized for " + c + " is:" + _log); 
            }
        }
        return _log;
    }
    
    
    /**
     * reset the Log object to force a reload
     */
    public void resetLog() {
        if (_log != null && _log.isDebugEnabled()) {
            _log.debug("OnDemandLogger reset for " + c); 
        }
        _log = null;
    }

    /**
     * @return true if Log is set
     */
    public boolean hasLog() {
        return _log != null;
    }

    public void debug(Object arg0, Throwable arg1) {
        getLog().debug(arg0, arg1);
    }


    public void debug(Object arg0) {
        getLog().debug(arg0);
    }


    public void error(Object arg0, Throwable arg1) {
        getLog().error(arg0, arg1);
    }


    public void error(Object arg0) {
        getLog().error(arg0);
    }


    public void fatal(Object arg0, Throwable arg1) {
        getLog().fatal(arg0, arg1);
    }


    public void fatal(Object arg0) {
        getLog().fatal(arg0);
    }


    public void info(Object arg0, Throwable arg1) {
        getLog().info(arg0, arg1);
    }


    public void info(Object arg0) {
        getLog().info(arg0);
    }


    public boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
    }


    public boolean isErrorEnabled() {
        return getLog().isErrorEnabled();
    }


    public boolean isFatalEnabled() {
        return getLog().isFatalEnabled();
    }


    public boolean isInfoEnabled() {
        return getLog().isInfoEnabled();
    }


    public boolean isTraceEnabled() {
        return getLog().isTraceEnabled();
    }


    public boolean isWarnEnabled() {
        return getLog().isWarnEnabled();
    }


    public void trace(Object arg0, Throwable arg1) {
        getLog().trace(arg0, arg1);
    }


    public void trace(Object arg0) {
        getLog().trace(arg0);
    }


    public void warn(Object arg0, Throwable arg1) {
        getLog().warn(arg0, arg1);
    }


    public void warn(Object arg0) {
        getLog().warn(arg0);
    }


}
