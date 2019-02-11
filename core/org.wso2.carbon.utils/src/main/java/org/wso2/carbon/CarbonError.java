/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon;

import java.util.List;
import java.util.ArrayList;

/**
 * Class <code>CarbonError</code> is used to encapsulate error messages which are eventually to be displayed
 * inside the proper error page. This is used inside UI jsps.
 */
public class CarbonError {
    // the attribute which should be set on the request when forwarding to other jsps
    public static final String ID = "carbonError";
    private List<String> errors;

    public CarbonError() {
        errors = new ArrayList<String>();
    }

    public void addError(String msg) {
        errors.add(msg);
    }

    public List<String> getErrors() {
        return errors;
    }
}
