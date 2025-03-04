package org.wso2.carbon.http.client;

import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class BasicConnectionHttpClientImpl extends HttpClientImpl {
    private static BasicHttpClientConnectionManager basicHttpConnectionManager;

    public BasicConnectionHttpClientImpl() {
        super(getBasicHttpClientConnectionManager());
    }

    private static BasicHttpClientConnectionManager getBasicHttpClientConnectionManager() {
        if (basicHttpConnectionManager == null || basicHttpConnectionManager.isClosed()) {
            // To make thread safe
            synchronized (BasicConnectionHttpClientImpl.class) {
                // Check again as multiple threads can reach above step
                if (basicHttpConnectionManager == null || basicHttpConnectionManager.isClosed()) {
                    // TODO
                    final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
                    final SSLContext sslContext;
                    try {
                        sslContext = SSLContexts.custom()
                                .loadTrustMaterial(null, acceptingTrustStrategy)
                                .build();
                    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                        throw new RuntimeException(e);
                    }
                    final DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
//                    final SSLConnectionSocketFactory sslsf =
//                            new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                    final Registry<DefaultClientTlsStrategy> socketFactoryRegistry =
                            RegistryBuilder.<DefaultClientTlsStrategy>create()
                                    .register("https", tlsStrategy)
                                    .build();

                    basicHttpConnectionManager = new BasicHttpClientConnectionManager();
                }
            }
        }
        return basicHttpConnectionManager;
    }
}
