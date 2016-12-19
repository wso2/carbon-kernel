# Using the Maven Bundle Plugin
> The usage of the Maven Bundle plugin for development in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

Carbon 5 Kernel introduced a new version of the [Carbon parent project](https://github.com/wso2/carbon-parent/tree/carbon-parent-2), i.e., carbon-parent-2. This new version includes the Maven Bundle plugin within the Carbon parent project. Therefore, it is no longer necessary to repeat all the configurations relevant to this plugin separately for all other child components. Instead, all the child components that use [carbon-parent-2](https://github.com/wso2/carbon-parent/tree/carbon-parent-2) as the parent project will inherit the common configurations of the plugin from the parent. However, even though the common configurations are inherited by all components, it is necessary to be able to change some configurations for each child component separately. It is possible to do this now, because all the changeable configurations are parameterized in the [pom.xml of the carbon-parent-2](https://github.com/wso2/carbon-parent/blob/carbon-parent-2/pom.xml). Therefore, the child components can freely configure the required parameters.

Shown below is how the Maven Bundle plugin is included in the pom.xml file of [carbon-parent-2](https://github.com/wso2/carbon-parent/blob/carbon-parent-2/pom.xml).

     <plugin>
	<groupId>org.apache.felix</groupId>
		<artifactId>maven-bundle-plugin</artifactId>
		<version>${maven.bundle.plugin.version}</version>
		<extensions>${maven.bundle.plugin.extensions}</extensions>
		<configuration>
			<obrRepository>NONE</obrRepository>
			<instructions>
				<Bundle-Activator>${bundle.activator}</Bundle-Activator>
				<Bundle-ActivationPolicy>${bundle.activation.policy}</Bundle-ActivationPolicy>
				<Bundle-ClassPath>${bundle.classpath}</Bundle-ClassPath>
				<Bundle-Contributors>${bundle.contributors}</Bundle-Contributors>
				<Bundle-Copyright>WSO2 Inc</Bundle-Copyright>
				<Bundle-Description>${project.description}</Bundle-Description>
				<Bundle-Developers>${bundle.developers}</Bundle-Developers>
				<Bundle-DocURL>${bundle.docurl}</Bundle-DocURL>
				<Bundle-License>http://www.apache.org/licenses/LICENSE-2.0.txt</Bundle-License>
				<Bundle-Name>${bundle.name}</Bundle-Name>
				<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
				<Bundle-Vendor>WSO2 Inc</Bundle-Vendor>
				<Conditional-Package>${conditional.package}</Conditional-Package>
				<DynamicImport-Package>${dynamic.import.package}</DynamicImport-Package>
				<Export-Package>${export.package}</Export-Package>
				<Fragment-Host>${fragment.host}</Fragment-Host>
				<Import-Package>${import.package}</Import-Package>
				<Include-Resource>${include.resource}</Include-Resource>
				<Meta-Persistence>${meta.persistence}</Meta-Persistence>
				<Private-Package>${private.package}</Private-Package>
				<Provide-Capability>${provide.capability}</Provide-Capability>
				<Require-Bundle>${require.bundle}</Require-Bundle>
				<Require-Capability>${require.capability}</Require-Capability>
				<Service-Component>${service.component}</Service-Component>
				<Microservices>${microservices}</Microservices>
				<_dsannotations>${dsannotations}</_dsannotations>
			</instructions>
	            </configuration>
            </plugin>

Following is the list of parameters you can use inside a child POM in order to override the configurations inherited from [carbon-parent-2](https://github.com/wso2/carbon-parent/blob/carbon-parent-2/pom.xml).

| Configuration       | Parameter          | Default value  |
| :-----------: |:-------------:| :----:|
| extensions     | maven.bundle.plugin.extensions | true |
| Bundle-Activator      | bundle.activator     |   - |
| Bundle-ActivationPolicy | bundle.activation.policy      |    The only policy defined is the lazy activation policy. If no Bundle-ActivationPolicy header is speci- fied, the bundle will use eager activation. |
| Bundle-ClassPath    | bundle.classpath | - |
| Bundle-Description      | bundle.description     |   - |
| Bundle-Developers    | bundle.developers | WSO2 Inc |
| Bundle-Contributors      | bundle.contributors     |   WSO2 Inc |
| Bundle-DocURL    | bundle.docurl | https://docs.wso2.com |
| Bundle-Name      | bundle.name     |   project.artifactId |
| Conditional-Package    | conditional.package | - |
| DynamicImport-Package     | dynamic.import.package     |   - |
| Export-Package    | export.package | - |
| Fragment-Host      | fragment.host     |   - |
| Import-Package    | import.package | - |
| Include-Resource      | include.resource     |   {maven-resources} |
| Meta-Persistence    | meta.persistence | - |
| Private-Package     | private.package    |   - |
| Provide-Capability   | provide.capability | - |
| Require-Bundle     | require.bundle     |   - |
| Require-Capability    | require.capability | - |
| Service-Component     | service.component    |   - |
| Microservices   | microservices | - |
| _dsannotations     | dsannotations     |   - |

Shown below is a sample `pom.xml` file of a Carbon component, which has changed the default configurations inherited from the parent by using the parameters given above. 

      <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            .........................
                 <!-- This is where you have to include your configuration values for maven-bundle-plugin -->
                      <properties>
	                  <bundle.activator>org.wso2.carbon.kernel.internal.CarbonCoreBundleActivator</bundle.activator>
                          <private.package>org.wso2.carbon.kernel.internal.*,</private.package>
                          <export.package>
                              !org.wso2.carbon.kernel.internal,
                              org.wso2.carbon.kernel.*; version="${carbon.kernel.package.export.version}",
                          </export.package>
                          <import.package>
                              org.eclipse.osgi.util,
                              org.slf4j.*;version="${slf4j.logging.package.import.version.range}",
                              org.osgi.framework.*;version="${osgi.framework.package.import.version.range}",
                              org.eclipse.osgi.framework.console;version="${osgi.framework.console.package.import.version.range}",
                              javax.xml.bind.*;version="${osgi.framework.javax.xml.bind.package.import.version.range}",
                              org.osgi.service.cm.*; version="${osgi.services.cm.package.import.version.range}",
                              org.osgi.service.*;version="${equinox.osgi.services.package.import.version.range}",
                              org.osgi.util.tracker; version="${osgi.service.tracker.package.import.version.range}",
                          </import.package>
                         </properties>
       </project> 
