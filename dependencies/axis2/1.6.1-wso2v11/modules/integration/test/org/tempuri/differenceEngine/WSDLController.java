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

package org.tempuri.differenceEngine;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Difference;

public class WSDLController implements ComparisonController {   
    public WSDLController () {
    }

    /**
     * Determine whether a Difference that this listener has been notified of
     *  should halt further XML comparison. This implementation halts 
     *  if the Difference is not recoverable.
     * @param afterDifference the last Difference passed to <code>differenceFound</code>
     * @return false if the difference is recoverable, otherwise return true
     */
    public boolean haltComparison(Difference afterDifference) {
        if (afterDifference.isRecoverable()) {
            return false;
        }
        
        return true;
    }
}
