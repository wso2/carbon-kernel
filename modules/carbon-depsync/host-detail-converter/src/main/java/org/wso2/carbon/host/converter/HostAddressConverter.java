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
@Plugin(name = "HostAddressConverter", category = "Converter")
@ConverterKeys({"ip"})
public class HostAddressConverter extends LogEventPatternConverter {

    protected HostAddressConverter(final String[] options) {
        super("ip", "ip");
    }

    public static HostAddressConverter newInstance(String[] options) {
        return new HostAddressConverter(options);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        toAppendTo.append(getHostAddress());
    }

    protected String getHostAddress() {
        String hostAddress = "127.0.1.1";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            //TODO handle exception
        }
        return hostAddress;
    }
}
