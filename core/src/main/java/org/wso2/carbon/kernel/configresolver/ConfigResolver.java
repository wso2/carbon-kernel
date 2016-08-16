package org.wso2.carbon.kernel.configresolver;

import org.wso2.carbon.kernel.configresolver.configfiles.AbstractConfigFile;

import java.io.File;
import java.io.FileInputStream;

/**
 * ConfigResolver helps to parse and update the configuration files. This will update the configuration values with
 * following placeholders ${env:alias}, ${sys:alias} and ${sec:alias}
 *
 * @since 5.2.0
 */
public interface ConfigResolver {
    <T extends AbstractConfigFile> T getConfig(File file, Class<T> clazz);
    <T extends AbstractConfigFile> T getConfig(FileInputStream inputStream, String fileNameKey, Class<T> clazz);
    String getConfig(String key);
}
