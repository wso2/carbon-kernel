From Carbon Kernel 5.0.0 onwards, the Pax Exam framework provides the infrastructure for composing unit test cases. See the following topics for details:

### How Pax Exam works

Pax Exam starts up the OSGi framework (which is equinox for the Carbon kernel) with the minimum level of bundles that are necessary for the pax-exam operations. Then the Pax Exam user can provision the bundles to the OSGi container. When the Pax Exam boots up, these provisioned bundles will be installed in the container. The list of bundles that are provisioned by default in a Carbon server are given here. However, when you develop a Carbon component, you will be able to change the default setting by provisioning all the required bundles for your component.

### Writing unit tests for a Carbon component
Follow the steps given below to incorporate unit test cases for your Carbon component.

#### *Step 1: Changing the default Pax Exam configurations*
You can change the default Pax Exam configurations by following the steps given below.

1. Change the pax.exam.system property in the pom.xml file of the OSGi test component from 'test' to 'default' as shown below. 
As mentioned in the Pax Exam Configuration Documentation, Pax Exam starts the OSGi container in 'test' mode with a standard set of options that are compatible with Pax Exam 2.3.0. This setting will provision only the default set of bundles listed here for your component. Therefore, using the 'default' mode allows you to add other dependencies for your component, in addition to the default bundles.

        <build>
        …….
           <plugins>
           …….
              <plugin>
                 <groupId>org.apache.maven.plugins</groupId>
                 <artifactId>maven-surefire-plugin</artifactId>
                 <configuration>
                    <systemPropertyVariables>
                       …….
                       <pax.exam.system>default</pax.exam.system>
                       …….
                    </systemPropertyVariables>
                       …….
                    <systemProperties>
                 …….
                 </configuration>
              …….
              </plugin>
           …….
           </plugins>
        …….
        </build>

2. Update the pom.xml of your Carbon component with the following dependencies:

Dependency for OSGi Test Utils:

    <dependency>
       <groupId>org.wso2.carbon</groupId>
       <artifactId>osgi-test-util</artifactId>
       <version>5.1.0</version>
    </dependency>

Other required dependencies for your component.

3. Optionally, you can change the default log level in Pax Exam (which is 'debug') by adding the “org.ops4j.pax.logging.DefaultServiceLog.level” system property to the pom.xml file of the OSGi test component as shown below.
