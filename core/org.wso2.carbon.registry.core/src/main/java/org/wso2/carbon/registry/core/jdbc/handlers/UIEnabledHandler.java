/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.jdbc.handlers;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Base class for handler implementations which generate custom UIs. There are three main categories
 * of custom UIs. Those are:
 * <p/>
 * Browse: Displays the resource contents in read-only mode. Edit: Provides controls for editing the
 * contents of an existing resource. New: Provides controls for creating a new resource.
 * <p/>
 * Handler may provide any number of UIs under above three categories. For example handler may
 * provide two UIs to browse the contents of an XML file. Handler implementations should provide the
 * UI key and descriptive name for the UI for each of the UIs there are generating.
 * <p/>
 * Edit and New UIs should have an associated EditProcessor implementation to convert the UI inputs
 * to resource content.
 */
public abstract class UIEnabledHandler extends Handler {

    private String defaultBrowseView;
    private String defaultEditView;
    private String defaultNewView;

    private static final String REG_PARENT_PATH = "<reg>parentPath</reg>";

    /**
     * Implementation of the {@link Handler#get} method of the Handler class. UIEnabledHandler
     * implementations should not implement (override) this method. Instead, they should implement
     * {@link #getBrowseView}, {@link #getEditView} and {@link #getNewView} methods to generate
     * corresponding UIs.
     *
     * @param requestContext Details of the request.
     *
     * @return If valid custom UI is specified in the resource path, this will return a Resource
     *         instance with content filled as a HTML UI that can be rendered. If not, this will
     *         return a Resource instance with raw content for resources and child list for
     *         collections.
     * @throws RegistryException
     */
    public Resource get(RequestContext requestContext) throws RegistryException {

        ResourcePath resourcePath = requestContext.getResourcePath();
        Resource resource;
        if (resourcePath.parameterExists(RegistryConstants.BROWSE_PROPERTY)) {

            resource = generateBrowseView(resourcePath, requestContext);

        } else if (resourcePath.parameterExists(RegistryConstants.EDIT_PROPERTY)) {

            resource = generateEditView(resourcePath, requestContext);

        } else if (resourcePath.parameterExists(RegistryConstants.NEW_PROPERTY)) {

            resource = generateNewView(resourcePath, requestContext);

        } else {
            resource = getRawResource(requestContext);
        }

        fillViews(resource);
        return resource;
    }

    /**
     * Returns a Resource instance specified in the requestContext. Resource is set with raw
     * resource content. UIEnabledHandler implementations may override this method to provide
     * alternative contents for the raw resource.
     *
     * @param requestContext Request details.
     *
     * @return Resource instance.
     * @throws RegistryException if an error occurs while getting the raw resource.
     */
    public Resource getRawResource(RequestContext requestContext)
            throws RegistryException {

        if (requestContext.getResource() != null) {
            return requestContext.getResource();
        }

        if (requestContext.getResourcePath().isCurrentVersion()) {
            return requestContext.getRepository().get(requestContext.getResourcePath().getPath());
        }

        return null;
    }

    /**
     * Implementations have to implement this, if they provide more than one browse view.
     * <p/>
     * Implementations can provide the identification key and a descriptive name for all browse UIs
     * they provide by implementing this method. These information has to be provided as a string
     * array, where each string contains information about one UI. Information of a UI should be in
     * the form of "<UI key>:<display name>".
     * <p/>
     * For example, an implementation of this method may look like:
     * <p/>
     * public String[] getBrowseViews() { return new String[] { "text:Text view", "summary:Summary",
     * "details:Detailed view"}; }
     * <p/>
     * Above example indicates that the handler provides three browse views with keys text, summary
     * and details.
     *
     * @return String array containing the information about available UIs.
     */
    public abstract String[] getBrowseViews();

    /**
     * Implementations have to implement this, if they provide more than edit browse view. Provides
     * information about edit view UIs. Details are similar to {@link #getBrowseViews} method.
     *
     * @return String array containing the information about available UIs.
     */
    public abstract String[] getEditViews();

    /**
     * Implementations have to implement this, if they provide more than one new view. Provides
     * information about new resource view UIs. Details are similar to {@link #getBrowseViews}
     * method.
     *
     * @return String array containing the information about available UIs.
     */
    public String[] getNewViews() {
        return new String[]{"undefined:UI name is not defined"};
    }

    /**
     * Among the multiple views that a handler may generate, one view is identified as the default
     * view, which is used to render the contents, if UI is not specified. It is set to raw view by
     * default. Handler implementations may override this method to provide alternative default view
     * for browsing the resource content.
     *
     * @return UI key of the default UI.
     */
    public String getDefaultBrowseView() {

        if (defaultBrowseView == null) {
            defaultBrowseView = calculateDefaultView(getBrowseViews());
        }

        return defaultBrowseView;
    }

