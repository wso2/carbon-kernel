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

import org.apache.tiles.factory.AbstractTilesContainerFactory;
import org.apache.tiles.factory.BasicTilesContainerFactory;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.servlet.ServletApplicationContext;
import org.apache.tiles.startup.AbstractTilesInitializer;

import javax.servlet.ServletContext;

public class CustomTilesInitializer extends AbstractTilesInitializer {

    @Override
    protected AbstractTilesContainerFactory createContainerFactory(ApplicationContext applicationContext) {

        return new BasicTilesContainerFactory();
    }

    @Override
    protected ApplicationContext createTilesApplicationContext(ApplicationContext context) {

        return new ServletApplicationContext((ServletContext) context.getContext());
    }
}
