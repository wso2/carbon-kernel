package org.apache.axis2.datasource.jaxb;

import javax.xml.namespace.NamespaceContext;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


public class OMStreamNamespaceContext implements NamespaceContext {

    private Map namespaceToPrefixMap;
    private Map prefixToNamespaceMap;

    public OMStreamNamespaceContext() {
        this.namespaceToPrefixMap = new HashMap();
        this.prefixToNamespaceMap = new HashMap();
    }

    public void registerNamespace(String namespace, String prefix) {
        this.namespaceToPrefixMap.put(namespace, prefix);
        this.prefixToNamespaceMap.put(prefix, namespace);
    }

    public String getNamespaceURI(String prefix) {
        return (String) prefixToNamespaceMap.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return (String) namespaceToPrefixMap.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        return prefixToNamespaceMap.keySet().iterator();
    }
}
