/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.app;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.*;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.context.AbstractResponseContext;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.context.EmptyResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.EntityTag;
import org.apache.abdera.util.MimeTypeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.targets.ResourceTarget;
import org.wso2.carbon.registry.app.targets.ResponseTarget;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * This is the component that provides the business logic which runs on the Abdera instance. The
 * Provider will use it's Workspace Manager to determine which Collection Adapter to dispatch a
 * request to. Once an adapter is selected, the Provider will determine what kind of request is
 * being made and will forward the request on to the appropriate Collection Adapter method.
 * <p/>
 * The Registry Adapter is a specialized Collection Adapter designed to expose registry logic within
 * an APP world.
 */
@SuppressWarnings("deprecation")
// TODO: get rid of the global suppression on deprecation warnings, and fix the use of
// URLEncoder.encode, by providing the correct encoding.
public class RegistryAdapter
        extends AbstractEntityCollectionAdapter<Resource> {

    private static final Log log = LogFactory.getLog(RegistryAdapter.class);
    private static final String SANITIZE_PATTERN = "[^A-Za-z0-9\\.\\%!\\\\'()*+,=_\\s\\?]+";

    private Factory factory = new Abdera().getFactory();
    private int curResource = 1;

    @Deprecated
    @SuppressWarnings("unused")
    public RegistryAdapter(RegistryResolver resolver,
                           EmbeddedRegistryService embeddedRegistryService)
            throws RegistryException {
    }

    /**
     * Default constructor
     */
    public RegistryAdapter() {
    }


    /**
     * Handle anything out of the ordinary Abdera-supported world.
     * <p/>
     * This method basically acts as a clearing house for all of the Registry-specific URLs that
     * result in special processing, like ...;tags or ...;comments, etc.  The resolver will have
     * already parsed the URL and matched it with a particular custom TargetType, so in here we
     * switch control based on the TargetType to a meta data processing routine.
     *
     * @param request the RequestContext containing all the relevant info
     *
     * @return a ResponseContext indicating the disposition of the request
     */
    @SuppressWarnings({"ConstantConditions"})
    public ResponseContext extensionRequest(RequestContext request) {
        Target target = request.getTarget();
        final TargetType type = target.getType();
        if (!(target instanceof ResourceTarget)) {
            if (type.equals(ResponseTarget.RESPONSE_TYPE)) {
                return ((ResponseTarget) target).getResponse();
            }
            // Deal with non-resource URLs, like "/tags..."
            if (type.equals(RegistryResolver.TAG_URL_TYPE)) {
                return processTagURLRequest(request);
            }
        }
        Resource resource = ((ResourceTarget) target).getResource();
        String path = resource.getPath();
        if (type.equals(RegistryResolver.TAGS_TYPE)) {
            return processTagsRequest(request, path);
        }
        if (type.equals(RegistryResolver.LOGS_TYPE)) {
            return processLogsRequest(request, path);
        }
        if (type.equals(RegistryResolver.RATINGS_TYPE)) {
            return processRatingsRequest(request, path);
        }
        if (type.equals(RegistryResolver.VERSIONS_TYPE)) {
            return processVersionsRequest(request, path);
        }
        if (type.equals(RegistryResolver.RENAME_TYPE)) {
            return processRenameRequest(request, path);
        }
        if (type.equals(RegistryResolver.COPY_TYPE)) {
            return processCopyRequest(request, path);
        }
        if (type.equals(RegistryResolver.MOVE_TYPE)) {
            return processMoveRequest(request, path);
        }
        if (type.equals(RegistryResolver.DELETE_TYPE)) {
            return processDeleteRequest(request, path);
        }
        if (type.equals(RegistryResolver.QUERY_TYPE)) {
            return processQueryRequest(request, path);
        }
        if (type.equals(RegistryResolver.COLLECTION_CUSTOM_TYPE)) {
            if (request.getMethod().equals(APPConstants.HTTP_HEAD)) {
                // Doing a HEAD on a collection
                try {
                    return buildHeadEntryResponse(request, getId(resource),
                            resource.getLastModified());
                } catch (ResponseContextException e) {
                    log.error("HEAD request for collection failed", e);
                    return e.getResponseContext();
                }
            }

            // Must be a PUT.
            return putCollection(request, path);
        }
        if (type.equals(RegistryResolver.ASSOCIATIONS_TYPE)) {
            String temp = resource.getPermanentPath();
            if (temp == null) {
                temp = resource.getPath();
            }
            return processAssociationRequest(request, temp);
        }
        if (type.equals(RegistryResolver.COMMENTS_TYPE)) {
            return processCommentsRequest(request, path);
        }
        if (type.equals(RegistryResolver.RESTORE_TYPE)) {
            try {
                getSecureRegistry(request).restoreVersion(resource.getPermanentPath());
            } catch (RegistryException e) {
                return new StackTraceResponseContext(e);
            }
            return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
        }
        if (type.equals(RegistryResolver.ASPECT_TYPE)) {
            return processAspectRequest(request, path);
        }
        if (type.equals(RegistryResolver.CHECKPOINT_TYPE)) {
            return processCheckpointRequest(request, path);
        }
        // Deal with imports
        if (type.equals(RegistryResolver.IMPORT_TYPE)) {
            return processImportRequest(request, path);
        }

        // handle dump, restore request.
        if (type.equals(RegistryResolver.DUMP_TYPE)) {
            return processDumpRequest(request, path);
        }

        return null;
    }

    /**
     * Handle PUT of a collection (a feed).
     *
     * @param request active RequestContext
     * @param path    the resource path
     *
     * @return a ResponseContext which could contain success or an error
     */
    private ResponseContext putCollection(RequestContext request, String path) {
        try {
            final Registry secureRegistry = getSecureRegistry(request);
            Collection resource = secureRegistry.newCollection();
            Feed feed = (Feed) request.getDocument().getRoot();
            // Just updating meta data
            org.wso2.carbon.registry.app.Properties properties =
                    feed.getExtension(PropertyExtensionFactory.PROPERTIES);
            RemoteRegistry.createPropertiesFromExtensionElement(properties, resource);
            resource.setDescription(feed.getSubtitle());
            if (feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE)) != null) {
                resource.setMediaType(
                        feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE)));
            }
            secureRegistry.put(path, resource);
        } catch (Exception e) {
            return new StackTraceResponseContext(e);
        }

        EmptyResponseContext response = new EmptyResponseContext(HttpURLConnection.HTTP_OK);
        try {
            response.setLocation(
                    URLDecoder.decode(getAtomURI(path, request), RegistryConstants.DEFAULT_CHARSET_ENCODING).replaceAll(" ", "+"));
        } catch (UnsupportedEncodingException e) {
            // no action
        }
        return response;
    }

    // Method to obtain the atom uri for the given path.
    private String getAtomURI(String path, RequestContext context) {
        return getAbsoluteBase(context) + APPConstants.ATOM + path;
    }

    // Method to obtain the base uri.
    private String getAbsoluteBase(RequestContext context) {
        String uri = context.getBaseUri().trailingSlash().toString();
        uri = uri.substring(0, uri.length() - 1);  // remove trailing slash
        if (!uri.endsWith("registry")) {
            uri += "/registry";
        }
        return uri;
    }

    // Method to obtain a new feed.
    private Feed getNewFeed(String id) {
        Feed feed = factory.newFeed();
        feed.setId(id);
        feed.setUpdated(new Date());
        return feed;
    }

    /**
     * Utility function to parse query string TODO: Isn't there a standard way to do this?
     *
     * @param query query string to parse
     *
     * @return a Map of name -> value for each parameter
     */
    public static Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new HashMap<String, String>();
        if (query == null) {
            return map;
        }

        StringTokenizer st = new StringTokenizer(query, "?&=", true);
        String previous = "";
        while (st.hasMoreTokens()) {
            String current = st.nextToken();
            if ("=".equals(current)) {
                try {
                    map.put(URLDecoder.decode(previous, RegistryConstants.DEFAULT_CHARSET_ENCODING),
                            URLDecoder.decode(st.nextToken(), RegistryConstants.DEFAULT_CHARSET_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    break;
                }
            } else if (!("?".equals(current)) && !("&".equals(current))) {
                previous = current;
            }
        }
        return map;
    }

    // Method to obtain the absolute uri for the given path.
    private String getAbsolutePath(RequestContext request, String path) {
        return request.getBaseUri() + "atom" + path;
    }

    private ResponseContext processAspectRequest(RequestContext request, String path) {
        String method = request.getMethod();
        Registry registry;
        try {                                                    
            registry = getSecureRegistry(request);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }

        String discriminator =
                ((String[]) request.getAttribute(RequestContext.Scope.REQUEST, APPConstants.PARAMETER_SPLIT_PATH))[1];
        if (discriminator.equals("aspects")) {
            if (method.equals(APPConstants.HTTP_POST)) {
                // Associate
                try {
                    String aspect = readToString(request.getInputStream());
                    registry.associateAspect(path, aspect);
                } catch (Exception e) {
                    return new StackTraceResponseContext(e);
                }
                return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
            }
            return null;
        }
        // There should be an aspect name - parse it out
        assert (discriminator.charAt(7) == '(');
        int right = discriminator.indexOf(')');
        assert (right > -1);
        String aspectName = discriminator.substring(7, right);
        String action;
        if (discriminator.length() > right + 1) {
            // Got an action too?
            assert (method.equals(APPConstants.HTTP_POST));
            action = discriminator.substring(right + 1);
            try {
                registry.invokeAspect(path, aspectName, action);
            } catch (RegistryException e) {
                return new StackTraceResponseContext(e);
            }
            // TODO - do we have to read the request fully?
            return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
        }
        assert (method.equals(APPConstants.HTTP_GET));

        // Return list of available actions.
        Feed feed = getNewFeed("tag:aspectActions"); // TODO - fix ID
        String[] actions;
        try {
            actions = registry.getAspectActions(path, aspectName);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        for (String a : actions) {
            Entry e = factory.newEntry();
            e.setId("tag:aspectAction(" + a + ")");
            e.setContent(a);
            feed.addEntry(e);
        }
        return buildResponseContextFromFeed(feed);
    }

    private ResponseContext processCommentsRequest(RequestContext request, String path) {
        final String method = request.getMethod();
        if (method.equals(APPConstants.HTTP_GET)) {
            int colonIdx = request.getUri().toString().indexOf(':');
            if (colonIdx > -1) {
                return getEntry(request);
            } else {
                // Return comments feed
                return getFeed(request);
            }
        } else {
            try {
                final Registry secureRegistry = getSecureRegistry(request);
                if (method.equals(APPConstants.HTTP_POST)) {
                    // Accept either Atom or plain text for comments
                    org.wso2.carbon.registry.core.Comment comment =
                            new org.wso2.carbon.registry.core.Comment();
                    final String contentType = request.getContentType().toString();
                    if (request.isAtom()) {
                        Entry entry = (Entry) request.getDocument().getRoot();
                        comment.setText(entry.getContent());
                        comment.setUser(entry.getAuthor().getName());
                        if (entry.getUpdated() != null) {
                            comment.setCreatedTime(entry.getUpdated());
                        }
                    } else if (contentType.equals("text/plain")) {
                        InputStream is = request.getInputStream();
                        String text = readToString(is);
                        comment.setText(text);
                    }
                    String commentPath = secureRegistry.addComment(path, comment);
                    EmptyResponseContext responseContext = new EmptyResponseContext(
                            HttpURLConnection.HTTP_OK);
                    try {
                        responseContext.setLocation(URLDecoder
                                .decode(getAtomURI(commentPath, request), RegistryConstants.DEFAULT_CHARSET_ENCODING).replaceAll(" ",
                                "+"));
                    } catch (UnsupportedEncodingException e) {
                        // no action
                    }
                    return responseContext;
                } else if (method.equals(APPConstants.HTTP_PUT)) {
                    Entry entry = (Entry) request.getDocument().getRoot();
                    String text = entry.getContent();
                    secureRegistry.editComment(path, text);
                    return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
                } else if (method.equals(APPConstants.HTTP_DELETE)) {
                    secureRegistry.delete(path);
                    return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
                }
            } catch (Exception e) {
                return new StackTraceResponseContext(e);
            }
        }
        // unsupported method
        return new EmptyResponseContext(HttpURLConnection.HTTP_NOT_ACCEPTABLE);
    }

    private ResponseContext processAssociationRequest(RequestContext request, String path) {
        if (request.getMethod().equals(APPConstants.HTTP_GET)) {
            String type = null;
            String uri = request.getUri().toString();
            if (uri.indexOf(RegistryConstants.VERSION_SEPARATOR) > 0) {
                uri = uri.substring(uri.indexOf(RegistryConstants.VERSION_SEPARATOR) + RegistryConstants.VERSION_SEPARATOR.length());
            }
            int idx = uri.lastIndexOf(':');
            if (idx > -1) {
                type = uri.substring(idx + 1, uri.length());
            }
            // Return associations feed
            Association[] associations;
            try {
                if (type != null) {
                    associations = getSecureRegistry(request).getAssociations(path, type);
                } else {
                    associations = getSecureRegistry(request).getAllAssociations(path);
                }
            } catch (RegistryException e) {
                return new StackTraceResponseContext(e);
            }

            // Build feed
            Feed feed = getNewFeed("tag:associationFeed");
            for (Association association : associations) {
                final String associationType = association.getAssociationType();
                final String destinationPath = association.getDestinationPath();
                final String sourcePath = association.getSourcePath();

                Entry e = factory.newEntry();
                e.setTitle("'" + associationType + "' association");
                e.setId("tag:association"); // todo - ID should be unique
                if (destinationPath.startsWith("http://")) {
                    e.addLink(URLEncoder.encode(destinationPath), Link.REL_ALTERNATE);
                } else {
                    e.addLink(getAbsolutePath(request, destinationPath).replaceAll(" ", "+"));
                }
                e.setSummary(associationType);
                e.setContent(destinationPath);
                e.setTitle(sourcePath);
                feed.addEntry(e);
            }
            return buildResponseContextFromFeed(feed);
        }
        if (request.getMethod().equals(APPConstants.HTTP_POST)) {
            // Adding an association, expecting XML that looks like
            // <reg:association type="type">http://associationPath</reg:association>
            try {
                Element assocEl = request.getDocument().getRoot();
                if (!APPConstants.QN_ASSOC.equals(assocEl.getQName())) {
                    return new EmptyResponseContext(400, "Bad association element");
                }
                String type = assocEl.getAttributeValue(APPConstants.ASSOC_TYPE);
                String assocPath = assocEl.getText();
                getSecureRegistry(request).addAssociation(path, assocPath, type);
                return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
            } catch (Exception e) {
                return new StackTraceResponseContext(e);
            }
        }
        if (request.getMethod().equals(APPConstants.HTTP_DELETE)) {
            String destinationPath = request.getHeader("Destination");
            String type = request.getHeader("AssociationType");
            try {
                getSecureRegistry(request).removeAssociation(path, destinationPath, type);
                return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
            } catch (Exception e) {
                return new StackTraceResponseContext(e);
            }
        }

        return null;
    }

    private ResponseContext processTagURLRequest(RequestContext request) {
        String uri = (String) request.getAttribute(RequestContext.Scope.REQUEST, "pathInfo");
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        if (uri.equals("/tags")) {
            // TODO - implement this!
            return new StringResponseContext("this is a list of tags with links", HttpURLConnection.HTTP_OK);
        }
        String tag = uri.substring(6);
        Feed feed = getNewFeed("http://wso2.org/jdbcregistry/TagPaths");
        feed.setTitle("Resource path for " + tag);
        TaggedResourcePath[] paths;
        try {
            paths = getSecureRegistry(request).getResourcePathsWithTag(tag);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        for (TaggedResourcePath tagPath : paths) {
            Entry entry = factory.newEntry();
            String path = tagPath.getResourcePath();
            entry.setTitle(path);
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "taggings"),
                    "" + tagPath.getTagCount());
            Map<String, String> tagCounts = tagPath.getTagCounts();
            java.util.Properties properties = new java.util.Properties();
            for (Map.Entry<String, String> e : tagCounts.entrySet()) {
                properties.put(e.getKey(), Arrays.asList(e.getValue()));
            }
            /*Iterator<String> iCounts = tagCounts.keySet().iterator();
            while (iCounts.hasNext()) {
                String key = iCounts.next();
                String count = tagCounts.get(key);
                properties.put(key, Arrays.asList(count));
            }*/
            RemoteRegistry.addPropertyExtensionElement(properties,
                    factory,
                    entry,
                    PropertyExtensionFactory.TAGS,
                    PropertyExtensionFactory.TAG);
//                entry.addSimpleExtension(new QName("tagCounts"), "" + tagPath.getTagCount())
//            entry.addLink(baseUri + "atom" + path, APPConstants.PATH);
//            entry.addLink(baseUri + "atom" + path);
            feed.addEntry(entry);
        }

        return buildResponseContextFromFeed(feed);
    }

    private ResponseContext processImportRequest(RequestContext request, String path) {
        String slug = request.getSlug();

        String suggestedPath = path + getGoodSlug(path, slug, request);

        Resource resource = new ResourceImpl();
        Document<Entry> doc;
        try {
            doc = request.getDocument();
        } catch (IOException e) {
            return new StackTraceResponseContext(e);
        }
        org.wso2.carbon.registry.app.Properties properties =
                doc.getRoot().getExtension(PropertyExtensionFactory.PROPERTIES);
        RemoteRegistry.createPropertiesFromExtensionElement(properties, resource);
        resource.setMediaType(request.getContentType().toString());

//        The resource can come with a UUID. Hence retrieving it here
       if(doc.getRoot().getSimpleExtension(APPConstants.QN_UUID_TYPE) != null){
           resource.setUUID(doc.getRoot().getSimpleExtension(APPConstants.QN_UUID_TYPE));
       }

        String location;
        try {
//            InputStream is = request.getInputStream();
//            String importURL = readToString(is);
//            if (importURL.contains("=")) {
//                importURL = importURL.substring(importURL.indexOf('=') + 1);
//            }
//            importURL = importURL.substring(0, importURL.lastIndexOf(';'));
            // removes the "application/resource-import" string from incoming path 
            String importURL = request.getParameter("importURL");
            if (importURL.endsWith(";application/resource-import")) {
                importURL = importURL.substring(0, importURL.length() - ";application/resource-import".length());
            }
            location = getSecureRegistry(request).importResource(suggestedPath,
                    importURL,
                    resource);
        } catch (Exception e) {
            return new StringResponseContext(e, HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        ResponseContext rc = new EmptyResponseContext(HttpURLConnection.HTTP_OK);
        try {
            rc.setLocation(
                    URLDecoder.decode(getAtomURI(location, request), RegistryConstants.DEFAULT_CHARSET_ENCODING).replaceAll(" ", "+"));
        } catch (UnsupportedEncodingException e) {
            // no action
        }

        return rc;
    }

    private ResponseContext processRenameRequest(RequestContext request, String path) {
        if (!request.getMethod().equals(APPConstants.HTTP_POST)) {
            ResponseContext rc = new EmptyResponseContext(405, "Method not allowed");
            rc.setAllow(APPConstants.HTTP_POST);
            return rc;
        }

        try {
            InputStream is = request.getInputStream();
            String newPath = readToString(is);
            getSecureRegistry(request).rename(path, newPath);
        } catch (Exception e) {
            return new StackTraceResponseContext(e);
        }

        return new StringResponseContext("Rename successful.", HttpURLConnection.HTTP_OK);
    }

    private ResponseContext processCopyRequest(RequestContext request, String path) {
        if (!request.getMethod().equals(APPConstants.HTTP_POST)) {
            ResponseContext rc = new EmptyResponseContext(405, "Method not allowed");
            rc.setAllow(APPConstants.HTTP_POST);
            return rc;
        }

        try {
            InputStream is = request.getInputStream();
            String newPath = readToString(is);
            getSecureRegistry(request).copy(path, newPath);
        } catch (Exception e) {
            return new StackTraceResponseContext(e);
        }

        return new StringResponseContext("Copy successful.", HttpURLConnection.HTTP_OK);
    }

    private ResponseContext processMoveRequest(RequestContext request, String path) {
        if (!request.getMethod().equals(APPConstants.HTTP_POST)) {
            ResponseContext rc = new EmptyResponseContext(405, "Method not allowed");
            rc.setAllow(APPConstants.HTTP_POST);
            return rc;
        }

        try {
            InputStream is = request.getInputStream();
            String newPath = readToString(is);
            getSecureRegistry(request).move(path, newPath);
        } catch (Exception e) {
            return new StackTraceResponseContext(e);
        }

        return new StringResponseContext("Move successful.", HttpURLConnection.HTTP_OK);
    }

    private ResponseContext processDeleteRequest(RequestContext request, String path) {
        assert (request.getMethod().equals(APPConstants.HTTP_DELETE));
        String tagName = null;
        String commentId = null;
        try {
            Object temp = request.getAttribute(RequestContext.Scope.REQUEST, "tagName");
            if (temp != null) {
                tagName = URLDecoder.decode((String)temp, RegistryConstants.DEFAULT_CHARSET_ENCODING);
            } else {
                temp = request.getAttribute(RequestContext.Scope.REQUEST, "commentId");
                if (temp != null) {
                    commentId = (String)temp;
                }
            }
        } catch (Exception e) {
            log.error("An exception occurred while processing removeTag request", e);
        }
        try {

            if (tagName != null) {
                getSecureRegistry(request).removeTag(path, tagName);
            } else if (commentId != null) {
                getSecureRegistry(request).removeComment(path + ";comments:" + commentId);
            } else {
                getSecureRegistry(request).delete(path);
            }
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        return new EmptyResponseContext(HttpURLConnection.HTTP_OK);
    }

    private ResponseContext processRatingsRequest(RequestContext request, String path) {
        if (request.getMethod().equals(APPConstants.HTTP_GET)) {
            // Return ratings feed.
            Feed feed = factory.newFeed();
            feed.setUpdated(new Date()); // TODO - updated at last rating
            feed.setId("http://wso2.org/jdbcregistry:averageRating"); // TODO - make real ID
            feed.setTitle("Average Rating for the resource " + path);

            String nodeLink =
                    getAbsolutePath(request, Utils.encodeRegistryPath(path).replaceAll(" ", "+")) +
                            RegistryConstants.URL_SEPARATOR +
                            APPConstants.PARAMETER_RATINGS;
            feed.addLink(nodeLink);
            try {
                Registry myRegistry = getSecureRegistry(request);
                feed.addSimpleExtension(APPConstants.QN_AVERAGE_RATING,
                        "" + myRegistry.getAverageRating(path));
                final String username =
                        (String) request.getAttribute(RequestContext.Scope.REQUEST, "ratingUser");
                String rating = Integer.toString(myRegistry.getRating(path, username));
                if (username != null) {
                    Entry e = factory.newEntry();
                    e.setId("tag:something");
                    e.setContent(rating);
                    e.addLink(rating, APPConstants.PARAMETER_PATH);
                    feed.addEntry(e);
                }
            } catch (RegistryException e) {
                return new StackTraceResponseContext(e);
            }
            return buildResponseContextFromFeed(feed);
        } else if (request.getMethod().equals(APPConstants.HTTP_POST)) {
            // We're trying to rate the resource.
            try {
                InputStream is = request.getInputStream();
                String rateStr = readToString(is);
                int rating = Integer.parseInt(rateStr);
                getSecureRegistry(request).rateResource(path, rating);
                return new StringResponseContext("Resource rated successfully",
                        HttpURLConnection.HTTP_OK);
            } catch (Exception e) {
                return new StackTraceResponseContext(e);
            }
        }
        return null;
    }

    private ResponseContext processQueryRequest(RequestContext request, String path) {
        // TODO : Process PUT
        String query = request.getUri().getQuery();
        Map parameters;
        if (query == null) {
            parameters = new HashMap();
        } else {
            parameters = RemoteRegistry.decodeQueryString(query);
            if (parameters == null) {
                return new EmptyResponseContext(400,
                        "URI decoding failed on " + query + " at path " + path);
            }
        }

        Feed feed;
        try {
            final Registry secureRegistry = getSecureRegistry(request);
            Collection results = secureRegistry.executeQuery(path, parameters);
            feed = getNewFeed("tag:id");  // TODO: generate correct ID
            feed.setTitle(path);
            String[] childPaths = results.getChildren();
            for (String child : childPaths) {
                Resource entryObj = secureRegistry.get(child);
                Entry entry = factory.newEntry();
                IRI feedIRI = new IRI(getFeedIriForEntry(entryObj, request));
                addEntryDetails(request, entry, feedIRI, entryObj);
//                entry.addLink(child, "queryResult");
                feed.addEntry(entry);
            }
        } catch (Exception e) {
            return new StackTraceResponseContext(e);
        }

        return buildResponseContextFromFeed(feed);
    }

    private ResponseContext processVersionsRequest(RequestContext request, String path) {
        String[] versionPaths;
        try {
            Registry registry = getSecureRegistry(request);
            versionPaths = registry.getVersions(path);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        Feed feed = getNewFeed("tag:" + path + ";versions");
        for (String version : versionPaths) {
            Entry e = factory.newEntry();
            e.addLink(version, "versionLink");
            feed.addEntry(e);
        }
        return buildResponseContextFromFeed(feed);
    }

    private ResponseContext processLogsRequest(RequestContext request, String path) {
        Map<String, String> parameters = parseQueryString(request.getUri().getQuery());
        String user = parameters.get("user");
        Date fromDate = null;
        Date toDate = null;
        String recentParam = parameters.get("recentFirst");

        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
        String dateStr = parameters.get("from");
        if (dateStr != null) {
            try {
                fromDate = format.parse(dateStr);
            } catch (ParseException e) {
                return new EmptyResponseContext(400, "Bad 'from' date format '" + dateStr + "'");
            }
        }
        dateStr = parameters.get("to");
        if (dateStr != null) {
            try {
                toDate = format.parse(dateStr);
            } catch (ParseException e) {
                return new EmptyResponseContext(400, "Bad 'to' date format '" + dateStr + "'");
            }
        }
        boolean recentFirst = recentParam == null || recentParam.equals("true");
        int action = -1;
        LogEntry[] logs;
        try {
            final Registry reg = getSecureRegistry(request);
            logs = reg.getLogs(path, action, user, fromDate, toDate, recentFirst);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        String uri = request.getUri().toString();
        int colonIdx = uri.indexOf(':');
        if (colonIdx > -1 && uri.length() > colonIdx + 1) {
            int entryIdx;
            try {
                entryIdx = Integer.parseInt(uri.substring(colonIdx + 1));
            } catch (NumberFormatException e) {
                return new EmptyResponseContext(400, "Bad log entry id '" +
                        uri.substring(colonIdx + 1) + "'");
            }
            if (entryIdx < 0 || entryIdx > logs.length - 1) {
                return new EmptyResponseContext(400, "Bad log entry id '" + entryIdx + "'");
            }
            LogEntry logentry = logs[logs.length - 1 - entryIdx];
            Entry entry = factory.newEntry();
            entry.setUpdated(new Date());
            entry.setId("http://wso2.org/jdbcregistry,2007:logs:" + entryIdx);
            entry.setTitle(logentry.getTitle());
            entry.setEdited(logentry.getDate());
            entry.setContentAsHtml(logentry.getText());
            entry.addAuthor(logentry.getUserName());
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "action"),
                    "" + logentry.getAction());
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.PARAMETER_PATH),
                    logentry.getResourcePath());
            try {
                return buildGetEntryResponse(request, entry);
            } catch (ResponseContextException e) {
                return createErrorResponse(e);
            }

        }
        Feed feed = factory.newFeed();
        feed.setId("http://wso2.org/jdbcregistry,2007:logs");
        feed.setTitle("Logs for the resource " + path);
        feed.addLink("", Link.REL_SELF);
        feed.setUpdated(new Date());
        int count = logs.length - 1;
        for (LogEntry logentry : logs) {
            Entry entry = factory.newEntry();
            entry.setTitle(logentry.getTitle());
            entry.addLink((request.getUri() + ":" +
                    count).replaceAll(" ", "+"));
            count--;
            entry.setEdited(logentry.getDate());
            entry.setContentAsHtml(logentry.getText());
            entry.addAuthor(logentry.getUserName());
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "action"),
                    "" + logentry.getAction());
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.PARAMETER_PATH),
                    logentry.getResourcePath());
            feed.addEntry(entry);
        }
        return buildResponseContextFromFeed(feed);
    }

    private ResponseContext processCheckpointRequest(RequestContext request, String path) {
        try {
            Registry registry = getSecureRegistry(request);
            registry.createVersion(path);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        return new StringResponseContext("Version successfully created", HttpURLConnection.HTTP_OK);
    }

    private ResponseContext buildResponseContextFromFeed(Feed feed) {
        Document<Feed> docFeed = feed.getDocument();
        ResponseContext rc = new BaseResponseContext<Document<Feed>>(docFeed);
        rc.setEntityTag(calculateEntityTag(docFeed.getRoot()));
        Date updated = feed.getUpdated();
        if (updated != null) {
            rc.setLastModified(updated);
        }
        return rc;
    }

    private ResponseContext processTagsRequest(RequestContext request, String path) {
        if (request.getMethod().equals(APPConstants.HTTP_GET)) {
            // Return the tags.
            // TODO - resource.getTags()?
            Tag[] tags;
            try {
                final Registry reg = getSecureRegistry(request);
                tags = reg.getTags(path);
            } catch (RegistryException e) {
                return new StackTraceResponseContext(e);
            }
            Feed feed = factory.newFeed();
            feed.setId("http://wso2.org/jdbcregistry:tags" +
                    Utils.encodeRegistryPath(path).replaceAll(" ", "+"));
            feed.setTitle("Tags for " + path);
//            String nodeLink = baseUri + "atom" + path +
//                              RegistryConstants.URL_SEPARATOR +
//                              PARAMETER_TAGS;
//            feed.addLink(nodeLink);
            feed.setUpdated(new Date());

            for (Tag tag : tags) {
                Entry entry = factory.newEntry();
                entry.setTitle(tag.getTagName());
                entry.setContent(tag.getTagName());
                entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "taggings"),
                        "" + tag.getTagCount());
                feed.addEntry(entry);
            }

            return buildResponseContextFromFeed(feed);
        }
        if (request.getMethod().equals(APPConstants.HTTP_POST)) {
            // HTTP_POST adds a tag
            if (request.isAtom()) {
                String tag;
                try {
                    Entry e = (Entry) request.getDocument().getRoot();
                    tag = e.getContent();
                    final Registry registry = getSecureRegistry(request);
                    registry.applyTag(path, tag);
                } catch (Exception e) {
                    return new StackTraceResponseContext(e);
                }
                final EmptyResponseContext response = new EmptyResponseContext(200, "Tag applied");
                try {
                    response.setLocation(URLDecoder.decode(getAbsolutePath(request, path), RegistryConstants.DEFAULT_CHARSET_ENCODING)
                            .replaceAll(" ", "+") +
                            RegistryConstants.URL_SEPARATOR + "tags:" + tag);
                } catch (UnsupportedEncodingException e) {
                    // no action
                }
                return response;
            }
            String firstTag = null;
            try {
                InputStream is = request.getInputStream();
                String tagText = readToString(is);
                String[] tags = tagText.split(" ");
                for (String tag : tags) {
                    if (firstTag == null) {
                        firstTag = tag;
                    }
                    try {
                        final Registry registry = getSecureRegistry(request);
                        registry.applyTag(path, tag);
                    } catch (RegistryException e) {
                        return new StackTraceResponseContext(e);
                    }
                }
            } catch (IOException e) {
                return new StackTraceResponseContext(e);
            }
            final EmptyResponseContext response = new EmptyResponseContext(200, "Tag applied");
            try {
                response.setLocation(URLDecoder.decode(getAbsolutePath(request, path), RegistryConstants.DEFAULT_CHARSET_ENCODING)
                        .replaceAll(" ", "+") +
                        RegistryConstants.URL_SEPARATOR + "tags:" + firstTag);
            } catch (UnsupportedEncodingException e) {
                // no action
            }
            return response;
        }
        return null;
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    private ResponseContext processDumpRequest(RequestContext request, String path) {
        if (request.getMethod().equals(APPConstants.HTTP_POST)) {
            // We're trying to restore the dump resource.
            try {
                Reader reader = request.getReader();
                getSecureRegistry(request).restore(path, reader);
                return new StringResponseContext("Resource restored successfully",
                        HttpURLConnection.HTTP_OK);
            } catch (Exception e) {
                return new StackTraceResponseContext(e);
            }
        } else if (request.getMethod().equals(APPConstants.HTTP_GET)) {
            // get the dump
            try {
                return new OMElementResponseContext(getSecureRegistry(request), path);
            } catch (Exception e) {
                return new StackTraceResponseContext(e);
            }
        } else {
            String msg = "Invalid http method with the restore " + path + ". " +
                    "Expected post to restore and get to dump.";
            log.error(msg);
            // We need an exception object to create a stack trace response context.
            return new StackTraceResponseContext(new RegistryException(msg));
        }
    }

    /**
     * Method to build a get entry response.
     *
     * @param request the request context.
     * @param entry   the entry
     *
     * @return the response context.
     */
    protected ResponseContext buildGetEntryResponse(RequestContext request, Entry entry)
            throws ResponseContextException {
        Feed feed = createFeedBase(request);
        entry.setSource(feed.getAsSource());
        Document<Entry> entryDoc = entry.getDocument();
        AbstractResponseContext rc = new BaseResponseContext<Document<Entry>>(entryDoc);
        rc.setEntityTag(calculateEntityTag(entry));
        Date updated = entry.getUpdated();
        if (updated != null) {
            rc.setLastModified(updated);
        }
        return rc;
    }

    /**
     * Method to build a get feed response.
     *
     * @param feed the feed.
     *
     * @return the response context.
     */
    protected ResponseContext buildGetFeedResponse(Feed feed) {
        Document<Feed> document = feed.getDocument();
        AbstractResponseContext rc = new BaseResponseContext<Document<Feed>>(document);
        rc.setEntityTag(calculateEntityTag(document.getRoot()));
        Date updated = feed.getUpdated();
        if (updated != null) {
            rc.setLastModified(updated);
        }
        return rc;
    }

    // Method to read an input stream to string
    private String readToString(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    // Method to calculate Entity Tag
    private EntityTag calculateEntityTag(Base base) {
        String id = null;
        String modified = null;
        if (base instanceof Entry) {
            id = ((Entry) base).getId().toString();
            if (((Entry) base).getUpdatedElement() != null) {
                modified = ((Entry) base).getUpdatedElement().getText();
            }
        } else if (base instanceof Feed) {
            id = ((Feed) base).getId().toString();
            modified = ((Feed) base).getUpdatedElement().getText();
        }
        return EntityTag.generate(id, modified);
    }

    /**
     * Method to post an entry.
     *
     * @param request the request context.
     *
     * @return the response context.
     */
    public ResponseContext postEntry(RequestContext request) {
        Document<Element> document;
        try {
            Parser parser = request.getAbdera().getParser();
            ParserOptions options = parser.getDefaultParserOptions();
            if (request.getAcceptCharset() != null && !request.getAcceptCharset().isEmpty()) {
                options.setCharset(request.getAcceptCharset());
            } else {
                options.setCharset("UTF-8");
            }
            document = request.getDocument(parser, options);
        } catch (IOException e) {
            return new StackTraceResponseContext(e);
        }
        if (document.getRoot().getQName().equals(Constants.FEED)) {
            // Posting a <feed>, so this is probably a collection creation.
            return postFeed(request);
        }
        return super.postEntry(request);
    }

    /**
     * Method to post a feed.
     *
     * @param request the request context.
     *
     * @return the response context.
     */
    public ResponseContext postFeed(RequestContext request) {
        Document<Feed> doc;
        try {
            doc = request.getDocument();
        } catch (IOException e) {
            return new StackTraceResponseContext(e);
        }
        Feed feed = doc.getRoot();
        String slug = request.getSlug();
        if (slug == null) {
            slug = feed.getTitle();
        }

        // Following code replaces spaces with "_". Commenting out Sanitizer.sanitize and doing the same thing other
        // replacing spaces with "_". 
        // slug = Sanitizer.sanitize(slug, "-", SANITIZE_PATTERN);
        slug = slug.replaceAll(SANITIZE_PATTERN, "-");

        String parentPath = ((ResourceTarget) request.getTarget()).getResource().getPath();
        if (!parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath += RegistryConstants.PATH_SEPARATOR;
        }
        String path = parentPath + slug;
        String real;
        try {
            final Registry registry = getSecureRegistry(request);
            Collection resource = registry.newCollection();
            org.wso2.carbon.registry.app.Properties properties =
                    feed.getExtension(PropertyExtensionFactory.PROPERTIES);
            RemoteRegistry.createPropertiesFromExtensionElement(properties, resource);
            resource.setDescription(feed.getSubtitle());
            if (feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE)) != null) {
                resource.setMediaType(
                        feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE)));
            }
            if(feed.getSimpleExtension(APPConstants.QN_UUID_TYPE) != null){
                resource.setUUID(feed.getSimpleExtension(APPConstants.QN_UUID_TYPE));
            }
            real = registry.put(path, resource);
        } catch (RegistryException e) {
            return new StackTraceResponseContext(e);
        }
        StringResponseContext responseContext =
                new StringResponseContext("Feed created at " + real,
                        HttpURLConnection.HTTP_CREATED);
        Map<String, String> map = new HashMap<String, String>();
        map.put("collection", real.substring(1));
        try {
            responseContext.setLocation(URLDecoder.decode(request.absoluteUrlFor(
                    TargetType.TYPE_COLLECTION, map), RegistryConstants.DEFAULT_CHARSET_ENCODING).replaceAll(" ", "+"));
        } catch (UnsupportedEncodingException e) {
            log.error("The encoding is not supported.", e);
        }
        return responseContext;
    }

    /**
     * Method to post an entry to the collection.
     *
     * @param title   the title of the entry.
     * @param updated the updated time.
     * @param authors the list of authors.
     * @param summary the summary text.
     * @param content the resource content.
     * @param request the request context.
     *
     * @throws ResponseContextException if the operation failed.
     */
    public Resource postEntry(String title,
                              IRI id,
                              String summary,
                              Date updated,
                              List<Person> authors,
                              Content content,
                              RequestContext request) throws ResponseContextException {
        Resource resource = ((ResourceTarget) request.getTarget()).getResource();
        String path = resource.getPath();
        final Registry registry;
        try {
            registry = getSecureRegistry(request);
        } catch (RegistryException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }

        final String[] splitPath = (String[]) request.getAttribute(RequestContext.Scope.REQUEST,
                APPConstants.PARAMETER_SPLIT_PATH);
        final String text = content.getText();
        if (splitPath != null && APPConstants.PARAMETER_COMMENTS.equals(splitPath[1])) {
            // Comment post
            org.wso2.carbon.registry.core.Comment comment =
                    new org.wso2.carbon.registry.core.Comment(text);
            try {
                registry.editComment(path, text);
                String commentPath = registry.addComment(path, comment);
                comment.setPath(commentPath);
            } catch (RegistryException e) {
                throw new ResponseContextException(new StackTraceResponseContext(e));
            }
            return comment;
        }

        String name = request.getSlug();
        if (name == null) {
            if (title != null) {
                // Following code replaces spaces with "_". Commenting out Sanitizer.sanitize and
                // doing the same thing other
                // replacing spaces with "_".
                // slug = Sanitizer.sanitize(slug, "-", SANITIZE_PATTERN);
                name = title.replaceAll(SANITIZE_PATTERN, "-");
            } else {
                name = generateResourceName();
            }
        }

        if (!path.endsWith("/")) {
            path += "/";
        }

        if (APPConstants.IMPORT_MEDIA_TYPE.equals(request.getContentType().toString())) {
            // This is an import.
            String importURL = request.getParameter("importURL");
            String suggestedPath = request.getSlug();
            String location;
            try {
                final Registry secureRegistry = getSecureRegistry(request);
                location = secureRegistry.importResource(suggestedPath,
                        importURL,
                        new ResourceImpl());
                return secureRegistry.get(location);
            } catch (RegistryException e) {
                throw new ResponseContextException(new StackTraceResponseContext(e));
            }
        }

        Entry entry;
        try {
            entry = (Entry) request.getDocument().getRoot();
        } catch (IOException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }

        Resource ret;
        try {
            ret = registry.newResource();
            fillResourceFromEntry(entry, ret);


            registry.put(path + name, ret);
        } catch (Exception e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }
        ((ResourceImpl) ret).setPath(path + name);
        return ret;
    }

    // Method to fill a resource from an entry.
    private void fillResourceFromEntry(Entry entry, Resource ret) throws RegistryException {
        org.wso2.carbon.registry.app.Properties properties =
                entry.getExtension(PropertyExtensionFactory.PROPERTIES);
        RemoteRegistry.createPropertiesFromExtensionElement(properties, ret);
        String mediaType = entry.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE));
        if (mediaType != null) {
            ret.setMediaType(mediaType);
        }

        Content content = entry.getContentElement();
        if (content == null || content.getText() == null || content.getText().equals("")) {
            // creates an null input stream.
            ret.setContent(null);
        } else if (content.getContentType() == Content.Type.TEXT) {
            ret.setContent(content.getText());
            if (mediaType == null) {
                mediaType = "text/plain";
            }
            ret.setMediaType(mediaType);
        } else if (content.getContentType() == Content.Type.MEDIA) {
            try {
                ret.setContentStream(content.getDataHandler().getInputStream());
            } catch (IOException e) {
                log.error("Error occurred while streaming the content", e);
                return;
            }
            // if (mediaType == null) ret.setMediaType(content.getMimeType().toString());
        }
        if (entry.getSummary() != null) {
            ret.setDescription(entry.getSummary());
        }
        if(entry.getSimpleExtension(APPConstants.QN_UUID_TYPE) != null){
            ret.setUUID(entry.getSimpleExtension(APPConstants.QN_UUID_TYPE));
        }
    }

    /**
     * Method to delete an entry from the collection.
     *
     * @param resourceName the resource name.
     * @param request      the request context.
     *
     * @throws ResponseContextException if the operation failed.
     */
    public void deleteEntry(String resourceName, RequestContext request)
            throws ResponseContextException {
    }

    /**
     * Method to determine whether the given resource is a media entry.
     *
     * @param entry the resource.
     *
     * @return whether the given resource is a media entry.
     * @throws ResponseContextException if the operation failed.
     */
    public boolean isMediaEntry(Resource entry) throws ResponseContextException {
        if (entry instanceof Collection) {
            return false;
        }
        // If this isn't atom, it's a media entry
        Object content;
        try {
            content = entry.getContent();
        } catch (RegistryException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }
        return (!MimeTypeHelper.isAtom(entry.getMediaType()) && !(content instanceof Number) &&
                !(content instanceof String));
    }

    /**
     * Method to obtain the resource content.
     *
     * @param entry   the resource.
     * @param request the request context.
     *
     * @return the resource content object.
     * @throws ResponseContextException
     */
    public Object getContent(Resource entry, RequestContext request)
            throws ResponseContextException {
        // No content for Collections
        if (entry instanceof Collection) {
            return null;
        }

        Object c;
        try {
            c = entry.getContent();
        } catch (RegistryException e) {
            return null;
        }

        return c.toString();
    }

    /**
     * Method to obtain the entry IRI link.
     *
     * @param entryObj       the entry object.
     * @param feedIri        the feed IRI
     * @param requestContext the request context.
     *
     * @return the entry IRI link.
     * @throws ResponseContextException if the operation failed.
     */
    protected String getLink(Resource entryObj, IRI feedIri, RequestContext requestContext)
            throws ResponseContextException {
        String path = entryObj.getPath();
        final int idx = path.indexOf("ratings:");
        String link;
        if (entryObj instanceof org.wso2.carbon.registry.core.Comment) {
            String name = path.substring(path.indexOf(':'));
            link = feedIri.toString() + name;
        } else if (idx > -1) {
            link = feedIri.toString();
        } else {
            link = super.getLink(entryObj, feedIri, requestContext);
        }

        // if the request comes with the tenant domain as a parameter we insert the tenant domain
        // for all the referred link
        String tenantDomain = (String) requestContext.getAttribute(RequestContext.Scope.REQUEST,
                MultitenantConstants.TENANT_DOMAIN);
        if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain) &&
        		!tenantDomain.equals("")) {
            int atomPos = link.indexOf("/registry/atom");
            if (atomPos != -1) {
                link = link.substring(0, atomPos) + "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX +
                        "/" + tenantDomain + "/registry/atom" +
                        link.substring(atomPos + "/registry/atom".length());
            }
            int resourcePos = link.indexOf("/registry/resource");
            if (resourcePos != -1) {
                link = link.substring(0, resourcePos) + "/" +
                        MultitenantConstants.TENANT_AWARE_URL_PREFIX +
                        "/" + tenantDomain + "/registry/resource" +
                        link.substring(resourcePos + "/registry/resource".length());
            }
        }
        return link;
    }

    /**
     * Method to add entry details.
     *
     * @param entry    the entry.
     * @param feedIri  the IRI of the feed.
     * @param entryObj the resource.
     * @param request  the request context.
     *
     * @return the entry IRI link.
     * @throws ResponseContextException if the operation failed.
     */
    protected String addEntryDetails(RequestContext request,
                                     Entry entry,
                                     IRI feedIri,
                                     Resource entryObj) throws ResponseContextException {
//        final Registry registry;
//        try {
//            registry = getSecureRegistry(request);
//        } catch (RegistryException ex) {
//            throw new ResponseContextException(new StackTraceResponseContext(ex));
//        }
        if (entryObj == null) {
            return null;
        }
        final String link = getLink(entryObj, feedIri, request);
        // We need an alternate link for the collection here
        //if (!(entryObj instanceof org.wso2.carbon.registry.core.Comment)) {
        entry.addLink(link, Link.REL_ALTERNATE);
        //}
//        if (entryObj instanceof Collection) {
//        }
//        String foo = request.getBaseUri().toString();
//        foo += entryObj.getPath();
//        e.addLink(foo, "alternate");

        if (entryObj instanceof org.wso2.carbon.registry.core.Comment) {
            entry.addLink(link, Link.REL_SELF);
            entry.addLink(URLEncoder.encode(
                    ((org.wso2.carbon.registry.core.Comment) entryObj).getResourcePath()),
                    "resourcePath");
            String commentText = ((org.wso2.carbon.registry.core.Comment) entryObj).getText();
            entry.setContent(commentText);
            entry.setSummary(commentText);
        }
        entry.addLink(URLEncoder.encode(entryObj.getPath()), APPConstants.PARAMETER_PATH);

        long snapshotID = ((ResourceImpl) entryObj).getMatchingSnapshotID();
        if (snapshotID > -1) {
            entry.addSimpleExtension(APPConstants.QN_SNAPSHOT_ID, Long.toString(snapshotID));
        }
        entry.addSimpleExtension(APPConstants.QN_LAST_UPDATER, entryObj.getLastUpdaterUserName());
        if (entryObj.getCreatedTime() != null) {
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "createdTime"),
                    new AtomDate(entryObj.getCreatedTime().getTime()).getValue());
        }
        final String mediaType = entryObj.getMediaType();
        if (mediaType != null && mediaType.length() > 0) {
            entry.addSimpleExtension(APPConstants.QN_MEDIA_TYPE, mediaType);
        }

