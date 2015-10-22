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

import org.apache.commons.logging.Log;

import java.util.ArrayList;

public class CommonsLogHandler implements Log {
    ArrayList<Object> logList = new ArrayList<Object>();

    public ArrayList<Object> getLogList() {
        return logList;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isErrorEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isFatalEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInfoEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isTraceEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWarnEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void trace(Object o, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Object o, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void info(Object o) {
        logList.add(o);

    }

    @Override
    public void info(Object o, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void warn(Object o, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void error(Object o, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void fatal(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void fatal(Object o, Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getLogLevel() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
