# Using the global configuration model

WSO2 Carbon 5 introduces a new configuration deployment model, which allows products to maintain all the server configurations in one configuration file. This global configuration file is named deployment.yaml and is stored in the <PRODUCT_HOME>/conf directory of your product pack. By default, the following minimal configuration elements are specified in the deployment.yaml file of a Carbon product.

```
 # Carbon Configuration Parameters
wso2.carbon:
   # value to uniquely identify a server
 id: carbon-kernel
   # server name
 name: WSO2 Carbon Kernel
   # ports used by this server
 ports:
     # port offset
   offset: 0
   ```

> Note the following changes introduced in Carbon Kernel 5.2.0:
 * The new deployment.yaml file contains all the Carbon runtime configurations and it replaces the carbon.yaml file that existed previously.
 * Three new annotations (Configuration, Element, Ignore) for configuration bean classes.
 * New annotation processor (ConfigurationProcessor) to discover configuration bean classes in the component.
 * Maven plugin (ConfigDocumentMojo) to create the configuration file by reading the configuration bean classes.
 * An OSGI service (ConfigProvider) to provide the relevant object for the given bean class.

## Step 1: Adding configurations to a Carbon component

When you develop a Carbon component, you do not need to bundle separate configuration files with the feature. Instead, the required user configurations should be defined as one or more Java beans annotated with the following three annotations:

 * `org.wso2.carbon.kernel.annotations.Configuration` : This is a class-level annotation, which corresponds to a configuration bean to be used by a component.
 * `org.wso2.carbon.kernel.annotations.Element` : This is a field-level annotation, which corresponds to a field of the class.
 * `org.wso2.carbon.kernel.annotations.Ignore` : This is a field level annotation, which specifies that the field needs to be ignored when the configuration is generated.
 
To create the configuration document for your component, create a Bean class with default values annotated with provided annotations. See the following example:

```
@Configuration(namespace = "wso2.carbon", description = "Carbon Configuration Parameters")
public class CarbonConfiguration {

   public CarbonConfiguration() {
       // Reads the {@value Constants#PROJECT_DEFAULTS_PROPERTY_FILE} property file and assign project version.
       Properties properties = ConfigurationUtils.loadProjectProperties();
       version = properties.getProperty(Constants.MAVEN_PROJECT_VERSION);
   }

   @Element(description = "value to uniquely identify a server")
   private String id = "carbon-kernel";

   @Element(description = "server name")
   private String name = "WSO2 Carbon Kernel";

   @Ignore
   private String version;

   private String tenant = Constants.DEFAULT_TENANT;

   @Element(description = "ports used by this server")
   private PortsConfig ports = new PortsConfig();

   @Element(description = "StartupOrderResolver related configurations")
   private StartupResolverConfig startupResolver = new StartupResolverConfig();

   @Element(description = "JMX Configuration")
   private JMXConfiguration jmx = new JMXConfiguration();

   public String getId() {
       return id;
   }

  …..
}
```

## Step 2: Getting the configuration bean object at runtime

1. Add Reference to the ConfigProvider OSGI service as below.
 ```
 /**
* Get the ConfigProvider service.
* This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
*
* @param configProvider the ConfigProvider service that is registered as a service.
*/
@Reference(
      name = "carbon.config.provider",
      service = ConfigProvider.class,
      cardinality = ReferenceCardinality.MANDATORY,
      policy = ReferencePolicy.DYNAMIC,
      unbind = "unregisterConfigProvider"
)
protected void registerConfigProvider(ConfigProvider configProvider) {
  DataHolder.getInstance().setConfigProvider(configProvider);
}

/**
* This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
*
* @param configProvider the ConfigProvider service that get unregistered.
*/
protected void unregisterConfigProvider(ConfigProvider configProvider) {
  DataHolder.getInstance().setConfigProvider(null);
}
```

2. Get the particular bean object by calling the api getConfigurationObject(Class<T> configClass) with bean class as below. This will return configuration object of the class with overriding the values of deployment.yaml. If configuration doesn't exist in deployment.yaml, returns object with default values.

  ```
  <Bean> bean = DataHolder.getInstance().getConfigProvider().getConfigurationObject(<Bean>.class);
```

3. Get the particular configuration map of the namespace in deployment.yaml file by calling getConfigurationMap(String namespace) with namespace as below. This will return the configuration map of the namespace, if configuration exists for the given namespace in deployment.yaml.

 ```
 Map map = DataHolder.getInstance().getConfigProvider().getConfigurationMap(<namespace>);
 ```
 
