/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bridge;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The FrameworkLauncher provides the logic to:
 * 1) init
 * 2) deploy
 * 3) start
 * 4) stop
 * 5) undeploy
 * 6) destroy
 * an instance of the OSGi framework.
 * These 6 methods are provided to help manage the lifecycle and are called from outside this
 * class by the BridgeServlet. To create an extended FrameworkLauncher over-ride these methods to allow
 * custom behaviour.
 */
public class EquinoxFrameworkLauncher implements FrameworkLauncher {
    private static final String WS_DELIM = " \t\n\r\f";
    protected static final String FILE_SCHEME = "file:";
    protected static final String FRAMEWORK_BUNDLE_NAME = "org.eclipse.osgi";
    protected static final String STARTER =
            "org.eclipse.core.runtime.adaptor.EclipseStarter";
    protected static final String FRAMEWORKPROPERTIES =
            "org.eclipse.osgi.framework.internal.core.FrameworkProperties";
    protected static final String NULL_IDENTIFIER = "@null";
    protected static final String OSGI_FRAMEWORK = "osgi.framework";
    protected static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    protected static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
    protected static final String OSGI_INSTALL_AREA = "osgi.install.area";
    protected static final String OSGI_FORCED_RESTART = "osgi.forcedRestart";
    protected static final String RESOURCE_BASE = "/WEB-INF/eclipse/";
    protected static final String LAUNCH_INI = "launch.ini";

    private static final String ENABLE_OSGI_CONSOLE = "osgiConsole";
    private static final String ENABLE_OSGI_DEBUG = "osgiDebugOptions";
    private static final String BUNDLE_CREATORS = "bundleCreators";
    
    private static final String APPLY_PATCHES = "applyPatches";
    private static final String BUNDLE_BACKUP_DIR = "patch0000";

    /**
     * Is the OSGi framework running?
     */
    private boolean isRunning;

    static final PermissionCollection allPermissions = new PermissionCollection() {
        private static final long serialVersionUID = 482874725021998286L;
        // The AllPermission permission
        Permission allPermission = new AllPermission();

        // A simple PermissionCollection that only has AllPermission
        public void add(Permission permission) {
            // do nothing
        }

        public boolean implies(Permission permission) {
            return true;
        }

        public Enumeration elements() {
            return new Enumeration() {
                int cur = 0;

                public boolean hasMoreElements() {
                    return cur < 1;
                }

                public Object nextElement() {
                    if (cur == 0) {
                        cur = 1;
                        return allPermission;
                    }
                    throw new NoSuchElementException();
                }
            };
        }
    };

    static {
        // We do this to ensure the anonymous Enumeration class in allPermissions is pre-loaded
        if (allPermissions.elements() == null) {
            throw new IllegalStateException();
        }
    }

    protected ServletConfig servletConfig;
    protected ServletContext context;
    private File platformDirectory;
    private ClassLoader frameworkContextClassLoader;
    private URLClassLoader frameworkClassLoader;

    public void init(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        context = servletConfig.getServletContext();
        init();
    }

    /**
     * init is the first method called on the FrameworkLauncher and can be used for any initial setup.
     * The default behaviour is to do nothing.
     */
    public void init() {
        // do nothing for now
        System.setProperty(START_TIME, String.valueOf(System.currentTimeMillis()));
    }

    /**
     * destory is the last method called on the FrameworkLauncher and can be used for any final cleanup.
     * The default behaviour is to do nothing.
     */
    public void destroy() {
        // do nothing for now
    }

    /**
     * deploy is used to move the OSGi framework libraries into a location suitable for execution.
     * The default behaviour is to copy the contents of the webapps WEB-INF/eclipse directory
     * to the webapps temp directory.
     */
    public synchronized void deploy() {
        platformDirectory = getCarbonComponentRepo();

        File plugins = new File(platformDirectory, "plugins");

        File patchesDir;
        String patchesPath = System.getProperty("carbon.patches.dir.path");
        if (patchesPath == null) {
            patchesDir = new File(platformDirectory, "patches");
        } else {
            patchesDir = new File(patchesPath);
        }
//
        File dropinsFolder = new File(platformDirectory, "dropins");
        //copying resources inside patches folder to the work area.
        //TODO Copying of patches should only be performed by the master node, in multiple instance case.
        String applyPatches = System.getProperty(APPLY_PATCHES);
        if (applyPatches != null) {
            try {
                applyPatches(patchesDir, plugins);
            } catch (IOException e) {
                context.log("Error occurred while applying patches", e);
            }
        }

        // copy create OSGi framework extension bundles

    }

