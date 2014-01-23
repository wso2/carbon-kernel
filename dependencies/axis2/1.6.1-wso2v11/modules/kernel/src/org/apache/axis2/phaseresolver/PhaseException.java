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


package org.apache.axis2.phaseresolver;

import org.apache.axis2.AxisFault;

/**
 * Class PhaseException
 */
public class PhaseException extends AxisFault {

    private static final long serialVersionUID = 5690503396724929322L;

    /**
     * Constructor PhaseException
     *
     * @param message
     */
    public PhaseException(String message) {
        super(message);
    }

    /**
     * Constructor PhaseException
     *
     * @param cause
     */
    public PhaseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor PhaseException
     *
     * @param message
     * @param cause
     */
    public PhaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
