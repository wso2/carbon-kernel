/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.caching.impl;

import java.io.Serializable;

/**
 * TODO: class description
 */
class SerializableTestObject implements Serializable {

    private String name;
    private String address;
    private Long id;

    SerializableTestObject(String name, String address, Long id) {
        this.name = name;
        this.address = address;
        this.id = id;
    }

    String getName() {
        return name;
    }

    String getAddress() {
        return address;
    }

    Long getId() {
        return id;
    }
}
