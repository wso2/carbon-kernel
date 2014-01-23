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
package org.apache.axiom.ts;

import java.io.InputStream;

import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.OMMetaFactory;

/**
 * Base class for test cases that are executed against the files from the conformance test set.
 * 
 * @see org.apache.axiom.om.AbstractTestCase#getConformanceTestFiles()
 */
public abstract class ConformanceTestCase extends AxiomTestCase {
    private final String file;

    public ConformanceTestCase(OMMetaFactory metaFactory, String file) {
        super(metaFactory);
        this.file = file;
        int idx = file.lastIndexOf('/');
        setName(getName() + " [file=" + file.substring(idx+1) + "]");
    }

    protected InputStream getFileAsStream() {
        return AbstractTestCase.getTestResource(file);
    }
}
