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

package org.apache.axis2.jaxws.description.builder;

import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import java.lang.annotation.Annotation;
import java.util.Arrays;

public class ActionAnnot implements Action {
    
    private FaultAction[] fault;
    private String input;
    private String output;

    private ActionAnnot() {
    }
    
    private ActionAnnot(FaultAction[] fault, String input, String output) {
        this.fault = fault;
        this.input = input;
        this.output = output;
    }
    
    public static ActionAnnot createActionAnnotImpl() {
        return new ActionAnnot();
    }
    
    public static ActionAnnot createActionAnnotImpl(FaultAction[] fault, String input, String output) {
        return new ActionAnnot(fault, input, output);
    }
    
    public void setFault(FaultAction[] fault) {
        this.fault = fault;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public FaultAction[] fault() {
        return fault;
    }

    public String input() {
        return input;
    }

    public String output() {
        return output;
    }

    //hmm, should we really do this
    public Class<? extends Annotation> annotationType() {
        return Annotation.class;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@Action.fault= " + Arrays.toString(fault));
        sb.append(newLine);
        sb.append("@Action.input= " + input);
        sb.append(newLine);
        sb.append("@Action.output= " + output);
        sb.append(newLine);
        return sb.toString();
    }
}
