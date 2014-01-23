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

package org.apache.axis2.jaxws.description.builder;

import junit.framework.TestCase;

import javax.xml.ws.Holder;
import java.util.List;

/** Tests the parsing of Generics that are used in the DescriptionBuilderComposite processing. */
public class ParameterParsingTests extends TestCase {

    public void testHolder() {
        String holderInputString = "javax.xml.ws.Holder<java.lang.Object>";
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType(holderInputString);
        assertEquals("javax.xml.ws.Holder<java.lang.Object>", pdc.getParameterType());

        assertTrue(DescriptionBuilderUtils.isHolderType(holderInputString));
        assertTrue(pdc.isHolderType());
        String holderResultString = DescriptionBuilderUtils.getRawType(holderInputString);
        assertEquals("javax.xml.ws.Holder", holderResultString);
        holderResultString = pdc.getRawType();
        assertEquals("javax.xml.ws.Holder", holderResultString);
        javax.xml.ws.Holder validateHolder = new javax.xml.ws.Holder();
        assertEquals(validateHolder.getClass(), pdc.getParameterTypeClass());

        String actualTypeResult = DescriptionBuilderUtils.getHolderActualType(holderInputString);
        assertEquals("java.lang.Object", actualTypeResult);
        actualTypeResult = pdc.getHolderActualType();
        assertEquals("java.lang.Object", actualTypeResult);
        java.lang.Object validateObject = new java.lang.Object();
        assertEquals(validateObject.getClass(), pdc.getHolderActualTypeClass());
    }

    public void testHolderMyObject() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType(
                "javax.xml.ws.Holder<org.apache.axis2.jaxws.description.builder.MyObject>");
        assertEquals("javax.xml.ws.Holder<org.apache.axis2.jaxws.description.builder.MyObject>",
                     pdc.getParameterType());

        assertTrue(pdc.isHolderType());
        assertEquals("javax.xml.ws.Holder", pdc.getRawType());
        assertEquals(Holder.class, pdc.getParameterTypeClass());

