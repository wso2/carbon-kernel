package org.wso2.carbon.axis2.runtime.util;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CarbonMessageUtil {
    private CarbonMessageUtil() {

    }

    public static CarbonMessage createHttpCarbonResponse(OutputStream outputStream, int code, String contentType) {

        DefaultCarbonMessage response = new DefaultCarbonMessage();
        ByteBuffer responseContent = ByteBuffer.wrap(((ByteArrayOutputStream) outputStream).toByteArray());

        response.addMessageBody(responseContent);

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
        transportHeaders.put(Constants.HTTP_CONTENT_ENCODING, Constants.GZIP);
        if (contentType != null) {
            transportHeaders.put(Constants.HTTP_CONTENT_TYPE, contentType);
        } else {
            transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_PLAIN);
        }

        transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, Integer.toString(responseContent.limit()));

        response.setHeaders(transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, code);
        response.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);

        response.setEomAdded(true);
        return response;
    }

    public static CarbonMessage createHttpCarbonErrorResponse(String errorMessage, int code, String contentType) {

        DefaultCarbonMessage response = new DefaultCarbonMessage();
        String payload = errorMessage;

        if ((contentType != null && (contentType.equals(Constants.TEXT_XML) || contentType.equals
                (Constants.APPLICATION_XML)))) {
            payload = "<errorMessage>" + errorMessage + "</errorMessage>";
        }
        response.setStringMessageBody(payload);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
        transportHeaders.put(Constants.HTTP_CONTENT_ENCODING, Constants.GZIP);
        if (contentType != null) {
            transportHeaders.put(Constants.HTTP_CONTENT_TYPE, contentType);
        } else {
            transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_PLAIN);
        }
        transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, code);
        response.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);

        response.setEomAdded(true);
        return response;
    }
}
