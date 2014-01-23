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

package org.apache.axiom.util.stax.xop;

import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.axiom.ext.stax.datahandler.DataHandlerProvider;

/**
 * Encapsulates an algorithm that decides whether base64 encoded binary data should be optimized
 * using XOP. The implementation takes the decision based on the submitted binary content and the
 * "eligible for optimization" flag. Depending on the context of use, this flag is provided by the
 * return value of {@link org.apache.axiom.ext.stax.datahandler.DataHandlerReader#isOptimized()} or
 * the <code>optimize</code> argument of
 * {@link org.apache.axiom.ext.stax.datahandler.DataHandlerWriter#writeDataHandler(DataHandler, String, boolean)}
 * or
 * {@link org.apache.axiom.ext.stax.datahandler.DataHandlerWriter#writeDataHandler(DataHandlerProvider, String, boolean)}.
 */
public interface OptimizationPolicy {
    /**
     * Policy implementation that optimizes all binary content marked as eligible for optimization.
     */
    OptimizationPolicy DEFAULT = new OptimizationPolicy() {
        public boolean isOptimized(DataHandler dataHandler, boolean optimize) {
            return optimize;
        }

        public boolean isOptimized(DataHandlerProvider dataHandlerProvider, boolean optimize) {
            return optimize;
        }
    };
    
    /**
     * Policy implementation that optimizes all binary content, regardless of whether is has been
     * marked as eligible for optimization.
     */
    OptimizationPolicy ALL = new OptimizationPolicy() {
        public boolean isOptimized(DataHandler dataHandler, boolean optimize) {
            return true;
        }

        public boolean isOptimized(DataHandlerProvider dataHandlerProvider, boolean optimize) {
            return true;
        }
    };
    
    /**
     * Determine whether the binary content supplied by a given {@link DataHandler} should be
     * optimized.
     * 
     * @param dataHandler
     *            the binary content
     * @param optimize
     *            indicates whether the binary content was initially marked as eligible for
     *            optimization (see above)
     * @return <code>true</code> if the binary content should be optimized using XOP, i.e. encoded
     *         using <tt>xop:Include</tt>
     * @throws IOException
     *             if an error occurs while reading the data handler
     */
    boolean isOptimized(DataHandler dataHandler, boolean optimize) throws IOException;
    
    /**
     * Determine whether the binary content supplied by a given {@link DataHandlerProvider} should
     * be optimized.
     * 
     * @param dataHandlerProvider
     *            the binary content
     * @param optimize
     *            indicates whether the binary content was initially marked as eligible for
     *            optimization (see above)
     * @return <code>true</code> if the binary content should be optimized using XOP, i.e. encoded
     *         using <tt>xop:Include</tt>
     * @throws IOException
     *             if an error occurs while reading the data handler
     */
    boolean isOptimized(DataHandlerProvider dataHandlerProvider, boolean optimize) throws IOException;
}
