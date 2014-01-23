package org.apache.axis2.jaxws.jaxb.string;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(serviceName = "JAXBStringService", endpointInterface = "org.apache.axis2.jaxws.jaxb.string.JAXBStringPortType")
public class JAXBStringPortTypeImpl implements JAXBStringPortType {
    public EchoResponse echoString(@WebParam(name = "echo", targetNamespace = "http://string.jaxb.jaxws.axis2.apache.org", partName = "echo") Echo echo) {
        EchoResponse response = new EchoResponse();
        response.setResponse(echo.getArg());
        return response;
    }
}

