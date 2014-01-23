package org.apache.axis2.jaxws.respectbinding;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.Service;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.common.config.WSDLValidatorElement;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.DescriptionFactory.UpdateType;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.RespectBindingAnnot;
import org.apache.axis2.jaxws.feature.ServerFramework;
import org.apache.axis2.jaxws.server.config.RespectBindingConfigurator;
import org.apache.axis2.jaxws.spi.ServiceDelegate;



public class WSDLBindingsTest extends TestCase{
    private final static String namespaceURI="http://RespectBinding.jaxws22";
    private final static String serviceName="AddNumbersService";
    private final static String portName = "AddNumbersPort";
    private final static String wsdl = "RespectBinding.wsdl";

    private final static String policySample = "wspolicySample.wsdl";
    private final static String policySampleURI="http://www.example.com/stock/binding";
    private final static String policyServiceName="StockQuoteService";
    private final static String policyPortName = "Quote";

    /**
     * This test does the following
     * 1) Defines RespectBindingAnnotation with enabled=true
     * 2) Fakes creation of an EndpointDescription with RespectBinding
     * 3) Invokes RespectBindingConfigurator.
     * 4) Checks for all Extensibility Element definition in wsdl:binding .
     * 5) fails if it does not find expected elements in bindings.
     */
    