    /**
     * Method to set the default browse view.
     *
     * @param viewName UI key of the default browse UI
     */
    protected void setDefaultBrowseView(String viewName) {
        defaultBrowseView = viewName;
    }

    /**
     * Among the multiple views that a handler may generate, one view is identified as the default
     * view, which is used to edit the contents, if UI is not specified. It is set to raw view by
     * default. Handler implementations may override this method to provide alternative default view
     * for editing the resource content.
     *
     * @return UI key of the default UI.
     */
    public String getDefaultEditView() {

        if (defaultEditView == null) {
            defaultEditView = calculateDefaultView(getEditViews());
        }

        return defaultEditView;
    }

    /**
     * Method to set the default edit view.
     *
     * @param viewName UI key of the default edit UI
     */
    protected void setDefaultEditView(String viewName) {
        defaultEditView = viewName;
    }

    /**
     * Among the multiple views that a handler may generate, one view is identified as the default
     * view, which is used to render the new resource creation UI, if UI is not specified. It is set
     * to undefined view by default. This is to support handlers with only one view.
     * <p/>
     * Handler implementations that provide multiple new views should override this method to
     * provide default view for adding new resources.
     *
     * @return UI key of the default UI.
     */
    public String getDefaultNewView() {

        if (defaultNewView == null) {
            defaultNewView = calculateDefaultView(getNewViews());
        }

        return defaultNewView;
    }

    /**
     * Method to set the default new view.
     *
     * @param viewName UI key of the default new UI
     */
    protected void setDefaultNewView(String viewName) {
        defaultNewView = viewName;
    }

    /**
     * Implementations of this method should generate a HTML UI for rendering the resource content
     * and set it as the content of the returned resource.
     *
     * @param browseViewKey  UI key of the browse UI.
     * @param requestContext Details of the request.
     *
     * @return Resource filled with HTML UI as the content.
     * @throws RegistryException if an error occurs while getting the browse view.
     */
    public Resource getBrowseView(String browseViewKey, RequestContext requestContext)
            throws RegistryException {
        return null;
    }

    /**
     * Implementations of this method should generate a HTML UI for editing the resource content and
     * set it as the content of the returned resource.
     * <p/>
     * Generated UI should pass following parameters as HTTP request parameters to the custom
     * edit/new processing servlet. A common method to pass these parameters is to write them as
     * hidden input values in the generated HTML form.
     * <p/>
     * editProcessor: Name of the EditProcessor to process the request viewType: This should be set
     * to "edit". viewKey: Key of the view. EditProcessor implementation may have to act differently
     * according to the view key. resourcePath: Path of the resource to be updated redirectURL:
     * Request will be redirected to this URL after processing is complete. If this is not given
     * request will be redirected to resourcePath.
     *
     * @param editViewKey    UI key of the edit UI.
     * @param requestContext Details of the request.
     *
     * @return Resource filled with HTML UI as the content.
     * @throws RegistryException if an error occurs while getting the edit view.
     */
    public Resource getEditView(String editViewKey, RequestContext requestContext)
            throws RegistryException {
        return null;
    }

    /**
     * Implementations of this method should generate a HTML UI for creating a new resource content
     * and set it as the content of the returned resource.
     * <p/>
     * Generated UI should pass following parameters as HTTP request parameters to the custom
     * edit/new processing servlet. A common method to pass these parameters is to write them as
     * hidden input values in the generated HTML form.
     * <p/>
     * editProcessor: Name of the EditProcessor to process the request viewType: This should be set
     * to "new". viewKey: Key of the view. EditProcessor implementation may have to act differently
     * according to the view key. parentPath: Path of the parent collection of the new resource
     * resourceName: Name of the new resource redirectURL: Request will be redirected to this URL
     * after processing is complete. If this is not given request will be redirected to parentPath
     *
     * @param newViewKey     UI key of the new resource UI.
     * @param requestContext Details of the request.
     *
     * @return Resource filled with HTML UI as the content.
     * @throws RegistryException if an error occurs while getting the new view.
     */
    public Resource getNewView(String newViewKey, RequestContext requestContext)
            throws RegistryException {

        Resource resource = requestContext.getRegistry().newResource();
        resource.setContent("Resource creation UI is not provided.");
        return resource;
    }