    /**
     * undeploy is the reverse operation of deploy and removes the OSGi framework libraries from their
     * execution location. Typically this method will only be called if a manual undeploy is requested in the
     * ServletBridge.
     * By default, this method removes the OSGi install and also removes the workspace.
     */
    public synchronized void undeploy() {
        if (platformDirectory == null) {
            context.log("Undeploy unnecessary. - (not deployed)");
            return;
        }

        if (frameworkClassLoader != null) {
            throw new IllegalStateException("Could not undeploy Framework - (not stopped)");
        }

        deleteDirectory(new File(platformDirectory, "configuration"));
        deleteDirectory(new File(platformDirectory, "features"));
        deleteDirectory(new File(platformDirectory, "plugins"));
        deleteDirectory(new File(platformDirectory, "workspace"));
        deleteDirectory(new File(platformDirectory, "p2"));

        if (!new File(platformDirectory, ".eclipseproduct").delete()) {
            context.log("Failed to deleted the directory: .eclipseproduct");
        }

        if (!new File(platformDirectory, "artifacts.xml").delete()) {
            context.log("Failed to deleted the file: artifacts.xml");
        }

        if (!new File(platformDirectory, "eclipse.ini").delete()) {
            context.log("Failed to deleted the file: eclipse.ini");
        }
        platformDirectory = null;
    }

