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

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class ProgressBarPanel  extends JPanel {
    private  volatile boolean stop = false;
    private static int DELAY = 500;
    public volatile String val=null;
    private JLabel progressDescription;
    private JProgressBar progressSent;

    public ProgressBarPanel (){
        init();
    }
    public  void requestStop() {
        stop = true;
    }
    private void init(){
        setVisible(false);
        progressDescription =new JLabel();
        progressDescription.setText("");
        progressSent =new JProgressBar();
        progressSent.setStringPainted(true);
        this.setLayout(new GridBagLayout());

        this.add(progressDescription
                , new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1,  1.0, 1.0
                , GridBagConstraints.WEST  , GridBagConstraints.HORIZONTAL
                , new Insets(10, 10, 0, 10), 0, 0));

        this.add(progressSent
                , new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0
                , GridBagConstraints.WEST , GridBagConstraints.HORIZONTAL
                , new Insets(10, 10, 0,10), 0, 0));

    }
    public void setProgressText(String s) {
        progressDescription.setText(s);
    }

    public void setProgressValue(int i) {
        progressSent.setValue(i);
    }
    public void aboutToDisplayPanel() {

        setProgressValue(0);
       // setProgressText("Connecting to Server...");

    }
    public void displayingPanel() {

        Thread t = new Thread() {

            public void run() {

                int minimum = progressSent.getMinimum();
                int maximum =progressSent.getMaximum();
                Runnable runner = new Runnable() {
                    public void run() {
                        if(stop && progressSent .getValue()<75){

                            progressSent .setIndeterminate(false);
                            int value = progressSent .getValue();
                            progressSent .setValue(value+4);
                            setProgressValue(value+4);
                          //  progressDescription .setText("Genarate Code. Please wait.....");
                        } else if(!stop){
                            progressSent .setIndeterminate(true);

                        }
                    }
                };
                for (int i=minimum; i<maximum; i++) {
                    try {
                        SwingUtilities.invokeAndWait(runner);
                        // Our task for each step is to just sleep
                        Thread.sleep(DELAY);
                    } catch (InterruptedException ignoredException) {
                    } catch (InvocationTargetException ignoredException) {
                    }
                }
            }
        };
        t.start();
    }
}
