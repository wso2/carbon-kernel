package org.wso2.carbon.http.socket.appender;


import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.SerializedLayout;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An Appender that delivers events over socket connections. Supports HTTP.
 */
@Plugin(name = "HTTP", category = "Core", elementType = "appender", printObject = true)
public class HTTPAppender extends AbstractAppender {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private String host = "localhost";
    private int port = 8080;

    protected HTTPAppender(String name,
                           org.apache.logging.log4j.core.Filter filter, Layout<? extends Serializable> layout, final boolean ignoreExceptions, String host, int port) {
        super(name, filter, layout, ignoreExceptions);
        this.host = host;
        this.port = port;
    }

    // The append method is where the appender does the work.
    // Given a log event, you are free to do with it what you want.
    // This example demonstrates:
    // 1. Concurrency: this method may be called by multiple threads concurrently
    // 2. How to use layouts
    // 3. Error handling
    @Override
    public void append(LogEvent event) {
        OutputStream outputStream = null;
        HttpURLConnection httpCon = null;
        try {
            URL url = new URL("http://" + host + ":" + port);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("POST");
            synchronized (this) {
                outputStream = httpCon.getOutputStream();
                outputStream.write(getLayout().toByteArray(event));
                int responseCode = httpCon.getResponseCode();
                outputStream.flush();
                outputStream.close();
                httpCon.disconnect();
            }

        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpCon != null) {
                httpCon.disconnect();
            }

        }
    }

    // Your custom appender needs to declare a factory method
    // annotated with `@PluginFactory`. Log4j will parse the configuration
    // and call this factory method to construct an appender instance with
    // the configured attributes.
    @PluginFactory
    public static HTTPAppender createAppender(
            // @formatter:off
            @PluginAttribute("host") final String host,
            @PluginAttribute(value = "port", defaultInt = 0) final int port,
            @PluginAttribute(value = "connectTimeoutMillis", defaultInt = 0) final int connectTimeoutMillis,
            @PluginAttribute("name") final String name,
            @PluginAttribute(value = "immediateFlush", defaultBoolean = true) boolean immediateFlush,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final org.apache.logging.log4j.core.Filter filter) {
        // @formatter:on

        if (layout == null) {
            layout = SerializedLayout.createLayout();
        }

        if (name == null) {
            LOGGER.error("No name provided for HTTPAppender");
            return null;
        }
        immediateFlush = true;

        return new HTTPAppender(name, filter, layout, ignoreExceptions, host, port);
    }

}
