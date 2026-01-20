package org.wso2.carbon.base.api;

import org.w3c.dom.Element;

import java.util.Set;

public interface ServerConfigurationService {
    void setConfigurationProperty(String key, String value);

    void overrideConfigurationProperty(String key, String value);

    String getFirstProperty(String key);

    String[] getProperties(String key);

    Element getDocumentElement();

    public Set<String> getDeniedAdminServices();

}
