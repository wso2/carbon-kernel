package org.apache.axis2.description;

import javax.xml.namespace.QName;

/**
 * This class is used to encapsulate WSDL operations information. It's used by
 * other data-driven JUnit Test Classes that deal with <code>AxisOperation</code>s.
 */
class OperationReference {
    
    private String wsdlPath;
    private QName serviceName;
    private String portName;
    private QName operationName;
    
    public OperationReference() { }

    public OperationReference(String wsdlPath, 
                              QName serviceName, 
                              String portName,
                              QName operationName) {
        this.wsdlPath = wsdlPath;
        this.serviceName = serviceName;
        this.portName = portName;
        this.operationName = operationName;
    }

    public String getWsdlPath() {
        return wsdlPath;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }

    public QName getOperationName() {
        return operationName;
    }

    public void setWsdlPath(String wsdlPath_) {
        wsdlPath = wsdlPath_;
    }

    public void setServiceName(QName serviceName_) {
        serviceName = serviceName_;
    }

    public void setPortName(String portName_) {
        portName = portName_;
    }

    public void setOperationName(QName operationName_) {
        operationName = operationName_;
    }

    public String toString() {
        return "serviceName:" + serviceName + "; portName:" + portName +
            ";  operationName:" + operationName;
    }

}
