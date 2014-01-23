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

package org.apache.axis2.databinding.types;


/**
 * Custom class for supporting XSD data type NOTATION.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-1/#element-notation">XML Schema Part 1: 3.12
 *      Notation Declarations</a>
 */

public class Notation implements java.io.Serializable {

    private static final long serialVersionUID = 2393074651972192536L;

    NCName name;
    URI publicURI;
    URI systemURI;

    public Notation() {
    }

    public Notation(NCName name, URI publicURI, URI systemURI) {
        this.name = name;
        this.publicURI = publicURI;
        this.systemURI = systemURI;
    }

    public NCName getName() {
        return name;
    }

    public void setName(NCName name) {
        this.name = name;
    }

    public URI getPublic() {
        return publicURI;
    }

    public void setPublic(URI publicURI) {
        this.publicURI = publicURI;
    }

    public URI getSystem() {
        return systemURI;
    }

    public void setSystem(URI systemURI) {
        this.systemURI = systemURI;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Notation))
            return false;
        Notation other = (Notation)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (publicURI == null) {
            if (other.publicURI != null) {
                return false;
            }
        } else if (!publicURI.equals(other.publicURI)) {
            return false;
        }
        if (systemURI == null) {
            if (other.systemURI != null) {
                return false;
            }
        } else if (!systemURI.equals(other.systemURI)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the sum of the hashcodes of {name,publicURI,systemURI} for whichever properties in
     * that set is non null.  This is consistent with the implementation of equals, as required by
     * {@link Object#hashCode() Object.hashCode}.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        int hash = 0;
        if (null != name) {
            hash += name.hashCode();
        }
        if (null != publicURI) {
            hash += publicURI.hashCode();
        }
        if (null != systemURI) {
            hash += systemURI.hashCode();
        }
        return hash;
    }

    /**
     * Note - A lot of code that depended on certain descriptions has been deleted from this class
     */

}
