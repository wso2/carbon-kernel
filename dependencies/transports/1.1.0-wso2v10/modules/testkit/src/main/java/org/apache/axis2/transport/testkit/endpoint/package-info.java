/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * Provides interface and support classes for test endpoints.
 * <p>
 * The test kit uses in-only and in-out test endpoints. See
 * {@link org.apache.axis2.transport.testkit.endpoint.InOnlyEndpoint} and
 * {@link org.apache.axis2.transport.testkit.endpoint.InOutEndpoint} for
 * more details.
 * <p>
 * Note that an endpoint is assumed to be a lightweight test resource, i.e.
 * setting up and tearing down an endpoint should be inexpensive to
 * moderately expensive. If endpoint implementations require a server environment that is
 * expensive to set up, this environment should be provided by a different test
 * resource on which the endpoint implementations depend. This pattern is used for example
 * by {@link org.apache.axis2.transport.testkit.axis2.endpoint.AxisTestEndpoint} and its subclasses
 * which depend on {@link org.apache.axis2.transport.testkit.axis2.endpoint.AxisTestEndpointContext}
 * to provide the server environment.
 * <p>
 * An endpoint implementation should use a dependency on the appropriate subclass of
 * {@link org.apache.axis2.transport.testkit.channel.Channel} in order to get the
 * required information to bind the endpoint to the underlying transport protocol.
 */
package org.apache.axis2.transport.testkit.endpoint;
