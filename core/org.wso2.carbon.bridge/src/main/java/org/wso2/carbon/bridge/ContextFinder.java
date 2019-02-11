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

/**
*  Context Finder for Felix. This will mimic the functionality of Equinox ContextFinder
*/

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ContextFinder extends ClassLoader implements PrivilegedAction {
	static final class Finder extends SecurityManager {
		public Class[] getClassContext() {
			return super.getClassContext();
		}
	}

	//This is used to detect cycle that could be caused while delegating the loading to other classloaders
	//It keeps track on a thread basis of the set of requested classes and resources
	private static ThreadLocal cycleDetector = new ThreadLocal();
	static ClassLoader finderClassLoader;
	static Finder contextFinder;
	static {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				finderClassLoader = ContextFinder.class.getClassLoader();
				contextFinder = new Finder();
				return null;
			}
		});
	}

	private final ClassLoader parentContextClassLoader;

	public ContextFinder(ClassLoader contextClassLoader) {
		super(contextClassLoader);
		this.parentContextClassLoader = contextClassLoader != null ? contextClassLoader : new ClassLoader(null) {/*parentless classloader*/};
	}

	// Return a list of all classloaders on the stack that are neither the
	// ContextFinder classloader nor the boot classloader.  The last classloader
	// in the list is either a bundle classloader or the framework's classloader
	// We assume that the bootclassloader never uses the context classloader to find classes in itself.
	ArrayList basicFindClassLoaders() {
		Class[] stack = contextFinder.getClassContext();
		ArrayList result = new ArrayList(1);
		ClassLoader previousLoader = null;
		for (int i = 1; i < stack.length; i++) {
			ClassLoader tmp = stack[i].getClassLoader();
			if (stack[i] != ContextFinder.class && tmp != null && tmp != this) {
				if (checkClassLoader(tmp)) {
					if (previousLoader != tmp) {
						result.add(tmp);
						previousLoader = tmp;
					}
				}
				// stop at the framework classloader or the first bundle classloader TODO ;
				/*if (tmp == finderClassLoader || tmp instanceof BundleClassLoader)
					break;*/
			}
		}
		return result;
	}

	// ensures that a classloader does not have the ContextFinder as part of the
	// parent hierachy.  A classloader which has the ContextFinder as a parent must
	// not be used as a delegate, otherwise we endup in endless recursion.
	private boolean checkClassLoader(ClassLoader classloader) {
		if (classloader == null || classloader == getParent())
			return false;
		for (ClassLoader parent = classloader.getParent(); parent != null; parent = parent.getParent())
			if (parent == this)
				return false;
		return true;
	}

	private ArrayList findClassLoaders() {
		if (System.getSecurityManager() == null)
			return basicFindClassLoaders();
		return (ArrayList) AccessController.doPrivileged(this);
	}

	public Object run() {
		return basicFindClassLoaders();
	}

	//Return whether the request for loading "name" should proceed.
	//False is returned when a cycle is being detected
	private boolean startLoading(String name) {
		Set classesAndResources = (Set) cycleDetector.get();
		if (classesAndResources != null && classesAndResources.contains(name))
			return false;

		if (classesAndResources == null) {
			classesAndResources = new HashSet(3);
			cycleDetector.set(classesAndResources);
		}
		classesAndResources.add(name);
		return true;
	}

	private void stopLoading(String name) {
		((Set) cycleDetector.get()).remove(name);
	}

	protected Class loadClass(String arg0, boolean arg1) throws ClassNotFoundException {
		//Shortcut cycle
		if (startLoading(arg0) == false)
			throw new ClassNotFoundException(arg0);

		try {
			ArrayList toConsult = findClassLoaders();
			for (Iterator loaders = toConsult.iterator(); loaders.hasNext();)
				try {
					return ((ClassLoader) loaders.next()).loadClass(arg0);
				} catch (ClassNotFoundException e) {
					// go to the next class loader
				}
			// avoid calling super.loadClass here because it checks the local cache (bug 127963)
			return parentContextClassLoader.loadClass(arg0);
		} finally {
			stopLoading(arg0);
		}
	}

	public URL getResource(String arg0) {
		//Shortcut cycle
		if (startLoading(arg0) == false)
			return null;
		try {
			ArrayList toConsult = findClassLoaders();
			for (Iterator loaders = toConsult.iterator(); loaders.hasNext();) {
				URL result = ((ClassLoader) loaders.next()).getResource(arg0);
				if (result != null)
					return result;
				// go to the next class loader
			}
			return super.getResource(arg0);
		} finally {
			stopLoading(arg0);
		}
	}

	protected Enumeration findResources(String arg0) throws IOException {
		//Shortcut cycle
		if (startLoading(arg0) == false)
			return null;
		try {
			ArrayList toConsult = findClassLoaders();
			for (Iterator loaders = toConsult.iterator(); loaders.hasNext();) {
				Enumeration result = ((ClassLoader) loaders.next()).getResources(arg0);
				if (result != null && result.hasMoreElements())
					return result;
				// go to the next class loader
			}
			return super.findResources(arg0);
		} finally {
			stopLoading(arg0);
		}
	}
}