    private Resource generateBrowseView(ResourcePath resourcePath, RequestContext requestContext)
            throws RegistryException {

        Resource resource;
        String viewName = resourcePath.getParameterValue(RegistryConstants.BROWSE_PROPERTY);
        if (viewName == null || "".equals(viewName) ||
                RegistryConstants.DEFAULT_VIEW_NAME.equals(viewName)) {
            viewName = getDefaultBrowseView();
        }

        if (RegistryConstants.RAW_VIEW_NAME.equals(viewName)) {
            resource = getRawResource(requestContext);

        } else {

            resource = getBrowseView(viewName, requestContext);

            if (resource.getProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY) == null) {

                if (RegistryConstants.TEXT_VIEW_NAME.equals(viewName)) {
                    resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                            RegistryConstants.R_VIEW_TEXT);
                } else if (RegistryConstants.XML_VIEW_NAME.equals(viewName)) {
                    resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                            RegistryConstants.R_VIEW_XML);
                } else {
                    resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                            RegistryConstants.R_GENERAL);
                }
            }
        }

        resource.setProperty(
                RegistryConstants.CONTENT_UI_MODE, RegistryConstants.BROWSE_MODE);

        return resource;
    }

    // Generates an edit view.
    private Resource generateEditView(ResourcePath resourcePath, RequestContext requestContext)
            throws RegistryException {

        Resource resource;
        String viewName = resourcePath.getParameterValue(RegistryConstants.EDIT_PROPERTY);
        if (viewName == null || "".equals(viewName) ||
                RegistryConstants.DEFAULT_VIEW_NAME.equals(viewName)) {
            viewName = getDefaultEditView();
        }

        if (RegistryConstants.RAW_VIEW_NAME.equals(viewName)) {
            resource = getRawResource(requestContext);

        } else {

            resource = getEditView(viewName, requestContext);

            if (resource.getProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY) == null) {

                if (RegistryConstants.TEXT_VIEW_NAME.equals(viewName)) {
                    resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                            RegistryConstants.R_EDIT_TEXT);
                } else if (RegistryConstants.XML_VIEW_NAME.equals(viewName)) {
                    resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                            RegistryConstants.R_EDIT_XML);
                } else {
                    resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                            RegistryConstants.R_GENERAL);
                }
            }
        }

        resource.setProperty(
                RegistryConstants.CONTENT_UI_MODE, RegistryConstants.EDIT_MODE);

        return resource;
    }

    // Generates a new view.
    private Resource generateNewView(ResourcePath resourcePath, RequestContext requestContext)
            throws RegistryException {

        Resource resource;
        String viewName = resourcePath.getParameterValue(RegistryConstants.NEW_PROPERTY);
        if (viewName == null || "".equals(viewName) ||
                RegistryConstants.DEFAULT_VIEW_NAME.equals(viewName)) {
            viewName = getDefaultNewView();
        }

        // there is no built-in default view name for new views. handlers should handle the default
        // new view as well.

        resource = getNewView(viewName, requestContext);

        replaceRegistryParameters(resource, resourcePath);

        if (resource.getProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY) == null) {

            if (RegistryConstants.TEXT_VIEW_NAME.equals(viewName)) {
                resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                        RegistryConstants.R_NEW_TEXT);
            } else if (RegistryConstants.XML_VIEW_NAME.equals(viewName)) {
                resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                        RegistryConstants.R_NEW_XML);
            } else {
                resource.setProperty(RegistryConstants.UI_RENDERING_METHOD_PROPERTY,
                        RegistryConstants.R_GENERAL);
            }
        }

        resource.setProperty(
                RegistryConstants.CONTENT_UI_MODE, RegistryConstants.NEW_MODE);

        requestContext.setProcessingComplete(true);

        return resource;
    }

    // Utility method to replace some parameters.
    private void replaceRegistryParameters(
            Resource resource, ResourcePath resourcePath)
            throws RegistryException {

        Object o = resource.getContent();
        if (o == null || !(o instanceof String)) {
            return;
        }

        String content = (String) o;

        String parentPath = resourcePath.getParameterValue("parentPath");
        content = content.replaceFirst(REG_PARENT_PATH,
                "<input type='hidden' name='parentPath' value='" + parentPath + "'/>");

        resource.setContent(content);
    }

    // Utility method to replace fill views.
    private void fillViews(Resource resource) {

        String[] browseViews = getBrowseViews();
        for (String browseView : browseViews) {
            resource.addProperty("registry.browse-views", browseView);
        }

        String[] editViews = getEditViews();
        for (String editView : editViews) {
            resource.addProperty("registry.edit-views", editView);
        }

        String[] newViews = getNewViews();
        for (String newView : newViews) {
            resource.addProperty("registry.new-views", newView);
        }
    }

    // Method to calculate the default view
    private String calculateDefaultView(String[] views) {

        if (views.length > 0) {
            String[] viewParts = views[0].split(":");
            return viewParts[0];
        }

        return RegistryConstants.RAW_VIEW_NAME;
    }
}
