$CARBON_HOME/repository is the main repository for all kind of deployments and
configurations in Carbon. This includes all Runtime related artifacts. In
addition to that, Carbon configurations are also hosted under this folder.

1. lib
   Directory contains all the client side Axis2 libraries. These libraries will be copied here after
   starting the server once or by running 'ant' from CARBON_HOME/bin.

2. deployment
   Directory can be used to deploy artifacts on carbon kernel based server.
   See deployment/README for more details.

3. conf
   Directory contains all the configuration files. eg: carbon.xml.

4. components
   Directory contains all OSGi related stuff. Carbon bundles, OSGi configuration
   files and p2 stuff. See components/README for more details.

5. logs
   Directory contains all Carbon logs.
