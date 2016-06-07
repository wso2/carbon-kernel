package org.wso2.carbon.container.options;

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
