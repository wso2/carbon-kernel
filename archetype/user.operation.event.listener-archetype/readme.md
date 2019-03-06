##IS-USER-OPERATION-EVENT-LISTENER-ARCHETYPE

Follow the steps to create the IS-User-Operation-Event-Listener

* Run the following command in `<parent>` or `<parent>/<archetype>`
 or `<parent>/<archetype>/<carbon-is-user-event-listener-archetype>`
 
        mvn clean install

* Now the archetype is added to your local maven repository and you can build
project using the archetype by following command

        mvn archetype:generate
                -DarchetypeGroupId=org.wso2.carbon
                -DarchetypeArtifactId=org.wso2.carbon.user.core.listener-user.operation.event.listener-archetype
                -DarchetypeVersion=<kernel_version>
                -DgroupId=<your_groupID>
                -DartifactId=<your_artifact_id>
                -Dversion=<your_verion>
            
*After making the changes,build your project using `mvn clean install` and 
put the generated jar into `<is server>/repository/components/dropins`  