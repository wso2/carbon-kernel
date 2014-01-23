/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.axis2.classloader;

import java.net.URL;

/**
 * @version $Rev: 704201 $ $Date: 2008-10-14 00:22:25 +0530 (Tue, 14 Oct 2008) $
 */
public abstract class AbstractUrlResourceLocation implements ResourceLocation {
    private final URL codeSource;

    public AbstractUrlResourceLocation(URL codeSource) {
        this.codeSource = codeSource;
    }

    public final URL getCodeSource() {
        return codeSource;
    }

    public void close() {
    }

    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractUrlResourceLocation that = (AbstractUrlResourceLocation) o;
        return codeSource.equals(that.codeSource);
    }

    public final int hashCode() {
        return codeSource.hashCode();
    }

    public final String toString() {
        return "[" + getClass().getName() + ": " + codeSource + "]";
    }
}
