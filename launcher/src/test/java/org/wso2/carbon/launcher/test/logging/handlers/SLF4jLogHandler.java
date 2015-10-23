/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.launcher.test.logging.handlers;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.ArrayList;

/**
 * SLF4jLogHandler class which implements the Logger interface.
 *
 * @see Logger
 */
public class SLF4jLogHandler implements Logger {
    ArrayList<String> logList = new ArrayList<String>();

    public ArrayList<String> getLogList() {
        return logList;
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isTraceEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDebugEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInfoEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s) {
        logList.add(s);
    }

    @Override
    public void info(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWarnEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isErrorEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
