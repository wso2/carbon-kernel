/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ui;

import org.wso2.carbon.ui.util.UIResourceProvider;
import org.osgi.framework.*;

import java.net.URL;
import java.util.Set;
import java.util.HashSet;

/**
 * This class acts as a registry for UI resources. JspServlet and other parties who need UI resources,
 * use an instance of this class to load UI resources.
 * <p/>
 * First this class loads the requested resource from the default resourceProvider, which is the
 * BundleBasedUIResourceProvider. If it fails, the this class loads the resource from the custom UIResourceProviders
 */
public class UIResourceRegistry implements UIResourceProvider, ServiceListener {

    private UIResourceProvider defaultUIResourceProvider;
    private Set<UIResourceProvider> resourceProviderSet;
    private BundleContext bundleContext;

    public void initialize(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        resourceProviderSet = new HashSet<UIResourceProvider>();

        try {
            //1. Registering this class as a ServiceListener which is interested in services
            //registered with UIResourceProvider key.
            bundleContext.addServiceListener(this,
                    "(" + Constants.OBJECTCLASS + "=" + UIResourceProvider.class.getName() + ")");

            //2. Getting registered UIResourceProvider OSGi services.
            ServiceReference[] references = bundleContext.getServiceReferences((String)null,
                    "(" + Constants.OBJECTCLASS + "=" + UIResourceProvider.class.getName() + ")");

            if (references != null && references.length > 0) {
                for (ServiceReference reference : references) {
                    UIResourceProvider uiResourceProvider = (UIResourceProvider) bundleContext.getService(reference);
                    if (uiResourceProvider != null) {
                        //Synchronized all the add methods, because this method may be invoked concurrently
                        resourceProviderSet.add(uiResourceProvider);
                    }
                }
            }
        } catch (InvalidSyntaxException ignored) {
        }
    }

    public URL getUIResource(String path) {
        URL url = defaultUIResourceProvider.getUIResource(canonicalURI(path));
        if (url == null) {
            for (UIResourceProvider resourceProvider : resourceProviderSet) {
                url = resourceProvider.getUIResource(path);
                if (url != null) {
                    break;
                }
            }
        }
        return url;
    }

    public Set<String> getUIResourcePaths(String path) {
        Set<String> resourcePathSet = defaultUIResourceProvider.getUIResourcePaths(path);
        if (resourcePathSet.isEmpty()) {
            for (UIResourceProvider resourceProvider : resourceProviderSet) {
                resourcePathSet = resourceProvider.getUIResourcePaths(path);
                if (!resourcePathSet.isEmpty()) {
                    break;
                }
            }
        }
        return resourcePathSet;
    }

    public void setDefaultUIResourceProvider(UIResourceProvider defaultUIResourceProvider) {
        this.defaultUIResourceProvider = defaultUIResourceProvider;
    }

    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
            UIResourceProvider uiResourceProvider = (UIResourceProvider)
                    bundleContext.getService(serviceEvent.getServiceReference());
            resourceProviderSet.add(uiResourceProvider);

        } else if (serviceEvent.getType() == ServiceEvent.UNREGISTERING) {
            UIResourceProvider uiResourceProvider = (UIResourceProvider)
                    bundleContext.getService(serviceEvent.getServiceReference());
            resourceProviderSet.remove(uiResourceProvider);
        }
    }

    protected static final String canonicalURI(String s) {
        if (s == null) {
            return null;
        } else {
            StringBuilder result = new StringBuilder();
            int len = s.length();
            int pos = 0;

            while(pos < len) {
                char c = s.charAt(pos);
                if (isPathSeparator(c)) {
                    while(pos + 1 < len && isPathSeparator(s.charAt(pos + 1))) {
                        ++pos;
                    }

                    if (pos + 1 < len && s.charAt(pos + 1) == '.') {
                        if (pos + 2 >= len) {
                            break;
                        }

                        switch (s.charAt(pos + 2)) {
                            case '.':
                                if (pos + 3 < len && isPathSeparator(s.charAt(pos + 3))) {
                                    pos += 3;

                                    int separatorPos;
                                    for(separatorPos = result.length() - 1; separatorPos >= 0 && !isPathSeparator(result.charAt(separatorPos)); --separatorPos) {
                                    }

                                    if (separatorPos >= 0) {
                                        result.setLength(separatorPos);
                                    }
                                    continue;
                                }
                                break;
                            case '/':
                            case '\\':
                                pos += 2;
                                continue;
                        }
                    }
                }

                result.append(c);
                ++pos;
            }

            return result.toString();
        }
    }

    protected static final boolean isPathSeparator(char c) {
        return c == '/' || c == '\\';
    }
}
