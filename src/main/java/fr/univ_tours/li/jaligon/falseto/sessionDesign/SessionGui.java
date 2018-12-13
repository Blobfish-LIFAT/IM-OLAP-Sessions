/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.MeasureFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import static fr.univ_tours.li.jaligon.falseto.sessionDesign.DFMGui.hierarchyList;
import static fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionBuilder.info;


/**
 *
 * @author julien
 */
public class SessionGui extends JPanel {

    /**
     * Creates new form SessionGui
     */
    private HashMap<Point, Qfset> queryMap;
    private ArrayList<Qfset> session = new ArrayList<Qfset>();
    private static Qfset selectedQuery;
    private static ArrayList<Qfset> selectedSession;
    private ArrayList<Qfset> executedQueries = new ArrayList<Qfset>();

    private int distanceLevel = 15;
    private int radiusLevel = 12;
    private int heightTitleFact = 0;
    private int heightFact = 40;
    private int widthFact = 35;
    private int fontSizeLevel = 7;
    private int fontSizeTitleFact = 0;
    private int fontSizeMeasureFact = 6;
    private int substringLevelSize = 3;
    private int substringMeasureSize = 3;
    private boolean isSelectedQueryDisplayable = true;

    private int sideSize = 150;

    public void setDistanceLevel(int distanceLevel) {
        this.distanceLevel = distanceLevel;
    }

    public void setRadiusLevel(int radiusLevel) {
        this.radiusLevel = radiusLevel;
    }

    public void setHeightTitleFact(int heightTitleFact) {
        this.heightTitleFact = heightTitleFact;
    }

    public void setHeightFact(int heightFact) {
        this.heightFact = heightFact;
    }

    public void setWidthFact(int widthFact) {
        this.widthFact = widthFact;
    }

    public void setFontSizeLevel(int fontSizeLevel) {
        this.fontSizeLevel = fontSizeLevel;
    }

    public void setFontSizeTitleFact(int fontSizeTitleFact) {
        this.fontSizeTitleFact = fontSizeTitleFact;
    }

    public void setFontSizeMeasureFact(int fontSizeMeasureFact) {
        this.fontSizeMeasureFact = fontSizeMeasureFact;
    }

    public void setSubstringLevelSize(int substringLevelSize) {
        this.substringLevelSize = substringLevelSize;
    }

    public void setSubstringMeasureSize(int substringMeasureSize) {
        this.substringMeasureSize = substringMeasureSize;
    }

    public void setSideSize(int sideSize) {
        this.sideSize = sideSize;
    }

    public ArrayList<Qfset> getSelectedSession() {
        return selectedSession;
    }

    public void setSession(ArrayList<Qfset> session) {
        this.session = session;
    }

    public ArrayList<Qfset> getExecutedQueries() {
        return executedQueries;
    }

    public void addExecutedQuery(Qfset executedQuery) {
        executedQueries.add(executedQuery);
    }

    public ArrayList<Qfset> getSession() {
        return session;
    }

    public void addQuery(Qfset q) {
        this.session.add(q);
    }

    public void setSelectedQuery(Qfset selectedQuery) {
        this.selectedQuery = selectedQuery;
    }

    public void isSelectedQueryDisplayable(boolean isdisplayable) {
        this.isSelectedQueryDisplayable = isdisplayable;
    }

    public Qfset getSelectedQuery() {
        return selectedQuery;
    }

    public SessionGui(String title) {
        initComponents();
        setOpaque(false);
        queryMap = new HashMap<Point, Qfset>();

        this.setBorder(BorderFactory.createTitledBorder(title));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (Point p : queryMap.keySet()) {
                    if (e.getX() <= p.getX() + sideSize && e.getX() >= p.getX()) {
                        if (e.getY() <= p.getY() + sideSize && e.getY() >= p.getY()) {
                            selectedQuery = queryMap.get(p);
                            if (SwingUtilities.isRightMouseButton(e)) {
                                FalsetoPopupMenu menu = new FalsetoPopupMenu();
                                menu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                        break;
                    }
                }

                selectedSession = session;

                info.setVisible(true);

                SessionGui sb = new SessionGui("Query Viewer");
                sb.addQuery(selectedQuery);
                sb.setDistanceLevel(30);
                sb.setRadiusLevel(20);
                sb.setHeightFact(70);
                sb.setWidthFact(80);
                sb.setFontSizeLevel(10);
                sb.setFontSizeMeasureFact(9);
                sb.setSubstringLevelSize(15);
                sb.setSubstringMeasureSize(15);
                sb.setSideSize(250);
                sb.isSelectedQueryDisplayable(false);

                JPanel activeQuery = SessionBuilder.info.getActiveQueryPanel();
                activeQuery.setVisible(true);
                activeQuery.setOpaque(false);
                if (activeQuery.getComponentCount() == 0) {
                    activeQuery.setLayout(new FlowLayout());
                    activeQuery.add(sb);
                } else {
                    activeQuery.setLayout(new FlowLayout());
                    activeQuery.removeAll();
                    activeQuery.add(sb);
                }

                ArrayList<String> todisplay = new ArrayList<>();
                HashSet<Member> alreadyViewed = new HashSet<>();

                for (SelectionFragment sf : selectedQuery.getSelectionPredicates()) {
                    if (alreadyViewed.contains(sf.getValue())) {
                        continue;
                    }
                    alreadyViewed.add(sf.getValue());
                    String s = sf.getLevel().getUniqueName() + "={" + sf.getValue().getName() + ",";
                    for (SelectionFragment sf2 : selectedQuery.getSelectionPredicates()) {
                        if (!alreadyViewed.contains(sf2.getValue()) && sf2.getLevel() == sf.getLevel()) {
                            s += sf2.getValue().getName() + ",";
                            alreadyViewed.add(sf2.getValue());
                        }
                    }
                    s = s.toLowerCase();
                    s = s.subSequence(0, s.length() - 1) + "}";

                    //if (s.length() > 60) {
                    //    s = s.subSequence(0, 61) + " etc...";
                    //}
                    todisplay.add(s);
                }

                //int incSelect = 0;
                if (todisplay.isEmpty()) {
                    SessionBuilder.info.selectionActiveQueryScrollPane.setVisible(false);
                } else {
                    SessionBuilder.info.selectionActiveQueryScrollPane.setVisible(true);
                    SessionBuilder.info.selectionActiveQueryTextArea.setText("");
                    for (String s : todisplay) {
                        SessionBuilder.info.selectionActiveQueryTextArea.setText(SessionBuilder.info.selectionActiveQueryTextArea.getText() + s + "\n");
                    }
                }

                SessionBuilder.info.revalidate();
                SessionBuilder.info.repaint();
                SessionBuilder.frame.repaint();

            }
        });

