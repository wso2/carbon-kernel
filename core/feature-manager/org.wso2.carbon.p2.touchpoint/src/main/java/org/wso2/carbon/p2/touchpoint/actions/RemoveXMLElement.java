package org.wso2.carbon.p2.touchpoint.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wso2.carbon.p2.touchpoint.TouchpointActionConstants;
import org.wso2.carbon.p2.touchpoint.Utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Map;

public class RemoveXMLElement extends ProvisioningAction{
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
            try {
                File file = new File(targetXML);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                Document document = dbf.newDocumentBuilder().parse(file);

                XPathFactory xpf = XPathFactory.newInstance();
                XPath xpath = xpf.newXPath();
                XPathExpression expression = xpath.compile(xPathQuery);

                Node nodeToDel = (Node) expression.evaluate(document, XPathConstants.NODE);
                if (nodeToDel == null) {
                    return Utils.createError("Given xpath expression doesn't refer to a node, xpath: " + xPathQuery);
                }
                nodeToDel.getParentNode().removeChild(nodeToDel);

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                t.transform(new DOMSource(document), new StreamResult(file));

            } catch (Exception e) {
                return Utils.createError("Error while executing RemoveXMLElement touchpoint ", e);
            }
        }
        return Status.OK_STATUS;
    }

    @Override
    public IStatus undo(Map<String, Object> stringObjectMap) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
