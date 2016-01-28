# Carbon data sources

### Introduction

This is a POC conducted to read datasource configuration files and bind these data sources through jndi so these data sources can be used by other OSGi bundles. For the moment this support only file based JNDI service.


### Usage

- In this maven project, 3 maven modules can be found.
- org.wso2.carbon.datasource.core module will create an OSGi bundle which will read the datasource configuration files and bind through jndi.
- feature module will encapsulate org.wso2.carbon.datasource.core.jar and it's dependencies and create a feature so it is installable into carbon-kernel.
- distribution module, which will download carbon kernel 5.0.0, install the feature and zip it with the name 'carbon-datasource-1.0.0-SNAPSHOT.zip'
- Extracted location of the aforementioned zip file is considered as CARBON_HOME for this readme.
- data source configuration file can be found in CARBON_HOME/conf/datasources directory. Update the master-datasources.xml file to suite your requirement.
- In addition you can place any data source configuration file in CARBON_HOME/conf/datasources directory having it's file name ends with '-datasources.xml'. This is the convention used in previous carbon versions.
- Place the required jdbc driver jar in the CARBON_HOME/osgi/dropins folder. Make sure dynamic import is enabled in the driver jar.

Important

This project has a dependency with carbon-jndi. So for this to work well, build carbon-jndi and place the carbon-jndi-1.0.0-SNAPSHOT.jar in CARBON_HOME/osgi/dropins.


After completing aforementioned activities, start C5 kernel by running carbon.sh or carbon.bat.

- A Runner.java class is provided to test this outside the OSGi environment. Make sure you update master-datasources.xml before running this class.
