/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.*;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is a core class of the Remote Atom Based implementation of the Registry. This will be used
 * mostly as a front-end client to access the Registry repository. This implementation is based on a
 * REST-ful Atom Pub protocol implementation provided by Apache Abdera.
 */
@SuppressWarnings("deprecation")
// TODO: get rid of the global suppression on deprecation warnings, and fix the use of
// URLDecoder.decode, by providing the correct encoding.
public class RemoteRegistry implements Registry {

    private static final String TEXT_PLAIN_MEDIA_TYPE = "text/plain";
    private String baseURI;
    private Log log = LogFactory.getLog(RemoteRegistry.class);
    //This will keep the value of username and the password for authorization
/**
     * The context for the atom servlet.
     */
    private String authorizationString = null;
    private String username = null;
    private Abdera abdera = new Abdera();
    private static CachedResources cache = new CachedResources();

    /**
     * To create a remote registry need to provide a URL of a remote registry and the URL should be
     * something like http://localhost:8080/wso2registry/atom/r1
     *
     * @param registryURL URL to the registry or to the resource
     */
    public RemoteRegistry(URL registryURL) {
        baseURI = registryURL.toString();
        if (baseURI.endsWith("/")) {
            baseURI = baseURI.substring(0, baseURI.length() - 1);
        }
    }

    /**
     * To create a remote registry to connect to a secure registry or a registry where we need to
     * pass userName and the password to perform any operation. Passing not null value registry will
     * send the username and the password for each request. Need to remember here is that the
     * username and the password send as plain text.
     *
     * @param registryURL URL to the registry or to the resource
     * @param userName    user name of the registry, value can be null, if the value is null then
     *                    the user will be the anonymous.
     * @param password    password, value can be null
     *
     * @throws RegistryException if an error occurred.
     */
    public RemoteRegistry(URL registryURL, String userName, String password)
            throws RegistryException {
        baseURI = registryURL.toString();
        if (baseURI.endsWith("/")) {
            baseURI = baseURI.substring(0, baseURI.length() - 1);
        }
        this.username = userName;
        if (userName != null && password != null) {

            authorizationString = userName + ":" + password;
            authorizationString = "Basic " + Base64.encode(authorizationString.getBytes());
        }
    }

    /**
     * To create a remote registry to connect to a secure registry or a registry where we need to
     * pass userName and the password to perform any operation. Passing not null value registry will
     * send the username and the password for each request. Need to remember here is that the
     * username and the password send as plain text.
     *
     * @param registryURL URL to the registry or to the resource as a String.
     * @param userName    user name of the registry, value can be null, if the value is null then
     *                    the user will be the anonymous.
     * @param password    password, value can be null
     *
     * @throws RegistryException     if an error occurred.
     * @throws MalformedURLException if the URL was not properly formed.
     */
    public RemoteRegistry(String registryURL, String userName, String password)
            throws MalformedURLException, RegistryException {
        this(new URL(registryURL), userName, password);
    }

    public Resource newResource() throws RegistryException {
        ResourceImpl resource = new RemoteResourceImpl();
        resource.setAuthorUserName(username);
        return resource;
    }

    public Collection newCollection() throws RegistryException {
        CollectionImpl collection = new CollectionImpl();
        collection.setAuthorUserName(username);
        return collection;
    }

    public Resource get(String path) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse;
        String encodedPath;
        // If the request is to fetch all comments for a given path, then encode ":" as well to
        // avoid confusion with versioned paths.
        if (path.endsWith(RegistryConstants.URL_SEPARATOR + APPConstants.PARAMETER_COMMENTS)) {
            encodedPath = encodeURL(path);
            if (encodedPath.contains(RegistryConstants.VERSION_SEPARATOR)) {
                int index = encodedPath.lastIndexOf(RegistryConstants.VERSION_SEPARATOR);
                encodedPath = encodedPath.substring(0, index).replace(":", "%3A") +
                        encodedPath.substring(index);
            } else {
                encodedPath = encodedPath.replace(":", "%3A");
            }
        } else {
            encodedPath = encodeURL(path);
        }
        if (!cache.isResourceCached(path)) {
            clientResponse =
                    abderaClient.get(baseURI + "/atom" + encodedPath, getAuthorization());
        } else {
            clientResponse =
                    abderaClient.get(baseURI + "/atom" + encodedPath,
                            getAuthorizationForCaching(path));
        }
        if (clientResponse.getType() == Response.ResponseType.CLIENT_ERROR ||
                clientResponse.getType() == Response.ResponseType.SERVER_ERROR) {
            if (clientResponse.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                abderaClient.teardown();
                throw new ResourceNotFoundException(path);
            }
            abderaClient.teardown();
            throw new RegistryException(clientResponse.getStatusText());
        }

        if (clientResponse.getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            abderaClient.teardown();
            /*do caching here */
            log.debug(
                    "Cached resource returned since no modification has been done on the resource");
            return cache.getCachedResource(path);
        }
        String eTag = clientResponse.getHeader("ETag");
        Element introspection = clientResponse.getDocument().getRoot();
        ResourceImpl resource;
        if (introspection instanceof Feed) {
            // This is a collection
            Feed feed = (Feed) introspection;
            String state = feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_STATE));
            if (state != null && state.equals("Deleted")) {
                abderaClient.teardown();
                throw new ResourceNotFoundException(path);
            }
            resource = createResourceFromFeed(feed);
        } else {
            Entry entry = (Entry) introspection;
            resource = createResourceFromEntry(entry);
        }
        /* if the resource is not Get before  add it to cache before adding it check the max cache
  or if the resource is modified then new resource is replacing the current resource in the cache
   * size configured in registry.xml */
        if (!cache.cacheResource(path, resource, eTag,
                RegistryConstants.MAX_REG_CLIENT_CACHE_SIZE)) {
            log.debug("Max Cache size exceeded the configured Cache size");
        }


        abderaClient.teardown();
