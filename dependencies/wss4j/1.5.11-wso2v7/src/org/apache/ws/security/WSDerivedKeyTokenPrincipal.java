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
package org.apache.ws.security;

import java.security.Principal;

/**
 * This class implements the <code>Principal</code> interface and
 * represents a <code>DerivedKeyToken</code>.
 * The principal's name will be the <code>wsu:Id</code> value of the 
 * <code>DerivedKeyToken</code>
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class WSDerivedKeyTokenPrincipal implements Principal {

    private String id;
    private String nonce;
    private String label;
    private int length;
    private int offset;
    private String basetokenId;
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getNonce() {
        return nonce;
    }

    public WSDerivedKeyTokenPrincipal(String id) {
        this.id = id;
    }

    public String getName() {
        return id;
    }
    
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBasetokenId() {
        return basetokenId;
    }

    public void setBasetokenId(String basetokenId) {
        this.basetokenId = basetokenId;
    }

}
