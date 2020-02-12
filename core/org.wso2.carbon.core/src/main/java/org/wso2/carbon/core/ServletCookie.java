/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.core;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

/**
 *  This extended method handle the sameSite value and newly added value for the cookies.
 */
public class ServletCookie extends Cookie {

    private SameSiteCookie sameSite;
    private Map<String, String> attributes;

    public ServletCookie(String name, String value) {

        super(name, value);
    }

    public void setSameSite(SameSiteCookie value) {

        this.sameSite = value;
    }

    public SameSiteCookie getSameSite() {

        return sameSite;
    }

    public void setAttribute(String attributeName, String attributeValue) {

        if (this.attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(attributeName, attributeValue);
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }
}
