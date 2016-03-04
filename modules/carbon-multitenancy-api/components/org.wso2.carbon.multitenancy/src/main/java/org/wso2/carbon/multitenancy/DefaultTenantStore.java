/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.multitenancy;

import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.multitenancy.api.Tenant;
import org.wso2.carbon.multitenancy.api.TenantStore;
import org.wso2.carbon.multitenancy.exception.TenantStoreException;
import org.wso2.carbon.multitenancy.model.TenantConfig;
import org.wso2.carbon.multitenancy.model.TenantStoreConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Default file based implementation of the TenantStore represents the storage of tenants.
 *
 * @since 1.0.0
 */
public class DefaultTenantStore implements TenantStore {

    private TenantStoreConfig tenantStoreConfig;
    private JAXBContext jaxbContext;
    private Map<String, TenantConfig> tenantConfigMap;

    private File tenantStoreXMLPath = Paths.get(Utils.getCarbonHome().toString(),
            "data", "tenant", "tenants.xml").toFile();

    @Override
    public void init() throws TenantStoreException {
        try {
            jaxbContext = JAXBContext.newInstance(TenantStoreConfig.class);
            loadConfig();
        } catch (JAXBException | TenantStoreException e) {
            throw new TenantStoreException("Error while initializing tenant store", e);
        }
    }

    @Override
    public Tenant loadTenant(String tenantDomain) throws TenantStoreException {
        if (tenantConfigMap.containsKey(tenantDomain)) {
            TenantConfig tenantConfig = tenantConfigMap.get(tenantDomain);
            return populateTenant(tenantConfig);
        }
        throw new TenantStoreException("Tenant with the domain " + tenantDomain + " does not exists");
    }

    @Override
    public void addTenant(Tenant tenant) throws TenantStoreException {
        TenantConfig tenantConfig = populateTenantConfig(tenant);
        tenantStoreConfig.addTenantConfig(tenantConfig);
        saveConfig();
    }

    @Override
    public Tenant deleteTenant(String tenantDomain) throws TenantStoreException {
        throw new UnsupportedOperationException();
    }

    private void saveConfig() throws TenantStoreException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tenantStoreXMLPath), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(tenantStoreConfig, writer);
        } catch (IOException | JAXBException e) {
            throw new TenantStoreException("Error while saving tenant configuration", e);
        }
    }

    private void loadConfig() throws TenantStoreException {
        try (Reader reader = new InputStreamReader(new FileInputStream(tenantStoreXMLPath), StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            tenantStoreConfig = (TenantStoreConfig) unmarshaller.unmarshal(reader);
            populateTenantConfigMap();
        } catch (JAXBException | IOException e) {
            throw new TenantStoreException("Error while loading tenant configuration", e);
        }
    }

    private Tenant populateTenant(TenantConfig tenantConfig) {
        return new Tenant(tenantConfig.getDomain());
    }

    private TenantConfig populateTenantConfig(Tenant tenant) {
        TenantConfig tenantConfig = new TenantConfig();
        tenantConfig.setDomain(tenant.getDomain());
        return tenantConfig;
    }


    private void populateTenantConfigMap() {
        tenantConfigMap = new HashMap<>(tenantStoreConfig.getTenantConfigs().size());
        tenantStoreConfig.getTenantConfigs()
                .forEach(tenantConfig -> tenantConfigMap.put(tenantConfig.getDomain(), tenantConfig));
    }
}
