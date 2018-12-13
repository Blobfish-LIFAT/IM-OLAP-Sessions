/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Fragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Log;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering.Cluster;
import fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering.HierarchicalClustering;
import fr.univ_tours.li.jaligon.falseto.Summary.Intention.Clustering.SummarizationHierarchicalTree;
import fr.univ_tours.li.jaligon.falseto.logParsing.gpsj.StatisticalStudentSessionLogParsing;
import fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionBuilder;
import fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionGui;

/**
 *
 * @author julien
 */
public class SummaryGUI extends JPanel {

    //private HashMap<Log, SummarizationHierarchicalTree> trees = new HashMap<>();
    public static Log initialLog;
    private static Log currentLog;
    public static Cluster currentCluster;
    public static List<List<Cluster>> previousActions = new ArrayList<List<Cluster>>();
    public static List<Cluster> currentChildren;
    private List<JScrollPane> summaryPanes;
    private JPanel sessionPanel = new JPanel();
    private JScrollPane jsSummary;

    /**
     * Creates new form SummaryGU
     */
    public SummaryGUI() {
        super();

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);

        this.setBorder(BorderFactory.createTitledBorder("Log Browsing"));

        if (initialLog == null) {
            Log log = getLogs();
            
            initialLog = log;
            previousActions = new ArrayList<>();
        }

