/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.app;

import org.apache.abdera.protocol.Request;
import org.apache.abdera.protocol.Resolver;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.context.EmptyResponseContext;
import org.apache.abdera.protocol.server.impl.SimpleTarget;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.app.targets.ResourceTarget;
import org.wso2.carbon.registry.app.targets.ResponseTarget;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that is capable of resolving requests made via the Atom API, and identify how to process
 * them. Some of the target types are provided by Abdera itself, whereas some others are defined.
 */
public class RegistryResolver implements Resolver<Target> {

    private static Log log = LogFactory.getLog(RegistryResolver.class);

    /**
     * The target type for tags.
     */
    public static final TargetType TAGS_TYPE = TargetType.get("tags", true);

    /**
     * The target type for logs.
     */
    public static final TargetType LOGS_TYPE = TargetType.get("logs", true);

    /**
     * The target type for ratings.
     */
    public static final TargetType RATINGS_TYPE = TargetType.get("ratings", true);

    /**
     * The target type for rename operations.
     */
    public static final TargetType RENAME_TYPE = TargetType.get("rename", true);

    /**
     * The target type for copy operations.
     */
    public static final TargetType COPY_TYPE = TargetType.get("copy", true);

    /**
     * The target type for move operations.
     */
    public static final TargetType MOVE_TYPE = TargetType.get("move", true);

    /**
     * The target type for comments.
     */
    public static final TargetType COMMENTS_TYPE = TargetType.get("comments", true);

    /**
     * The target type for tag urls.
     */
    public static final TargetType TAG_URL_TYPE = TargetType.get("tagURL", true);

    /**
     * The target type for associations.
     */
    public static final TargetType ASSOCIATIONS_TYPE = TargetType.get("associations", true);

    /**
     * The target type for restore operations.
     */
    public static final TargetType RESTORE_TYPE = TargetType.get("restore", true);

    /**
     * The target type for aspects.
     */
    public static final TargetType ASPECT_TYPE = TargetType.get("aspect", true);

    /**
     * The target type for versions.
     */
    public static final TargetType VERSIONS_TYPE = TargetType.get("versions", true);

    /**
     * The target type for check points.
     */
    public static final TargetType CHECKPOINT_TYPE = TargetType.get("checkpoint", true);

    /**
     * The target type for queries..
     */
    public static final TargetType QUERY_TYPE = TargetType.get("query", true);

    /**
     * The target type for import requests.
     */
    public static final TargetType IMPORT_TYPE = TargetType.get("import", true);

    /**
     * The target type for delete requests.
     */
    public static final TargetType DELETE_TYPE = TargetType.get("delete", true);

    /**
     * The target type for custom collections.
     */
    public static final TargetType COLLECTION_CUSTOM_TYPE = TargetType.get("col-custom", true);

    /**
     * The target type for dump.
     */
    public static final TargetType DUMP_TYPE = TargetType.get("dump", true);

    private EmbeddedRegistryService embeddedRegistryService;
    private String basePath;

    public RegistryResolver(EmbeddedRegistryService embeddedRegistryService, String basePath) {
        this.embeddedRegistryService = embeddedRegistryService;
        this.basePath = basePath;
    }

    private static Map<String, TargetType> types;

    static {
        types = new HashMap<String, TargetType>();
        types.put("tags", TAGS_TYPE);
        types.put("logs", LOGS_TYPE);
        types.put("ratings", RATINGS_TYPE);
        types.put("comments", COMMENTS_TYPE);
        types.put("rename", RENAME_TYPE);
        types.put("copy", COPY_TYPE);
        types.put("move", MOVE_TYPE);
        types.put("tagURL", TAG_URL_TYPE);
        types.put("associations", ASSOCIATIONS_TYPE);
        types.put("restore", RESTORE_TYPE);
        types.put("versions", VERSIONS_TYPE);
        types.put("checkpoint", CHECKPOINT_TYPE);
        types.put("query", QUERY_TYPE);
        types.put("application/resource-import", IMPORT_TYPE);

        types.put("dump", DUMP_TYPE);
    }

