package org.wso2.carbon.kernel.configresolver;

import org.wso2.carbon.kernel.configresolver.configfiles.AbstractConfigFile;

/**
 * ConfigResolver helps to parse and update the configuration files. This will update the configuration values with
 * following placeholders ${env:alias}, ${sys:alias} and ${sec:alias}
 *
 * @since 5.2.0
 */
public interface ConfigResolver {
    <T extends AbstractConfigFile> T getConfig(T configFile);
    String getConfig(String key);
}