    public void testExtenisbilityElementAtBinding() throws Exception{
        QName serviceQName = new QName(namespaceURI, serviceName);
        URL wsdlUrl = getWsdlURL(wsdl);
        assertNotNull(wsdl);

        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();

        Map<String, List<Annotation>> map = new HashMap<String, List<Annotation>>();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();

        RespectBindingAnnot wsFeature = new RespectBindingAnnot();
        // Define RespectBinding and set as enabled
        wsFeature.setEnabled(true);
        wsFeatures.add(wsFeature);

        map.put(AddNumbersPortTypeSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);       
        ServiceDelegate.setServiceMetadata(serviceDBC);
        try{
            //create service with wsdlurl.
            Service service = Service.create(wsdlUrl, serviceQName);
            assertNotNull("Service is null",service);

            //Fetch Service Delegate so we can read EndpointDescription.
            ServiceDelegate sd = getDelegate(service);
            assertNotNull("ServiceDelegate is null", sd);

            ServiceDescription serviceDesc = sd.getServiceDescription();
            assertNotNull("ServiceDescription is null", serviceDesc);

            //Create EndpointDescription
            DescriptionFactory.updateEndpoint(serviceDesc, AddNumbersPortTypeSEI.class, new QName(namespaceURI, portName), UpdateType.GET_PORT);

            //Read EndpointDescription
            EndpointDescription ed = serviceDesc.getEndpointDescription(new QName(namespaceURI, portName));
            assertNotNull("EndpointDescription is null", ed);

            //Use RespectBindingConfigurator to read extensibility element in wsdlBindings.
            RespectBindingConfigurator rbc = new RespectBindingConfigurator();
            //Mock Object Server Framework.
            ServerFramework sf = new ServerFramework();
            Annotation a = wsFeature;
            sf.addConfigurator(RespectBindingFeature.ID, rbc);
            sf.addAnnotation(a);
            //lets hang the RespectBinding annotation to the EndpointDefinition
            addAnnotation(ed, sf);           
            rbc.configure(ed);
            Set<WSDLValidatorElement> elements = ed.getRequiredBindings();
            assertNotNull("Set of WSDLValidatorElement was null", elements);
            assertEquals("Expecting 5 Extension elements from wsdl in Set of WSDLValidatorElements but found "+elements.size(),elements.size(), 5);

        }catch(Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
     
    public void testPolicySetSample() throws Exception{
        QName serviceQName = new QName(policySampleURI, policyServiceName);
        URL wsdlUrl = getWsdlURL(policySample);
        assertNotNull(policySample);

       DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();

        Map<String, List<Annotation>> map = new HashMap<String, List<Annotation>>();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();

        RespectBindingAnnot wsFeature = new RespectBindingAnnot();
        // Define RespectBinding and set as enabled
        wsFeature.setEnabled(true);
        wsFeatures.add(wsFeature);
        
        map.put(StockQuoteSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);       
        ServiceDelegate.setServiceMetadata(serviceDBC);
        try{
            //create service with wsdlurl.
            Service service = Service.create(wsdlUrl, serviceQName);
            assertNotNull("Service is null",service);

            //Fetch Service Delegate so we can read EndpointDescription.
            ServiceDelegate sd = getDelegate(service);
            assertNotNull("ServiceDelegate is null", sd);
            
            ServiceDescription serviceDesc = sd.getServiceDescription();
            assertNotNull("ServiceDescription is null", serviceDesc);
            
            QName name = new QName(policySampleURI, policyPortName);
            //Create EndpointDescription
            DescriptionFactory.updateEndpoint(serviceDesc, StockQuoteSEI.class, name, UpdateType.GET_PORT);

            //Read EndpointDescription
            EndpointDescription ed = serviceDesc.getEndpointDescription(name);
            assertNotNull("EndpointDescription is null", ed);

            //Use RespectBindingConfigurator to read extensibility element in wsdlBindings.
            RespectBindingConfigurator rbc = new RespectBindingConfigurator();
            //Mock Object Server Framework.
            ServerFramework sf = new ServerFramework();
            Annotation a = wsFeature;
            sf.addConfigurator(RespectBindingFeature.ID, rbc);
            sf.addAnnotation(a);
            //lets hang the RespectBinding annotation to the EndpointDefinition
            addAnnotation(ed, sf);           
            rbc.configure(ed);
            Set<WSDLValidatorElement> elements = ed.getRequiredBindings();
            assertNotNull("Set of WSDLValidatorElement was null", elements);
            assertEquals("Expecting 4 Extension elements from wsdl in Set of WSDLValidatorElements but found "+elements.size(),elements.size(), 4);
        }catch(Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    private void addAnnotation(EndpointDescription ed, ServerFramework sf){
        try {
            try {
                Field framework = ed.getClass().getDeclaredField("framework");
                framework.setAccessible(true);
                framework.set(ed, sf);
            } catch (NoSuchFieldException e) {
                // This may be a generated service subclass, so get the delegate from the superclass
                Field framework = ed.getClass().getSuperclass().getDeclaredField("framework");
                framework.setAccessible(true);
                framework.set(ed, sf);
            }
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private ServiceDelegate getDelegate(Service service){
        // Need to get to the private Service._delegate
        ServiceDelegate returnServiceDelegate = null;
        try {
            try {
                Field serviceDelgateField = service.getClass().getDeclaredField("delegate");
                serviceDelgateField.setAccessible(true);
                returnServiceDelegate = (ServiceDelegate) serviceDelgateField.get(service);
            } catch (NoSuchFieldException e) {
                // This may be a generated service subclass, so get the delegate from the superclass
                Field serviceDelegateField = service.getClass().getSuperclass().getDeclaredField("delegate");
                serviceDelegateField.setAccessible(true);
                returnServiceDelegate = (ServiceDelegate) serviceDelegateField.get(service);
            } 
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return returnServiceDelegate;
    }

    private URL getWsdlURL(String wsdlFileName) {
        URL url = null;
        String wsdlLocation = getWsdlLocation(wsdlFileName);
        try {
            File file = new File(wsdlLocation);
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("Exception converting WSDL file to URL: " + e.toString());
        }
        return url;
    }

    static String getWsdlLocation(String wsdlFileName) {
        String wsdlLocation = null;
        String baseDir = System.getProperty("basedir",".");
        wsdlLocation = baseDir + "/test-resources/wsdl/" + wsdlFileName;
        return wsdlLocation;
    }


    @WebService(name="AddNumbersPortType", targetNamespace="http://RespectBinding.jaxws22")
    interface AddNumbersPortTypeSEI {
        public int sum(int num1, int num2);
    }

    @WebService(name="StockQuotePortType", targetNamespace="http://www.example.com/stock/binding")
    interface StockQuoteSEI {
        public int GetLastTradePrice(String symbol);
    }
}
