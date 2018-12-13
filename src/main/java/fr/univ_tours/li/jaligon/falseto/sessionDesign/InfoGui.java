/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import fr.univ_tours.li.jaligon.falseto.Generics.Generics;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering.Cluster;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.SummaryGUI;

/**
 *
 * @author julien
 */
public class InfoGui extends JPanel {

    public static ArrayList<Member> measures;
    public static ArrayList<Member> selections;
    public static ArrayList<Level> groupBySets;
    private ArrayList<MemberItem> selectionForLevelInfo;
    private ArrayList<MemberItem> allSelectionsInfo;
    private ArrayList<MemberItem> measureInfo;

    /**
     * Creates new form InfoGui
     */
    public InfoGui() {
        initComponents();

        hidePictures();

        selectionActiveQueryScrollPane.setVisible(false);

        measures = new ArrayList<Member>();
        selections = new ArrayList<Member>();
        selectionForLevelInfo = new ArrayList<MemberItem>();
        allSelectionsInfo = new ArrayList<MemberItem>();
        measureInfo = new ArrayList<MemberItem>();
        groupBySets = new ArrayList<Level>();
        measureSetInfo();

        ImageIcon image = new ImageIcon("pictures/waiting.gif");
        waitingPicture.setIcon(image);
        waitingPicture.setVisible(false);

        selectionSetPanel.setVisible(false);
        measureSetPanel.setVisible(false);
        sessionPanel.setVisible(false);
        summaryPanel.setVisible(false);
        activeQueryPanel.setVisible(false);

        Component editor = selectionMembersInfo.getEditor().getEditorComponent();

        editor.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
            }

            public void keyPressed(KeyEvent ke) {
            }

