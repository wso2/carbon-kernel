package org.apache.axis2.builder;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.RequestContext;


/**
 * This class is an implementation of RequestContext to generate a request
 * without the use of a Servlet. An example use of this class is to use the
 * apache commons-fileupload multipart/form-data parsing capabilities without
 * the need of a Servlet request object.
 */
public class RequestContextImpl implements RequestContext {

    private final InputStream inputStream;
    private final String contentType;
    private final String characterEncoding;
    private final int contentLength;

    public RequestContextImpl(InputStream inputStream, String contentType, String characterEncoding, int contentLength) {
       this.contentType = contentType;
       this.inputStream = inputStream;
       this.characterEncoding = characterEncoding;
       this.contentLength = contentLength;
    }

    public String getCharacterEncoding() {
       return this.characterEncoding;
    }

    public String getContentType() {
       return this.contentType;
    }

    public int getContentLength() {
       return this.contentLength;
    }

    public InputStream getInputStream() throws IOException {
       return this.inputStream;
    }

}
