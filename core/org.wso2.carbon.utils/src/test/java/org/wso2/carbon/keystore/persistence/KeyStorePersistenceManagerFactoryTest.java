/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.keystore.persistence;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.keystore.persistence.impl.HybridKeyStorePersistenceManager;
import org.wso2.carbon.keystore.persistence.impl.JDBCKeyStorePersistenceManager;
import org.wso2.carbon.keystore.persistence.impl.RegistryKeyStorePersistenceManager;

import java.lang.reflect.Field;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the behavior of the KeyStore Persistence Manager Factory class.
 */
public class KeyStorePersistenceManagerFactoryTest {

    @BeforeMethod
    public void setUp() {

        initMocks(this);
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsDatabase() throws Exception {

        setPrivateStaticField(KeyStorePersistenceManagerFactory.class, "KEYSTORE_STORAGE_TYPE", "database");

        // Validate KeyStore Persistence Manager.
        KeyStorePersistenceManager keyStorePersistenceManager =
                KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        assertTrue(keyStorePersistenceManager instanceof JDBCKeyStorePersistenceManager);
    }

    @Test
    public void shouldReturnHybridPersistenceManagerWhenConfigIsOnMigration() throws Exception {

        setPrivateStaticField(KeyStorePersistenceManagerFactory.class, "KEYSTORE_STORAGE_TYPE", "hybrid");

        // Validate KeyStore Persistence Manager.
        KeyStorePersistenceManager keyStorePersistenceManager =
                KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        assertTrue(keyStorePersistenceManager instanceof HybridKeyStorePersistenceManager);
    }

    @Test
    public void shouldReturnRegistryBasedPersistenceManagerWhenConfigIsRegistry() throws Exception {

        setPrivateStaticField(KeyStorePersistenceManagerFactory.class, "KEYSTORE_STORAGE_TYPE", "registry");

        // Validate KeyStore Persistence Manager.
        KeyStorePersistenceManager keyStorePersistenceManager =
                KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        assertTrue(keyStorePersistenceManager instanceof RegistryKeyStorePersistenceManager);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsInvalid() throws Exception {

        setPrivateStaticField(KeyStorePersistenceManagerFactory.class, "KEYSTORE_STORAGE_TYPE", "invalid");

        // Validate KeyStore Persistence Manager.
        KeyStorePersistenceManager keyStorePersistenceManager =
                KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        assertTrue(keyStorePersistenceManager instanceof JDBCKeyStorePersistenceManager);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsEmpty() throws Exception {

        setPrivateStaticField(KeyStorePersistenceManagerFactory.class, "KEYSTORE_STORAGE_TYPE", "");

        // Validate KeyStore Persistence Manager.
        KeyStorePersistenceManager keyStorePersistenceManager =
                KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        assertTrue(keyStorePersistenceManager instanceof JDBCKeyStorePersistenceManager);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsNull() throws Exception {

        setPrivateStaticField(KeyStorePersistenceManagerFactory.class, "KEYSTORE_STORAGE_TYPE", null);

        // Validate KeyStore Persistence Manager.
        KeyStorePersistenceManager keyStorePersistenceManager =
                KeyStorePersistenceManagerFactory.getKeyStorePersistenceManager();
        assertTrue(keyStorePersistenceManager instanceof JDBCKeyStorePersistenceManager);
    }

}