            public void keyReleased(KeyEvent ke) {

                if (selectionMembersInfo.getEditor().getItem().getClass() != MemberItem.class) {
                    String s = (String) selectionMembersInfo.getEditor().getItem();

                    selectionMembersInfo.removeAllItems();

                    for (MemberItem mi : allSelectionsInfo) {
                        if (mi.getMember().getName().toUpperCase().startsWith(s.toUpperCase())) {
                            selectionMembersInfo.addItem(mi);
                        }
                    }

                    selectionMembersInfo.getEditor().setItem(s);

                    selectionMembersInfo.showPopup();

                    Object child = selectionMembersInfo.getAccessibleContext().getAccessibleChild(0);
                    BasicComboPopup popup = (BasicComboPopup) child;
                    JList list = popup.getList();
                    Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, list);
                    JScrollPane scrollPane = (JScrollPane) c;

                    int height = 0;
                    if (list.getSize().getHeight() > 8 * 20) {
                        height = 8 * 20;
                    } else {
                        height = (int) list.getSize().getHeight();
                    }

                    Dimension size = new Dimension(selectionMembersInfo.getWidth() - 5, height);
                    /*if (size.width > 20) {
                     size.width -= 5;
                     }*/

                    scrollPane.setPreferredSize(size);
                    scrollPane.setMaximumSize(size);

                    Dimension popupSize = popup.getSize();
                    popupSize.width = size.width;
                    popupSize.height = size.height + 5;
                    Component parent = popup.getParent();
                    parent.setSize(popupSize);

                    parent.validate();
                    parent.repaint();

                }
            }
        });

        /*JTextComponent tc = (JTextComponent) selectionMembersInfo.getEditor().getEditorComponent();
         tc.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent de) {
         //throw new UnsupportedOperationException("Not supported yet.");
         }

         public void removeUpdate(DocumentEvent de) {
         //throw new UnsupportedOperationException("Not supported yet.");
         }

         public void changedUpdate(DocumentEvent de) {
         System.out.println("UP");
         }
         });*/
    }

    public void summaryEnable() {
        summaryPanel.setVisible(true);
    }

    public void summaryDisable() {
        summaryPanel.setVisible(false);
    }

    public void measureSetEnable() {
        measureSetPanel.setVisible(true);
    }

    public void measureSetDisable() {
        measureSetPanel.setVisible(false);
    }

    public void selectionSetEnable() {
        selectionSetPanel.setVisible(true);
    }

    public void selectionSetDisable() {
        selectionSetPanel.setVisible(false);
    }

    public void measureSetInfo() {
        if (measureInfo.isEmpty()) {

            for (Level l : fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCube().getDimensions()[0].getHierarchy().getLevels()) {
                List<Member> members = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCube().getSchema().getSchemaReader().getLevelMembers(l, true);

                Collections.sort(members);
                for (Member m : members) {
                    MemberItem mi = new MemberItem(m);
                    measureInfo.add(mi);
                    measureSetInfo.addItem(mi);
                }
            }
        }

        measureSetPanel.setVisible(true);
        selectionSetPanel.setVisible(false);
    }

    public void selectionSetInfo(final Level l) {
        waitingPicture.setVisible(true);
        selectionLevelInfo.setText(l.getName() + "=");
        selectionMembersInfo.removeAllItems();
        selectionForLevelInfo.clear();
        allSelectionsInfo.clear();

        Thread t = new Thread() {
            @Override
            public void run() {

                List<Member> members = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCube().getSchema().getSchemaReader().withLocus().getLevelMembers(l, true);
                Collections.sort(members);

                for (Member m : members) {
                    MemberItem mi = new MemberItem(m);
                    selectionMembersInfo.addItem(mi);
                    allSelectionsInfo.add(mi);
                    if (selections.contains(m)) {
                        selectionForLevelInfo.add(mi);
                    }
                }
                selectionInfoText();
                waitingPicture.setVisible(false);
            }
        };
        t.start();

        measureSetPanel.setVisible(false);
        selectionSetPanel.setVisible(true);
        selectionMembersInfo.hidePopup();

    }

    public void groupBySetInfo() {
        hidePictures();
        groupBySets = getGroupBySet();

        /*String s = "";
         for (Level l : groupBySetList) {
         s += "Dimension: " + l.getDimension().getName() + ", Level: " + l.getName() + "\n";
         }

         //System.out.println(s);
         groupBySetInfo.setText(s);*/
    }

    private ArrayList<Level> getGroupBySet() {
        ArrayList<Level> result = new ArrayList<Level>();

        for (ArrayList<Level> list : DFMGui.links) {
            for (Level l : list) {
                if (!result.contains(l)) {
                    result.add(l);
                }
            }
        }

        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectionSetPanel = new JPanel();
        selectionLevelInfo = new javax.swing.JLabel();
        selectionMembersInfo = new javax.swing.JComboBox();
        addSelectionButton = new javax.swing.JButton();
        removeSelectionButton = new javax.swing.JButton();
        pictureSelection = new javax.swing.JLabel();
        jScrollPane1 = new JScrollPane();
        selectionList = new JList();
        measureSetPanel = new JPanel();
        measureSetInfo = new javax.swing.JComboBox();
        addMeasureButton = new javax.swing.JButton();
        removeMeasureButton = new javax.swing.JButton();
        jScrollPane3 = new JScrollPane();
        measureList = new JList();
        pictureMeasure = new javax.swing.JLabel();
        sessionPanel = new JPanel();
        validateSessionButton = new javax.swing.JButton();
        mdxButton = new javax.swing.JButton();
        waitingPicture = new javax.swing.JLabel();
        summaryPanel = new JPanel();
        summaryDrillDownButton = new javax.swing.JButton();
        previousSummaryButton = new javax.swing.JButton();
        resetSummaryButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        activeQueryPanel = new JPanel();
        selectionActiveQueryScrollPane = new JScrollPane();
        selectionActiveQueryTextArea = new javax.swing.JTextArea();

        selectionSetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selection Predicates"));

        selectionLevelInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        selectionLevelInfo.setText("jLabel1");

        selectionMembersInfo.setEditable(true);
        selectionMembersInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionMembersInfoActionPerformed(evt);
            }
        });

        addSelectionButton.setText("Add");
        addSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectionButtonActionPerformed(evt);
            }
        });

        removeSelectionButton.setText("Remove");
        removeSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectionButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(selectionList);

        org.jdesktop.layout.GroupLayout selectionSetPanelLayout = new org.jdesktop.layout.GroupLayout(selectionSetPanel);
        selectionSetPanel.setLayout(selectionSetPanelLayout);
        selectionSetPanelLayout.setHorizontalGroup(
            selectionSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectionSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectionSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(selectionSetPanelLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(selectionLevelInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(selectionSetPanelLayout.createSequentialGroup()
                        .add(selectionSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1)
                            .add(selectionSetPanelLayout.createSequentialGroup()
                                .add(selectionMembersInfo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addSelectionButton))
                            .add(selectionSetPanelLayout.createSequentialGroup()
                                .add(removeSelectionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 226, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(pictureSelection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        selectionSetPanelLayout.setVerticalGroup(
            selectionSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectionSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectionLevelInfo)
                .add(12, 12, 12)
                .add(selectionSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectionMembersInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addSelectionButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 123, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(selectionSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pictureSelection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeSelectionButton))
                .addContainerGap())
        );

        measureSetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Measure Set"));

        addMeasureButton.setText("Add");
        addMeasureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMeasureButtonActionPerformed(evt);
            }
        });

        removeMeasureButton.setText("Remove");
        removeMeasureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMeasureButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(measureList);

        org.jdesktop.layout.GroupLayout measureSetPanelLayout = new org.jdesktop.layout.GroupLayout(measureSetPanel);
        measureSetPanel.setLayout(measureSetPanelLayout);
        measureSetPanelLayout.setHorizontalGroup(
            measureSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(measureSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(measureSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3)
                    .add(measureSetPanelLayout.createSequentialGroup()
                        .add(removeMeasureButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 226, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pictureMeasure, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(measureSetPanelLayout.createSequentialGroup()
                        .add(measureSetInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addMeasureButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        measureSetPanelLayout.setVerticalGroup(
            measureSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(measureSetPanelLayout.createSequentialGroup()
                .add(measureSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(measureSetInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addMeasureButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(measureSetPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pictureMeasure, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .add(removeMeasureButton)))
        );

        sessionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Session"));

        validateSessionButton.setText("Validate");
        validateSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateSessionButtonActionPerformed(evt);
            }
        });

        mdxButton.setText("Get MDX");
        mdxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mdxButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout sessionPanelLayout = new org.jdesktop.layout.GroupLayout(sessionPanel);
        sessionPanel.setLayout(sessionPanelLayout);
        sessionPanelLayout.setHorizontalGroup(
            sessionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sessionPanelLayout.createSequentialGroup()
                .add(validateSessionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mdxButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        sessionPanelLayout.setVerticalGroup(
            sessionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sessionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sessionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(validateSessionButton)
                    .add(mdxButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        waitingPicture.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        waitingPicture.setText("loading...");

        summaryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Summary"));

        summaryDrillDownButton.setText("Split log");
        summaryDrillDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryDrillDownButtonActionPerformed(evt);
            }
        });

        previousSummaryButton.setText("Fusion logs");
        previousSummaryButton.setEnabled(false);
        previousSummaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousSummaryButtonActionPerformed(evt);
            }
        });

        resetSummaryButton.setText("Reset");
        resetSummaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetSummaryButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout summaryPanelLayout = new org.jdesktop.layout.GroupLayout(summaryPanel);
        summaryPanel.setLayout(summaryPanelLayout);
        summaryPanelLayout.setHorizontalGroup(
            summaryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSeparator2)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(resetSummaryButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, summaryPanelLayout.createSequentialGroup()
                        .add(previousSummaryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(summaryDrillDownButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)))
                .addContainerGap())
        );
        summaryPanelLayout.setVerticalGroup(
            summaryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryPanelLayout.createSequentialGroup()
                .add(summaryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previousSummaryButton)
                    .add(summaryDrillDownButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resetSummaryButton))
        );

        org.jdesktop.layout.GroupLayout activeQueryPanelLayout = new org.jdesktop.layout.GroupLayout(activeQueryPanel);
        activeQueryPanel.setLayout(activeQueryPanelLayout);
        activeQueryPanelLayout.setHorizontalGroup(
            activeQueryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 406, Short.MAX_VALUE)
        );
        activeQueryPanelLayout.setVerticalGroup(
            activeQueryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 361, Short.MAX_VALUE)
        );

        selectionActiveQueryTextArea.setEditable(false);
        selectionActiveQueryTextArea.setBackground(new java.awt.Color(204, 204, 204));
        selectionActiveQueryTextArea.setColumns(20);
        selectionActiveQueryTextArea.setLineWrap(true);
        selectionActiveQueryTextArea.setRows(5);
        selectionActiveQueryScrollPane.setViewportView(selectionActiveQueryTextArea);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sessionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(measureSetPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(selectionSetPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, waitingPicture, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .add(activeQueryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(selectionActiveQueryScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(sessionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(measureSetPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectionSetPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(waitingPicture, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 17, Short.MAX_VALUE)
                .add(activeQueryPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectionActiveQueryScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        activeQueryPanel.getAccessibleContext().setAccessibleName("");
        activeQueryPanel.getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents

    private void selectionMembersInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionMembersInfoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_selectionMembersInfoActionPerformed

    private void addMeasureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMeasureButtonActionPerformed
        // TODO add your handling code here:
        hidePictures();

        Member measure = ((MemberItem) measureSetInfo.getSelectedItem()).getMember();

        if (measures.contains(measure)) {
            pictureMeasure.setVisible(true);
            ImageIcon image = new ImageIcon("pictures/error.png");
            pictureMeasure.setIcon(image);
        } else {
            measures.add(measure);
            pictureMeasure.setVisible(true);
            ImageIcon image = new ImageIcon("pictures/success.png");
            pictureMeasure.setIcon(image);
        }

        measureInfoText();
    }//GEN-LAST:event_addMeasureButtonActionPerformed

    private void removeMeasureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMeasureButtonActionPerformed
        // TODO add your handling code here:
        hidePictures();

        if (measureList.getSelectedValue() != null) {
            Member measure = ((MemberItem) measureList.getSelectedValue()).getMember();

            if (measures.remove(measure)) {
                pictureMeasure.setVisible(true);
                ImageIcon image = new ImageIcon("pictures/success.png");
                pictureMeasure.setIcon(image);
            } else {
                pictureMeasure.setVisible(true);
                ImageIcon image = new ImageIcon("pictures/error.png");
                pictureMeasure.setIcon(image);
            }
            measureInfoText();
        }

    }//GEN-LAST:event_removeMeasureButtonActionPerformed

    private void addSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectionButtonActionPerformed
        // TODO add your handling code here:
        hidePictures();
        groupBySetInfo();

        boolean add = false;
        if (selectionMembersInfo.getSelectedItem().getClass() == MemberItem.class) {
            MemberItem item = (MemberItem) selectionMembersInfo.getSelectedItem();
            Member selection = item.getMember();
            if (!selections.contains(selection)) {
                add = true;
                for (Member m : selections) {
                    if (m.getLevel().getHierarchy()== selection.getLevel().getHierarchy() && m.getLevel() != selection.getLevel()) {
                        add = false;
                        break;
                    }
                }

                //System.out.println("GB : "+groupBySets.size());
                for (Level l : groupBySets) {
                    if (l.getHierarchy() == selection.getLevel().getHierarchy() && l.getDepth() < selection.getLevel().getDepth()) {
                        add = false;
                        break;
                    }
                }

                if (add) {
                    selections.add(selection);

                    if (!selectionForLevelInfo.contains(item)) {
                        selectionForLevelInfo.add(item);
                    }
                    pictureSelection.setVisible(true);
                    ImageIcon image = new ImageIcon("pictures/success.png");
                    pictureSelection.setIcon(image);
                    SessionBuilder.dfm.repaint();
                }
            }
        }

        if (!add) {
            pictureSelection.setVisible(true);
            ImageIcon image = new ImageIcon("pictures/error.png");
            pictureSelection.setIcon(image);
        }
        selectionInfoText();
    }//GEN-LAST:event_addSelectionButtonActionPerformed

    private void removeSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectionButtonActionPerformed
        // TODO add your handling code here:
        hidePictures();

        boolean remove = false;
        if (selectionList.getSelectedValue() != null) {
            if (selectionList.getSelectedValue().getClass() == MemberItem.class) {
                Member selection = ((MemberItem) selectionList.getSelectedValue()).getMember();

                if (selections.remove(selection)) {
                    remove = true;
                    pictureSelection.setVisible(true);
                    ImageIcon image = new ImageIcon("pictures/success.png");
                    pictureSelection.setIcon(image);
                    SessionBuilder.dfm.repaint();
                }
                selectionInfoText();
            }

            if (!remove) {
                pictureSelection.setVisible(true);
                ImageIcon image = new ImageIcon("pictures/error.png");
                pictureSelection.setIcon(image);
            }
        }
    }//GEN-LAST:event_removeSelectionButtonActionPerformed

    private void mdxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mdxButtonActionPerformed
        // TODO add your handling code here:
        MDXGui mdx = new MDXGui();
        mdx.setVisible(true);
    }//GEN-LAST:event_mdxButtonActionPerformed

    private void validateSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateSessionButtonActionPerformed

        ArrayList<Qfset> session = SessionBuilder.sessionsGui.getSession();

        if (session.size() < 3) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "The session size must be greater than 3 queries.");
        } else {
            SessionBuilder.timeEnd = System.currentTimeMillis();

            String[] queries = new String[session.size()];

            for (int i = 1; i <= queries.length; i++) {
                queries[i - 1] = "query " + i;
            }

            //String input = (String) JOptionPane.showInputDialog(SessionBuilder.frame, "Please select the query of your session that best meets the requirement", "Query", JOptionPane.INFORMATION_MESSAGE, null, queries, null);
            //if (input != null) {
            String idQuestion = JOptionPane.showInputDialog(SessionBuilder.frame, "Please indicate the requirement number");

            if (idQuestion != null) {
                try {
                    saveLogFile(idQuestion);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(InfoGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(InfoGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                SessionBuilder.sessionsGui.getSession().clear();
                SessionBuilder.sessionsGui.setSelectedQuery(null);
                clearQuery();
                SessionBuilder.js.setVisible(false);
                SessionBuilder.frame.pack();

                //SessionBuilder.configureSessionGui();
                SessionBuilder.timeBegin = System.currentTimeMillis();
            }
            //}
        }
    }//GEN-LAST:event_validateSessionButtonActionPerformed

    public void saveLogFile(String idQuestion) throws FileNotFoundException, IOException {
        Properties p = new Properties();
        //FileInputStream fs = new FileInputStream(Generics.CONNECTION_PROPERTIES);
        FileInputStream fs = new FileInputStream(Generics.CONNECTION_PROPERTIES);
        p.load(fs);

        String path = p.getProperty("testFilePath");

        PrintWriter sortie = null;

        try {
            File fi = new File(path);
            fi.mkdirs();

            //java.net.InetAddress.getLocalHost().getHostAddress()
            FileWriter f = new FileWriter(path + SessionBuilder.idQuestionnaire + "_" + SessionBuilder.random + "_" + SessionBuilder.id + ".txt", true);
            BufferedWriter buffer = new BufferedWriter(f);
            sortie = new PrintWriter(buffer);

            ArrayList<Qfset> session = SessionBuilder.sessionsGui.getSession();
            //System.out.println(session.size());
            sortie.println("Date: " + Generics.date());
            sortie.println("NbClickReco: " + SessionBuilder.nbClickReco);
            sortie.println("NbClickSummary: " + SessionBuilder.nbClickSummary);
            sortie.println("Time for designing the session: " + (SessionBuilder.timeEnd - SessionBuilder.timeBegin) + " ms");
            sortie.println("ID question: " + idQuestion);
            for (int i = 0; i < session.size(); i++) {
                sortie.println("#" + i + "\n" + session.get(i).toString());
            }

            sortie.println("#endSession");

        } catch (IOException e) {
            System.out.println(
                    "File Error");
        } finally {
            sortie.close();
        }

    }

    public void execute() {
        int confirm = JOptionPane.showConfirmDialog(SessionBuilder.frame, "Please note that the execution will definitively add the query into the session. Do you want to execute the query ?", "Confirm the execution", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            defaultGroupBy();
            Qfset q = new Qfset();
            for (Member measure : measures) {
                q.addMeasure(measure);
            }
            for (Member selection : selections) {
                q.addSelection(selection);
            }
            for (Level projection : groupBySets) {
                q.addProjection(projection);
            }

            SessionBuilder.frame.setVisible(false);
            new QueryGui(q);
        }
    }

    private void summaryDrillDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryDrillDownButtonActionPerformed
        // TODO add your handling code here:
        if (SummaryGUI.currentCluster != null) {
            List<Cluster> summaries = SummaryGUI.currentCluster.getChildren();

            if (!summaries.isEmpty()) {
                SummaryGUI.previousActions.add(SummaryGUI.currentChildren);
                SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI(SummaryGUI.currentCluster));
                previousSummaryButton.setEnabled(true);
                SummaryGUI.currentCluster = null;
            } else {
                JOptionPane.showMessageDialog(SessionBuilder.frame, "No summary available.");
            }
        } else {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No summary selected.");
        }
    }//GEN-LAST:event_summaryDrillDownButtonActionPerformed

    private void previousSummaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousSummaryButtonActionPerformed
        // TODO add your handling code here:
        if (!SummaryGUI.previousActions.isEmpty()) {
            List<Cluster> past = SummaryGUI.previousActions.get(SummaryGUI.previousActions.size() - 1);
            SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI(past));
            SummaryGUI.previousActions.remove(past);
            if (SummaryGUI.previousActions.isEmpty()) {
                previousSummaryButton.setEnabled(false);
            }
        } else {
            previousSummaryButton.setEnabled(false);
        }
    }//GEN-LAST:event_previousSummaryButtonActionPerformed

    private void resetSummaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetSummaryButtonActionPerformed
        // TODO add your handling code here:
        SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI());
    }//GEN-LAST:event_resetSummaryButtonActionPerformed

    public void clearQuery() {
        measures.clear();
        selections.clear();
        groupBySets.clear();
        DFMGui.links.clear();
        groupBySetInfo();
        selectionInfoText();
        measureInfoText();
        SessionBuilder.sessionsGui.setSelectedQuery(null);

        if (SessionBuilder.sessionsGui.getSession().isEmpty()) {

        } else {
            SessionBuilder.configureSessionGui();
        }
    }

    public void measureInfoText() {
        DefaultListModel model = new DefaultListModel();

        for (MemberItem mi : measureInfo) {
            if (measures.contains(mi.getMember())) {
                model.addElement(mi);
            }
        }

        measureList.setModel(model);

        SessionBuilder.dfm.repaint();
    }

    public void selectionInfoText() {
        DefaultListModel model = new DefaultListModel();

        for (MemberItem mi : selectionForLevelInfo) {
            if (selections.contains(mi.getMember())) {
                model.addElement(mi);
            }
        }

        selectionList.setModel(model);
    }

    private void hidePictures() {
        pictureMeasure.setVisible(false);
        pictureSelection.setVisible(false);
    }

    public void displaySessionPanel() {
        sessionPanel.setVisible(true);
    }

    public void hideSessionPanel() {
        sessionPanel.setVisible(false);
    }

    private void defaultGroupBy() {
        for (Hierarchy h : DFMGui.hierarchyList) {
            boolean add = true;
            for (Level pf : groupBySets) {
                if (pf.getHierarchy() == h) {
                    add = false;
                    break;
                }
            }
            if (add) {

                for (Member m : selections) {
                    if (m.getLevel().getHierarchy() == h) {
                        groupBySets.add(m.getLevel());
                        add = false;
                        break;
                    }
                }

                if (add) {
                    groupBySets.add(h.getAllMember().getLevel());
                }
            }
        }
    }

    public JPanel getActiveQueryPanel() {
        return activeQueryPanel;
    }

    public JPanel getSessionPanel() {
        return sessionPanel;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel activeQueryPanel;
    private javax.swing.JButton addMeasureButton;
    private javax.swing.JButton addSelectionButton;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton mdxButton;
    private JList measureList;
    private javax.swing.JComboBox measureSetInfo;
    private JPanel measureSetPanel;
    private javax.swing.JLabel pictureMeasure;
    private javax.swing.JLabel pictureSelection;
    public javax.swing.JButton previousSummaryButton;
    private javax.swing.JButton removeMeasureButton;
    private javax.swing.JButton removeSelectionButton;
    private javax.swing.JButton resetSummaryButton;
    public JScrollPane selectionActiveQueryScrollPane;
    public javax.swing.JTextArea selectionActiveQueryTextArea;
    private javax.swing.JLabel selectionLevelInfo;
    private JList selectionList;
    private javax.swing.JComboBox selectionMembersInfo;
    private JPanel selectionSetPanel;
    private JPanel sessionPanel;
    private javax.swing.JButton summaryDrillDownButton;
    private JPanel summaryPanel;
    private javax.swing.JButton validateSessionButton;
    private javax.swing.JLabel waitingPicture;
    // End of variables declaration//GEN-END:variables
}
