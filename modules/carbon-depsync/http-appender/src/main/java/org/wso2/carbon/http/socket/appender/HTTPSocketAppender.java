package org.wso2.carbon.http.socket.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.util.Booleans;

import java.io.Serializable;

/**
 * An Appender that delivers events over socket connections. Supports HTTP.
 */
@Plugin(name = "HTTPSocket", category = "Core", elementType = "appender", printObject = true)
public class HTTPSocketAppender extends AbstractOutputStreamAppender<AbstractSocketManager> {

    protected HTTPSocketAppender(String name, Layout<? extends Serializable> layout, Filter filter,
                                 boolean ignoreExceptions, boolean immediateFlush, AbstractSocketManager manager) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * Creates a HTTP appender.
     *
     * @param host                 The name of the host to connect to.
     * @param port                 The port to connect to on the target host.
     * @param connectTimeoutMillis the connect timeout in milliseconds.
     * @param name                 The name of the Appender.
     * @param immediateFlush       "true" if data should be flushed on each write.
     * @param ignoreExceptions     If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise they
     *                             are propagated to the caller.
     * @param layout               The layout to use (defaults to SerializedLayout).
     * @param filter               The Filter or null.
     * @return A HTTPAppender.
     */
    @PluginFactory
    public static HTTPSocketAppender createAppender(
            // @formatter:off
            @PluginAttribute("host") final String host,
            @PluginAttribute(value = "port", defaultInt = 0) final int port,
            @PluginAttribute(value = "connectTimeoutMillis", defaultInt = 0) final int connectTimeoutMillis,
            @PluginAttribute("name") final String name,
            @PluginAttribute(value = "immediateFlush", defaultBoolean = true) boolean immediateFlush,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        // @formatter:on

        if (layout == null) {
            layout = SerializedLayout.createLayout();
        }

        if (name == null) {
            LOGGER.error("No name provided for HTTPAppender");
            return null;
        }
        immediateFlush = true;

        final AbstractSocketManager manager = createSocketManager(name, host, port, connectTimeoutMillis,
                layout);

        return new HTTPSocketAppender(name, layout, filter, ignoreExceptions, immediateFlush, manager);
    }

    /**
     * Creates a HTTP appender.
     *
     * @param host                 The name of the host to connect to.
     * @param portNum              The port to connect to on the target host.
     * @param protocolIn           The Protocol to use.
     * @param connectTimeoutMillis the connect timeout in milliseconds.
     * @param delayMillis          The interval in which failed writes should be retried.
     * @param immediateFail        True if the write should fail if no socket is immediately available.
     * @param name                 The name of the Appender.
     * @param immediateFlush       "true" if data should be flushed on each write.
     * @param ignore               If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise they
     *                             are propagated to the caller.
     * @param layout               The layout to use (defaults to SerializedLayout).
     * @param filter               The Filter or null.
     * @param advertise            "true" if the appender configuration should be advertised, "false" otherwise.
     * @param config               The Configuration
     * @return A HTTPAppender.
     * @deprecated Use
     */
    @Deprecated
    public static HTTPSocketAppender createAppender(
            // @formatter:off
            final String host,
            final String portNum,
            final String protocolIn,
            final int connectTimeoutMillis,
            // deprecated
            final String delayMillis,
            final String immediateFail,
            final String name,
            final String immediateFlush,
            final String ignore,
            Layout<? extends Serializable> layout,
            final Filter filter,
            final String advertise,
            final Configuration config) {
        // @formatter:on
        boolean isFlush = Booleans.parseBoolean(immediateFlush, true);
        final boolean isAdvertise = Boolean.parseBoolean(advertise);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final boolean fail = Booleans.parseBoolean(immediateFail, true);
        final int reconnectDelayMillis = AbstractAppender.parseInt(delayMillis, 0);
        final int port = AbstractAppender.parseInt(portNum, 0);
        return createAppender(host, port, connectTimeoutMillis, name, isFlush,
                ignoreExceptions, layout, filter);
    }

    /**
     * Creates an AbstractSocketManager.
     *
     * @throws IllegalArgumentException if the protocol cannot be handled.
     */
    protected static AbstractSocketManager createSocketManager(final String name, final String host,
                                                               final int port, final int connectTimeoutMillis, final Layout<? extends Serializable> layout) {

        return HTTPSocketManager.getSocketManager(host, port, connectTimeoutMillis, layout);
    }

}
