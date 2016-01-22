This directory contains carbon-jndi implementation

1) Copy  org.wso2.carbon.jndi-1.0.0.jar file to the dropins directory

2) Add the following property to launch.ini file inside the conf/osgi directory.

carbon.initial.osgi.bundles=\
  file\:plugins/org.eclipse.osgi.services_3.4.0.v20140312-2051.jar@1\:true,\
  file\:plugins/org.ops4j.pax.logging.pax-logging-api_1.8.4.jar@2\:true,\
  file\:plugins/org.ops4j.pax.logging.pax-logging-log4j2_1.8.4.jar@2\:true,\
  file\:profiles/org.wso2.carbon.jndi-1.0.0.jar@3\:true,\
  file\:plugins/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar@3\:true



  e.g. usage of the JNDI API

     try {
          Context ctx = new InitialContext();
          Reference dataSource = (Reference) ctx.lookup("jdbc");
          logger.info("user-name retrieved from initial-context is {}", dataSource.getClass().getName());
      } catch (NamingException e) {
          logger.error("error", e);
      }







