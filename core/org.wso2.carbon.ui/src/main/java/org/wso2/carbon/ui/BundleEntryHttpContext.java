package org.wso2.carbon.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.context.ServletContextHelper;

public class BundleEntryHttpContext extends ServletContextHelper implements HttpContext {
    private Bundle bundle;
    private String bundlePath;

    public BundleEntryHttpContext(Bundle bundle) {
        this.bundle = bundle;
    }

    public BundleEntryHttpContext(Bundle b, String bundlePath) {
        this(b);
        if (bundlePath != null) {
            if (bundlePath.endsWith("/")) {
                bundlePath = bundlePath.substring(0, bundlePath.length() - 1);
            }

            if (bundlePath.length() == 0) {
                bundlePath = null;
            }
        }

        this.bundlePath = bundlePath;
    }

    public String getMimeType(String arg0) {
        return null;
    }

    public boolean handleSecurity(HttpServletRequest arg0, HttpServletResponse arg1) throws IOException {
        return true;
    }

    public URL getResource(String resourceName) {
        if (this.bundlePath != null) {
            resourceName = this.bundlePath + resourceName;
        }

        int lastSlash = resourceName.lastIndexOf(47);
        if (lastSlash == -1) {
            return null;
        } else {
            String path = resourceName.substring(0, lastSlash);
            if (path.length() == 0) {
                path = "/";
            }

            String file = this.sanitizeEntryName(resourceName.substring(lastSlash + 1));
            Enumeration entryPaths = this.bundle.findEntries(path, file, false);
            return entryPaths != null && entryPaths.hasMoreElements() ? (URL)entryPaths.nextElement() : null;
        }
    }

    private String sanitizeEntryName(String name) {
        StringBuffer buffer = null;

        for(int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            switch (c) {
                case '*':
                case '\\':
                    if (buffer == null) {
                        buffer = new StringBuffer(name.length() + 16);
                        buffer.append(name.substring(0, i));
                    }

                    buffer.append('\\').append(c);
                    break;
                default:
                    if (buffer != null) {
                        buffer.append(c);
                    }
            }
        }

        return buffer == null ? name : buffer.toString();
    }

    public Set<String> getResourcePaths(String path) {
        if (this.bundlePath != null) {
            path = this.bundlePath + path;
        }

        Enumeration entryPaths = this.bundle.findEntries(path, (String)null, false);
        if (entryPaths == null) {
            return null;
        } else {
            Set result = new HashSet();

            while(entryPaths.hasMoreElements()) {
                URL entryURL = (URL)entryPaths.nextElement();
                String entryPath = entryURL.getFile();
                if (this.bundlePath == null) {
                    result.add(entryPath);
                } else {
                    result.add(entryPath.substring(this.bundlePath.length()));
                }
            }

            return result;
        }
    }
}
