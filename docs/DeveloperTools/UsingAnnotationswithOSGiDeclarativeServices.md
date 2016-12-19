# Using Annotations with OSGi Declarative Services
> The process of using annotations with OSGi declarative services when developing a WSO2 product is explained below. For the full list of capabilities available in Carbon Kernel, see the [root README.md file](../../README.md). 

The OSGi compendium specification has now standardized the annotation usage for declarative services. The core services bundle (org.eclipse.osgi.services) includes the set of classes and packages that can be used for this purpose. The specification clearly mentions that there should be some tools that are capable of processing these annotation and generating the declarative services component descriptor files. The Apache Felix project has developed these tools (maven dependencies and plugins), which can process the standard annotations and generate the descriptor meta files for you. A point to note here is that the Apache Felix project also has its own annotations based on the `org.apache.felix.scr.annotations` dependency, which has similar names as those mentioned in the specification. The standard annotations specified by the compendium specification are supported by the `org.apache.felix.scr.ds-annotations` felix project
.
The following content will focus on the standard annotations and explains how they can be used within your project:

## Required dependencies

The main dependencies and plugins that you will be needing are as follows:

```
<dependency>
     <groupId>org.eclipse.osgi</groupId>
     <artifactId>org.eclipse.osgi.services</artifactId>
</dependency>

<dependency>
     <groupId>org.apache.felix</groupId>
     <artifactId>org.apache.felix.scr.ds-annotations</artifactId>
</dependency>

<plugin>
     <groupId>org.apache.felix</groupId>
     <artifactId>maven-scr-plugin</artifactId>
     <version>1.16.0</version>
     <executions>
          <execution>
               <id>generate-scr-scrdescriptor</id>
               <goals>
                    <goal>scr</goal>
               </goals>
          </execution>
     </executions>
</plugin>
```

Alternatively, by using the maven-bundle-plugin, you can use the “_dsannotations” instruction to process the annotated, declarative service components and to generate the component descriptors. An example of using maven-bundle-plugin for this purpose is given below.

```
<plugin>
     <groupId>org.apache.felix</groupId>
     <artifactId>maven-bundle-plugin</artifactId>
     <version>2.4.0</version>
     <extensions>true</extensions>
     <configuration>
          <instructions>
               <Bundle-Vendor>WSO2 Inc</Bundle-Vendor>
               <Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
               <Export-Package>
                    org.wso2.carbon.deployment.*;version="5.0.0"
               </Export-Package>
               <Import-Package>
                    org.slf4j.*;version="${slf4j.logging.import.version.range}",
                    org.wso2.carbon.kernel.*;version="${carbon.kernel.package.import.version.range}",
                    org.osgi.framework.*;version="${osgi.framework.import.version.range}"
               </Import-Package>
               <_dsannotations>*</_dsannotations>
          </instructions>
     </configuration>
</plugin>
```

The available annotation classes are explained below (based on the OSGi compendium specification).

* **Activate:** Identifies the annotated method as the active method of a Service component.
* **Component:** Identifies the annotated class as a Service component.
* **ConfigurationPolicy:** Configuration policy for the component annotation.
* **Deactivate:** Identifies the annotated method as the deactivate method of a Service component.
* **Modified:** Identifies the annotated method as the modified method of a Service component.
* **Reference:** Identifies the annotated method as a bind method of a Service component.
* **ReferenceCardinality:** Cardinality for the reference annotation.
* **ReferencePolicy:** Policy for the reference annotation.
* **ReferencePolicyOption:** Policy option for the reference annotation.

## Sample service component using annotations

Shown below is a sample service component class that uses the standard annotations.

```
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.servlet.SampleServlet;

import javax.servlet.ServletException;

/**
 * This service  component is responsible for retrieving the HttpService
 * OSGi service and register servlets
 */

@Component(
        name = "org.wso2.carbon.transport.HttpServiceComponent",
        immediate = true
)
public class HttpServiceComponent {

    private static final Logger logger = LoggerFactory.getLogger(HttpServiceComponent.class);

    private HttpService httpService;

    @Activate
    protected void start() {
        SampleServlet servlet = new SampleServlet();
        String context = "/sample";
        try {
            logger.info("Registeringmple servlet : {}", context);
            httpService.registerServlet(context, servlet, null,
                                        httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            logger.error("Errore registering servlet", e);
        }
    }


    @Reference(
            name = "http.service",
            service = HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC,
            unbind = "unsetHttpService"
    )
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}
```
                                  
With the usage of Maven dependencies and the plugin given in the 'Usage' section above, the component descriptor will be generated as shown below. Also, if a component class is an implementation class of a service that requires to be registered as a service, the @Component annotation will automatically populate the component descriptor with that information.

```
<components xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0">
     <scr:component immediate="true" name="org.wso2.carbon.transport.HttpServiceComponent" activate="start">
          <implementation class="org.wso2.carbon.transport.internal.HttpServiceComponent"/>
          <reference name="http.service" interface="org.osgi.service.http.HttpService" cardinality="1..1" policy="static" bind="setHttpService" unbind="unsetHttpService"/>
     </scr:component>
</components>
```
                                  
## Descriptions of annotations
                                  
Given below are the descriptions of annotations.
                                  
* **@Component:**
  This annotation identifies the annotated class as a service component.
   ```
    @Component(
        name = "ClusteringAgentServiceComponent",
        immediate = true,
        property = "Agent=hazelcast")
    ```
    The component annotation can take multiple parameters as shown in the above example. These will be available for that component at run time. For example, if your component needs to register the component or service level properties, it can be done by using the “property” parameter and with one or many “key=value” pairs.

* **@Reference:**
  The @Reference annotation should be applied to a method that will be used as a “bind” method of a service component. You can refer the sample class above. The “unbind” parameter specifies the unbind method in the service component, along with other parameters that are required by the reference.

    ```
    @Reference(
            name = "http.service",
            service = HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC,
            unbind = "unsetHttpService")
     ```

* **@Activate, @Deactivate, @Modified:**
  These three annotations are used with the respective methods that will be called when the status of a service component changes from one to another. For example, the @Activate annotated method gets invoked when the service component becomes satisfied with all the service references and their requirements.