    /**
     * Method to identify the response target for the request.
     *
     * @param request the request.
     *
     * @return the response target.
     */
    public Target resolve(Request request) {
        RequestContext context = (RequestContext) request;
        final ServletRequestContext requestContext;
        if (context instanceof ServletRequestContext) {
            requestContext = (ServletRequestContext) request;
        } else {
            requestContext = null;
        }
        if (embeddedRegistryService == null) {
            if (requestContext != null) {
                embeddedRegistryService =
                        (EmbeddedRegistryService) requestContext.getRequest().getSession()
                                .getServletContext().getAttribute("registry");
            }
            if (embeddedRegistryService == null) {
                String msg = "Error in retrieving the embedded registry service.";
                log.error(msg);
            }
        }

        //TODO (reg-sep) 
        UserRegistry registry = null;
        String uri = context.getUri().toString();
        String loggedIn = null;
        if (requestContext != null) {
            loggedIn = ((ServletRequestContext) request).getRequest().getParameter("loggedIn");
        }
        if (loggedIn != null) {
            String loggedUser =
                    (String) requestContext.getRequest().getSession().getServletContext()
                            .getAttribute("logged-user");
            try {
                registry = embeddedRegistryService.getRegistry(loggedUser);
                uri = uri.substring(0, uri.lastIndexOf('?'));
            } catch (RegistryException e) {
                final StringResponseContext response =
                        new StringResponseContext("Unauthorized",
                                HttpURLConnection.HTTP_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                return new ResponseTarget(context, response);
            }
        }

        if (registry == null) {
            // Set up secure registry instance
            String authorizationString = request.getAuthorization();
            if (authorizationString != null) {
                // splitting the Authorization string "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
                String values[] = authorizationString.split("\\ ");
                if (values == null || values.length == 0) {
                    final StringResponseContext response =
                            new StringResponseContext("Unauthorized",
                                    HttpURLConnection.HTTP_UNAUTHORIZED);
                    response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                    return new ResponseTarget(context, response);
                } else if ("Basic".equals(values[0])) {
                    try {
                        // Decode username/password
                        authorizationString = new String(Base64.decode(values[1]));
                        values = authorizationString.split("\\:");
                        String userName = values[0];
                        String password = values[1];
                        String tenantDomain =
                                (String) ((ServletRequestContext) request).getRequest().
                                        getAttribute(MultitenantConstants.TENANT_DOMAIN);
                        int tenantId;
                        String userNameAlong;
                        if (tenantDomain == null || 
                        		MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                            tenantId = getTenantId(userName);
                            userNameAlong = getUserName(userName);
                        } else {
                            tenantId = getTenantIdFromDomain(tenantDomain);
                            userNameAlong = userName;
                        }
                        registry = embeddedRegistryService.getRegistry(userNameAlong,
                                password, tenantId);
                    } catch (Exception e) {
                        final StringResponseContext response =
                                new StringResponseContext("Unauthorized",
                                        HttpURLConnection.HTTP_UNAUTHORIZED);
                        response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                        return new ResponseTarget(context, response);
                    }
                } else {
                    // TODO - return an ExceptionTarget which contains the authentication problem
                    // return new ExceptionTarget(400, "Only basic authentication is supported!");
                    return null;
                }
            } else {
                String tenantDomain = (String) requestContext.getRequest().
                        getAttribute(MultitenantConstants.TENANT_DOMAIN);
                int calledTenantId = MultitenantConstants.SUPER_TENANT_ID;
                if (tenantDomain != null &&
                		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    if (RegistryContext.getBaseInstance().getRealmService() == null) {
                        String msg = "Error in getting the tenant manager. " +
                                "The realm service is not available.";
                        log.error(msg);
                        return new ResponseTarget(context, new EmptyResponseContext(400, msg));
                    }
                    TenantManager tenantManager =
                            RegistryContext.getBaseInstance().getRealmService().
                                    getTenantManager();
                    try {
                        calledTenantId = tenantManager.getTenantId(tenantDomain);
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        String msg =
                                "Error in converting tenant domain to the id for tenant domain: " +
                                        tenantDomain + ".";
                        log.error(msg, e);
                        return new ResponseTarget(context, new EmptyResponseContext(400, msg));
                    }
                    try {
                        if (!tenantManager.isTenantActive(calledTenantId)) {
                            // the tenant is not active.
                            String msg =
                                    "The tenant is not active. tenant domain: " + tenantDomain +
                                            ".";
                            log.error(msg);
                            return new ResponseTarget(context, new EmptyResponseContext(400, msg));
                        }
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        String msg =
                                "Error in converting tenant domain to the id for tenant domain: " +
                                        tenantDomain + ".";
                        log.error(msg, e);
                        return new ResponseTarget(context, new EmptyResponseContext(400, msg));
                    }
                    RegistryContext.getBaseInstance().
                            getRealmService().getBootstrapRealmConfiguration();

                }
                String anonUser = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                try {
                    registry = embeddedRegistryService.getRegistry(anonUser, calledTenantId);
                } catch (RegistryException e) {
                    String msg = "Error in creating the registry.";
                    log.error(msg, e);
                    return new ResponseTarget(context, new EmptyResponseContext(400, msg));
                }
            }
        }

        // Squirrel this away so the adapter can get it later (after all that work we just did!)
        context.setAttribute("userRegistry", registry);

        final String method = context.getMethod();

        /*
        Following code moved further down
        try {
            uri = URLDecoder.decode(uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            return null;
        } */

        if (uri.startsWith(RegistryConstants.PATH_SEPARATOR
                + RegistryConstants.REGISTRY_INSTANCE
                + RegistryConstants.PATH_SEPARATOR
                + RegistryConstants.REGISTRY_INSTANCE)) {
            //if ROOT war is renamed to 'registry', uri will look like following,
            //'/registry/registry/foo/bar'
            //Hence,we need to remove the first 'registry'
            uri = uri.replaceFirst(RegistryConstants.PATH_SEPARATOR
                    + RegistryConstants.REGISTRY_INSTANCE, "");
        }

        // URI will start with the baseURI, which we need to strip off.

        String[] excludeStartArr = {basePath + APPConstants.ATOM,
                basePath + APPConstants.RESOURCE, basePath + "/tags"};

        if (basePath == null) {
            log.error("Base path is null. Aborting the operation.");
            final StringResponseContext response =
                    new StringResponseContext("Internal Server Error",
                            HttpURLConnection.HTTP_INTERNAL_ERROR);
            return new ResponseTarget(context, response);

        } else if (!basePath.equals("")) {
            for (String excludeStartStr : excludeStartArr) {
                // URI will start with the baseURI, which we need to strip off.
                if (uri.indexOf(excludeStartStr) > -1 &&
                        uri.length() > uri.indexOf(excludeStartStr) + basePath.length()) {
                    uri = uri.substring(uri.indexOf(excludeStartStr) + basePath.length());
                    break;
                }
            }
        }


        if (!uri.startsWith(
                RegistryConstants.PATH_SEPARATOR + RegistryConstants.REGISTRY_INSTANCE)) {
            uri = uri.substring(uri.indexOf(RegistryConstants.PATH_SEPARATOR));
        }
        context.setAttribute("pathInfo", uri);
        String[] parts = splitPath(uri); // splits with "\;"
        boolean hasColon = false;
        TargetType type = null;

        // Let's just see if this is an import first - in which case we can just send it
        // on through.

        if (parts.length > 1) {
            String discriminator = parts[1];

            // If this is a version request, don't do anything special.  Otherwise process.
            if (discriminator.startsWith("version:")) {
                if (parts.length > 2) {
                    // Make sure this is a restore.
                    if (parts[2].equals("restore")) {
                        type = RESTORE_TYPE;
                        uri = parts[0] + RegistryConstants.URL_SEPARATOR + parts[1];
                    } else if (parts[2].equals(APPConstants.ASSOCIATIONS)) {
                        type = ASSOCIATIONS_TYPE;
                        uri = parts[0] + RegistryConstants.URL_SEPARATOR + parts[1];
                    } else {
                        // There's an extra semicolon here somewhere.
                        return null;
                    }
                }
            } else {
                // Store the split URL for later
                context.setAttribute(APPConstants.PARAMETER_SPLIT_PATH, parts);
                int idx = discriminator.indexOf('?');
                if (idx > -1) {
                    discriminator = discriminator.substring(0, idx);
                }

                String suffix = null;
                idx = discriminator.indexOf(':');
                if (idx > -1) {
                    suffix = discriminator.substring(idx + 1, discriminator.length());
                    discriminator = discriminator.substring(0, idx);
                    hasColon = true;
                }

                if (discriminator.startsWith("aspect")) {
                    type = ASPECT_TYPE;
                } else {
                    type = types.get(discriminator);
                }

                if (discriminator.equals("tag") && method.equals("DELETE") && hasColon) {
                    context.setAttribute("tagName", suffix);
                    type = DELETE_TYPE;
                } else if (discriminator.equals("comment") && method.equals("DELETE") && hasColon) {
                    context.setAttribute("commentId", suffix);
                    type = DELETE_TYPE;
                } else if (discriminator.equals("ratings") && hasColon) {
                    context.setAttribute("ratingUser", suffix);
                    type = RATINGS_TYPE;
                }

                // If we have a discriminator that we don't understand, return a 404
                if (type == null) {
                    return null;
                }

                // For the rest of this code, we'll want the "raw" resource URI
                if (!hasColon || !(type.equals(COMMENTS_TYPE) || type.equals(RATINGS_TYPE) ||
                        type.equals(TAGS_TYPE))) {
                    uri = parts[0];
                }
                if (hasColon && type.equals(TAGS_TYPE)) {
                    type = null;
                }
            }
        }

        int idx = uri.indexOf('?');
        if (idx > -1) {
            String queryString = uri.substring(idx + 1, uri.length());
            context.setAttribute("queryString", queryString);
            uri = uri.substring(0, idx);
        }

        try {
            uri = URLDecoder.decode(uri, RegistryConstants.DEFAULT_CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            return null;
        }

        boolean isMedia = false;
        if (uri.startsWith(APPConstants.RESOURCE)) {
            uri = uri.substring(APPConstants.RESOURCE.length());
            isMedia = true;
        } else if (uri.startsWith(APPConstants.ATOM)) {
            uri = uri.substring(APPConstants.ATOM.length());
        } else if (uri.startsWith("/tags") && (uri.length() == 5 || uri.charAt(5) == '/')) {
            return new SimpleTarget(TAG_URL_TYPE, context);
        } else {
            return null;
        }

        if (uri.length() == 0) {
            uri = "/";
        }

        // See if we're asking for a paginated collection
        String startParam = context.getParameter("start");
        String pageLenParam = context.getParameter("pageLen");
        int start = (startParam == null) ? -1 : Integer.parseInt(startParam);
        int pageLen = (pageLenParam == null) ? -1 : Integer.parseInt(pageLenParam);

        Resource resource = null;
        if (type != null && type.equals(DUMP_TYPE) &&
                method != null && method.equals("POST")) {
            // for restoring a dump we don't need to have available resource
            // here we will create a fake resource to store the path

            resource = new ResourceImpl();
            ((ResourceImpl) resource).setPath(uri);
        } else {
            // in a restore, path don't need to exist.
            CurrentSession.setUserRealm(registry.getUserRealm());
            CurrentSession.setUser(registry.getUserName());
            try {
                if (!AuthorizationUtils.authorize(RegistryUtils.getAbsolutePath(
                        registry.getRegistryContext(), uri),
                        ActionConstants.GET)) {
                    final StringResponseContext response =
                            new StringResponseContext("Unauthorized",
                                    HttpURLConnection.HTTP_UNAUTHORIZED);
                    response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                    return new ResponseTarget(context, response);
                } else if (start > -1 || pageLen > -1) {
                    resource = registry.get(uri, start, pageLen);
                } else {
                    resource = registry.get(uri);
                }
            } catch (AuthorizationFailedException e) {
                final StringResponseContext response =
                        new StringResponseContext("Unauthorized",
                                HttpURLConnection.HTTP_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Basic realm=\"WSO2-Registry\"");
                return new ResponseTarget(context, response);
            } catch (ResourceNotFoundException e) {
                // If this is a straight-ahead POST to a non-existent directory, create it?
                if (method.equals("POST") && parts.length == 1) {
                    // Need to create it.
                    try {
                        Collection c = registry.newCollection();
                        registry.put(uri, c);
                        resource = registry.get(uri);
                    } catch (RegistryException e1) {
                        log.error(e1);
                        return null;
                    }
                }
                if (resource == null) {
                    return null;
                }
            } catch (RegistryException e) {
                return null; // return 404
            } finally{
            	  CurrentSession.removeUser();
                  CurrentSession.removeUserRealm();
            }

            if (method.equals("GET")) {
                // eTag based conditional get
                String ifNonMatchValue = context.getHeader("if-none-match");
                if (ifNonMatchValue != null) {
                    String currentETag = Utils.calculateEntityTag(resource);
                    if (ifNonMatchValue.equals(currentETag)) {
                        /* the version is not modified */
                        ResponseContext response = new StringResponseContext("Not Modified",
                                HttpURLConnection.HTTP_NOT_MODIFIED);
                        return new ResponseTarget(context, response);
                    }
                }

                // date based conditional get
                long ifModifiedSinceValue = 0;
                Date ifModifiedSince = context.getDateHeader("If-Modified-Since");
                if (ifModifiedSince != null) {
                    ifModifiedSinceValue = ifModifiedSince.getTime();
                }

                if (ifModifiedSinceValue > 0) {
                    long lastModifiedValue = resource.getLastModified().getTime();
                    // convert the time values from milliseconds to seconds
                    ifModifiedSinceValue /= 1000;
                    lastModifiedValue /= 1000;

                    /* condition to check we have latest updates in terms of dates */
                    if (ifModifiedSinceValue >= lastModifiedValue) {
                        /* no need to response with data */
                        ResponseContext response = new StringResponseContext("Not Modified",
                                HttpURLConnection.HTTP_NOT_MODIFIED);
                        return new ResponseTarget(context, response);
                    }
                }
            }
        }
        context.setAttribute("MyResolver", this);
        if (type == null) {
            if (method.equals("DELETE")) {
                // Unfortunately, deletions aren't quite handled the way we want them
                // in AbstractEntityCollectionProvider, so for now we're using an
                // extensionRequest to get it done.
                type = DELETE_TYPE;
            } else {
                if (resource instanceof Collection) {
                    if (method.equals("HEAD") || method.equals("PUT")) {
                        // Abdera doesn't handle HEAD or PUT on collections yet. this should
                        // go away once that's fixed!
                        type = COLLECTION_CUSTOM_TYPE;
                    } else {
                        type = TargetType.TYPE_COLLECTION;
                    }
                } else {
                    type = isMedia ? TargetType.TYPE_MEDIA : TargetType.TYPE_ENTRY;
                }
            }
        }

        return new ResourceTarget(type, context, resource);
    }


    /**
     * This method will split the path into parts around the path separator, returning an array of
     * the parts.
     *
     * @param path URL path
     *
     * @return String array of the split string
     */
    private String[] splitPath(String path) {
        return path.split("\\" + RegistryConstants.URL_SEPARATOR);
    }

    @SuppressWarnings("unused")
    public String getBasePath() {
        return basePath;
    }

    public static String getUserName(String userNameWithDomain) throws RegistryException {
        int atIndex = userNameWithDomain.indexOf('@');
        if (atIndex == -1) {
            // no domain in the inserted test
            return userNameWithDomain;
        }
        return userNameWithDomain.substring(atIndex + 1);
    }

    public static int getTenantId(String userNameWithDomain) throws RegistryException {
        int atIndex = userNameWithDomain.indexOf('@');
        if (atIndex == -1) {
            // no domain 
            return MultitenantConstants.SUPER_TENANT_ID;
        }
        String domain = userNameWithDomain.substring(atIndex + 1, userNameWithDomain.length());
        return getTenantIdFromDomain(domain);
    }

    private static int getTenantIdFromDomain(String domain) throws RegistryException {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        RealmService realmService = registryContext.getRealmService();
        if (realmService == null) {
            String msg = "Error in getting the tenant manager. The realm service is not available.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        TenantManager tenantManager = realmService.getTenantManager();
        try {
            return tenantManager.getTenantId(domain);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error in getting the tenant manager.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

}
