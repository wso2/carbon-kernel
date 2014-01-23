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


package org.apache.axis2.description;

import java.util.ArrayList;
import java.util.List;

/**
 * Class FlowImpl
 */
public class Flow {

    /**
     * Field list
     */
    protected final List<HandlerDescription> list;

    /**
     * Constructor FlowImpl
     */
    public Flow() {
        list = new ArrayList<HandlerDescription>();
    }

    /**
     * Method addHandler.
     *
     * @param handler
     */
    public void addHandler(HandlerDescription handler) {
        list.add(handler);
    }

    /**
     * Method getHandler.
     *
     * @param index
     * @return Returns HandlerDescription.
     */
    public HandlerDescription getHandler(int index) {
        return (HandlerDescription) list.get(index);
    }

    /**
     * Method getHandlerCount.
     *
     * @return Returns int.
     */
    public int getHandlerCount() {
        return list.size();
    }
}
