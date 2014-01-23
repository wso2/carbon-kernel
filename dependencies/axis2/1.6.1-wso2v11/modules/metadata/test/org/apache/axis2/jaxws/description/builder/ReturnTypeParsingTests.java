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

import java.util.ArrayList;
import java.util.List;

/** Tests the parsing of Generics that are used in the DescriptionBuilderComposite processing. */
public class ReturnTypeParsingTests extends TestCase {

    public void testNonHolderGenric() {
        String inputString =
                "java.util.List<org.apache.axis2.jaxws.description.builder.MyReturnTestObject>";
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType(inputString);
        assertEquals(
                "java.util.List<org.apache.axis2.jaxws.description.builder.MyReturnTestObject>",
                mdc.getReturnType());

        assertEquals(java.util.List.class, mdc.getReturnTypeClass());
    }

    public void testPrimitivesEncoded() {
        String[] primitivesToTest = { "Z", "B", "C", "D", "F", "I", "J", "S",
                "V" };
        Class[] primitiveClasses = { boolean.class, byte.class, char.class,
                double.class, float.class, int.class, long.class, short.class,
                void.class };

        for (int i = 0; i < primitivesToTest.length; i++) {
            assertNull(DescriptionBuilderUtils.getRawType(primitivesToTest[i]));
            MethodDescriptionComposite mdc = new MethodDescriptionComposite();
            mdc.setReturnType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], mdc.getReturnTypeClass());
        }
    }

    public void testPrimitives() {
        String[] primitivesToTest = { "boolean", "byte", "char", "double",
                "float", "int", "long", "short", "void" };
        Class[] primitiveClasses = { boolean.class, byte.class, char.class,
                double.class, float.class, int.class, long.class, short.class,
                void.class };

        for (int i = 0; i < primitivesToTest.length; i++) {
            assertNull(DescriptionBuilderUtils.getRawType(primitivesToTest[i]));
            MethodDescriptionComposite mdc = new MethodDescriptionComposite();
            mdc.setReturnType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], mdc.getReturnTypeClass());
        }
    }

    public void testPrimitiveArrays() {
        String[] primitivesToTest = { "boolean[]", "byte[]", "char[]",
                "double[]", "float[]", "int[]", "long[]", "short[]" };
        Class[] primitiveClasses = { boolean[].class, byte[].class,
                char[].class, double[].class, float[].class, int[].class,
                long[].class, short[].class };

        for (int i = 0; i < primitivesToTest.length; i++) {
            MethodDescriptionComposite mdc = new MethodDescriptionComposite();
            mdc.setReturnType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], mdc.getReturnTypeClass());
        }
    }

    public void testPrimitiveMultiDimArrays() {
        String[] primitivesToTest = { "boolean[][]", "byte[][][]",
                "char[][][][]", "double[][][][][]", "float[][][][][][]",
                "int[]", "long[]", "short[]" };
        Class[] primitiveClasses = { boolean[][].class, byte[][][].class,
                char[][][][].class, double[][][][][].class,
                float[][][][][][].class, int[].class, long[].class,
                short[].class };
        for (int i = 0; i < primitivesToTest.length; i++) {
            MethodDescriptionComposite mdc = new MethodDescriptionComposite();
            mdc.setReturnType(primitivesToTest[i]);
            assertEquals(primitiveClasses[i], mdc.getReturnTypeClass());
        }
    }

    public void testJavaLangObjectArrays() {
        MethodDescriptionComposite pdcObject = new MethodDescriptionComposite();
        pdcObject.setReturnType("java.lang.Object[]");
        Object[] verifyObject = new Object[5];
        assertEquals(verifyObject.getClass(), pdcObject.getReturnTypeClass());

        MethodDescriptionComposite pdcString = new MethodDescriptionComposite();
        pdcString.setReturnType("java.lang.String[][][]");
        String[][][] verifyString = new String[5][1][3];
        assertEquals(verifyString.getClass(), pdcString.getReturnTypeClass());

        MethodDescriptionComposite pdcInteger = new MethodDescriptionComposite();
        pdcInteger.setReturnType("java.lang.Integer[][][][]");
        Integer[][][][] verifyInteger = new Integer[5][1][3][12];
        assertEquals(verifyInteger.getClass(), pdcInteger
                .getReturnTypeClass());
    }

    public void testMyObjectArray() {
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType("org.apache.axis2.jaxws.description.builder.MyReturnTestObject[][]");
        MyReturnTestObject[][] myObject = new MyReturnTestObject[2][3];
        assertEquals(myObject.getClass(), mdc.getReturnTypeClass());
    }

    public void testArrayListOfPrimitiveArray() {
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType("java.util.ArrayList<byte[]>");
        assertEquals("java.util.ArrayList<byte[]>", mdc.getReturnType());
        assertEquals(ArrayList.class, mdc.getReturnTypeClass());
    }

    public void testListOfMyObjectArray() {
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType(
                "java.util.List<org.apache.axis2.jaxws.description.builder.MyReturnTestObject[][]>");
        assertEquals(
                "java.util.List<org.apache.axis2.jaxws.description.builder.MyReturnTestObject[][]>",
                mdc.getReturnType());

        assertEquals(List.class, mdc.getReturnTypeClass());
    }

    public void testGenericArray() {
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType("java.util.List<java.lang.String[]>[]");
        assertEquals("java.util.List<java.lang.String[]>[]",
                     mdc.getReturnType());
        assertEquals(List[].class, mdc.getReturnTypeClass());
    }

    public void testGenericArrayMultiDimension() {
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType(
                "java.util.List<org.apache.axis2.jaxws.description.builder.MyReturnTestObject>[][]");
        assertEquals(
                "java.util.List<org.apache.axis2.jaxws.description.builder.MyReturnTestObject>[][]",
                mdc.getReturnType());
        assertEquals(List[][].class, mdc.getReturnTypeClass());
    }

    public void testWildcardGenericArray() {
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setReturnType("java.util.List<?>[]");
        assertEquals("java.util.List<?>[]",
                     mdc.getReturnType());
        assertEquals(List[].class, mdc.getReturnTypeClass());
    }

}

class MyReturnTestObject {

}