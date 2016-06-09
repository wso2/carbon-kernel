package org.wso2.carbon.container.options;

import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.exam.Option;

public class EnvironmentPropertyOption implements Option {

    private final String key;
    private String value;

    public EnvironmentPropertyOption(String key) {
        this.key = key;
        this.value = "";
    }

    public EnvironmentPropertyOption value(String value) {
        NullArgumentException.validateNotNull(value, "Value");
        this.value = value;
        return this;
    }

    public String getOption() {
        return String.format("%s=%s", this.key, this.value);
    }
}
