package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

class JinjaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JinjaParser.class);

    private JinjaParser() {}

    static String execute(Map<String, Object> context, String templateFilePath) {
        Jinjava jinjava = new Jinjava();
        String renderedTemplate = "";
        try {
            String template = Resources.toString(Resources.getResource(templateFilePath), Charsets.UTF_8);
            renderedTemplate = jinjava.render(template, context);

        } catch (IOException e) {
            LOGGER.error("Error while parsing Jinja template", e);
        }

        return renderedTemplate;
    }
}
