/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.log4j.MDC;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

/**
 * Test the scenarios for adding the correlation id from the valve to be used in correlationLogs
 */
public class RequestCorrelationIdValveTest {

    private static final String HEADER_TO_ID_MAPPING_CONFIG = "{'activityid':'Correlation-ID'}";
    private static final String QUERYPARAM_TO_ID_MAPPING_CONFIG = "{'RelayState':'Correlation-ID'}";
    private static final String TEST_HEADER_RECEIVED = "activityid";
    private static final String TEST_PARAM_RECEIVED = "RelayState";
    private static final String TEST_HEADER_RECEIVED_VALUE = "activityIdValue";
    private static final String TEST_PARAM_VALUE = "relayStateValue";
    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private static final Map<String, String> headerReceivedMap = new HashMap<>();
    private static final Map<String, String> queryParamReceivedMap = new HashMap<>();

    /**
     * This case checks if the MDC is set when an correlationId is received through a header
     *
     * @throws LifecycleException
     * @throws IOException
     * @throws ServletException
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.valves"})
    public void correlationCase1() throws LifecycleException, IOException, ServletException {

        Valve nextValve = mock(Valve.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                assertNotNull(MDC.get(CORRELATION_ID_MDC));
                return null;
            }
        }).when(nextValve).invoke(any(Request.class), any(Response.class));
        createValveForTesting(nextValve).invoke(createMockRequestWithHeader(), mock(Response.class));
    }

    /**
     * This case checks if the MDC is set when the correlation id is received through query params
     *
     * @throws LifecycleException
     * @throws IOException
     * @throws ServletException
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.valves"})
    public void correlationCase2() throws LifecycleException, IOException, ServletException {

        Valve nextValve = mock(Valve.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                assertNotNull(MDC.get(CORRELATION_ID_MDC));
                return null;
            }
        }).when(nextValve).invoke(any(Request.class), any(Response.class));
        createValveForTesting(nextValve).invoke(createMockRequestWithParams(), mock(Response.class));
    }

    /**
     * This case checks whether a randomly generated MDC is set when no correlation id is received.
     *
     * @throws LifecycleException
     * @throws IOException
     * @throws ServletException
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.valves"})
    public void correlationCase3() throws LifecycleException, IOException, ServletException {

        Valve nextValve = mock(Valve.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                assertNotNull(MDC.get(CORRELATION_ID_MDC));
                return null;
            }
        }).when(nextValve).invoke(any(Request.class), any(Response.class));
        createValveForTesting(nextValve).invoke(mock(Request.class), mock(Response.class));
    }

    private RequestCorrelationIdValve createValveForTesting(Valve nextValve) throws LifecycleException {

        RequestCorrelationIdValve testingValve = new RequestCorrelationIdValve();
        Container container = mock(Container.class);
        testingValve.setNext(nextValve);

        testingValve.setContainer(container);
        testingValve.setHeaderToCorrelationIdMapping(HEADER_TO_ID_MAPPING_CONFIG);
        testingValve.setQueryToCorrelationIdMapping(QUERYPARAM_TO_ID_MAPPING_CONFIG);
        testingValve.init();
        return testingValve;
    }

    private Request createMockRequestWithHeader() {

        headerReceivedMap.put(TEST_HEADER_RECEIVED, TEST_HEADER_RECEIVED_VALUE);
        Request testServletRequest = mock(Request.class);
        when(testServletRequest.getHeaderNames()).thenReturn(Collections.enumeration(headerReceivedMap.keySet()));
        when(testServletRequest.getHeader(TEST_HEADER_RECEIVED))
                .thenReturn(headerReceivedMap.get(TEST_HEADER_RECEIVED));
        return testServletRequest;
    }

    private Request createMockRequestWithParams() {

        queryParamReceivedMap.put(TEST_PARAM_RECEIVED, TEST_PARAM_VALUE);
        Request testServletRequest = mock(Request.class);
        when(testServletRequest.getParameterNames()).thenReturn(Collections
                .enumeration(queryParamReceivedMap.keySet()));
        when(testServletRequest.getParameter(TEST_PARAM_RECEIVED))
                .thenReturn(queryParamReceivedMap.get(TEST_PARAM_RECEIVED));
        return testServletRequest;
    }
}