//        String path = entryObj.getPath();
//        try {
//            Tag [] tags = registry.getTags(path);
//            if (tags.length > 0) {
//                Element ext = factory.newElement(new QName(RegistryConstants.REGISTRY_NAMESPACE,
//                                                           "tags"));
//                for (Tag tag : tags) {
//                    Element tagEl =
//                            factory.newElement(new QName(RegistryConstants.REGISTRY_NAMESPACE,
//                                                         "tag"),
//                                               ext);
//                    tagEl.setText(tag.getTagName());
//                }
//                e.addExtension(ext);
//            }
//        } catch (RegistryException e1) {
//            log.error(e1);
//        }

        RemoteRegistry.addPropertyExtensionElement(entryObj.getProperties(),
                factory,
                entry,
                PropertyExtensionFactory.PROPERTIES,
                PropertyExtensionFactory.PROPERTY);

        return super.addEntryDetails(request, entry, feedIri, entryObj);
    }

    /**
     * Method to add feed details.
     *
     * @param feed    the feed.
     * @param request the request context.
     *
     * @throws ResponseContextException if the operation failed.
     */
    protected void addFeedDetails(Feed feed, RequestContext request)
            throws ResponseContextException {
        super.addFeedDetails(feed, request);
        final Resource resource = ((ResourceTarget) request.getTarget()).getResource();
        RemoteRegistry.addPropertyExtensionElement(resource.getProperties(),
                factory,
                feed,
                PropertyExtensionFactory.PROPERTIES,
                PropertyExtensionFactory.PROPERTY);
        if (request.getTarget().getType() == RegistryResolver.COMMENTS_TYPE) {
            feed.addSimpleExtension(APPConstants.QN_COMMENTS, "true");
        }
        feed.addSimpleExtension(APPConstants.QN_LAST_UPDATER, resource.getLastUpdaterUserName());
        long snapshotID = ((ResourceImpl) resource).getMatchingSnapshotID();
        if (snapshotID > -1) {
            feed.addSimpleExtension(APPConstants.QN_SNAPSHOT_ID, Long.toString(snapshotID));
        }
        if (resource instanceof Collection) {
            try {
                feed.addSimpleExtension(APPConstants.QN_CHILD_COUNT,
                        "" + ((Collection) resource).getChildCount());
            } catch (RegistryException e) {
                throw new ResponseContextException(new StackTraceResponseContext(e));
            }
        }
        if (resource.getCreatedTime() != null) {
            feed.addSimpleExtension(new QName(APPConstants.NAMESPACE, "createdTime"),
                    new AtomDate(resource.getCreatedTime().getTime()).getValue());
        }
        feed.addLink(URLEncoder.encode(resource.getPath()), APPConstants.PARAMETER_PATH);
        feed.setSubtitle(resource.getDescription());
    }

    /**
     * Method to get an iterator of entries.
     *
     * @param request the request context.
     *
     * @return an iterator of resources.
     * @throws ResponseContextException if the operation failed.
     */
    public Iterable<Resource> getEntries(final RequestContext request)
            throws ResponseContextException {
        Resource resource = ((ResourceTarget) request.getTarget()).getResource();

        final String[] splitPath = (String[]) request.getAttribute(RequestContext.Scope.REQUEST,
                APPConstants.PARAMETER_SPLIT_PATH);
        if (splitPath != null && APPConstants.PARAMETER_COMMENTS.equals(splitPath[1])) {
            // Looking for comments, not the resource itself
            try {
                resource = getSecureRegistry(request).get(resource.getPath() +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_COMMENTS);
            } catch (RegistryException e) {
                throw new ResponseContextException(new StackTraceResponseContext(e));
            }

        }

        if (resource instanceof Collection) {
            final Resource r = resource;
            return new Iterable<Resource>() {
                public Iterator<Resource> iterator() {
                    try {
                        return new ResourceIterator((Object[]) r.getContent(),
                                getSecureRegistry(request));
                    } catch (RegistryException e) {
                        return null;
                    }
                }
            };
        }

        return null;
    }

    /**
     * An implementation of an iterator, which is capable of iterating resources.
     */
    private static class ResourceIterator implements Iterator<Resource> {

        private Object[] paths;
        private int i = 0;
        private Registry registry;

        /**
         * Constructor accepting an array of resource paths and a registry instance.
         *
         * @param paths    array of resource paths.
         * @param registry the registry instance.
         */
        public ResourceIterator(Object[] paths, Registry registry) {
            this.paths = Arrays.copyOf(paths, paths.length);
            this.registry = registry;
        }

        public boolean hasNext() {
            return i < paths.length;
        }

        public Resource next() {
            Object resourceOrString = paths[i++];

            if (resourceOrString instanceof Resource) {
                return (Resource) resourceOrString;
            }

            try {
                return registry.get((String) resourceOrString);
            } catch (RegistryException e) {
                return null;
            }
        }

        public void remove() {
        }
    }

    /**
     * Method to obtain the resource from the request.
     *
     * @param resourceName the resource name.
     * @param request      the request.
     *
     * @return the resource.
     * @throws ResponseContextException if the operation failed.
     */
    public Resource getEntry(String resourceName, RequestContext request)
            throws ResponseContextException {
        return ((ResourceTarget) request.getTarget()).getResource();
    }

    /**
     * Method to obtain the unique identifier of a resource.
     *
     * @param entry the resource.
     *
     * @return the unique identifier.
     * @throws ResponseContextException if the operation failed.
     */
    public String getId(Resource entry) throws ResponseContextException {
        return "urn:uuid:" + entry.getUUID();
    }

    /**
     * Method to obtain the name of a resource.
     *
     * @param entry the resource.
     *
     * @return the name.
     * @throws ResponseContextException if the operation failed.
     */
    public String getName(Resource entry) throws ResponseContextException {
        String path = entry.getPath();
        //path = IRI.normalizeString(path);
        int idx = path.lastIndexOf('/');
        path = path.substring(idx + 1, path.length());
        return URLEncoder.encode(path);
    }

    /**
     * Method to obtain the title of a resource.
     *
     * @param entry the resource.
     *
     * @return the title.
     * @throws ResponseContextException if the operation failed.
     */
    public String getTitle(Resource entry) throws ResponseContextException {
        if (entry instanceof org.wso2.carbon.registry.core.Comment) {
            return "Comment by " + entry.getAuthorUserName();
        }
        return entry.getPath();
    }

    /**
     * Method to obtain the updated time of a resource.
     *
     * @param entry the resource.
     *
     * @return the updated time.
     * @throws ResponseContextException if the operation failed.
     */
    public Date getUpdated(Resource entry) throws ResponseContextException {
        return entry.getLastModified();
    }

    /**
     * Method to add an entry to the collection.
     *
     * @param request the request context.
     *
     * @return the response context.
     */
    public ResponseContext putEntry(RequestContext request) {
        String path = ((ResourceTarget) request.getTarget()).getResource().getPath();
        Resource ret;
        try {
            Entry entry = (Entry) request.getDocument().getRoot();
            Registry registry = getSecureRegistry(request);
            ret = registry.newResource();
            fillResourceFromEntry(entry, ret);

            registry.put(path, ret);
        } catch (Exception e) {
            return new StackTraceResponseContext(e);
        }
        EmptyResponseContext response = new EmptyResponseContext(HttpURLConnection.HTTP_OK);
        try {
            response.setLocation(
                    URLDecoder.decode(getAtomURI(path, request), RegistryConstants.DEFAULT_CHARSET_ENCODING).replaceAll(" ", "+"));
        } catch (UnsupportedEncodingException e) {
            // no action
        }
        return response;
    }

    /**
     * Method to add an entry to the collection.
     *
     * @param entry   the resource to add.
     * @param title   the title of the entry.
     * @param updated the updated time.
     * @param authors the list of authors.
     * @param summary the summary text.
     * @param content the resource content.
     * @param request the request context.
     *
     * @throws ResponseContextException if the operation failed.
     */
    public void putEntry(Resource entry,
                         String title,
                         Date updated,
                         List<Person> authors,
                         String summary,
                         Content content,
                         RequestContext request) throws ResponseContextException {

    }

    /**
     * Method to obtain the primary author for an entry.
     *
     * @param request the request context.
     *
     * @return the primary author.
     */
    public String getAuthor(RequestContext request) throws ResponseContextException {
        return ((ResourceTarget) request.getTarget()).getResource().getAuthorUserName();
    }

    /**
     * Method to obtain the list of authors for an entry.
     *
     * @param entry   the resource entry.
     * @param request the request context.
     *
     * @return the list of authors.
     * @throws ResponseContextException if the operation failed.
     */
    public List<Person> getAuthors(Resource entry, RequestContext request)
            throws ResponseContextException {
        Person author = request.getAbdera().getFactory().newAuthor();
        author.setName(entry.getAuthorUserName());
        return Arrays.asList(author);
    }

    /**
     * Method to obtain the identifier for an entry.
     *
     * @param request the request context.
     *
     * @return the identifier.
     */
    public String getId(RequestContext request) {
        try {
            return getId(((ResourceTarget) request.getTarget()).getResource());
        } catch (ResponseContextException e) {
            return null;
        }
    }

    /**
     * Method to obtain the title for an entry.
     *
     * @param request the request context.
     *
     * @return the title.
     */
    public String getTitle(RequestContext request) {
        Resource resource = ((ResourceTarget) request.getTarget()).getResource();
        if (request.getTarget().getType().equals(RegistryResolver.COMMENTS_TYPE)) {
            return "Comments for '" + resource.getPath() + "'";
        }
        return resource.getPath();
    }

    /**
     * Method to obtain the summary for an entry.
     *
     * @param entry   the resource entry.
     * @param request the request context.
     *
     * @return the summary text.
     * @throws ResponseContextException if the operation failed.
     */
    public Text getSummary(Resource entry, RequestContext request) throws ResponseContextException {
        Text text = factory.newSummary();
        text.setValue(entry.getDescription());
        return text;
    }

    /**
     * Posts a new media entry.
     *
     * @param mimeType    the MIME type of the content.
     * @param slug        the slug as a String.
     * @param inputStream the content stream.
     * @param request     the request context.
     *
     * @return the generated media object.
     * @throws ResponseContextException if the operation failed.
     */
    public Resource postMedia(MimeType mimeType,
                              String slug,
                              InputStream inputStream,
                              RequestContext request) throws ResponseContextException {
        final Registry registry;
        try {
            registry = getSecureRegistry(request);
        } catch (RegistryException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }

        String path = ((ResourceTarget) request.getTarget()).getResource().getPath();

        final String[] splitPath = (String[]) request.getAttribute(RequestContext.Scope.REQUEST,
                APPConstants.PARAMETER_SPLIT_PATH);
        if (splitPath != null && APPConstants.PARAMETER_COMMENTS.equals(splitPath[1])) {
                if (!mimeType.toString().equals("text/plain")) {
                    throw new ResponseContextException(
                            "Can only post Atom or text/plain to comments!",
                            HttpURLConnection.HTTP_BAD_REQUEST);
                }
                // Comment post
                org.wso2.carbon.registry.core.Comment comment;
                try {
                    comment = new org.wso2.carbon.registry.core.Comment(readToString(inputStream));
                } catch (IOException e) {
                    throw new ResponseContextException(new StackTraceResponseContext(e));
                }
                try {
                    String commentPath = registry.addComment(path, comment);
                    comment.setPath(commentPath);
                    comment.setParentPath(path + RegistryConstants.URL_SEPARATOR + APPConstants.PARAMETER_COMMENTS);
                } catch (RegistryException e) {
                    throw new ResponseContextException(new StackTraceResponseContext(e));
                }
                return comment;
        }

        if (!path.endsWith("/")) {
            path += "/";
        }
        path += getGoodSlug(path, slug, request);
        boolean isCollection = "app/collection".equals(mimeType.toString());
        Resource ret;
        try {
            ret = isCollection ? registry.newCollection() : registry.newResource();
        } catch (RegistryException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }
        ret.setMediaType(mimeType.toString());

        try {

            if (!isCollection) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[RegistryConstants.DEFAULT_BUFFER_SIZE];
                try {
                    while (inputStream.available() > 0) {
                        int amount = inputStream.read(buffer, 0,
                                RegistryConstants.DEFAULT_BUFFER_SIZE);
                        bos.write(buffer, 0, amount);
                    }
                } catch (IOException e) {
                    // nothing here
                }
                String content = RegistryUtils.decodeBytes(bos.toByteArray());
                ret.setContent(content);
            }

            registry.put(path, ret);
        } catch (RegistryException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }
        return ret;
    }

    // Method to generate slug.
    private String getGoodSlug(String path, String inputSlug, RequestContext request) {
        String outputSlug = inputSlug;
        if (outputSlug == null) {
            outputSlug = "resource";
        }
//        slug = Sanitizer.sanitize(slug, "", SANITIZE_PATTERN);
        if (outputSlug.startsWith("/")) {
            outputSlug = outputSlug.substring(1);
        }
        try {
            Registry myRegistry = getSecureRegistry(request);
            Resource resource = myRegistry.get(path + outputSlug);
            while (resource != null) {
                int i = 1;
                final int len = outputSlug.length();
                char c = outputSlug.charAt(len - i);
                while (i < len && c >= '0' && c <= '9') {
                    i++;
                    c = outputSlug.charAt(len - i);
                }
                int j;
                if (i == 1) {
                    j = 1;
                } else {
                    String prefix = outputSlug.substring(0, len - i + 1);
                    j = Integer.parseInt(outputSlug.substring(len - i + 1)) + 1;
                    outputSlug = prefix;
                }
                outputSlug = outputSlug + j;
                resource = myRegistry.get(path + outputSlug);
            }
        } catch (ResourceNotFoundException ignore) {
            // We are expecting an exception here, and it is not an error this time.
            log.debug("The resource was not found at path: " + path + outputSlug);
        } catch (RegistryException e) {
            log.error("The operation failed", e);
            return null;
        }
        return outputSlug;
    }

    /**
     * Method to obtain the content type of an entry.
     *
     * @param entry the entry.
     *
     * @return the content type.
     */
    public String getContentType(Resource entry) {
        return entry.getMediaType();
    }

    /**
     * Method to obtain the media name.
     *
     * @param entry the resource (media entry).
     *
     * @return the media name.
     * @throws ResponseContextException if the operation failed.
     */
    public String getMediaName(Resource entry) throws ResponseContextException {
        return entry.getPath().substring(1);
    }

    /**
     * Method to add media content.
     *
     * @param feedIri  the feed IRI.
     * @param entry    the entry.
     * @param entryObj the resource object.
     * @param request  the request context.
     *
     * @return the added media IRI.
     * @throws ResponseContextException
     */
    protected String addMediaContent(IRI feedIri,
                                     Entry entry,
                                     Resource entryObj,
                                     RequestContext request) throws ResponseContextException {
        String fullUrl;
        if (entry.getAlternateLink() != null && entry.getAlternateLink().getHref() != null) {
            fullUrl = entry.getAlternateLink().getHref().toString();
            if (fullUrl == null || fullUrl.length() == 0 ||
                    fullUrl.indexOf(APPConstants.ATOM) <= 0) {
                fullUrl = feedIri.toString();
            }
        } else {
            fullUrl = feedIri.toString();
        }

        String absoluteBase = fullUrl.substring(0, fullUrl.indexOf(APPConstants.ATOM));
        IRI mediaIri = new IRI(absoluteBase + APPConstants.RESOURCE + "/"
                + URLEncoder.encode(getMediaName(entryObj)));
        String mediaLink = mediaIri.toString();
        String mime = getContentType(entryObj);
        if (mime == null) {
            mime = "application/octet-stream";
        }
        try {
            new MimeType().match(mime);
        } catch (MimeTypeParseException e) {
            mime = "application/octet-stream";
        }
        entry.setContent(mediaIri, mime);
        entry.addLink(mediaLink, Link.REL_EDIT_MEDIA);

        return mediaLink;
    }

    /**
     * Method to obtain an input stream for the given resource.
     *
     * @param entry the resource entry.
     *
     * @return the resource content as a stream.
     * @throws ResponseContextException if the operation failed.
     */
    public InputStream getMediaStream(Resource entry) throws ResponseContextException {
        try {
            return new ByteArrayInputStream((byte[]) entry.getContent());
        } catch (RegistryException e) {
            throw new ResponseContextException(new StackTraceResponseContext(e));
        }
    }

    /**
     * Generates the feed IRI for the given entry.
     *
     * @param entryObj the entry object.
     * @param request  the request context.
     *
     * @return the feed IRI.
     */
    protected String getFeedIriForEntry(Resource entryObj, RequestContext request) {
        // We need to build the feed uri manually as the forwarded urls from the tenant url servlet
        // filter is causing problem (additional / at the start)in the request.getResolvedUri()
        // method
        IRI baseUri = request.getBaseUri();
        String feedString = baseUri.getScheme() + "://" + baseUri.getAuthority();

        IRI requestUri = request.getUri();
        String requestUriStr = "";
        if (requestUri != null) {
            requestUriStr = request.getUri().toString();
            // remove the additional '/' character.
            if (requestUriStr.length() > 1 &&
                    requestUriStr.charAt(0) == '/' && requestUriStr.charAt(1) == '/') {
                requestUriStr = requestUriStr.substring(1);
            } else if (requestUriStr.length() > 0 && requestUriStr.charAt(0) != '/') {
                requestUriStr = "/" + requestUriStr;
            }
        }
        return feedString + requestUriStr;
//        return request.getResolvedUri().toString();
//        setHref(entryObj.getPath());
//        String href = getHref(request);
//        return getAtomURI(entryObj.getParentPath());
    }

    /**
     * This method will create a SecureRegistry for APP. If the user has send the user name and the
     * password then the JDBC registry will be created using the value he has given, else the JDBC
     * registry instance will be created as anonymous user.
     *
     * @param request RequestContext to get the authorization header
     *
     * @return created JDBC registry instance
     * @throws RegistryException If something went wrong
     */
    private Registry getSecureRegistry(RequestContext request) throws RegistryException {
        Registry reg =
                (Registry) request.getAttribute(RequestContext.Scope.REQUEST, "userRegistry");
        if (reg == null) {
            throw new RegistryException("Couldn't find UserRegistry in RequestContext!");
        }
        return reg;
    }

    // Method to generate the resource name.
    private synchronized String generateResourceName() {
        return "resource" + curResource++;
    }
}
