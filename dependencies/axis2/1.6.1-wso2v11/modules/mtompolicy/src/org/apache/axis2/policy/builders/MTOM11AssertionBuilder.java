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
package org.apache.axis2.policy.builders;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.policy.model.MTOM10Assertion;
import org.apache.axis2.policy.model.MTOM11Assertion;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.builders.AssertionBuilder;

/**
 * This builder is responsible for the creation of a MTOM assertion object </br>
 * (compliant with the WS-MTOMPolicy verion 1.1).
 * The builder will be picked by the
 * "org.apache.neethi.AssertionBuilderFactory".
 */

public class MTOM11AssertionBuilder implements AssertionBuilder{
    
    private static Log log = LogFactory.getLog(MTOM10AssertionBuilder.class);

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {

        MTOM11Assertion mtomAssertion = new MTOM11Assertion();

        processMTOM11Assertion(element, mtomAssertion);

        return mtomAssertion;
    }

    public QName[] getKnownElements() {
        return new QName[] { new QName(MTOM11Assertion.NS,
                MTOM11Assertion.MTOM_LN) };
    }

    private void processMTOM11Assertion(OMElement element,
            MTOM11Assertion mtomAssertion) {

        // Checking wsp:Optional attribute
        String value = element
                .getAttributeValue(Constants.Q_ELEM_OPTIONAL_ATTR);
        boolean isOptional = JavaUtils.isTrueExplicitly(value);

        mtomAssertion.setOptional(isOptional);

    }

}
