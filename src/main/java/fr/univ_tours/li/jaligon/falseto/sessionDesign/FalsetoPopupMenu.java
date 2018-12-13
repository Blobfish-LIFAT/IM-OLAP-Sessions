/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import static fr.univ_tours.li.jaligon.falseto.sessionDesign.InfoGui.groupBySets;
import static fr.univ_tours.li.jaligon.falseto.sessionDesign.InfoGui.measures;
import static fr.univ_tours.li.jaligon.falseto.sessionDesign.InfoGui.selections;
import static fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionBuilder.tabbedPane;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.SummaryGUI;

/**
 *
 * @author julien
 */
public class FalsetoPopupMenu extends JPopupMenu {

    public FalsetoPopupMenu() {

        JMenuItem queryBased;
        JMenuItem queryBasedSim;
        JMenuItem sessionBased;
        JMenuItem sessionBasedSim;
        JMenuItem queryViewer;

        queryBased = new JMenuItem("Query-based Search (spec)");
        queryBased.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                queryBasedSearch();
            }
        });
        queryBasedSim = new JMenuItem("Query-based Search (sim)");
        queryBasedSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                queryBasedSearchSim();
            }
        });
        sessionBased = new JMenuItem("Session-based Search (spec)");
        sessionBased.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sessionBasedSearch();
            }
        });
        sessionBasedSim = new JMenuItem("Session-based Search (sim)");
        sessionBasedSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sessionBasedSearchSim();
            }
        });
        queryViewer = new JMenuItem("Query Designer");
        queryViewer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tabbedPane.setEnabledAt(0, true);
                queryViewer();
            }
        });

        this.add(queryBased);
        this.add(queryBasedSim);
        this.add(sessionBased);
        this.add(sessionBasedSim);
        this.addSeparator();
        this.add(queryViewer);
    }

    public FalsetoPopupMenu(String type) {
        if (type.equals("queryDesign")) {
            JMenuItem queryBased;
            JMenuItem queryBasedSim;
            JMenuItem execute;
            JMenuItem clear;

            queryBased = new JMenuItem("Query-based Search (spec)");
            queryBased.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
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

                    SessionBuilder.sessionsGui.setSelectedQuery(q);

                    queryBasedSearch();
                }
            });
            queryBasedSim = new JMenuItem("Query-based Search (sim)");
            queryBasedSim.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
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

                    SessionBuilder.sessionsGui.setSelectedQuery(q);
   
                    queryBasedSearchSim();
                }
            });
            execute = new JMenuItem("Execute");
            execute.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    execute();
                }
            });
            clear = new JMenuItem("Clear");
            clear.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    clear();
                }
            });

            this.add(queryBased);
            this.add(queryBasedSim);
            this.addSeparator();
            this.add(clear);
            this.addSeparator();
            this.add(execute);
        }
    }

    private void queryBasedSearch() {
        SessionBuilder.info.previousSummaryButton.setEnabled(false);
        Qfset filter = SessionBuilder.sessionsGui.getSelectedQuery();

        if (filter == null) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No query selected from the session.");
        } else {
            SessionBuilder.info.summaryEnable();
            SessionBuilder.info.measureSetDisable();
            SessionBuilder.info.selectionSetDisable();
            SessionBuilder.tabbedPane.setSelectedIndex(1);
            SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI(filter, -1));
        }
    }

    private void queryBasedSearchSim() {
        SessionBuilder.info.previousSummaryButton.setEnabled(false);
        Qfset filter = SessionBuilder.sessionsGui.getSelectedQuery();

        if (filter == null) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No query selected from the session.");
        } else {
            SessionBuilder.info.summaryEnable();
            SessionBuilder.info.measureSetDisable();
            SessionBuilder.info.selectionSetDisable();
            SessionBuilder.tabbedPane.setSelectedIndex(1);
            SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI(filter, 0.7));
        }
    }

    private void sessionBasedSearch() {
        SessionBuilder.info.previousSummaryButton.setEnabled(false);
        List<Qfset> filter = SessionBuilder.sessionsGui.getSelectedSession();

        if (filter == null) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No session selected.");
        } else {
            SessionBuilder.info.summaryEnable();
            SessionBuilder.info.measureSetDisable();
            SessionBuilder.info.selectionSetDisable();
            SessionBuilder.tabbedPane.setSelectedIndex(1);
            SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI(filter, -1));
        }
    }

    private void sessionBasedSearchSim() {
        SessionBuilder.info.previousSummaryButton.setEnabled(false);
        List<Qfset> filter = SessionBuilder.sessionsGui.getSelectedSession();

        if (filter == null) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No session selected.");
        } else {
            SessionBuilder.info.summaryEnable();
            SessionBuilder.info.measureSetDisable();
            SessionBuilder.info.selectionSetDisable();
            SessionBuilder.tabbedPane.setSelectedIndex(1);
            SessionBuilder.tabbedPane.setComponentAt(1, new SummaryGUI(filter, 0.7));
        }
    }

    private void queryViewer() {
        Qfset selectedQuery = SessionBuilder.sessionsGui.getSelectedQuery();

        InfoGui.measures.clear();

        for (MeasureFragment mf : selectedQuery.getMeasures()) {
            InfoGui.measures.add(mf.getAttribute());
        }

        ArrayList<Level> levels = new ArrayList<Level>();
        DFMGui.links.clear();
        InfoGui.groupBySets.clear();

        ArrayList<ProjectionFragment> projections = new ArrayList<ProjectionFragment>();

        for (Hierarchy h : DFMGui.hierarchyList) {
            for (ProjectionFragment pf : selectedQuery.getAttributes()) {
                if (h == pf.getHierarchy()) {
                    projections.add(pf);
                    break;
                }
            }
        }

        for (ProjectionFragment pf : projections) {
            InfoGui.groupBySets.add(pf.getLevel());

            levels.add(pf.getLevel());

            if (levels.size() == 2) {
                DFMGui.links.add(levels);
                levels = new ArrayList<Level>();
                levels.add(pf.getLevel());
            }
        }

        InfoGui.selections.clear();
        for (SelectionFragment sf : selectedQuery.getSelectionPredicates()) {
            InfoGui.selections.add(sf.getValue());
        }

        SessionBuilder.dfm.repaint();
        SessionBuilder.info.groupBySetInfo();
        SessionBuilder.info.selectionInfoText();
        SessionBuilder.info.measureInfoText();
        SessionBuilder.info.repaint();

        SessionBuilder.tabbedPane.setSelectedIndex(0);
        SessionBuilder.info.summaryDisable();

    }

    private void execute() {
        SessionBuilder.info.execute();
    }

    private void clear() {
        SessionBuilder.info.clearQuery();
    }

}
