
# Using Maven Archetypes for Development
> This section explains some Maven archetypes that simplify the process of building OSGi bundles/Carbon components. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

* **[Creating a Carbon Component in One Step](#creating-a-carbon-component-in-one-step)**
* **[Creating a Generic OSGi Bundle in One Step](#creating-a-generic-osgi-bundle-in-one-step)**

## Creating a Carbon Component in One Step

A Carbon component that includes a sample implementation of a service component, which consumes an OSGi service registered by Carbon Kernel can be created with one command using the following archetype: `carbon-component-archetype`. The details of the archetype and the details of the project you are creating should be passed as properties when you execute the command. These properties are explained below.

* The following properties are used to specify the details of the archetype:
 
 | Property                | Description                      | Value                                  | Optional/Mandatory  |
 | :---------------------: |:--------------------------------:| :-------------------------------------:| :------------------:|
 | `archetypeGroupId`      | The groupId of the archetype.    | `org.wso2.carbon`                      | Mandatory           |
 | `archetypeArtifactId`   | The artifactId of the archetype. | `org.wso2.carbon.archetypes.component` | Mandatory           |
 | `archetypeVersion`      | The version of the archetype.    | Example: 5.0.0                         | Optional            |
 
* Given below are the properties that will set the project details. You can specify the required values for these properties. However, if these properties are not used, the default values given below will be used to create the project.
 
 | Property              | Description                               | Default Value                       |
 | :-------------------: |:-----------------------------------------:| :----------------------------------:|
 | `groupId`             | The groupId of the project.               | `org.wso2.carbon`                   |
 | `artifactId`          | The artifactId of the project.            | `org.wso2.carbon.serviceconsumer`   |
 | `version`             | The version of the project.               | `1.0.0-SNAPSHOT`                    |
 | `package`             | The package hierarchy for the project.    | `org.wso2.carbon.serviceconsumer`   |
 
> To read more on other properties that you can use when generating a project from an archetype, see this [link](http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html). 

## Example 

Follow the steps given below to see how a Carbon component is generated using this archetype.

1. Execute the following command:

        mvn archetype:generate 
        -DarchetypeGroupId=org.wso2.carbon 
        -DarchetypeArtifactId=org.wso2.carbon.archetypes.component
        -DarchetypeVersion=5.0.0  
        -DgroupId=org.sample 
        -DartifactId=org.sample.project 
        -Dversion=1.0.0 
        -Dpackage=org.sample.project
       
 > In the above command, we have passed values for all the project parameters in additions to the parameters defining the archetype (`org.wso2.carbon.archetypes.component`). If you do not pass any of the project parameters, you will be provided with the option to choose default values for some or all of the variable parameters depending on your choice.
 
2. You will see a result similar to the following, which notifies that the component generation is successful.

         [INFO] project created from Archetype in dir:     /home/manurip/Documents/Work/archetypeGeneration/usingCreatedArchetype/temp/org.sample.project
         [INFO] ------------------------------------------------------------------------
         [INFO] BUILD SUCCESS
         [INFO] ------------------------------------------------------------------------ 
       
3. See that the following project is created:

          org.sample.project 
           ├── pom.xml 
           └── src 
             └── main
              └── java
                └── org
                   └── sample
                      └── project
                        ├── GreeterImpl.java
                        ├── Greeter.java 
                        └── internal
                            ├── DataHolder.java
                            └── ServiceComponent.java
                            
# Creating a Generic OSGi Bundle in One Step

An OSGi bundle can be created with one command using the following archetype: `carbon-bundle-archetype`. The details of the archetype and the details of the project you are creating should be passed as properties when you execute the command. These properties are explained below.

* The following properties are used to specify the details of the archetype:
 
 | Property                | Description                      | Value                                | Optional/Mandatory  |
 | :---------------------: |:--------------------------------:| :-----------------------------------:| :------------------:|
 | `archetypeGroupId`      | The groupId of the archetype.    | `org.wso2.carbon`                    | Mandatory           |
 | `archetypeArtifactId`   | The artifactId of the archetype. | `org.wso2.carbon.archetypes.bundle`  | Mandatory           |
 | `archetypeVersion`      | The version of the archetype.    | Example: 5.0.0                       | Optional            |
 
* Given below are the properties that will set the project details. You can specify the required values for these properties. However, if these properties are not used, the default values given below will be used to create the project.
 
 | Property              | Description                               | Default Value                     |
 | :-------------------: |:-----------------------------------------:| :--------------------------------:|
 | `groupId`             | The groupId of the project.               | `org.wso2.carbon`                 |
 | `artifactId`          | The artifactId of the project.            | `org.wso2.carbon.helloworld`      |
 | `version`             | The version of the project.               | `1.0.0-SNAPSHOT`                  |
 | `package`             | The package hierarchy for the project.    | `org.wso2.carbon.helloworld`      |
 
> To read more on other properties you can use when generating a project from an archetype, see this [link](http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html). 

## Example

Follow the steps given below to see how an OSGi bundle is generated using this archetype.

1. Execute the following command.

        mvn archetype:generate \
        -DarchetypeGroupId=org.wso2.carbon \
        -DarchetypeArtifactId=org.wso2.carbon.archetypes.bundle \
        -DarchetypeVersion=5.0.0 \ 
        -DgroupId=org.sample \ 
        -DartifactId=org.sample.project \
        -Dversion=1.0.0 \
        -Dpackage=org.sample.project \

 In the above command, we have passed values for all the project parameters in addition to the parameters defining the archetype (`org.wso2.carbon.archetypes.bundle`). If you do not pass any of the project parameters, you will have the option to choose default values for some or all of the variable parameters depending on your choice.

2. You will see a result similar to the following, which notifies that the archetype generation is successful. 

        [INFO] project created from Archetype in dir: /home/manurip/Documents/Work/archetypeGeneration/usingCreatedArchetype/temp/org.sample.project
        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESS
        [INFO] ------------------------------------------------------------------------

3. See that the following project is created:
 
        org.sample.project
        ├── pom.xml 
        └── src
          ├── main
          │   └── java
          │       └── org
          │           └── sample 
          │               └── project 
          │                   ├── Greeter.java 
          │                   └── internal 
          │                       └── Activator.java 
          └── test
            └── java
             └── org
                └── sample 
                    └── project
                        └──GreeterTest.java
