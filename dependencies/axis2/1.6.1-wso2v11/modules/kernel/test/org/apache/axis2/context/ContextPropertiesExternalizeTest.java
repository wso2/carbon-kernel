/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.context;

import org.apache.axis2.description.OutOnlyAxisOperation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Test the externalization of properties on an AbstractContext.  Since each of the AbstractContext
 * subclasses have their own read and write External methods, each one is tested.
 */
public class ContextPropertiesExternalizeTest extends TestCase {
    
    public void testExternalizeMessageContext() {
        MessageContext mc = new MessageContext();
        mc.setProperty("key1", "value1");
        mc.setProperty("key2", "value2");
        mc.setProperty(null, "value3_nullKey");
        mc.setProperty("key4_nullValue", null);
        mc.setProperty("key5", "value5");
        
        assertTrue(mc.properties instanceof HashMap);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            mc.writeExternal(oos);
            oos.flush();
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            
            MessageContext mcRead = new MessageContext();
            mcRead.readExternal(ois);
            
            assertEquals(5, mcRead.properties.size());
            assertEquals("value1", mcRead.getProperty("key1"));
            assertEquals("value2", mcRead.getProperty("key2"));
            assertEquals("value3_nullKey", mcRead.getProperty(null));
            assertNull(mcRead.getProperty("key4_nullValue"));
            assertEquals("value5", mcRead.getProperty("key5"));
            assertTrue(mcRead.properties instanceof HashMap);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Caught exception: " + ex);
        }
    }

    public void testExternalizeOperationContext() {
        OperationContext ctx = new OperationContext(new OutOnlyAxisOperation(), new ServiceContext());
        
        ctx.setProperty("key1", "value1");
        ctx.setProperty("key2", "value2");
        ctx.setProperty(null, "value3_nullKey");
        ctx.setProperty("key4_nullValue", null);
        ctx.setProperty("key5", "value5");
        
        assertTrue(ctx.properties instanceof HashMap);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            ctx.writeExternal(oos);
            oos.flush();
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            
            OperationContext ctxRead = new OperationContext();
            ctxRead.readExternal(ois);
            
            assertEquals(5, ctxRead.properties.size());
            assertEquals("value1", ctxRead.getProperty("key1"));
            assertEquals("value2", ctxRead.getProperty("key2"));
            assertEquals("value3_nullKey", ctxRead.getProperty(null));
            assertNull(ctxRead.getProperty("key4_nullValue"));
            assertEquals("value5", ctxRead.getProperty("key5"));
            assertTrue(ctxRead.properties instanceof HashMap);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Caught exception: " + ex);
        }
    }
    
    public void testExternalizeServiceContext() {
        ServiceContext ctx = new ServiceContext();
        
        ctx.setProperty("key1", "value1");
        ctx.setProperty("key2", "value2");
        ctx.setProperty(null, "value3_nullKey");
        ctx.setProperty("key4_nullValue", null);
        ctx.setProperty("key5", "value5");
        
        assertTrue(ctx.properties instanceof HashMap);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            ctx.writeExternal(oos);
            oos.flush();
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            
            ServiceContext ctxRead = new ServiceContext();
            ctxRead.readExternal(ois);
            
            assertEquals(5, ctxRead.properties.size());
            assertEquals("value1", ctxRead.getProperty("key1"));
            assertEquals("value2", ctxRead.getProperty("key2"));
            assertEquals("value3_nullKey", ctxRead.getProperty(null));
            assertNull(ctxRead.getProperty("key4_nullValue"));
            assertEquals("value5", ctxRead.getProperty("key5"));
            assertTrue(ctxRead.properties instanceof HashMap);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Caught exception: " + ex);
        }
    }
    
    public void testExternalizeServiceGroupContext() {
        ServiceGroupContext ctx = new ServiceGroupContext();
        
        ctx.setProperty("key1", "value1");
        ctx.setProperty("key2", "value2");
        ctx.setProperty(null, "value3_nullKey");
        ctx.setProperty("key4_nullValue", null);
        ctx.setProperty("key5", "value5");
        
        assertTrue(ctx.properties instanceof HashMap);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            ctx.writeExternal(oos);
            oos.flush();
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            
            ServiceGroupContext ctxRead = new ServiceGroupContext();
            ctxRead.readExternal(ois);
            
            assertEquals(5, ctxRead.properties.size());
            assertEquals("value1", ctxRead.getProperty("key1"));
            assertEquals("value2", ctxRead.getProperty("key2"));
            assertEquals("value3_nullKey", ctxRead.getProperty(null));
            assertNull(ctxRead.getProperty("key4_nullValue"));
            assertEquals("value5", ctxRead.getProperty("key5"));
            assertTrue(ctxRead.properties instanceof HashMap);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Caught exception: " + ex);
        }
    }
    
    public void testExternalizeSessionContext() {
        SessionContext ctx = new SessionContext();
        
        ctx.setProperty("key1", "value1");
        ctx.setProperty("key2", "value2");
        ctx.setProperty(null, "value3_nullKey");
        ctx.setProperty("key4_nullValue", null);
        ctx.setProperty("key5", "value5");
        
        assertTrue(ctx.properties instanceof HashMap);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            ctx.writeExternal(oos);
            oos.flush();
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            
            SessionContext ctxRead = new SessionContext();
            ctxRead.readExternal(ois);
            
            assertEquals(5, ctxRead.properties.size());
            assertEquals("value1", ctxRead.getProperty("key1"));
            assertEquals("value2", ctxRead.getProperty("key2"));
            assertEquals("value3_nullKey", ctxRead.getProperty(null));
            assertNull(ctxRead.getProperty("key4_nullValue"));
            assertEquals("value5", ctxRead.getProperty("key5"));
            assertTrue(ctxRead.properties instanceof HashMap);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Caught exception: " + ex);
        }
    }
}