//        resource.setPath(path);
        return resource;
    }

    public Resource getMetaData(String path) throws RegistryException {
        // for the remote registry this is same as get as the content would be
        // retrieved in a separate request (at getContent())
        return get(path);
    }

    public String importResource(String suggestedPath, String sourceURL,
                                 org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return importResource(suggestedPath, sourceURL, (Resource) resource);
    }

    public Collection get(String path, int start, int pageSize) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse =
                abderaClient.get(baseURI + "/atom" + encodeURL(path) +
                        "?start=" + start + "&pageLen=" + pageSize, getAuthorization());
        if (clientResponse.getType() == Response.ResponseType.CLIENT_ERROR ||
                clientResponse.getType() == Response.ResponseType.SERVER_ERROR) {
            if (clientResponse.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                abderaClient.teardown();
                throw new ResourceNotFoundException(path);
            }
            abderaClient.teardown();
            throw new RegistryException(clientResponse.getStatusText());
        }
        Element introspection = clientResponse.getDocument().getRoot();
        if (!(introspection instanceof Feed)) {
            abderaClient.teardown();
            throw new RegistryException("Got " + introspection.getQName() +
                    " when expecting <feed>!");
        }
        CollectionImpl resource;
        // This is a collection
        Feed feed = (Feed) introspection;
        String state = feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_STATE));
        if (state != null && state.equals("Deleted")) {
            abderaClient.teardown();
            throw new ResourceNotFoundException(path);
        }
        resource = createResourceFromFeed(feed);
        abderaClient.teardown();
        return resource;
    }

    /**
     * This method will generate a resource object representing the Feed object and the logic will
     * be simply the reverse of the create feed of the Atom registry
     *
     * @param feed : Feed object which represent a resource object
     *
     * @return : Created resource
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          : If user is unable to open the URL connection
     */
    private CollectionImpl createResourceFromFeed(Feed feed)
            throws RegistryException {
        CollectionImpl resource = new CollectionImpl();
        org.wso2.carbon.registry.app.Properties properties =
                feed.getExtension(PropertyExtensionFactory.PROPERTIES);
        RemoteRegistry.createPropertiesFromExtensionElement(properties, resource);
        if (feed.getAuthor() != null) {
            resource.setAuthorUserName(feed.getAuthor().getName());
        }
        resource.setLastModified(feed.getUpdated());
        String createdDate = feed.getSimpleExtension(
                new QName(APPConstants.NAMESPACE, "createdTime"));
        if (createdDate != null) {
            try {
				resource.setCreatedTime(new AtomDate(createdDate).getDate());
			} catch (IllegalArgumentException e) {
				log.error("Error occured while trying to parse the created date", e);
			}
        }

        String lastUpdatedUser = feed.getSimpleExtension(APPConstants.QN_LAST_UPDATER);
        if (lastUpdatedUser != null) {
            resource.setLastUpdaterUserName(lastUpdatedUser);
        }

        final Link pathLink = feed.getLink(APPConstants.PARAMETER_PATH);
        String path = (pathLink != null) ? pathLink.getHref().toString() : feed.getTitle();
        path = URLDecoder.decode(path);
        resource.setPath(path);

        // This MUST be after path is set.
        String snapshotID = feed.getSimpleExtension(APPConstants.QN_SNAPSHOT_ID);
        if (snapshotID != null) {
            resource.setMatchingSnapshotID(Long.parseLong(snapshotID));
        }

        String mediaType = feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE));
        if (mediaType != null) {
            resource.setMediaType(mediaType);
        }

        resource.setDescription(feed.getSubtitle());
        String state = feed.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_STATE));
        if (state != null && "Deleted".equals(state)) {
            resource.setState(RegistryConstants.DELETED_STATE);
        }

        String childCount = feed.getSimpleExtension(APPConstants.QN_CHILD_COUNT);
        if (childCount != null) {
            resource.setChildCount(Integer.parseInt(childCount));
        }

        //Set the UUDI when generating the resource from entry.
        //If UUDI not present RemoteRegistry executeQuery() will failed.
        if (feed.getId() != null) {
            resource.setUUID(feed.getId().toString().replace("urn:uuid:", ""));
        }

        String isComments = feed.getSimpleExtension(APPConstants.QN_COMMENTS);
        if (isComments != null) {
            resource.setContent(getCommentsFromFeed(feed));
        } else {
            List entries = feed.getEntries();
            if (entries != null) {
                String[] childNodes = new String[entries.size()];
                for (int i = 0; i < entries.size(); i++) {
                    Entry entry = (Entry) entries.get(i);
                    Link childLink = Utils.getLinkWithRel(entry, APPConstants.PARAMETER_PATH);
                    /*Link childLink = entry.getLink("path");
                    if (childLink == null) {
                        for (Link link : entry.getLinks()) {
                            if (link.getRel() != null &&
                                    link.getRel().equals("path")) {
                                childLink = link;
                                break;
                            }
                        }
                    }      */
                    childNodes[i] = URLDecoder.decode(childLink.getHref().toString());
                }
                resource.setContent(childNodes);
            }
        }

        return resource;
    }

    // creates a resource from the entry.
    private ResourceImpl createResourceFromEntry(Entry entry) throws RegistryException {
        RemoteResourceImpl resource = new RemoteResourceImpl();

        final Link pathLink = Utils.getLinkWithRel(entry, APPConstants.PARAMETER_PATH);//entry.getLink("path");
        String path = (pathLink != null) ? pathLink.getHref().toString() : entry.getTitle();
        path = URLDecoder.decode(path);
        resource.setPath(path);

        String mediaType = entry.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE));
        if (mediaType != null) {
            resource.setMediaType(mediaType);
        }
        //Set the UUDI when generating the resource from entry.
        //If UUDI not present RemoteRegistry executeQuery() will failed.
        if (entry.getId() != null) {
            resource.setUUID(entry.getId().toString().replace("urn:uuid:", ""));
        }
        org.wso2.carbon.registry.app.Properties properties =
                entry.getExtension(PropertyExtensionFactory.PROPERTIES);
        createPropertiesFromExtensionElement(properties, resource);
        if (entry.getAuthor() != null) {
            resource.setAuthorUserName(entry.getAuthor().getName());
        }
        resource.setLastModified(entry.getUpdated());
        String createdDate = entry.getSimpleExtension(
                new QName(APPConstants.NAMESPACE, "createdTime"));
        if (createdDate != null) {
            try {
				resource.setCreatedTime(new AtomDate(createdDate).getDate());
			} catch (IllegalArgumentException e) {
				log.error("Error occured while trying to parse the created date",e);
			}
        }

        String lastUpdatedUser = entry.getSimpleExtension(APPConstants.QN_LAST_UPDATER);
        if (lastUpdatedUser != null) {
            resource.setLastUpdaterUserName(lastUpdatedUser);
        }

        String snapshotID = entry.getSimpleExtension(APPConstants.QN_SNAPSHOT_ID);
        if (snapshotID != null) {
            resource.setMatchingSnapshotID(Long.parseLong(snapshotID));
        }

        resource.setDescription(entry.getSummary());

        final Content content = entry.getContentElement();
        if (mediaType == null) {
            if (content.getContentType() == Content.Type.TEXT) {
                resource.setMediaType("text/plain");
            } else if (content.getContentType() == Content.Type.MEDIA) {
                resource.setMediaType(content.getMimeType().toString());
            }
        }

        IRI srcIri = content.getSrc();
        if (srcIri != null) {
            try {
                final URL url = new URL(Utils.encodeRegistryPath(
                        URLDecoder.decode(getFilteredTenantCountedContentURL(srcIri.toString()), RegistryConstants.DEFAULT_CHARSET_ENCODING)) +
                        ((resource.getPermanentPath() != null) ?
                                RegistryConstants.URL_SEPARATOR + "version:" + snapshotID : ""));
                resource.setContentURL(url);
                if (authorizationString != null) {
                    resource.setAuthorizationString(authorizationString);
                }
                return resource;
            } catch (IOException e) {
                throw new RegistryException("unable to receive source. " + srcIri.toString());
            }
        }

        String content1 = entry.getContent();
        resource.setContent(content1);
        return resource;
    }

    public boolean resourceExists(String path) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse response = abderaClient.head(baseURI + APPConstants.ATOM + encodeURL(path),
                getAuthorization());
        boolean exists = (response.getType() == Response.ResponseType.SUCCESS);
        abderaClient.teardown();
        return exists;
    }

    public String put(String suggestedPath, org.wso2.carbon.registry.api.Resource resource)
            throws org.wso2.carbon.registry.api.RegistryException {
        return put(suggestedPath, (Resource) resource);
    }

    public String put(String suggestedPath, Resource resource) throws RegistryException {
        int idx = suggestedPath.lastIndexOf('/');
        String relativePath = suggestedPath.substring(idx + 1);

        if (Pattern.matches("\\p{Alnum}*[~!@#%^&*()\\+\\;<>\\[\\]{},/\\\\\"\',]+\\p{Alnum}*",
                relativePath)) {
            throw new RegistryException(
                    "Invalid characters have been used in the resource name. " + relativePath
                            + ". Special characters are ~!@#%^*()+{}[]|\\<>;\"\',");
        }

        String parentPath = idx > 1 ? suggestedPath.substring(0, idx) : "/";

         // Does the resource already exist?  If so this is an update (PUT) not a create (POST)
         // boolean alreadyExists = resourceExists(suggestedPath);
         //TODO: Needs to implement the REST PUT/POST operations properly.
         boolean alreadyExists = false; // Until the above fix is made, this is to make sure POST is called all the time.


        AbderaClient abderaClient = new AbderaClient(abdera);
        final Factory factory = abdera.getFactory();
        boolean isCollection = resource instanceof Collection;

        ExtensibleElement element;
        if (isCollection) {
            Feed feed = factory.newFeed();
            feed.setId(baseURI + APPConstants.ATOM + encodeURL(suggestedPath));
//            feed.setId(encodeURL(suggestedPath));
            feed.setTitle(suggestedPath);
            feed.setSubtitle(resource.getDescription());
            feed.addAuthor(username);
            feed.setUpdated(new Date());
            element = feed;
        } else {
            Entry entry = factory.newEntry();
            entry.setId(baseURI + APPConstants.ATOM + encodeURL(suggestedPath));
//            entry.setId(encodeURL(suggestedPath));
            entry.setTitle(suggestedPath);
            entry.setSummary(resource.getDescription());
            entry.addAuthor(username);
            entry.setUpdated(new Date());
            Object content = resource.getContent();
            if (content instanceof byte[]) {
                ByteArrayInputStream in = new ByteArrayInputStream((byte[]) content);
                entry.setContent(in);
            } else if (content instanceof InputStream) {
                entry.setContent((InputStream) content);
            } else {
                entry.setContent((String) content);
            }
            element = entry;
        }
        java.util.Properties properties = resource.getProperties();
        addPropertyExtensionElement(properties, factory, element,
                PropertyExtensionFactory.PROPERTIES,
                PropertyExtensionFactory.PROPERTY);
        final String mediaType = resource.getMediaType();
        if (mediaType != null && mediaType.length() > 0) {
            element.addSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE), mediaType);
        }
