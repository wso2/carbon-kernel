
# Using Maven Archetypes

The following sections explain some Maven archetypes that will simplify the process of building OSGi bundles/Carbon components:

## Creating a Carbon Component in One Step

A Carbon component that includes a sample implementation of a service component, which consumes an OSGi service registered by Carbon Kernel can be created with one command using the following archetype: carbon-component-archetype. The details of the archetype and the details of the project you are creating should be passed as properties when you execute the command. These properties are explained below.

 *The following properties are used to specify the details of the archetype:*
 
 .......
 
 *Given below are the properties that will set the project details. You can specify the required values for these properties. However, if these properties are not used, the default values given below will be used to create the project.*
 
 .......
 
To read more on other properties that you can use when generating a project from an archetype, see this link. 

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
       
 In the above command, we have passed values for all the project parameters in additions to the parameters defining the archetype (org.wso2.carbon.archetypes.component). If you do not pass any of the project parameters, you will be provided with the option to choose default values for some or all of the variable parameters depending on your choice.
 
2. You will see a result similar to the following, which notifies that the component generation is successful.

         [INFO] project created from Archetype in dir:        /home/manurip/Documents/Work/archetypeGeneration/usingCreatedArchetype/temp/org.sample.project
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