        this.addMouseMotionListener(
                new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                    }
                });
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponents(g);
        if (session.isEmpty()) {
            this.setVisible(false);
        } else {
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int inc = sideSize / 2 + 25;
            //int gap = 150;
            int gap = sideSize / 2 + 25;

            for (int id = 0; id < session.size(); id++) {
                Qfset q = session.get(id);

                ArrayList<Member> selections = new ArrayList<Member>();

                for (SelectionFragment sf : q.getSelectionPredicates()) {
                    selections.add(sf.getValue());
                }

                ArrayList<Level> groupBySet = new ArrayList<>();

                for (ProjectionFragment pf : q.getAttributes()) {
                    Level l = pf.getLevel();
                    groupBySet.add(l);
                }

                ArrayList<ArrayList<Level>> levelsPerHierarchy = new ArrayList<ArrayList<Level>>();

                for (Hierarchy h : hierarchyList) {
                    ArrayList<Level> lev = new ArrayList<Level>();
                    lev.addAll(Arrays.asList(h.getLevels()));
                    levelsPerHierarchy.add(lev);
                }

                /*for (ProjectionFragment pf : q.getAttributes()) {
                 ArrayList<Level> levels = new ArrayList<Level>();
                 Level l = pf.getLevel();
                 groupBySet.add(l);
                 levels.add(l);

                 for (Member m : selections) {
                 if (l.getHierarchy() == m.getHierarchy()) {

                 if (l.getDepth() > m.getLevel().getDepth()) {
                 levels.add(0, m.getLevel());
                 } else if (l.getDepth() < m.getLevel().getDepth()) {
                 levels.add(m.getLevel());
                 }

                 break;
                 }
                 }

                 levelsPerHierarchy.add(levels);
                 }*/
                ArrayList<Member> measures = new ArrayList<Member>();

                for (MeasureFragment mf : q.getMeasures()) {
                    measures.add(mf.getAttribute());
                }

                graphics.setColor(Color.BLACK);

                int x = gap - sideSize / 2;
                int y = inc - sideSize / 2;

                Point p = new Point(x, y);

                if (isSelectedQueryDisplayable) {
                    if (q == selectedQuery) {
                        graphics.setColor(Color.RED);
                    }
                    graphics.drawRect(x, y, sideSize, sideSize);
                    int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
                    int fontSize = (int) Math.round(10.0 * screenRes / 72.0);
                    Font font = new Font("Arial", Font.PLAIN, fontSize);
                    graphics.setFont(font);
                    graphics.drawString("Query " + (id + 1), gap - 17, sideSize + 20);
                }

                this.queryMap.put(p, q);

                graphics.setColor(Color.BLACK);

                DFMGui.displayDFM(graphics, levelsPerHierarchy, groupBySet, selections, measures, gap, inc, distanceLevel, radiusLevel, heightTitleFact, heightFact, widthFact, fontSizeLevel, fontSizeTitleFact, fontSizeMeasureFact, substringLevelSize, substringMeasureSize, null, true);

                gap += sideSize + 15;
            }
            setPreferredSize(new Dimension(gap - 15 - 40, sideSize + 60));
            this.revalidate();

            /*if (gap < SessionBuilder.js.getWidth()) {
             SessionBuilder.js.setPreferredSize(new Dimension(SessionBuilder.js.getWidth() - 5, 190));
             } else {
             SessionBuilder.js.setPreferredSize(new Dimension(gap, 190));
             }*/
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 84, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
