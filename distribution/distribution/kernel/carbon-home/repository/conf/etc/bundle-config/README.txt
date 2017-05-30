This directory supports adding third-pary config files to specific bundles during runtime. 

Explanation: Each OSGi bundle has its own classLoader. Some thirdpary libs read configs from classPath. This scenario fails in OSGi runtime, since OSGi runtime does not share a common classPath for individual bundles. Bundling config files during the bundle creation process itself will solve the issue. However it limits the ability to edit the configs during restarts.

Here we are providing a workaround for such scenarios. The given config file will get resolved to a fragment bundle and will get attached to the specified host bundle. The host bundle name(symbolic name) is resolved by looking at the directory structure. Hence host bundle name should be directory name of the config file directory.


Example: The bundle with symbolic name, 'org.foo.bar' expects a config file named 'foobar.properties' from its classPath.

create a directory named 'org.foo.bar' inside 'repository/conf/etc/bundle-config' - (this directory) and place the foobar.properties file.


