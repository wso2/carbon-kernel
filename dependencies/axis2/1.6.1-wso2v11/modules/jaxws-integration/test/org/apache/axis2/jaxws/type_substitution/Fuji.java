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

package org.apache.axis2.jaxws.type_substitution;

public class Fuji extends Apple {

    private String location;

    public Fuji() {
        setType("Fuji");
    }

    public Fuji(String color, String flavor, String location) {
        setType("Fuji");
        setColor(color);
        setFlavor(flavor);
        
        setLocation(location);
    }

    public void setLocation(String arg0) {
        this.location = arg0;
    }

    public String getLocation() {
        return this.location;
    }

}
