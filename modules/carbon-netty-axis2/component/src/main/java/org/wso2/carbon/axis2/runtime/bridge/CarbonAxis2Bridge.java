package org.wso2.carbon.axis2.runtime.bridge;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is CarbonAxis2Bridge.
 *
 * @since 1.0.0
 */
public interface CarbonAxis2Bridge {
    public ResponseStatus processMessage(InputStream inputStream,
                                         OutputStream outputStream,
                                         String contentType,
                                         String soapActionHeader,
                                         String requestUri) throws CarbonAxis2BridgeException;
}
