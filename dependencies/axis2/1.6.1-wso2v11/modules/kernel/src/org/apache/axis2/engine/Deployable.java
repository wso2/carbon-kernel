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

package org.apache.axis2.engine;

import java.util.HashSet;
import java.util.Set;

/**
 * A Deployable is a container for something (Phases, Handlers) which wants
 * to be deployed in an ordered and constrained fashion via a
 * DeployableChain.
 */
public class Deployable {
    private String name;
    private String phase;
    private Set<String> successors;
    private Set<String> predecessors;
    
    boolean first;
    boolean last;

    Object target;

    public Deployable(String name) {
        this.name = name;
    }

    public Deployable(String name, Object target) {
        this.name = name;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void addSuccessor(String name) {
        if (successors == null) {
            successors = new HashSet<String>();
        }
        successors.add(name);
    }

    public void addPredecessor(String name) {
        if (predecessors == null) {
            predecessors = new HashSet<String>();
        }
        predecessors.add(name);
    }

    public Set<String> getPredecessors() {
        return predecessors;
    }

    public Set<String> getSuccessors() {
        return successors;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }
}
