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

package sample.spellcheck.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * class sample.google.spellcheck.SuggestionForm
 * This is the implementation of the GUI
 */
public class SuggestionForm extends javax.swing.JFrame implements HyperlinkListener {
    private AsyncPanel asyncPanel;
    private SyncPanel syncPanel;
    private JEditorPane helpDisplayPane;

    private JMenuItem syncMenuItem;
    private JMenuItem asyncMenuItem;
    private static final String HELP_FILE_NAME = "/docs/GoogleSpellCheck.html";


    public SuggestionForm() throws HeadlessException {
        asyncPanel = new AsyncPanel();
        syncPanel = new SyncPanel();

        JMenuBar menuBar;
        //Create the menu bar.
        menuBar = new JMenuBar();

        JMenu modeMenu = new JMenu("Mode");
        modeMenu.setMnemonic(KeyEvent.VK_M);
        syncMenuItem = new JMenuItem("Sync Mode", KeyEvent.VK_S);
        syncMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        syncMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSyncPanel();
            }
        });
        asyncMenuItem = new JMenuItem("ASync Mode", KeyEvent.VK_A);
        asyncMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        asyncMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAsyncPanel();
            }
        });
        modeMenu.add(syncMenuItem);
        modeMenu.add(asyncMenuItem);

        JMenu clearMenu = new JMenu("Clear");
        clearMenu.setMnemonic(KeyEvent.VK_C);
        JMenuItem clearMenuItem = new JMenuItem("Clear text boxes");
        clearMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        clearMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                asyncPanel.clear();
                syncPanel.clear();
            }
        });
        clearMenu.add(clearMenuItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem mnuItemHelp = new JMenuItem("Show Help");
        helpMenu.add(mnuItemHelp);

        mnuItemHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });

        menuBar.add(modeMenu);
        menuBar.add(clearMenu);
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);

        this.getContentPane().setLayout(new GridLayout(1, 1));
        setAsyncPanel();


    }

    public static void main(String[] args) {
        SuggestionForm form = new SuggestionForm();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        form.setLocation(screenSize.width / 4,
                screenSize.height / 4);
        form.setSize(screenSize.width / 2, screenSize.height / 2);
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //form.setResizable(false);
        //form.pack();
        form.setVisible(true);
    }

    private void setAsyncPanel() {
        this.getContentPane().removeAll();
        this.getContentPane().add(asyncPanel);
        this.syncMenuItem.setEnabled(true);
        this.asyncMenuItem.setEnabled(false);
        this.getContentPane().repaint();
        this.setTitle("Google Spell checker - Async Mode");
        this.setVisible(true);

    }

    private void setSyncPanel() {
        this.getContentPane().removeAll();
        this.getContentPane().add(syncPanel);
        this.syncMenuItem.setEnabled(false);
        this.asyncMenuItem.setEnabled(true);
        this.getContentPane().repaint();
        this.setTitle("Google Spell checker - Sync Mode");
        this.setVisible(true);
    }

    /**
     * method showHelp
     */
    private void showHelp() {

        JFrame frame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width / 5,
                screenSize.height / 5);
        frame.setSize(screenSize.width / 2, screenSize.height / 2);

        BorderLayout layout = new BorderLayout();

        JScrollPane jsp;


        helpDisplayPane = new JEditorPane();
        helpDisplayPane.addHyperlinkListener(this);
        helpDisplayPane.setEditable(false);
        helpDisplayPane.setContentType("text/html");

        jsp = new JScrollPane(helpDisplayPane);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(layout);
        contentPane.add(jsp, BorderLayout.CENTER);
        String helpDoc = System.getProperty("user.dir") + HELP_FILE_NAME;

        try {
            helpDisplayPane.setPage(new File(helpDoc).toURL());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Help file not detected", "Help file error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        frame.setVisible(true);
    }


    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                String url = e.getURL().toString();
                helpDisplayPane.setPage(url);
            } catch (Exception err) {
                JOptionPane.showMessageDialog(this, "Help file not detected", err.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }
}
