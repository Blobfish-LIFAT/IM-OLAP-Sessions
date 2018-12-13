/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import mondrian.olap.Level;
import mondrian.olap.MondrianException;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.RecommendationGUI;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.SummaryGUI;

/**
 *
 * @author julien
 */
public class QueryGui extends JFrame {

    private Qfset query;
    private List<Level> onColumns;
    private List<Level> onRows;
    private QueryResultGui queryResultGui;
    private QueryGui queryGui;
    private QueryResultInfoGui queryResultInfoGui;
    public JScrollPane js;
    private Thread t;

    public QueryGui(final Qfset query) {
        super("Query Result");
        this.query = query;
        onColumns = new ArrayList<Level>();
        onRows = new ArrayList<Level>();
        queryGui = this;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setPreferredSize(new Dimension(1280, 710));
        setLocation(100, 100);
        //setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        t = new Thread() {
            @Override
            public void run() {
                try {
                    JPanel waiting = new JPanel(new FlowLayout());
                    setPreferredSize(new Dimension(100, 710));

                    ImageIcon image = new ImageIcon("pictures/waiting.gif");
                    JLabel loading = new JLabel("", JLabel.CENTER);
                    loading.setIcon(image);
                    loading.setPreferredSize(new Dimension(100, 710));

                    JButton cancel = new JButton("Cancel");
                    cancel.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            t.stop();
                            SessionBuilder.frame.setVisible(true);
                            queryGui.setVisible(false);
                            System.gc();
                        }
                    });

                    waiting.add(loading);
                    waiting.add(cancel);

                    getContentPane().add(waiting, BorderLayout.CENTER);
                    //getContentPane().add(cancel);

                    queryResultInfoGui = new QueryResultInfoGui(queryGui);

                    queryResultGui = new QueryResultGui(queryGui);

                    queryGui.getContentPane().remove(waiting);

                    js = new JScrollPane(queryResultGui);
                    js.setOpaque(false); // we don't paint all our bits
                    js.getViewport().setBackground(Color.white);
                    js.setPreferredSize(new Dimension(704, 190));

                    queryGui.getContentPane().add(js, BorderLayout.CENTER);
                    queryResultInfoGui.setPreferredSize(new Dimension(320, 768));
                    queryGui.getContentPane().add(queryResultInfoGui, BorderLayout.EAST);

                    SessionBuilder.sessionsGui.setSelectedQuery(query);
                    SessionBuilder.sessionsGui.addQuery(query);
                    SessionBuilder.configureSessionGui();
                    SessionBuilder.sessionsGui.addExecutedQuery(query);
                    SessionBuilder.frame.pack();

                    SessionBuilder.info.setVisible(true);
                    SessionBuilder.info.getSessionPanel().setVisible(true);

                    try {
                        ASRA recommendation = new ASRA(SummaryGUI.initialLog.getSessions(), SessionBuilder.sessionsGui.getSession());
                        QuerySession reco = recommendation.computeASRA();
                        if (reco != null) {
                            SessionBuilder.tabbedPane.setComponentAt(2, new RecommendationGUI(reco));
                            SessionBuilder.tabbedPane.setEnabledAt(2, true);
                        } else {
                            SessionBuilder.tabbedPane.setEnabledAt(2, false);
                        }
                    } catch (Error e) {
                        System.out.println("Impossible Recommendation");
                    } catch (Exception ex) {
                        Logger.getLogger(QueryGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }

                    addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                            t.stop();
                            SessionBuilder.frame.setVisible(true);
                            queryGui.setVisible(false);
                            System.gc();
                        }

                    });

                } catch (SQLException ex) {
                    Logger.getLogger(QueryGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(QueryGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (MondrianException ex) {
                    ex.printStackTrace();
                    System.out.println("MDX query interrupted");
                    SessionBuilder.frame.setVisible(true);
                    queryGui.setVisible(false);
                    System.gc();
                }
            }
        };

        t.start();

        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Qfset getQuery() {
        return query;
    }

    public void addOnColumns(Level l) {
        this.onColumns.add(l);
    }

    public void addOnRows(Level l) {
        this.onRows.add(l);
    }

    public void removeAllOnColumns() {
        this.onColumns.clear();
    }

    public void removeOnColumns(Level l) {
        this.onColumns.remove(l);
    }

    public void removeAllOnRows() {
        this.onRows.clear();
    }

    public void removeOnRows(Level l) {
        this.onRows.remove(l);
    }

    public List<Level> getOnColumns() {
        return onColumns;
    }

    public List<Level> getOnRows() {
        return onRows;
    }

    public QueryResultGui getQueryResultGui() {
        return queryResultGui;
    }

    public QueryResultInfoGui getQueryResultInfoGui() {
        return queryResultInfoGui;
    }
}
