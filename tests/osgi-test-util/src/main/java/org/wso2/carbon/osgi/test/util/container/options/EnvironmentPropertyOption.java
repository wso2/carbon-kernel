package org.wso2.carbon.osgi.test.util.container.options;

import org.ops4j.pax.exam.Option;

public class EnvironmentPropertyOption implements Option {

    private String option;

    public EnvironmentPropertyOption(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
