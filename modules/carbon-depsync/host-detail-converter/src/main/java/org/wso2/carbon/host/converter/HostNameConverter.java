package org.wso2.carbon.host.converter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Custom PatternConverter for logging host details eg: ip,hostname.
 */
@Plugin(name = "HostNameConverter", category = "Converter")
@ConverterKeys({"hostname"})
public class HostNameConverter extends LogEventPatternConverter {

    protected HostNameConverter(final String[] options) {
        super("hostname", "hostname");
    }

    public static HostNameConverter newInstance(String[] options) {
        return new HostNameConverter(options);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        toAppendTo.append(getHostName());
    }

    protected String getHostName() {
        String hostName = "localhost";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            //TODO handle exception
        }
        return hostName;
    }
}
