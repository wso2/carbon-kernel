package org.wso2.carbon.p2.touchpoint.actions;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.p2.touchpoint.TouchpointActionConstants;
import org.wso2.carbon.p2.touchpoint.Utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;

public class AddXMLElementAction extends ProvisioningAction{

    private static final String ACTION_ADD_ELEMENT = "addXMLElement";

    @Override
    public IStatus execute(Map<String, Object> parameters) {
        String targetXML = (String) parameters.get(TouchpointActionConstants.PARM_FILE);
        if (targetXML == null) {
            return Utils.createError(TouchpointActionConstants.PARM_FILE + " parameter is not set ");
        } else {
            String xPathQuery =  (String) parameters.get(TouchpointActionConstants.PARM_XPATH_EXPRESSION);
            if (xPathQuery == null) {
                return Utils.createError(TouchpointActionConstants.PARM_XPATH_EXPRESSION + " parameter is not set ");
            }
            String elementToAdd = (String) parameters.get(TouchpointActionConstants.PARM_XML_ELEMENT);
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                File file = new File(targetXML);
                DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                Document document = documentBuilder.parse(file);
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expr = xpath.compile(xPathQuery);
                Object result = expr.evaluate(document, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;

                Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new ByteArrayInputStream(elementToAdd.getBytes()))
                        .getDocumentElement();
                Element imported = (Element) document.importNode(node, Boolean.TRUE);
                nodes.item(0).appendChild(imported);


                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(new DOMSource(document), new StreamResult(file));

            } catch (Exception e) {
                return Utils.createError("Error while executing AddXMLElementAction touchpoint ", e);
            }

        }

        return Status.OK_STATUS;
    }

    @Override
    public IStatus undo(Map<String, Object> parameters) {
        return Status.OK_STATUS;
    }
}
