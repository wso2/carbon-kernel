/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.tools.idea;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import javax.swing.*;

public class ServiceArchiveCreator extends AnAction {
    private ImageIcon myIcon;

    public ServiceArchiveCreator() {
        super("GC", "Create Service Archive File", null);
    }

    public void actionPerformed(AnActionEvent event) {
//        Java2CodeFrame win = new Java2CodeFrame();
//        win.setProject((Project) event.getDataContext().getData(DataConstants.PROJECT));
//        win.pack();
//        win.show();
    }

    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace())) {
            if (myIcon == null) {
                java.net.URL resource = ServiceArchiveCreator.class.getResource("/icons/garbage.png");
                myIcon = new ImageIcon(resource);
            }
            presentation.setIcon(myIcon);
        }
    }
}