//        We are not setting the UUID as the id of the feed since the UUID can be null. Hence we are not changing the old code
        if(resource.getUUID() != null){
            element.addSimpleExtension(APPConstants.QN_UUID_TYPE,resource.getUUID());
        }
        element.addSimpleExtension(new QName(APPConstants.NAMESPACE, "parentPath"),
                resource.getParentPath());
        if (((ResourceImpl) resource).isContentModified()) {
            element.addSimpleExtension(new QName(APPConstants.NAMESPACE, "contentModified"),
                    "true");
        }

        RequestOptions requestOptions = getAuthorization();
        requestOptions.setSlug(relativePath);

        ClientResponse resp;
        //TODO: Needs to implement the REST PUT/POST operations properly.
        if (!alreadyExists) {
            resp = abderaClient.post(baseURI + APPConstants.ATOM + encodeURL(parentPath),
                    element, requestOptions);
        } else {
            resp = abderaClient.put(baseURI + APPConstants.ATOM + encodeURL(suggestedPath),
                    element, requestOptions);
        }
         if (resp.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            abderaClient.teardown();
            String msg = "User is not authorized to add the resource to " + suggestedPath;
            log.error(msg);
            throw new RegistryException(msg);
        } else if(resp.getType() != Response.ResponseType.SUCCESS) {
            String msg = "Add resource fail. Suggested Path: " + suggestedPath +
                    ", Response Status: " + resp.getStatus() +
                    ", Response Type: " + resp.getType();
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
//        ResourceImpl impl = (ResourceImpl)resource;
//        impl.setPath(resultPath);
//        // todo - fix this to use util routine?
//        int i = resultPath.lastIndexOf('/');
//        impl.setParentPath(i == 0 ? "/" : resultPath.substring(0, i));
        abderaClient.teardown();
        if (resp.getLocation() != null) {
            String location = resp.getLocation().toString();
            if (location != null) {
                if (location.startsWith(baseURI)) {
                    return location.substring(baseURI.length() +
                            APPConstants.ATOM.length()).replace("+", " ");
                }
                return location.replace("+", " ");
            }
        }
        return suggestedPath;
    }

    /**
     * Method to add resource properties from the properties extension element.
     *
     * @param properties the properties extension element.
     * @param resource   the properties.
     */
    public static void createPropertiesFromExtensionElement(
            org.wso2.carbon.registry.app.Properties properties,
            Resource resource) {
        if (properties != null) {
            ((ResourceImpl) resource).setPropertiesModified(true);
            List propertyList = properties.getExtensions(PropertyExtensionFactory.PROPERTY);
            for (Object aPropertyList : propertyList) {
                Property property = (Property) aPropertyList;
                PropertyName pn = property.getExtension(PropertyExtensionFactory.PROPERTY_NAME);
                List<PropertyValue> pv =
                        property.getExtensions(PropertyExtensionFactory.PROPERTY_VALUE);
                String propertyName = pn.getText();
                for (PropertyValue valueElement : pv) {
                    resource.addProperty(propertyName, valueElement.getPropertyValue());
                }
            }
            if (propertyList.size() == 0) {
                /* this is to mark the property to be deleted */
                resource.setProperties(null);
            }
        }
    }

    /**
     * This will generate extension element and that will add to the entry , the created element
     * will be something like &lt;node1name&gt; &lt;node2Name&gt; + &lt;name&gt;&lt;/name&gt;
     * &lt;value&gt;&lt;/value&gt; &lt;/node2Name&gt; &lt;/node1name&gt;
     *
     * @param properties List of Name value pairs
     * @param factory    Abdera Factory
     * @param entry      Instance of entry where extension element need to add
     * @param node1name  Name of node 1
     * @param node2Name  Name of node 2
     */
    public static void addPropertyExtensionElement(java.util.Properties properties,
                                                   Factory factory,
                                                   ExtensibleElement entry,
                                                   QName node1name,
                                                   QName node2Name) {
        if (properties != null && properties.size() != 0) {
            Properties propertyElement = factory.newExtensionElement(node1name);
            for (Object keyObj : properties.keySet()) {
                String key = (String) keyObj;
                Property property = factory.newExtensionElement(node2Name);
                PropertyName pn = factory.newExtensionElement(
                        PropertyExtensionFactory.PROPERTY_NAME);
                pn.setPropertyName(key);
                property.addName(pn);
                Object valueList = properties.get(key);
                if (valueList instanceof List) {
                    for (Object value : (List) valueList) {
                        // null values can be treated in the same manner as Strings, since casting
                        // wouldn't change null.
                        if (value == null || value instanceof String) {
                            PropertyValue pv = factory.newExtensionElement(
                                    PropertyExtensionFactory.PROPERTY_VALUE);
                            pv.setPropertyValue((String) value);
                            property.addValue(pv);
                        }
                    }
                    propertyElement.setProperty(property);
                }
            }
            entry.addExtension(propertyElement);
        }
    }

    public String importResource(String suggestedPath, String sourceURL, Resource resource)
            throws RegistryException {
        int idx = suggestedPath.lastIndexOf('/');
        String relativePath = suggestedPath.substring(idx + 1);

        if (Pattern.matches("\\p{Alnum}*[~!@#%^&*()\\+=\\-;<>\\s\\[\\]{},/\\\\\"\',]+\\p{Alnum}*",
                relativePath)) {
            throw new RegistryException("Invalid characters have been used in the resource name.");
        }
        AbderaClient abderaClient = new AbderaClient(abdera);
        final Factory factory = abdera.getFactory();
        Entry entry = factory.newEntry();
        entry.setId(baseURI + APPConstants.ATOM + encodeURL(suggestedPath));
        entry.setTitle(suggestedPath);
        entry.setSummary(resource.getDescription());
        entry.addAuthor(username);
        entry.setUpdated(new Date());
        java.util.Properties properties = resource.getProperties();
        addPropertyExtensionElement(properties, factory, entry,
                PropertyExtensionFactory.PROPERTIES,
                PropertyExtensionFactory.PROPERTY);
        final String mediaType = resource.getMediaType();
        if (mediaType != null && mediaType.length() > 0) {
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.NAMESPACE_MEDIA_TYPE), mediaType);
        }
        entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "parentPath"), resource.getParentPath());
        if (((ResourceImpl) resource).isContentModified()) {
            entry.addSimpleExtension(new QName(APPConstants.NAMESPACE, "contentModified"), "true");
        }
        //        We are not setting the UUID as the id of the feed since the UUID can be null. Hence we are not changing the old code
        if(resource.getUUID() != null){
            entry.addSimpleExtension(APPConstants.QN_UUID_TYPE,resource.getUUID());
        }

        RequestOptions opts = getAuthorization();
        opts.setSlug(suggestedPath);
        opts.setContentType(resource.getMediaType());
        ClientResponse response =
                abderaClient.post(baseURI + APPConstants.ATOM + "?importURL=" +
                        encodeURL(sourceURL + RegistryConstants.URL_SEPARATOR +
                                APPConstants.IMPORT_MEDIA_TYPE),
                        entry,
                        opts);
        if (response.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource at " + sourceURL + " imported." +
                        ", Response Status: " + response.getStatus() +
                        ", Response Type: " + response.getType());
            }
            abderaClient.teardown();

            String location = response.getLocation().toString();
            if (location.startsWith(baseURI)) {
                return location.substring(baseURI.length() +
                        APPConstants.ATOM.length()).replace("+", " ");
            }
            return location.replace("+", " ");
        } else {
            String msg = "failed to import resource at " + sourceURL + "." +
                    ", Response Status: " + response.getStatus() +
                    ", Response Type: " + response.getType();
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public void delete(String path) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse resp = abderaClient.delete(baseURI + APPConstants.ATOM + encodeURL(path),
                getAuthorization());
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource at " + path + " deleted" +
                        ", Response Status: " + resp.getStatus() +
                        ", Response Type: " + resp.getType());
            }
            abderaClient.teardown();
        } else {
            String msg = "resource at " + path + " delete failed" +
                    ", Response Status: " + resp.getStatus() +
                    ", Response Type: " + resp.getType();
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }


    public String rename(String currentPath, String newPath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ByteArrayInputStream is = new ByteArrayInputStream(newPath.getBytes());
        ClientResponse resp =
                abderaClient.post(baseURI + APPConstants.ATOM +
                        encodeURL(currentPath +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_RENAME),
                        is,
                        getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource rename " + currentPath + " to " + newPath + "  succeeded" +
                        getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "resource rename from " + currentPath + " to " + newPath + " failed" +
                    getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
        return newPath;
    }

    private String getStatusAndType(ClientResponse resp) {
        return ", Response Status: " + resp.getStatus() +
                ", Response Type: " + resp.getType();
    }

    public String move(String currentPath, String newPath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ByteArrayInputStream is = new ByteArrayInputStream(newPath.getBytes());
        ClientResponse resp =
                abderaClient.post(baseURI + APPConstants.ATOM +
                        encodeURL(currentPath +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_MOVE),
                        is,
                        getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource move  from " + currentPath + " to " + newPath + " succeeded" +
                        getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "resource move from " + currentPath + " to " + newPath + " failed" +
                          getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
        // TODO - should pull real result path from the server response.
        return newPath;
    }

    public String copy(String sourcePath, String targetPath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ByteArrayInputStream is = new ByteArrayInputStream(targetPath.getBytes());
        ClientResponse resp =
                abderaClient.post(baseURI + APPConstants.ATOM +
                        encodeURL(sourcePath +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_COPY),
                        is,
                        getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource copy from " + sourcePath + " to " + targetPath + " succeeded" +
                            getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "resource copy from " + sourcePath + " to " + targetPath + "  failed" +
                    getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
        // TODO - should pull real result path from the server response.
        return targetPath;
    }

    public void createVersion(String path) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ByteArrayInputStream is = new ByteArrayInputStream("createVersion".getBytes());
        ClientResponse clientResponse =
                abderaClient.post(baseURI + APPConstants.ATOM +
                        encodeURL(path + RegistryConstants.URL_SEPARATOR +
                                APPConstants.CHECKPOINT),
                        is,
                        getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        final int status = clientResponse.getStatus();
        if (status < 200 || status > 299) {
            RegistryException e;
            if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                e = new ResourceNotFoundException(path);
            } else {
                e = new RegistryException("Response Status: " + clientResponse.getStatusText());
            }
            abderaClient.teardown();
            throw e;
        }
        abderaClient.teardown();
    }

    public String[] getVersions(String path) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        encodeURL(path +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_VERSION),
                        getAuthorization());
        Document introspection = clientResponse.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        List entries = feed.getEntries();
        if (entries != null) {
            String[] versions = new String[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                versions[i] = Utils.getLinkWithRel(entry, "versionLink").getHref().toString();
                //versions[i] = entry.getLink("versionLink").getHref().toString();
            }
            abderaClient.teardown();
            return versions;
        }
        abderaClient.teardown();
        return new String[0];
    }

    public void restoreVersion(String versionPath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        Entry entry = abdera.getFactory().newEntry();
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(versionPath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_RESTORE),
                entry,
                getAuthorization());
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource restore to " + versionPath + " succeeded" +
                       getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "resource restore " + versionPath + "  failed" +
                    getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public void addAssociation(String sourcePath, String associationPaths, String associationType)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        final Factory factory = abdera.getFactory();
        Element el = factory.newElement(APPConstants.QN_ASSOC);
        el.setAttributeValue(APPConstants.ASSOC_TYPE, associationType);
        el.setText(associationPaths);
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(sourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.ASSOCIATIONS),
                el,
                getAuthorization());
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("associating " + sourcePath + " to " + associationPaths +
                        " type " + associationType + " succeeded" +
                        getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "associating " + sourcePath + " to " + associationPaths +
                    " type " + associationType + "failed" +
                   getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }


    public void removeAssociation(String sourcePath, String associationPaths,
                                  String associationType)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        final Factory factory = abdera.getFactory();
        Element el = factory.newElement(APPConstants.QN_ASSOC);
        el.setAttributeValue(APPConstants.ASSOC_TYPE, associationType);
        el.setText(associationPaths);
        RequestOptions requestOptions = getAuthorization();
        requestOptions.setHeader("Destination", associationPaths);
        requestOptions.setHeader("AssociationType", associationType);
        ClientResponse resp = abderaClient.delete(baseURI + APPConstants.ATOM +
                encodeURL(sourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.ASSOCIATIONS),
                requestOptions);
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("remove association " + sourcePath + " to " + associationPaths +
                        " type " + associationType + " succeeded" +
                        getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "remove association " + sourcePath + " to " + associationPaths +
                    " type " + associationType + "failed" +
                   getStatusAndType(resp);
            log.error(msg);
            abderaClient.teardown();
            throw new RegistryException(msg);
        }
    }

    public Association[] getAllAssociations(String resourcePath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse resp = abderaClient.get(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.ASSOCIATIONS),
                getAuthorization());
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            Document introspection = resp.getDocument();
            Feed feed = (Feed) introspection.getRoot();
            Association[] associations = getAssociationsFromFeed(feed);
            abderaClient.teardown();
            return associations;
        } else {
            String msg = "uanble to get all associations for path " + resourcePath +
                    getStatusAndType(resp);
            log.error(msg);
            abderaClient.teardown();
            throw new RegistryException(msg);
        }
    }

    // method to obtain associations from a feed.
    private static Association[] getAssociationsFromFeed(Feed feed) {
        List entries = feed.getEntries();
        Association associations[] = null;
        if (entries != null) {
            associations = new Association[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                Association association = new Association();
                association.setSourcePath(entry.getTitle());
                association.setDestinationPath(entry.getContent());
                association.setAssociationType(entry.getSummary());
                associations[i] = association;
            }
        }
        return associations;
    }

    public Association[] getAssociations(String resourcePath, String associationType)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        encodeURL(resourcePath + RegistryConstants.URL_SEPARATOR +
                                APPConstants.ASSOCIATIONS + ":" +
                                associationType),
                        getAuthorization());
        Document introspection = clientResponse.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        List entries = feed.getEntries();
        Association associations[] = null;
        if (entries != null) {
            associations = new Association[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                Association association = new Association();
                association.setSourcePath(entry.getTitle());
                association.setDestinationPath(entry.getContent());
                association.setAssociationType(entry.getSummary());
                associations[i] = association;
            }
        }
        abderaClient.teardown();
        return associations;
    }

    public void applyTag(String resourcePath, String tag) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        Entry entry = abdera.getFactory().newEntry();
        entry.setContent(tag);
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_TAGS),
                entry,
                getAuthorization());

        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("Applying tag: " + tag + " for resourcePath + " + resourcePath +
                        " succeeded." +
                       getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg =
                    "Applying tag: " + tag + " for resourcePath + " + resourcePath + " failed." +
                           getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public TaggedResourcePath[] getResourcePathsWithTag(String tag) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse = abderaClient.get(baseURI + "/tags/" + tag,
                getAuthorization());

        Document introspection =
                clientResponse.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        List entries = feed.getEntries();
        TaggedResourcePath taggedResourcePaths[] = null;
        if (entries != null) {
            taggedResourcePaths = new TaggedResourcePath[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                org.wso2.carbon.registry.app.Properties properties =
                        entry.getExtension(PropertyExtensionFactory.TAGS);
                List propertyList = properties.getExtensions(PropertyExtensionFactory.TAG);
                Map<String, String> map = new HashMap<String, String>();
                for (Object aPropertyList : propertyList) {
                    Property property = (Property) aPropertyList;
                    PropertyName pn = property.getExtension(PropertyExtensionFactory.PROPERTY_NAME);
                    PropertyValue pv =
                            property.getExtension(PropertyExtensionFactory.PROPERTY_VALUE);
                    map.put(pn.getText(), pv.getText());
                }
                TaggedResourcePath tagPath = new TaggedResourcePath();
                tagPath.setResourcePath(entry.getTitle());
                tagPath.setTagCount(
                        Long.parseLong(entry.getSimpleExtension(new QName(APPConstants.NAMESPACE,
                                "taggings"))));
                taggedResourcePaths[i] = tagPath;
                tagPath.setTagCounts(map);
            }
        }
        abderaClient.teardown();
        return taggedResourcePaths;
    }

    public Tag[] getTags(String resourcePath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse = abderaClient.get(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_TAGS),
                getAuthorization());

        Document introspection =
                clientResponse.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        List entries = feed.getEntries();
        Tag tags[] = null;
        if (entries != null) {
            tags = new Tag[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                Tag tag = new Tag();
                tag.setTagCount(Long.parseLong(entry.getSimpleExtension(
                        new QName(APPConstants.NAMESPACE, "taggings"))));
                tag.setTagName(entry.getTitle());
                tags[i] = tag;
            }
        }
        abderaClient.teardown();
        return tags;
    }

    public void removeTag(String path, String tag) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        String encodedTag;
        try {
            encodedTag = URLEncoder.encode(tag, RegistryConstants.DEFAULT_CHARSET_ENCODING);
        } catch (Exception e) {
            log.error("An exception occurred while processing removeTag request", e);
            return;
        }
        ClientResponse resp = abderaClient.delete(baseURI + APPConstants.ATOM +
                encodeURL(path +
                        RegistryConstants.URL_SEPARATOR) +
                        "tag:" + encodedTag,
                getAuthorization());

        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("Removing tag: " + tag + " for resourcePath + " + path + " succeeded." +
                       getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "Removing tag: " + tag + " for resourcePath + " + path + " failed." +
                   getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public String addComment(String resourcePath, org.wso2.carbon.registry.api.Comment comment)
            throws org.wso2.carbon.registry.api.RegistryException {
        return addComment(resourcePath, (org.wso2.carbon.registry.core.Comment) comment);
    }

    public String addComment(String resourcePath, org.wso2.carbon.registry.core.Comment comment)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        Entry entry = abdera.getFactory().newEntry();
        entry.setId("tag:commentID"); // TODO - generate real ID
        entry.setTitle("Comment");
        entry.setUpdated(comment.getCreatedTime());
        entry.addAuthor(comment.getUser());
        entry.setContent(comment.getText());
        ClientResponse resp =
                abderaClient.post(baseURI + APPConstants.ATOM +
                        encodeURL(resourcePath +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_COMMENTS),
                        entry,
                        getAuthorization());
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("Adding comment for resourcePath + " + resourcePath + " succeeded." +
                        getStatusAndType(resp));
            }
            abderaClient.teardown();
            String location = resp.getLocation().toString();
            if (location.startsWith(baseURI)) {
                return location.substring(baseURI.length() +
                        APPConstants.ATOM.length()).replace("+", " ");
            }
            return location.replace("+", " ");
        } else {
            String msg = "Adding comment for resourcePath + " + resourcePath + " failed." +
                   getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }


    public void editComment(String commentPath, String text) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        Entry entry = abdera.getFactory().newEntry();
        entry.setContent(text);
        ClientResponse resp = abderaClient.put(baseURI + APPConstants.ATOM +
                encodeURL(commentPath),
                entry,
                getAuthorization());

        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("Editing comment for resourcePath + " + commentPath + " succeeded." +
                       getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "Editing comment for resourcePath + " + commentPath + " failed." +
                    getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public org.wso2.carbon.registry.core.Comment[] getComments(String _resourcePath)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        String resourcePath = _resourcePath;
        resourcePath = encodeURL(resourcePath);
        if (resourcePath.contains(RegistryConstants.VERSION_SEPARATOR)) {
            int index = resourcePath.lastIndexOf(RegistryConstants.VERSION_SEPARATOR);
            resourcePath = resourcePath.substring(0, index).replace(":", "%3A") +
                    resourcePath.substring(index);
        } else {
            resourcePath = resourcePath.replace(":", "%3A");
        }
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        resourcePath + RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_COMMENTS,
                        getAuthorization());
        Document introspection = clientResponse.getDocument();
        Element element = introspection.getRoot();
        Feed feed = (Feed) element;
        org.wso2.carbon.registry.core.Comment[] comments = getCommentsFromFeed(feed);
        abderaClient.teardown();

        return comments;
    }

    // method to obtain comments from feed.
    private org.wso2.carbon.registry.core.Comment[] getCommentsFromFeed(Feed feed) {
        List entries = feed.getEntries();
        org.wso2.carbon.registry.core.Comment comments[] = null;
        if (entries != null) {
            comments = new org.wso2.carbon.registry.core.Comment[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                org.wso2.carbon.registry.core.Comment comment =
                        new org.wso2.carbon.registry.core.Comment();
                if (entry.getUpdated() != null) {
                    comment.setCreatedTime(entry.getUpdated());
                }
                final Link resourceLink = Utils.getLinkWithRel(entry, "resourcePath");
                //final Link resourceLink = entry.getLink("resourcePath");
                if (resourceLink != null) {
                    comment.setResourcePath(URLDecoder.decode(resourceLink.getHref().toString()));
                }
                Link pathLink = Utils.getLinkWithRel(entry, APPConstants.PARAMETER_PATH);
                if (pathLink != null) {
                    String path = URLDecoder.decode(pathLink.getHref().toString());
                    comment.setPath(path);
                    comment.setCommentPath(path);
                }
                comment.setText(entry.getContent());
                comment.setUser(entry.getAuthor().getName());
                comments[i] = comment;
            }
        }
        return comments;
    }

    public void rateResource(String resourcePath, int rating) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ByteArrayInputStream is = new ByteArrayInputStream(Integer.toString(rating).getBytes());
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_RATINGS),
                is,
                getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("rating resource + " + resourcePath + " succeeded." +
                       getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "rating resource + " + resourcePath + " failed." +
                    getStatusAndType(resp);

            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public float getAverageRating(String resourcePath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        encodeURL(resourcePath +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_RATINGS),
                        getAuthorization());

        if (clientResponse.getStatus() != HttpURLConnection.HTTP_OK) {
            // throw RegistryException
            String msg = "Getting average rating failed. Path: " + resourcePath +
                    ", Response Status: " + clientResponse.getStatus() +
                    ", Response Type: " + clientResponse.getType();
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }

        Document introspection = clientResponse.getDocument();
        if (introspection.getRoot() instanceof Feed) {
            Feed feed = (Feed) introspection.getRoot();
            String floatValue = feed.getSimpleExtension(APPConstants.QN_AVERAGE_RATING);
            abderaClient.teardown();
            return Float.parseFloat(floatValue);
        }
        return 0;
    }

    public int getRating(String path, String userName) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        encodeURL(path + RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_RATINGS + ":" + userName),
                        getAuthorization());
        Document introspection =
                clientResponse.getDocument();
        if (introspection.getRoot() instanceof Feed) {
            Feed feed = (Feed) introspection.getRoot();
            List<Entry> entries = feed.getEntries();
            if (entries.size() == 1) {
                String intValue = entries.get(0).getContent();
                abderaClient.teardown();
                return Integer.parseInt(intValue);
            }
        }
        String msg = "Getting rating failed. Path: " + path;
        abderaClient.teardown();
        log.error(msg);
        throw new RegistryException(msg);
    }

    public Collection executeQuery(String path, Map parameters) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        RequestOptions requestOptions = getAuthorization();
        if (path == null) {
            path = "/";
        }
        ClientResponse resp = abderaClient.get(baseURI + APPConstants.ATOM +
                encodeURL(path + RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_QUERY) + "?" +
                buildQueryString(parameters),
                requestOptions);
        Document introspection = resp.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        Collection c = createResourceFromFeed(feed);
        abderaClient.teardown();
        return c;
    }

    /**
     * Method to build the query string from a parameter map.
     *
     * @param parameters the parameter map.
     *
     * @return the query string.
     */
    public static String buildQueryString(Map parameters) {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (Object keyObj : parameters.keySet()) {
            String name = (String) keyObj;
            String value = (String) parameters.get(name);
            if (!first) {
                buffer.append("&");
            } else {
                first = false;
            }
            try {
                buffer.append(URLEncoder.encode(name, RegistryConstants.DEFAULT_CHARSET_ENCODING));
                buffer.append("=");
                buffer.append(URLEncoder.encode(value, RegistryConstants.DEFAULT_CHARSET_ENCODING));
            } catch (UnsupportedEncodingException e) {
                // What the heck?? :)
                return "";
            }
        }
        return buffer.toString();
    }

    /**
     * Method to build the parameter map from a query string..
     *
     * @param _value the query string.
     *
     * @return the parameter map.
     */
    public static Map decodeQueryString(String _value) {
        Map<String, String> paramMap = new HashMap<String, String>();
        String value=_value;
        try {
            value = URLDecoder.decode(value, RegistryConstants.DEFAULT_CHARSET_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        for (String param : value.trim().split("&")) {
            if (param.trim().length() == 0) {
                continue;
            }

            param = param.trim();

            String paramKey = param.substring(0, param.indexOf('='));
            String paramValue = param.substring(param.indexOf('=') + 1);
            paramMap.put(paramKey, paramValue);

//            String[] paramParts = param.trim().split("=");
//            paramMap.put(paramParts[0], paramParts[1]);
        }

        return paramMap;
    }

    public LogEntry[] getLogs(String _resourcePath,
                              int action,
                              String userName,
                              Date from,
                              Date to,
                              boolean recentFirst) throws RegistryException {

        String resourcePath = _resourcePath;
        if (resourcePath == null || "".equals(resourcePath)) {
            resourcePath = "/";
        }

        AbderaClient abderaClient = new AbderaClient(abdera);
        RequestOptions requestOptions = getAuthorization();
        requestOptions.addDateHeader("ToDate", to);
        requestOptions.addDateHeader("FromDate", from);
        requestOptions.addHeader("Action", "" + action);
        requestOptions.addHeader("Author", userName);
        ClientResponse resp = abderaClient.get(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_LOGS),
                requestOptions);
        Document introspection =
                resp.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        List entries = feed.getEntries();
        LogEntry logs[] = null;
        if (entries != null) {
            logs = new LogEntry[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                LogEntry logEntry = new LogEntry();
                logEntry.setDate(entry.getEdited());
                logEntry.setActionData(entry.getContent());
                logEntry.setUserName(entry.getAuthor().getName());
                logEntry.setAction(Integer.parseInt(
                        entry.getSimpleExtension(new QName(APPConstants.NAMESPACE, "action"))));
                String path = entry.getSimpleExtension(new QName(APPConstants.NAMESPACE, APPConstants.PARAMETER_PATH));
                logEntry.setResourcePath(path);
                logs[i] = logEntry;
            }
        }
        abderaClient.teardown();
        return logs;
    }


    public LogEntryCollection getLogCollection(String resourcePath,
                                               int action,
                                               String userName,
                                               Date from,
                                               Date to,
                                               boolean recentFirst) throws RegistryException {
        throw new UnsupportedOperationException("Sorry we need to implement this method");
    }

    /**
     * This method will create a RequestOptions object adding  Authorization headers.
     *
     * @return RequestOptions Created RequestOptions object
     */
    private RequestOptions getAuthorization() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setAuthorization(authorizationString);
        return requestOptions;
    }

    private RequestOptions getAuthorizationForCaching(String path) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setAuthorization(authorizationString);
        requestOptions.setHeader("if-none-match", cache.getETag(path));
        return requestOptions;
    }

    /**
     * Method to encode a registry path as a URL.
     *
     * @param path the registry path.
     *
     * @return the encoded URL-format.
     */
    public static String encodeURL(String path) {
        String encodedRegistryPath = Utils.encodeRegistryPath(path);
        //I know I should use URLEncoder but when I used that it tries
        //  to encode "/" as well which is not need here
        return encodedRegistryPath.replaceAll(" ", "+");
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void addUser(String userName,
                        String password,
                        String confirmPassword,
                        String roleName,
                        String friendlyName) throws RegistryException {
        throw new UnsupportedOperationException("This method is no longer supported.");
    }

    public String[] getAvailableAspects() {
        throw new UnsupportedOperationException();
    }

    public void associateAspect(String resourcePath, String aspect) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        // POST as text to "<resource>;aspects"
        ByteArrayInputStream is = new ByteArrayInputStream(aspect.getBytes());
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.ASPECTS),
                is,
                getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                String msg = "Resource associated to aspect " +
                                getOnAppendedVal(aspect,resourcePath) + ".";
                log.debug(msg);
            }
            abderaClient.teardown();
        } else {
            String msg = "Resource associated to aspect " +
                    getOnAppendedVal(aspect,resourcePath) + ".";
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public void invokeAspect(String resourcePath, String aspectName, String action)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);

        // The content doesn't really matter here, so this is a placeholder for now.
        // Later on we'll likely want to support parameterized invocations, so we'll likely
        // enable posting form-encoded data.
        ByteArrayInputStream is = new ByteArrayInputStream("invoke".getBytes());
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.ASPECT) + "(" + encodeURL(aspectName) + ")" +
                action,
                is,
                getAuthorization().setContentType(TEXT_PLAIN_MEDIA_TYPE));
        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                String msg = "Succeeded in invoking aspect " + getOnAppendedVal(aspectName,resourcePath) +
                        " action " + action + ".";
                log.debug(msg);
            }
            abderaClient.teardown();
        } else {
            String msg = "Couldn't invoke aspect " + getOnAppendedVal(aspectName,resourcePath) +
                    " action " + action + ".";
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    private String getOnAppendedVal(String aspect, String resourcePath) {
     return aspect + " on " + resourcePath;
    }


    public void invokeAspect(String resourcePath, String aspectName, String action,
                             Map<String, String> parameters)
            throws RegistryException {
        throw new UnsupportedOperationException(
                "invokeAspect with parameters is not supported by Remote Registry");
    }

    public boolean removeAspect(String aspect) throws RegistryException {
        throw new UnsupportedOperationException(
                "removeAspect method is not supported by Remote Registry");
    }

    public boolean addAspect(String name, Aspect aspect) throws RegistryException {
        throw new UnsupportedOperationException(
                "removeAspect method is not supported by Remote Registry");
    }

    public void beginTransaction() throws RegistryException {
    }

    public void commitTransaction() throws RegistryException {
    }

    public void rollbackTransaction() throws RegistryException {
    }

    public String[] getAspectActions(String resourcePath, String aspectName)
            throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.ASPECT) + "(" + encodeURL(aspectName) + ")",
                        getAuthorization());
        Document introspection = clientResponse.getDocument();
        Feed feed = (Feed) introspection.getRoot();
        List entries = feed.getEntries();
        if (entries != null) {
            String[] aspectActions = new String[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = (Entry) entries.get(i);
                aspectActions[i] = entry.getContent();
            }
            abderaClient.teardown();
            return aspectActions;
        }
        abderaClient.teardown();
        return new String[0];
    }

    public RegistryContext getRegistryContext() {
        return RegistryContext.getBaseInstance();
    }


    public Collection searchContent(String keywords) throws RegistryException {
        throw new UnsupportedOperationException();
    }

    public void createLink(String path, String target) throws RegistryException {
        throw new UnsupportedOperationException();
    }

    public void createLink(String path, String target, String subTargetPath)
            throws RegistryException {
        throw new UnsupportedOperationException();
    }

    public void removeLink(String path) throws RegistryException {
        throw new UnsupportedOperationException();
    }

    // check in, check out functionality

    public void restore(String path, Reader reader) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        restore(path, reader, abderaClient);
    }

    public void dump(String path, Writer writer) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        dump(path, abderaClient, writer);
    }

    // some extended functionality as dump, restore may not work with the default timeout

    /**
     * check in the input axiom element into database
     *
     * @param path    path to check in
     * @param reader  reader containing resource
     * @param timeout the time to wait.
     *
     * @throws RegistryException if the operation failed.
     */
    public void restore(String path, Reader reader, int timeout) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        abderaClient.setSocketTimeout(timeout);
        restore(path, reader, abderaClient);
        abderaClient.teardown();
    }

    /**
     * check out the given path as an xml
     *
     * @param path    path to check out
     * @param writer  writer to write the response
     * @param timeout the time to wait.
     *
     * @throws RegistryException if the operation failed.
     */
    public void dump(String path, int timeout, Writer writer) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        abderaClient.setSocketTimeout(timeout);
        dump(path, abderaClient, writer);
        abderaClient.teardown();
    }

    // the implementation for dump
    private void restore(String path, Reader reader, AbderaClient abderaClient)
            throws RegistryException {
        InputStream is = new ReaderInputStream(reader);
        ClientResponse resp = abderaClient.post(baseURI + APPConstants.ATOM +
                encodeURL(path +
                        RegistryConstants.URL_SEPARATOR +
                        APPConstants.PARAMETER_DUMP),
                is,
                getAuthorization());

        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("resource dump restored at " + path);
            }
        } else {
            String msg = "Restoring to " + path + " failed.";
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    // the implementation for dump
    private void dump(String path, AbderaClient abderaClient, Writer writer)
            throws RegistryException {
        ClientResponse clientResponse =
                abderaClient.get(baseURI + APPConstants.ATOM +
                        encodeURL(path +
                                RegistryConstants.URL_SEPARATOR +
                                APPConstants.PARAMETER_DUMP),
                        getAuthorization());
        if (clientResponse.getType() == Response.ResponseType.SUCCESS) {
            Document introspection = clientResponse.getDocument();
            Element element = introspection.getRoot();
            if (element instanceof OMElement) {
                try {
                    ((OMElement) element).serialize(writer);
                } catch (XMLStreamException e) {
                    throw new RegistryException("Failed to serialize the xml", e);
                }
            }
        } else {
            String msg = "Failed to serialize the xml. Received Response: " +
                    clientResponse.getStatusText();
            log.error(msg);
            throw new RegistryException(msg);
        }
    }

    public String getEventingServiceURL(String path) throws RegistryException {
        throw new UnsupportedOperationException();
    }

    public void setEventingServiceURL(String path, String eventingServiceURL)
            throws RegistryException {
        throw new UnsupportedOperationException();
    }

    public void removeComment(String commentPath) throws RegistryException {
        AbderaClient abderaClient = new AbderaClient(abdera);
        String resourcePath = commentPath.substring(0, commentPath.indexOf(";comments:"));
        int commentId = Integer.parseInt(
                commentPath.substring(commentPath.indexOf(";comments:") + ";comments:".length()));
        ClientResponse resp = abderaClient.delete(baseURI + APPConstants.ATOM +
                encodeURL(resourcePath +
                        RegistryConstants.URL_SEPARATOR) +
                        "comment:" + commentId,
                getAuthorization());

        if (resp.getType() == Response.ResponseType.SUCCESS) {
            if (log.isDebugEnabled()) {
                log.debug("Removing comment: " + commentId + " for resourcePath + " + resourcePath +
                        " succeeded." + getStatusAndType(resp));
            }
            abderaClient.teardown();
        } else {
            String msg = "Removing comment: " + commentId + " for resourcePath + " + resourcePath +
                        " succeeded." + getStatusAndType(resp);
            abderaClient.teardown();
            log.error(msg);
            throw new RegistryException(msg);
        }

    }

    public boolean removeVersionHistory(String path, long snapshotId)
    		throws RegistryException {
    	throw new UnsupportedOperationException("Operation not permitted");
    }

    private String getFilteredTenantCountedContentURL(String srcIri) throws RegistryException {

        //Check whether the srcIri is a local remote registry related one, else evaluate tenant specific uri
        if (srcIri.startsWith(baseURI)) {
            return srcIri;
        }

        String url;
        String protocol = "";
        try {
            if (baseURI.startsWith("https")) {
                protocol = "https";
            } else if (baseURI.startsWith("http")) {
                protocol = "http";
            }

            String hostDomain = protocol + "://"
                    + new URL(srcIri).getHost() + ":"
                    + new URL(srcIri).getPort();

            String srcUriReplace = hostDomain + "/"
                    + "registry" + "/";
            url = srcIri.replace(srcUriReplace, baseURI + "/");

        } catch (MalformedURLException e) {
            throw new RegistryException("Malformed Host Url " + srcIri);
        }
        return url;
    }

}
