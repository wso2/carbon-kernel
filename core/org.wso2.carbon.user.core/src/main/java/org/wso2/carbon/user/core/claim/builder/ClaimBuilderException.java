/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.claim.builder;

public class ClaimBuilderException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -3422270808455087156L;

    public ClaimBuilderException() {
        super();
    }

    public ClaimBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClaimBuilderException(String message, boolean convertMessage) {
        super(message);
    }

    public ClaimBuilderException(String message) {
        super(message);
    }

    public ClaimBuilderException(Throwable cause) {
        super(cause);
    }
}
