package org.wso2.carbon.kernel.internal.logging;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;

public class CustomManagedService implements ManagedService {
    @Override
    public void updated(Dictionary<String, ?> dictionary) throws ConfigurationException {

    }
}
