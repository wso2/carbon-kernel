/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.core.dependencies.commons.collections;

import org.apache.commons.collections.functors.InvokerTransformer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This test is related to the security vulnerability in java object deserialization in Apache commons-collections.
 * Please refer https://issues.apache.org/jira/browse/COLLECTIONS-580 for more information.
 * <p/>
 * This test case tests the serialization and deserialization of InvokerTransformer objects.
 */
public class TestInvokerTransformer {

    /**
     * To serialize/deserialize InvokerTransformer, this property should be set to true
     * (commons-collections 3.2.2 onwards)
     */
    public final static String ENABLE_UNSAFE_SERIALIZATION =
            "org.apache.commons.collections.enableUnsafeSerialization";

    @Test(expectedExceptions = java.lang.UnsupportedOperationException.class)
    public void testSerialization() throws Exception {
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        byte[] data = serialize(transformer);
    }

    @Test
    public void testUnsafeSerialization() throws IOException, ClassNotFoundException {
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        System.setProperty(ENABLE_UNSAFE_SERIALIZATION, "true");
        byte[] data = serialize(transformer);
        Assert.assertNotNull(data);
        System.setProperty(ENABLE_UNSAFE_SERIALIZATION, "false");
    }

    @Test(expectedExceptions = java.lang.UnsupportedOperationException.class)
    public void testDeserialization() throws IOException, ClassNotFoundException {
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        System.setProperty(ENABLE_UNSAFE_SERIALIZATION, "true");
        byte[] data = serialize(transformer);
        Assert.assertNotNull(data);
        System.setProperty(ENABLE_UNSAFE_SERIALIZATION, "false");
        Object obj = deserialize(data);
    }

    @Test()
    public void testUnsafeDeserialization() throws IOException, ClassNotFoundException {
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        System.setProperty(ENABLE_UNSAFE_SERIALIZATION, "true");
        byte[] data = serialize(transformer);
        Assert.assertNotNull(data);
        Object obj = deserialize(data);
        System.setProperty(ENABLE_UNSAFE_SERIALIZATION, "false");
        Assert.assertTrue(obj instanceof InvokerTransformer);
    }

    private byte[] serialize(InvokerTransformer transformer) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(transformer);
        oos.close();

        return baos.toByteArray();
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }

}