        if (initialLog != null) {
            currentLog = initialLog;

            Cluster root = hierarchicalClustering();

            ArrayList<Cluster> clusters = getPrunedRoots(root);

            displayingSessions(clusters);
        }
    }

    public SummaryGUI(Cluster currentCluster) {
        super();

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);

        this.setBorder(BorderFactory.createTitledBorder("Log Browsing"));

        ArrayList<Cluster> clusters = getPrunedRoots(currentCluster);
        displayingSessions(clusters);
    }

    public SummaryGUI(List<Cluster> clusters) {
        super();

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);

        this.setBorder(BorderFactory.createTitledBorder("Log Browsing"));

        displayingSessions(clusters);
    }

    public SummaryGUI(Qfset queryFiltering, double threshold) {
        super();

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);

        this.setBorder(BorderFactory.createTitledBorder("Log Browsing"));

        if (threshold == -1) {
            currentLog = new Log(currentLog.select(queryFiltering));
        } else {
            currentLog = new Log(currentLog.select(queryFiltering, threshold));
        }

        if (currentLog.getSessions().isEmpty()) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No session found.");
            currentLog = initialLog;
        }

        Cluster root = hierarchicalClustering();
        ArrayList<Cluster> cluster;

        if (!root.getChildren().isEmpty()) {
            cluster = getPrunedRoots(root);
        } else {
            cluster = new ArrayList<>();
            cluster.add(root);
        }
        displayingSessions(cluster);

        previousActions = new ArrayList<List<Cluster>>();
    }

    //if threshold == -1 then the log is filtered using coverage relation, similarity-based otherwise
    public SummaryGUI(List<Qfset> sessionFiltering, double threshold) {
        super();

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);

        this.setBorder(BorderFactory.createTitledBorder("Log Browsing"));

        if (threshold == -1) {
            currentLog = new Log(currentLog.select(sessionFiltering));

        } else {
            currentLog = new Log(currentLog.select(new QuerySession(sessionFiltering, "Current Session"), threshold));
        }

        if (currentLog.getSessions().isEmpty()) {
            JOptionPane.showMessageDialog(SessionBuilder.frame, "No session found.");
            currentLog = initialLog;
        }

        Cluster root = hierarchicalClustering();
        ArrayList<Cluster> cluster;

        if (!root.getChildren().isEmpty()) {
            cluster = getPrunedRoots(root);
        } else {
            cluster = new ArrayList<>();
            cluster.add(root);
        }
        displayingSessions(cluster);

        previousActions = new ArrayList<List<Cluster>>();
    }

    private ArrayList<Cluster> getPrunedRoots(Cluster root) {
        ArrayList<Cluster> clusterRoots = new ArrayList<>();

        prune(root, clusterRoots);

        return clusterRoots;
    }

    private void prune(Cluster root, ArrayList<Cluster> clusterRoots) {

        clusterRoots.addAll(root.getChildren());

        while (clusterRoots.size() < 6) {
            Cluster cMin = null;
            int nbMin = Integer.MAX_VALUE;
            for (Cluster c : clusterRoots) {
                if (c.getLog().getSessions().size() < nbMin && c.getSummaries().get(0).isTop()) {
                    nbMin = c.getLog().getSessions().size();
                    cMin = c;
                }
            }

            if (cMin == null) {
                break;
            }

            if (cMin.getChildren() != null) {
                clusterRoots.remove(cMin);
                clusterRoots.addAll(cMin.getChildren());
            }
        }

    }

    private Cluster hierarchicalClustering() {

        if (currentLog.getSessions().size() != 1) {
            HierarchicalClustering hc = null;

            try {
                
                hc = new HierarchicalClustering(currentLog.getSessions());
            } catch (Exception ex) {
                Logger.getLogger(SummaryGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            final SummarizationHierarchicalTree a = new SummarizationHierarchicalTree(hc.getGraph(), currentLog);
            
            a.defineCluster();
           
            try {
                a.defineSummaries();
            } catch (IOException ex) {
                Logger.getLogger(SummaryGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            return a.getRoot();
        } else {
            Cluster c = new Cluster();
            c.setLog(currentLog);
            c.getSummaries().add(currentLog.getSessions().iterator().next());//only one session is present in currentLog, so it is also the summary

            return c;
        }

    }

    private void displayingSessions(List<Cluster> summaries) {
        JPanel p = new JPanel(new WrapLayout());
        p.setOpaque(false);
        p.setVisible(true);
        summaryPanes = new ArrayList<>();

        ArrayList<Cluster> past = new ArrayList<>();
        for (Cluster c : summaries) {
            final JScrollPane scrollPane = initUI(c);
            summaryPanes.add(scrollPane);
            p.add(scrollPane);

            past.add(c);
        }

        this.add(p, BorderLayout.NORTH);

        jsSummary = new JScrollPane(sessionPanel);
        jsSummary.setOpaque(false); // we don't paint all our bits
        jsSummary.getViewport().setBackground(Color.white);
        jsSummary.setPreferredSize(new Dimension(704, 200));
        jsSummary.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jsSummary.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsSummary.setVisible(false);

        this.add(jsSummary, BorderLayout.SOUTH);

        currentChildren = past;
    }

    protected JScrollPane initUI(final Cluster c) {
        //System.out.println(c.getLog().getSessions().size());

        HashMap<Fragment, Integer> nameFrequences = tagCloud(c);

        final JPanel panel = new JPanel(new WrapLayout());
        panel.setOpaque(false);
        //panel.setSize(50, 50);
        //Cloud cloud = new Cloud();
        double maxWeight = 0;

        for (Fragment f : nameFrequences.keySet()) {
            double weight = nameFrequences.get(f);
            maxWeight = Math.max(weight, maxWeight);

            String name = "";
            JLabel label = new JLabel();

            if (f.getType() == 0) {
                name = ((ProjectionFragment) f).getLevel().getName();
                label.setForeground(Color.orange);
            } else if (f.getType() == 1) {
                name = ((SelectionFragment) f).getValue().getName();
                label.setForeground(Color.green);
            } else if (f.getType() == 2) {
                name = ((MeasureFragment) f).getAttribute().getName();
            }

            label.setText(name);
            label.setOpaque(false);
            float sizeFont = (float) ((float) (weight / maxWeight) * 7.0 + 7.0);
            label.setFont(label.getFont().deriveFont(sizeFont));
            panel.add(label);

        }
        /*for (Tag tag : cloud.tags()) {
         final JLabel label = new JLabel(tag.getName());
         label.setOpaque(false);
         float sizeFont = (float) ((float) (tag.getWeight() / maxWeight) * 7.0 + 7.0);
         label.setFont(label.getFont().deriveFont(sizeFont));
         panel.add(label);
            

         }*/
        panel.setBorder(BorderFactory.createTitledBorder("" + c.getLog().getSessions().size() + " sessions"));
        panel.revalidate();
        panel.repaint();

        final JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setBackground(Color.white);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionBuilder.nbClickSummary++;
                System.out.println(SessionBuilder.nbClickSummary);
                for (JScrollPane jsp : summaryPanes) {
                    jsp.setBorder(BorderFactory.createLineBorder(Color.gray));
                }

                scrollPane.setBorder(BorderFactory.createLineBorder(Color.blue));
                currentCluster = c;

                getSummary();
                jsSummary.setVisible(true);
                getSession();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        return scrollPane;
    }

    protected HashMap<Fragment, Integer> tagCloud(Cluster c) {
        QuerySession s = c.getSummaries().get(0);

        HashMap<Fragment, Integer> frequences = new HashMap<>();

        for (Qfset q : s.getQueries()) {
            HashSet<Fragment> fragments = new HashSet<>();

            for (ProjectionFragment pf : q.getAttributes()) {
                if (!pf.getLevel().isAll()) {
                    fragments.add(pf);
                }
            }

            fragments.addAll(q.getMeasures());
            fragments.addAll(q.getSelectionPredicates());

            for (Fragment f : fragments) {
                if (frequences.containsKey(f)) {
                    frequences.put(f, frequences.get(f) + 1);
                } else {
                    frequences.put(f, 1);
                }
            }

        }

        return frequences;
    }

    private Log getLogs() {
        HashSet<QuerySession> result = new HashSet<>();

        File folder = new File("logs/");
        File[] folders = folder.listFiles();

        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                if (folders[i].isDirectory()) {
                    File file = null;
                    try {
                        file = new File(folders[i].getCanonicalPath());
                        System.out.println("Folder " + folders[i].getCanonicalPath());
                    } catch (IOException ex) {
                        Logger.getLogger(SummaryGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (int j = 0; j < files.length; j++) {
                            if (!files[j].isDirectory() && files[j].getName().endsWith(".txt")) {
                                
                                StatisticalStudentSessionLogParsing lp = null;
                                try {
                                    lp = new StatisticalStudentSessionLogParsing(files[j].getCanonicalPath());
                                    System.out.println("File " + files[j].getCanonicalPath());
                                } catch (IOException ex) {
                                    Logger.getLogger(SummaryGUI.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                Log log = lp.ReadSessionLog();
                                result.addAll(log.getSessions());  
                            }
                        }
                    }
                }
            }
        }

        if (!result.isEmpty()) {
            Log l = new Log(result);
            return l;
        } else {
            return null;
        }
    }

    private void getSummary() {
        QuerySession summary = currentCluster.getSummaries().get(0);

        SessionGui sg = new SessionGui("Summary");
        sg.setSession((ArrayList<Qfset>) summary.getQueries());

        sessionPanel.setLayout(new BorderLayout());
        sessionPanel.setOpaque(false);
        sessionPanel.removeAll();
        sessionPanel.add(sg, BorderLayout.NORTH);

    }

    private void getSession() {
        JPanel logPanel = new JPanel(new GridLayout(currentCluster.getLog().getSessions().size(), 1));

        for (QuerySession qs : currentCluster.getLog().getSessions()) {
            SessionGui sg = new SessionGui("Log Session");
            sg.setSession((ArrayList<Qfset>) qs.getQueries());
            logPanel.add(sg);
        }

        logPanel.setOpaque(false);
        sessionPanel.add(logPanel, BorderLayout.CENTER);
    }

    /*@Override
     protected void paintComponent(final Graphics g) {
     super.paintComponents(g);
     System.out.println(sessionPanel.getWidth()+" "+jsSummary.getWidth());
     if (sessionPanel.getWidth() != 0 && jsSummary.getWidth() != 0) {
     if (sessionPanel.getWidth() >= jsSummary.getWidth()) {
     jsSummary.getViewport().setViewSize(new Dimension(sessionPanel.getWidth(), 400));
     }
     }
     }*/
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
