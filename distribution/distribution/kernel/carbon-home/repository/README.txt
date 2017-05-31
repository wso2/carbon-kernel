$CARBON_HOME/repository is the main repository for all kind of deployments and
configurations in Carbon. This includes all Axis2 artifacts, Synapse artifacts etc. In
addition to that, Axis2 configurations, Carbon configurations etc are also hosted
under this folder.

1. lib
   Directory contains all the client side Axis2 libraries. These libraries will be copied here after
   starting the server once or by running 'ant' from CARBON_HOME/bin.

2. deployment
   Directory can be used to deploy Axis2 (can have Synapse, BPel stuff as well) artifacts for both
   server side and client side. See deployment/README for more details.

3. conf
   Directory contains all the configuration files. axis2.xml, carbon.xml etc.

4. components
   Directory contains all OSGi related stuff. Carbon bundles, OSGi configuration
   files and p2 stuff. See components/README for more details.

5. logs
   Directory contains all Carbon logs.

6. tenants
   Directory will contain relevant tenant artifacts in the case of a multitenant deployment.

7. resources
   Directory contains resources related to security etc.

8. data
   Directory contains LDAP related data.

9. database
   Directory contains the default application database.
