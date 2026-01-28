/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ui;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.tiles.factory.BasicTilesContainerFactory;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.servlet.ServletApplicationContext;
import org.apache.tiles.startup.TilesInitializer;
import org.apache.tiles.startup.AbstractTilesInitializer;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.request.servlet.ServletUtil;
import org.apache.tiles.web.startup.AbstractTilesListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
        service = ServletContextListener.class,
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=(" +
                        HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=carbonContext)"
        }
)
public class CustomTilesInitializerListener extends AbstractTilesListener {

    @Override
    protected TilesInitializer createTilesInitializer() {

        return new CustomTilesInitializer();
    }
}

