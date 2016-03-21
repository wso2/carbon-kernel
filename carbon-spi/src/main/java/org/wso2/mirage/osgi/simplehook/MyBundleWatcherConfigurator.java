package org.wso2.mirage.osgi.simplehook;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo;
import org.eclipse.osgi.storage.bundlefile.BundleEntry;

import java.util.ArrayList;

public class MyBundleWatcherConfigurator extends ClassLoaderHook implements HookConfigurator {

    @Override
    public void addHooks(HookRegistry hookRegistry) {
        System.out.println("hooked");
        hookRegistry.addClassLoaderHook(this);
    }

    @Override
    public void classLoaderCreated(ModuleClassLoader classLoader) {
        super.classLoaderCreated(classLoader);
        System.out.println(classLoader.getBundle().getSymbolicName());
    }

    @Override
    public boolean addClassPathEntry(ArrayList<ClasspathEntry> cpEntries, String cp, ClasspathManager hostmanager, BundleInfo.Generation sourceGeneration) {
        System.out.println(cp);
        return super.addClassPathEntry(cpEntries, cp, hostmanager, sourceGeneration);

    }

    @Override
    public byte[] processClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry, ClasspathManager manager) {
        System.out.println(name);
        return super.processClass(name, classbytes, classpathEntry, entry, manager);
    }
}
