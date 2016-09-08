package org.wso2.carbon.base.logging;

import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.Dictionary;

/**
 * Custom implementation of Configuration interface for unit testing purposes.
 *
 * @since 5.2.0
 */
public class CustomConfiguration implements Configuration {
    @Override
    public String getPid() {
        return null;
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        return null;
    }

    @Override
    public void update(Dictionary<String, ?> dictionary) throws IOException {

    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public String getFactoryPid() {
        return null;
    }

    @Override
    public void update() throws IOException {

    }

    @Override
    public void setBundleLocation(String s) {

    }

    @Override
    public String getBundleLocation() {
        return null;
    }

    @Override
    public long getChangeCount() {
        return 0;
    }
}