        assertEquals("org.apache.axis2.jaxws.description.builder.MyObject",
                     pdc.getHolderActualType());
        assertEquals(MyObject.class, pdc.getHolderActualTypeClass());
    }

    public void testNonHolderGenric() {
        String inputString = "java.util.List<org.apache.axis2.jaxws.description.builder.MyObject>";
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType(inputString);
        assertEquals("java.util.List<org.apache.axis2.jaxws.description.builder.MyObject>",
                     pdc.getParameterType());

        assertFalse(pdc.isHolderType());
        String genericType = pdc.getRawType();
        assertEquals("java.util.List", genericType);
        assertEquals(java.util.List.class, pdc.getParameterTypeClass());

        // This should be null since the generic is not a Holder type
        String actualParam = pdc.getHolderActualType();
        assertNull(actualParam);
        assertNull(pdc.getHolderActualTypeClass());
    }

    public void testHolderGeneric() {
        String holderInputString = "javax.xml.ws.Holder<java.util.List<java.lang.Object>>";
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType(holderInputString);
        assertEquals("javax.xml.ws.Holder<java.util.List<java.lang.Object>>",
                     pdc.getParameterType());

        assertTrue(pdc.isHolderType());
        String holderResultString = pdc.getRawType();
        assertEquals("javax.xml.ws.Holder", holderResultString);
        assertEquals(Holder.class, pdc.getParameterTypeClass());

        String actualTypeResult = pdc.getHolderActualType();
        assertEquals("java.util.List", actualTypeResult);
        assertEquals(List.class, pdc.getHolderActualTypeClass());
    }

    public void testPrimitivesEncoded() {
        String[] primitivesToTest = { "Z", "B", "C", "D", "F", "I", "J", "S", "V" };
        Class[] primitiveClasses = { boolean.class, byte.class, char.class, double.class,
                float.class, int.class, long.class, short.class, void.class };

        for (int i = 0; i < primitivesToTest.length; i++) {
            assertFalse(DescriptionBuilderUtils.isHolderType(primitivesToTest[i]));
            assertNull(DescriptionBuilderUtils.getRawType(primitivesToTest[i]));
            ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
            pdc.setParameterType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], pdc.getParameterTypeClass());
        }
    }

    public void testPrimitives() {
        String[] primitivesToTest =
                { "boolean", "byte", "char", "double", "float", "int", "long", "short", "void" };
        Class[] primitiveClasses = { boolean.class, byte.class, char.class, double.class,
                float.class, int.class, long.class, short.class, void.class };

        for (int i = 0; i < primitivesToTest.length; i++) {
            assertFalse(DescriptionBuilderUtils.isHolderType(primitivesToTest[i]));
            assertNull(DescriptionBuilderUtils.getRawType(primitivesToTest[i]));
            ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
            pdc.setParameterType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], pdc.getParameterTypeClass());
        }
    }

    public void testPrimitiveArrays() {
        String[] primitivesToTest = { "boolean[]", "byte[]", "char[]", "double[]", "float[]",
                "int[]", "long[]", "short[]" };
        Class[] primitiveClasses = { boolean[].class, byte[].class, char[].class, double[].class,
                float[].class, int[].class, long[].class, short[].class };

        for (int i = 0; i < primitivesToTest.length; i++) {
            ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
            pdc.setParameterType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], pdc.getParameterTypeClass());
        }
    }

    public void testPrimitiveMultiDimArrays() {
        String[] primitivesToTest = { "boolean[][]", "byte[][][]", "char[][][][]",
                "double[][][][][]", "float[][][][][][]", "int[]", "long[]", "short[]" };
        Class[] primitiveClasses = { boolean[][].class, byte[][][].class, char[][][][].class,
                double[][][][][].class, float[][][][][][].class, int[].class, long[].class,
                short[].class };
        for (int i = 0; i < primitivesToTest.length; i++) {
            ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
            pdc.setParameterType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], pdc.getParameterTypeClass());
        }
    }

    public void testJavaLangObjectArrays() {
        ParameterDescriptionComposite pdcObject = new ParameterDescriptionComposite();
        pdcObject.setParameterType("java.lang.Object[]");
        Object[] verifyObject = new Object[5];
        assertEquals(verifyObject.getClass(), pdcObject.getParameterTypeClass());

        ParameterDescriptionComposite pdcString = new ParameterDescriptionComposite();
        pdcString.setParameterType("java.lang.String[][][]");
        String[][][] verifyString = new String[5][1][3];
        assertEquals(verifyString.getClass(), pdcString.getParameterTypeClass());

        ParameterDescriptionComposite pdcInteger = new ParameterDescriptionComposite();
        pdcInteger.setParameterType("java.lang.Integer[][][][]");
        Integer[][][][] verifyInteger = new Integer[5][1][3][12];
        assertEquals(verifyInteger.getClass(), pdcInteger.getParameterTypeClass());
    }

    public void testMyObjectArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("org.apache.axis2.jaxws.description.builder.MyObject[][]");
        MyObject[][] myObject = new MyObject[2][3];
        assertEquals(myObject.getClass(), pdc.getParameterTypeClass());
    }

    public void testHolderOfPrimitiveArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("javax.xml.ws.Holder<byte[]>");
        assertEquals("javax.xml.ws.Holder<byte[]>", pdc.getParameterType());

        assertEquals(Holder.class, pdc.getParameterTypeClass());
        byte [] validateByteArray = new byte[10];
        assertEquals(validateByteArray.getClass(), pdc.getHolderActualTypeClass());
    }

    public void testHolderOfMyObjectArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType(
                "javax.xml.ws.Holder<org.apache.axis2.jaxws.description.builder.MyObject[][]>");
        assertEquals("javax.xml.ws.Holder<org.apache.axis2.jaxws.description.builder.MyObject[][]>",
                     pdc.getParameterType());
        assertEquals(Holder.class, pdc.getParameterTypeClass());
        MyObject[][] validateMyObject = new MyObject[5][10];
        assertEquals(validateMyObject.getClass(), pdc.getHolderActualTypeClass());
    }

    public void testHolderOfStringArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("javax.xml.ws.Holder<java.lang.String[]>");
        assertEquals("javax.xml.ws.Holder<java.lang.String[]>", pdc.getParameterType());
        assertEquals(String[].class, pdc.getHolderActualTypeClass());
    }

    public void testStringArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.lang.String[]");
        assertEquals("java.lang.String[]", pdc.getParameterType());
        assertEquals(String[].class, pdc.getParameterTypeClass());
    }

    public void testHolderOfGenericArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("javax.xml.ws.Holder<java.util.List<java.lang.String>[]>");
        assertEquals("javax.xml.ws.Holder<java.util.List<java.lang.String>[]>",
                     pdc.getParameterType());
        assertEquals(List[].class, pdc.getHolderActualTypeClass());
    }

    public void testHolderOfGenericArrayMultiDimension() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("javax.xml.ws.Holder<java.util.List<java.lang.String>[][][]>");
        assertEquals("javax.xml.ws.Holder<java.util.List<java.lang.String>[][][]>",
                     pdc.getParameterType());
        assertEquals(List[][][].class, pdc.getHolderActualTypeClass());
    }

    public void testHolderOfGenericWildcardArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("javax.xml.ws.Holder<java.util.List<?>[]>");
        assertEquals("javax.xml.ws.Holder<java.util.List<?>[]>", pdc.getParameterType());
        assertEquals(List[].class, pdc.getHolderActualTypeClass());
    }

    public void testGenericArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.util.List<java.lang.String>[]");
        assertEquals("java.util.List<java.lang.String>[]", pdc.getParameterType());
        assertEquals(List[].class, pdc.getParameterTypeClass());
    }

    public void testGenericArrayMultiDimension() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.util.List<java.lang.String>[][]");
        assertEquals("java.util.List<java.lang.String>[][]", pdc.getParameterType());
        assertEquals(List[][].class, pdc.getParameterTypeClass());
    }

    public void testGenericWildCardArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.util.List<?>[]");
        assertEquals("java.util.List<?>[]", pdc.getParameterType());
        assertEquals(List[].class, pdc.getParameterTypeClass());
    }

    public void testGenericArrayOfStringArray() {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.util.List<java.lang.String[]>[]");
        assertEquals("java.util.List<java.lang.String[]>[]", pdc.getParameterType());
        assertEquals(List[].class, pdc.getParameterTypeClass());
    }
}

class MyObject {

}