## Step 3: Building the Carbon feature

1. Add carbon core dependency to the component pom file. This is to get the custom annotations defined in carbon.core.
 ```
 <dependencies>
 …
   <dependency>
      <groupId>org.wso2.carbon</groupId>
      <artifactId>org.wso2.carbon.core</artifactId>
   </dependency>
…
</dependencies> 
```

2. Add maven plugin to the component pom file. This is to create configuration document by reading the configuration bean classes.
 ```
 <build>
  <plugins>
…
      <plugin>
          <groupId>org.wso2.carbon</groupId>
          <artifactId>org.wso2.carbon.plugins.configuration</artifactId>
          <executions>
              <execution>
                  <goals>
                      <goal>create-doc</goal>
                  </goals>
                  <phase>compile</phase>
              </execution>
          </executions>
      </plugin>
…
  </plugins>
</build>
```

3. Build the component using mvn clean install  and if every done correctly configuration document file(<config-namespace-value>.yaml) will create automatically inside `<CLASS_OUTPUT_DIRECTORY>/config-docs` directory.
4. Add maven dependency plugin to the feature pom file to copy the config-doc file to the feature. When feature builds, configuration document file(<config-namespace-value>.yaml) will copy to config-docs directory.  
 ```
 <plugins>
…
  <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <executions>
…
          <execution>
              <id>unpack</id>
              <phase>package</phase>
              <goals>
                  <goal>unpack</goal>
              </goals>
              <configuration>
                  <artifactItems>
                      <artifactItem>
                          <groupId><-- component-group-id --></groupId>
                          <artifactId><-- component-artifact-id --></artifactId>
                          <version><-- component-version --></version>
                          <type>bundle</type>
                          <overWrite>true</overWrite>
                          <outputDirectory>${project.build.directory}/docs</outputDirectory>
                          <includes>config-docs/**</includes>
                      </artifactItem>
                  </artifactItems>
              </configuration>
          </execution>
…
      </executions>
  </plugin>
…
</plugins> 
```

5. And add resource as below.
  ```
  
<build>
…
<resources>
…
  <resource>
      <directory>${project.build.directory}/docs/</directory>
  </resource>
…
</resources>
…
</build> 
```

Sample Configuration Doc file looks like, this is generated for CarbonConfiguration class. If you need to override default value, you need to copy this configs to deployment.yaml.

```
################################################################################
#   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved
#
#   Licensed under the Apache License, Version 2.0 (the \"License\");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an \"AS IS\" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
################################################################################
 # Carbon Configuration Parameters
wso2.carbon:
   # value to uniquely identify a server
 id: carbon-kernel
   # server name
 name: WSO2 Carbon Kernel
 tenant: default
   # ports used by this server
 ports:
     # port offset
   offset: 0
   # StartupOrderResolver related configurations
 startupResolver:
   capabilityListenerTimer:
       # delay in milliseconds before task is to be executed
     delay: 20
       # time in milliseconds between successive task executions
     period: 20
   pendingCapabilityTimer:
       # delay in milliseconds before task is to be executed
     delay: 60000
       # time in milliseconds between successive task executions
     period: 30000
   # JMX Configuration
 jmx:
     # To enable JMX Monitoring, change this value to true
   enabled: false
     # Server HostName
   hostName: 127.0.0.1
     # The port RMI server should be exposed
   rmiServerPort: 11111
     # The port RMI registry is exposed
   rmiRegistryPort: 9999
   ```

## Step 4: Updating the default configurations

If you need to override the default values, you need to copy  the configuration segment to the deployment.yaml file and change the value. Server will pick the new configs from the deployment.yaml after restarting the server. Added minimal set of configuration which might need change at runtime. If you need to change the update interval of the carbon deployment engine to 20, you need to copy the configuration segment to the deployment.yaml. Carbon deployment configuration documentation looks like (full configuration).

From above config, copy updateInterval configurations to deployment.yaml and change the value to 20, as below.

```
# Deployment Engine related configurations
wso2.deployment:
   # Deployment update interval in seconds. This is the interval between repository listener
   # executions.
 updateInterval: 20
 ```

Updated deployment.yaml looks like below.

```
# Carbon Configuration Parameters
wso2.carbon:
   # value to uniquely identify a server
 id: carbon-kernel
   # server name
 name: WSO2 Carbon Kernel
   # ports used by this server
 ports:
     # port offset
   offset: 0

# Deployment Engine related configurations
wso2.deployment:
   # Deployment update interval in seconds. This is the interval between repository listener
   # executions.
 updateInterval: 20
 ```
