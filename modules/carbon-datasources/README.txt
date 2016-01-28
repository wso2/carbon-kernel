# Carbon data sources

### Introduction

This is a POC conducted to read datasource configuration files and bind these data sources through jndi so these data sources can be used by other OSGi bundles. For the moment this support only file based JNDI service.


### Usage

- In this maven project, 2 maven modules can be found.
- org.wso2.carbon.datasource.core module will create an OSGi bundle which will read the datasource configuration files and bind through jndi.
- feature module will encapsulate org.wso2.carbon.datasource.core.jar and it's dependencies and create a feature so it is installable into carbon-kernel.

Dependent jar files
- commons-io
- jdbc-pool

- Once the feature is installed, create a folder named 'datasources' in your 'conf' folder carbon-kernel distribution. Copy the master-datasources.xml file found in 'org.wso2.carbon.datasource.core/src/main/resources' folder into datasources directory you just created. This master-datasources.xml file is a dummy configuration file. Thus you should update before use this

- In addition you can place any data source configuration file have it's file name ends with '-datasources.xml'. This is the convention used in previous carbon versions.

- Place the relavent jdbc driver jar in the dropins folder of the carbon-kernel distribution. Make sure dynamic import is enabled in the driver jar.

After completing aforementioned activities, start C5 kernel by running carbon.sh or carbon.bat.

- A Runner.java class is provided to test this outside the OSGi environment. Make sure you update master-datasources.xml before running this class.

Important

In order for this to work well in carbon-kernel, carbon-jndi is also needs to be in place. So do the configuration found in carbon-jndi/ReadMe.