    /**
     * start is used to "start" a previously deployed OSGi framework
     * The default behaviour will read launcher.ini to create a set of initial properties and
     * use the "commandline" configuration parameter to create the equivalent command line arguments
     * available when starting Eclipse.
     */
    public synchronized void start() {
        platformDirectory = getCarbonComponentRepo();
        if (platformDirectory == null) {
            throw new IllegalStateException(
                    "Could not start the Framework - (not deployed)");
        }

        if (frameworkClassLoader != null) {
            context.log("Framework is already started");
            return;
        }
        Map<String, String> initialPropsMap = buildInitialPropertyMap();
        String[] args = getArgs();

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            System.setProperty("osgi.framework.useSystemProperties", "false");
            frameworkClassLoader =
                    new ChildFirstURLClassLoader(new URL[]{new URL(initialPropsMap.get(OSGI_FRAMEWORK))},
                            this.getClass().getClassLoader());
            Class clazz = frameworkClassLoader.loadClass(STARTER);

            Method setInitialProperties =
                    clazz.getMethod("setInitialProperties", Map.class);
            setInitialProperties.invoke(null, initialPropsMap);

            registerRestartHandler(clazz);

            Method runMethod = clazz.getMethod("startup", String[].class, Runnable.class);
            runMethod.invoke(null, args, null);

            frameworkContextClassLoader = Thread.currentThread().getContextClassLoader();
            isRunning = true;
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t == null) {
                t = ite;
            }
            context.log("Error while starting Framework", t);
            throw new RuntimeException(t.getMessage());
        } catch (Exception e) {
            context.log("Error while starting Framework", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    /**
     * stop is used to "shutdown" the framework and make it avialable for garbage collection.
     * The default implementation also has special handling for Apache Commons Logging to "release" any
     * resources associated with the frameworkContextClassLoader.
     */
    public synchronized void stop() {
        isRunning = false;
        if (platformDirectory == null) {
            context.log("Shutdown unnecessary. (not deployed)");
            return;
        }

        if (frameworkClassLoader == null) {
            context.log("Framework is already shutdown");
            return;
        }

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Class clazz = frameworkClassLoader.loadClass(STARTER);
            Method method = clazz.getDeclaredMethod("shutdown", (Class[]) null);
            Thread.currentThread().setContextClassLoader(frameworkContextClassLoader);
            method.invoke(clazz, (Object[]) null);

            // Invalidate all existing Http Sessions
            HttpSessionManager.invalidateSessions();


            // ACL keys its loggers off of the ContextClassLoader which prevents GC without calling release.
            // This section explicitly calls release if ACL is used.
            try {
                clazz = this.getClass().getClassLoader()
                        .loadClass("org.apache.commons.logging.LogFactory");
                method = clazz.getDeclaredMethod("release", ClassLoader.class);
                method.invoke(clazz, frameworkContextClassLoader);
            } catch (ClassNotFoundException e) {
                // ignore, ACL is not being used
            }
        } catch (Exception e) {
            context.log("Error while stopping Framework", e);
            return;
        } finally {
            frameworkClassLoader = null;
            frameworkContextClassLoader = null;
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * copyResource is a convenience method to recursively copy resources from the ServletContext to
     * an installation target. The default behaviour will create a directory if the resourcepath ends
     * in '/' and a file otherwise.
     *
     * @param resourcePath - The resource root path
     * @param target       - The root location where resources are to be copied
     */
    protected void copyResource(String resourcePath, File target) {
        if (resourcePath.endsWith("/")) {
            if (!target.mkdir()) {
                context.log("Fail to create the directory: " + target.getAbsolutePath());
            }
            Set paths = context.getResourcePaths(resourcePath);
            if (paths == null) {
                return;
            }
            for (Iterator it = paths.iterator(); it.hasNext();) {
                String path = (String) it.next();
                File newFile = new File(target, path.substring(resourcePath.length()));
                copyResource(path, newFile);
            }
        } else {
            try {
                if (target.createNewFile()) {
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = context.getResourceAsStream(resourcePath);
                        if (is == null) {
                            return;
                        }
                        os = new FileOutputStream(target);
                        byte[] buffer = new byte[8192];
                        int bytesRead = is.read(buffer);
                        while (bytesRead != -1) {
                            os.write(buffer, 0, bytesRead);
                            bytesRead = is.read(buffer);
                        }
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            context.log("Unable to close the InputStream " + e.getMessage(), e);
                        }

                        try {
                            if (os != null) {
                                os.close();
                            }
                        } catch (IOException e) {
                            context.log("Unable to close the OutputStream " + e.getMessage(), e);
                        }
                    }
                }
            } catch (IOException e) {
                context.log("Error copying resources", e);
            }
        }
    }

    /**
     * Extracts all the feature jars and zip files specified in the compressFilePath to target with
     * the exception of jar files extracted to target/features/jarfilename folder
     *
     * @param compressFilesPath - The path which the feature archives are present
     * @param target            - The location where they should be extracted to
     */
    protected void extractFeatures(String compressFilesPath, File target) {
        if (compressFilesPath.endsWith("/")) {
            if (!target.mkdir()) {
                context.log("Fail to create the directory: " + target.getAbsolutePath());
            }
            Set paths = context.getResourcePaths(compressFilesPath);
            if (paths == null) {
                return;
            }
            for (Object path1 : paths) {
                String path = (String) path1;
                extractFeatures(path, target);
            }
        } else {
            if (compressFilesPath.endsWith(".jar")) {
                File tmpJarFilePath = new File(compressFilesPath);
                String featureName = tmpJarFilePath.getName().substring(
                        0, tmpJarFilePath.getName().length() - 4);
                File featureFolder = new File(
                        target.getPath() + File.separator + "features" + File.separator + featureName);
                extractResource(compressFilesPath, featureFolder);
            } else
                extractResource(compressFilesPath, target);
        }
    }

    /**
     * Extract an archive to the target location
     *
     * @param compressFilePath - Archive path
     * @param target           - Location to extract
     */
    protected void extractResource(String compressFilePath, File target) {
        try (ZipInputStream zipinputstream = new ZipInputStream(context.getResourceAsStream(compressFilePath))) {
            byte[] buf = new byte[1024];
            ZipEntry zipentry;

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                String entryName = zipentry.getName();
                int n;
                File newFile = new File(entryName);
                String directory = newFile.getParent();

                if (directory == null) {
                    if (newFile.isDirectory())
                        break;
                }

                if (zipentry.isDirectory()) {
                    zipinputstream.closeEntry();
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                if (newFile.isDirectory())
                    break;
                File outputFile = new File(target.getPath(), entryName);
                if (outputFile.getParentFile() != null && !outputFile.getParentFile().mkdirs()) {
                    throw new IOException("Fail to create the directory: " + outputFile.getParentFile().getAbsolutePath());
                }
                try (FileOutputStream fileoutputstream = new FileOutputStream(outputFile)) {
                    while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                        fileoutputstream.write(buf, 0, n);
                    }
                }
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }
        } catch (Exception ignored) {
            //ignore invalid compress file
            context.log(ignored.getMessage(), ignored);
        }
    }

    /**
     * deleteDirectory is a convenience method to recursively delete a directory
     *
     * @param directory - the directory to delete.
     * @return was the delete succesful
     */
    protected static boolean deleteDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        System.err.println("Fail to create the directory: " + file.getAbsolutePath());
                    }
                }
            }
        }
        return directory.delete();
    }

    /**
     * Used when to set the ContextClassLoader when the BridgeServlet delegates to a Servlet
     * inside the framework
     *
     * @return a Classloader with the OSGi framework's context classloader.
     */
    public synchronized ClassLoader getFrameworkContextClassLoader() {
        return frameworkContextClassLoader;
    }

    /**
     * Platfom Directory is where the OSGi software is installed
     *
     * @return the framework install location
     */
    protected synchronized File getPlatformDirectory() {
        return platformDirectory;
    }

    /**
     * loadProperties is a convenience method to load properties from a servlet context resource
     *
     * @param resource - The target to read properties from
     * @return the properties
     */
    protected Properties loadProperties(String resource) {
        Properties result = new Properties();
        InputStream in = null;
        try {
            URL location = context.getResource(resource);
            if (location != null) {
                in = location.openStream();
                result.load(in);
            }
        } catch (MalformedURLException e) {
            // no url to load from
        } catch (IOException e) {
            // its ok if there is no file
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return result;
    }

    /**
     * Searches for the given target directory starting in the "plugins" subdirectory
     * of the given location.  If one is found then this location is returned;
     * otherwise an exception is thrown.
     *
     * @param target target
     * @param start  the location to begin searching
     * @return the location where target directory was found
     */
    protected String searchFor(final String target, String start) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File candidate) {
                return candidate.getName().equals(target) ||
                        candidate.getName().startsWith(target + "_");
            }
        };
        File[] candidates = new File(start).listFiles(filter);
        if (candidates == null) {
            return null;
        }
        String[] arrays = new String[candidates.length];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = candidates[i].getName();
        }
        int result = findMax(arrays);
        if (result == -1) {
            return null;
        }
        return candidates[result].getAbsolutePath().replace(File.separatorChar, '/') +
                (candidates[result].isDirectory() ? "/" : "");
    }

    protected int findMax(String[] candidates) {
        int result = -1;
        Object maxVersion = null;
        for (int i = 0; i < candidates.length; i++) {
            String name = candidates[i];
            String version =
                    ""; // Note: directory with version suffix is always > than directory without version suffix
            int index = name.indexOf('_');
            if (index != -1) {
                version = name.substring(index + 1);
            }
            Object currentVersion = getVersionElements(version);
            if (maxVersion == null) {
                result = i;
                maxVersion = currentVersion;
            } else {
                if (compareVersion((Object[]) maxVersion, (Object[]) currentVersion) < 0) {
                    result = i;
                    maxVersion = currentVersion;
                }
            }
        }
        return result;
    }

    /**
     * Compares version strings.
     *
     * @param left  left
     * @param right right
     * @return result of comparison, as integer;
     *         <code><0</code> if left < right;
     *         <code>0</code> if left == right;
     *         <code>>0</code> if left > right;
     */
    private int compareVersion(Object[] left, Object[] right) {

        int result = ((Integer) left[0]).compareTo((Integer) right[0]); // compare major
        if (result != 0) {
            return result;
        }

        result = ((Integer) left[1]).compareTo((Integer) right[1]); // compare minor
        if (result != 0) {
            return result;
        }

        result = ((Integer) left[2]).compareTo((Integer) right[2]); // compare service
        if (result != 0) {
            return result;
        }

        return ((String) left[3]).compareTo((String) right[3]); // compare qualifier
    }

    /**
     * Do a quick parse of version identifier so its elements can be correctly compared.
     * If we are unable to parse the full version, remaining elements are initialized
     * with suitable defaults.
     *
     * @param version Version
     * @return an array of size 4; first three elements are of type Integer (representing
     *         major, minor and service) and the fourth element is of type String (representing
     *         qualifier). Note, that returning anything else will cause exceptions in the caller.
     */
    private Object[] getVersionElements(String version) {
        if (version.endsWith(".jar")) {
            version = version.substring(0, version.length() - 4);
        }
        Object[] result = {0, 0, 0, ""};
        StringTokenizer t = new StringTokenizer(version, ".");
        String token;
        int i = 0;
        while (t.hasMoreTokens() && i < 4) {
            token = t.nextToken();
            if (i < 3) {
                // major, minor or service ... numeric values
                try {
                    result[i++] = new Integer(token);
                } catch (Exception e) {
                    // invalid number format - use default numbers (0) for the rest
                    break;
                }
            } else {
                // qualifier ... string value
                result[i++] = token;
            }
        }
        return result;
    }

    /**
     * Here is the patch applying algorithm.
     * 1) Creates a patch0000 (if it does not exist) inside the patches directory. Backup all the bundles in the plugins
     * directory
     * 2) Then copy all the patchxxxx to the plugins folder.
     * @param patchesDir
     * @param pluginsDir
     * @throws IOException
     */
    protected void applyPatches(File patchesDir, File pluginsDir) throws IOException {
        File bundleBackupDir = new File(patchesDir, BUNDLE_BACKUP_DIR);
        if(!bundleBackupDir.exists()){
            //We need backup the plugins in the components/repository/plugins folder.
            File[] plugins = pluginsDir.listFiles();
            for(File plugin: plugins){
                BridgeUtils.copyFileToDir(plugin, bundleBackupDir);
            }
        }
        //Now lets apply patches.
        copyPatches(patchesDir, pluginsDir);
    }


    /**
     * The functionality is same as copyResources method. But copyPatches method copy all the *.jar files in the
     * /WEB-INF/patches folder to flat target folder. Folder structure within the /WEB-INF/patches folder is not reflected
     * in the target folder.
     *
     * @param source folder which contains the patches.
     * @param target target
     * @throws java.io.IOException
     */
    protected void copyPatches(File source, File target) throws IOException {
        if (source.isDirectory()) {
            //Sorting patch folders.
            File[] files = source.listFiles();
            Arrays.sort(files);
            for (File file : files) {
                copyPatches(file, target);
            }
        } else {
            BridgeUtils.copyFileToDir(source, target);
        }
    }

    private File getCarbonComponentRepo() {
        String carbonComponentsRepository;
        carbonComponentsRepository = System.getProperty("carbon.components.dir.path");
        if (carbonComponentsRepository == null) {
            String carbonRepo = System.getenv("CARBON_REPOSITORY");
            if (carbonRepo == null) {
                carbonRepo = System.getProperty("carbon.repository");
            }
            if (carbonRepo == null) {
                carbonRepo = Paths.get(System.getProperty("carbon.home") ,"repository").toString();
            }
            carbonComponentsRepository = Paths.get(carbonRepo,"components").toString();
        }
        File componentRepo = new File(carbonComponentsRepository);
        if (!componentRepo.exists() && !componentRepo.mkdirs()) {
            System.err.println("Fail to create the directory: " + componentRepo.getAbsolutePath());
        }
        return componentRepo;
    }

    private void registerRestartHandler(Class starterClazz) throws NoSuchMethodException,
            ClassNotFoundException,
            IllegalAccessException,
            InvocationTargetException {
        Method registerFrameworkShutdownHandler;
        try {
            registerFrameworkShutdownHandler = starterClazz.getDeclaredMethod(
                    "internalAddFrameworkShutdownHandler",
                    Runnable.class);
        } catch (NoSuchMethodException e) {
            // Ok. However we will not support restart events. Log this as info
            context.log(starterClazz.getName() +
                    " does not support setting a shutdown handler. Restart handling is disabled.");
            return;
        }
        if (!registerFrameworkShutdownHandler.isAccessible()) {
            registerFrameworkShutdownHandler.setAccessible(true);
        }
        Runnable restartHandler = createRestartHandler();
        registerFrameworkShutdownHandler.invoke(null, restartHandler);
    }

    private Runnable createRestartHandler() throws ClassNotFoundException, NoSuchMethodException {
        Class frameworkPropertiesClazz = frameworkClassLoader.loadClass(FRAMEWORKPROPERTIES);
        final Method getProperty = frameworkPropertiesClazz
                .getMethod("getProperty", String.class);
        Runnable restartHandler = new Runnable() {
            public void run() {
                try {
                    String forcedRestart =
                            (String) getProperty.invoke(null, OSGI_FORCED_RESTART);
                    if (Boolean.valueOf(forcedRestart).booleanValue()) {
                        stop();
                        start();
                    }
                } catch (InvocationTargetException ite) {
                    Throwable t = ite.getTargetException();
                    if (t == null) {
                        t = ite;
                    }
                    throw new RuntimeException(t.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        };
        return restartHandler;
    }

        /**
     * buildInitialPropertyMap create the initial set of properties from the contents of launch.ini
     * and for a few other properties necessary to launch defaults are supplied if not provided.
     * The value '@null' will set the map value to null.
     *
     * @return a map containing the initial properties
     */
    private Map<String, String> buildInitialPropertyMap() {
        Map<String, String> initialPropertyMap = new HashMap<String, String>();
        Properties launchProperties = loadProperties(RESOURCE_BASE + LAUNCH_INI);
        for (Object o : launchProperties.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.endsWith("*")) { //$NON-NLS-1$
                if (value.equals(NULL_IDENTIFIER)) {
                    clearPrefixedSystemProperties(key.substring(0, key.length() - 1),
                            initialPropertyMap);
                }
            } else if (value.equals(NULL_IDENTIFIER)) {
                initialPropertyMap.put(key, null);
            } else {
                initialPropertyMap.put((String) entry.getKey(), (String) entry.getValue());
            }
        }

        try {
            // install.area if not specified
            if (initialPropertyMap.get(OSGI_INSTALL_AREA) == null) {
                initialPropertyMap
                        .put(OSGI_INSTALL_AREA, platformDirectory.toURL().toExternalForm());
            }

            // configuration.area if not specified
            if (initialPropertyMap.get(OSGI_CONFIGURATION_AREA) == null) {
                File configurationDirectory =
                        new File(platformDirectory, "configuration");
                if (!configurationDirectory.exists() && !configurationDirectory.mkdirs()) {
                    context.log("Fail to create the directory: " + configurationDirectory.getAbsolutePath());
                }
                initialPropertyMap.put(OSGI_CONFIGURATION_AREA,
                        configurationDirectory.toURL().toExternalForm());
            }

            // instance.area if not specified
            if (initialPropertyMap.get(OSGI_INSTANCE_AREA) == null) {
                File workspaceDirectory = new File(platformDirectory, "workspace");
                if (!workspaceDirectory.exists() && !workspaceDirectory.mkdirs()) {
                    context.log("Failed to create the directory: " + workspaceDirectory.getAbsoluteFile());
                }
                initialPropertyMap
                        .put(OSGI_INSTANCE_AREA, workspaceDirectory.toURL().toExternalForm());
            }

            // osgi.framework if not specified
            if (initialPropertyMap.get(OSGI_FRAMEWORK) == null) {
                // search for osgi.framework in osgi.install.area
                String installArea = initialPropertyMap.get(OSGI_INSTALL_AREA);

                // only support file type URLs for install area
                if (installArea.startsWith(FILE_SCHEME)) {
                    installArea = installArea.substring(FILE_SCHEME.length());
                }

                String path = new File(installArea, "plugins").toString();
                path = searchFor(FRAMEWORK_BUNDLE_NAME, path);
                if (path == null) {
                    throw new RuntimeException("Could not find framework");
                }

                initialPropertyMap.put(OSGI_FRAMEWORK,
                        new File(path).toURL().toExternalForm());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error establishing location");
        }

        return initialPropertyMap;
    }

    /**
     * clearPrefixedSystemProperties clears System Properties by writing null properties in
     * the targetPropertyMap that match a prefix
     *
     * @param prefix            prefix
     * @param targetPropertyMap targetPropertyMap
     */
    private static void clearPrefixedSystemProperties(String prefix,
                                                      Map<String, String> targetPropertyMap) {
        for (Object o : System.getProperties().keySet()) {
            String propertyName = (String) o;
            if (propertyName.startsWith(prefix) && !targetPropertyMap.containsKey(propertyName)) {
                targetPropertyMap.put(propertyName, null);
            }
        }
    }

    /**
     * buildCommandLineArguments parses the commandline config parameter into a set of arguments
     *
     * @return an array of String containing the commandline arguments
     */
    private String[] getArgs() {
        List<String> args = new ArrayList<String>();

        // Enable osgi console
        // First try to get from the System property
        String enableOsgiConsole = System.getProperty(ENABLE_OSGI_CONSOLE);
        if (enableOsgiConsole == null) {
            // Next try to get it from the web.xml
            enableOsgiConsole = servletConfig.getInitParameter(ENABLE_OSGI_CONSOLE);
        } else {
            if (!enableOsgiConsole.toLowerCase().equals("true")) {
                try {
                    enableOsgiConsole =
                            "-console " + String.valueOf(Integer.parseInt(enableOsgiConsole));
                } catch (NumberFormatException ignored) {
                    enableOsgiConsole = "-console";
                }
            } else {
                enableOsgiConsole = "-console";
            }
        }

        if (enableOsgiConsole != null) {
            StringTokenizer tokenizer = new StringTokenizer(enableOsgiConsole, WS_DELIM);
            while (tokenizer.hasMoreTokens()) {
                String arg = tokenizer.nextToken();
                if (arg.startsWith("\"")) {
                    if (arg.endsWith("\"")) {
                        if (arg.length() >= 2) {
                            // strip the beginning and ending quotes
                            arg = arg.substring(1, arg.length() - 1);
                        }
                    } else {
                        String remainingArg = tokenizer.nextToken("\"");
                        arg = arg.substring(1) + remainingArg;
                        // skip to next whitespace separated token
                        tokenizer.nextToken(WS_DELIM);
                    }
                } else if (arg.startsWith("'")) {
                    if (arg.endsWith("'")) {
                        if (arg.length() >= 2) {
                            // strip the beginning and ending quotes
                            arg = arg.substring(1, arg.length() - 1);
                        }
                    } else {
                        String remainingArg = tokenizer.nextToken("'");
                        arg = arg.substring(1) + remainingArg;
                        // skip to next whitespace separated token
                        tokenizer.nextToken(WS_DELIM);
                    }
                }
                args.add(arg);
            }
            System.out.println("OSGi console has been enabled with options: " + enableOsgiConsole);
        }

        // Enable osgi debug
        // First try to get from the System property
        String enableOsgiDebug = System.getProperty(ENABLE_OSGI_DEBUG);
        if (enableOsgiDebug == null) {
            // Next try to get it from the web.xml
            enableOsgiDebug = servletConfig.getInitParameter(ENABLE_OSGI_DEBUG);
        } else {
            if (enableOsgiDebug.toLowerCase().equals("true")) {
                enableOsgiDebug = "lib/core/WEB-INF/eclipse/osgi-debug.options"; // TODO: Can get context root from carbon.xml in the future
            }
        }

        if (enableOsgiDebug != null) {
            args.add("-debug");
            args.add(enableOsgiDebug);
            System.out.println("OSGi debugging has been enabled with options: " + enableOsgiDebug);
        }

        return args.toArray(new String[]{});
    }

    /**
     * The ChildFirstURLClassLoader alters regular ClassLoader delegation and will check the URLs
     * used in its initialization for matching classes before delegating to it's parent.
     * Sometimes also referred to as a ParentLastClassLoader
     */
    protected class ChildFirstURLClassLoader extends URLClassLoader {

        public ChildFirstURLClassLoader(URL[] urls) {
            super(urls);
        }

        public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent,
                                        URLStreamHandlerFactory factory) {
            super(urls, parent, factory);
        }

        public URL getResource(String name) {
            URL resource = findResource(name);
            if (resource == null) {
                ClassLoader parent = getParent();
                if (parent != null) {
                    resource = parent.getResource(name);
                }
            }
            return resource;
        }

        protected synchronized Class loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            Class clazz = findLoadedClass(name);
            if (clazz == null) {
                try {
                    clazz = findClass(name);
                } catch (ClassNotFoundException e) {
                    ClassLoader parent = getParent();
                    if (parent != null) {
                        clazz = parent.loadClass(name);
                    } else {
                        clazz = getSystemClassLoader().loadClass(name);
                    }
                }
            }

            if (resolve) {
                resolveClass(clazz);
            }

            return clazz;
        }

        // we want to ensure that the framework has AllPermissions
        protected PermissionCollection getPermissions(CodeSource codesource) {
            return allPermissions;
        }
    }
}
