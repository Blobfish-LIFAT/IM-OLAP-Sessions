/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import static fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionBuilder.info;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
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
import java.util.List;
import javax.swing.BorderFactory;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;

/**
 *
 * @author julien
 */
public class DFMGui extends javax.swing.JPanel {

    /**
     * Creates new form DFMGui
     */
    public static ArrayList<ArrayList<Level>> links;
    public static HashMap<Level, Point> levelMap;
    private Point startDrag = null;
    private Level startLevelDrag = null;
    private Level endLevelDrag = null;
    private Point endDrag = null;
    private int x0;
    private int y0;
    public static ArrayList<Hierarchy> hierarchyList;
    private Point levelPoint;

    public DFMGui() {
        super();

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);

        this.setBorder(BorderFactory.createTitledBorder("Query Design"));

        links = new ArrayList<ArrayList<Level>>();
        levelMap = new HashMap<Level, Point>();

        hierarchyList();

        /*this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {

                        FalsetoPopupMenu menu = new FalsetoPopupMenu("queryDesign");
                        menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });*/
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                levelPoint = null;
                //Point p = new Point(e.getX() - 4, e.getY() - 45);
                Point p = new Point(e.getX(), e.getY());
                Level level = getLevel(p);

                if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                    startLevelDrag = level;
                }

                if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {

                    if (level != null) {
                        startDrag = p;
                        startLevelDrag = level;
                        endDrag = startDrag;

                    } else if (e.getX() > (x0 - 40) && e.getX() < (x0 + 60)) {
                        if (e.getY() > (y0 - 65) && e.getY() < (y0 + 55)) {
                            info.setVisible(true);
                            SessionBuilder.info.measureSetInfo();
                        }
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //Level level = getLevel(new Point(e.getX() - 4, e.getY() - 45));
                Level level = getLevel(new Point(e.getX(), e.getY()));

                if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                    endLevelDrag = level;
                    removeLink();

                    if (level == null) {
                        FalsetoPopupMenu menu = new FalsetoPopupMenu("queryDesign");
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
                    if (level != null) {
                        endLevelDrag = level;

                        if (startLevelDrag == endLevelDrag) {
                            info.setVisible(true);
                            SessionBuilder.info.selectionSetInfo(startLevelDrag);

                            //LogMiner.info.selectionSetInfo(startLevelDrag);
                        } else {

                            for (ArrayList<Level> l : DFMGui.links) {
                                if ((l.get(0).getHierarchy() == startLevelDrag.getHierarchy() && l.get(0) != startLevelDrag) || (l.get(1).getHierarchy() == startLevelDrag.getHierarchy() && l.get(1) != startLevelDrag)) {
                                    startLevelDrag = null;
                                    break;
                                }
                            }

                            for (ArrayList<Level> l : DFMGui.links) {
                                if ((l.get(0).getHierarchy() == level.getHierarchy() && l.get(0) != level) || (l.get(1).getHierarchy() == level.getHierarchy() && l.get(1) != level)) {
                                    endLevelDrag = null;
                                    break;
                                }
                            }
                        }
                    }
                    startDrag = null;
                    endDrag = null;
                }
                addLink();
                repaint();
                SessionBuilder.info.groupBySetInfo();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startDrag != null) {
                    //endDrag = new Point(e.getX() - 4, e.getY() - 45);
                    endDrag = new Point(e.getX(), e.getY());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                Level level = getLevel(p);

                if (level != null) {
                    levelPoint = p;
                } else {
                    levelPoint = null;
                }

                repaint();
            }
        });

    }

    private Level getLevel(Point p) {
        for (Point p1 : DFMGui.levelMap.values()) {
            //System.out.println(p1.getX()+" "+p.getX()+" "+p1.getY()+" "+p.getY());
            if (p1.distance(p) < 15) {
                for (Level l : DFMGui.levelMap.keySet()) {
                    if (DFMGui.levelMap.get(l).equals(p1)) {
                        return l;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponents(g);
        Graphics2D graphics = (Graphics2D) g;

        x0 = getWidth() / 2;
        y0 = getHeight() / 2;

        graphics.setColor(Color.BLACK);

        int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        int fontSize = (int) Math.round(8.0 * screenRes / 72.0);
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int distanceLevel = 40;
        int radiusLevel = 22;
        int heightTitleFact = 20;
        int heightFact = 90;
        int widthFact = 80;
        int fontSizeLevel = 8;
        int fontSizeTitleFact = 10;
        int fontSizeMeasureFact = 8;
        int substringLevelSize = 7;
        int substringMeasureSize = 14;

        ArrayList<ArrayList<Level>> levels = new ArrayList<ArrayList<Level>>();

        for (Hierarchy h : hierarchyList) {
            ArrayList<Level> lev = new ArrayList<Level>();
            lev.addAll(Arrays.asList(h.getLevels()));
            levels.add(lev);
        }

        displayDFM(graphics, levels, null, InfoGui.selections, InfoGui.measures, x0, y0, distanceLevel, radiusLevel, heightTitleFact, heightFact, widthFact, fontSizeLevel, fontSizeTitleFact, fontSizeMeasureFact, substringLevelSize, substringMeasureSize, levelMap, false);

        if (startDrag != null && endDrag != null) {
            graphics.setStroke(new BasicStroke(3));
            graphics.drawLine(endDrag.x, endDrag.y, startDrag.x, startDrag.y);
        }
        //addLink();

        graphics.setStroke(new BasicStroke(3));
        for (ArrayList<Level> l : links) {
            graphics.drawLine((int) levelMap.get(l.get(0)).getX(), (int) levelMap.get(l.get(0)).getY(), (int) levelMap.get(l.get(1)).getX(), (int) levelMap.get(l.get(1)).getY());
        }

        graphics.setStroke(new BasicStroke(1));

        /*fontSize = (int) Math.round(8.0 * screenRes / 72.0);
         font = new Font("Arial", Font.PLAIN, fontSize);
         graphics.setFont(font);*/
        if (levelPoint != null) {
            Level l = getLevel(levelPoint);
            ArrayList<Member> selections = InfoGui.selections;

            ArrayList<String> selectionName = new ArrayList<String>();
            int maxLength = 0;
            for (Member m : selections) {
                if (m.getLevel() == l) {
                    String name = m.getName();
                    if (name.length() > maxLength) {
                        maxLength = name.length();
                    }
                    selectionName.add(name);
                }
            }

            if (!selectionName.isEmpty()) {
                int height = selectionName.size() * 10 + 5;
                int width = maxLength * 5 + 10;
                graphics.setColor(Color.WHITE);
                graphics.fillRect((int) levelPoint.getX() - width, (int) levelPoint.getY() - height, width, height);
                Color baseColorSelection = new Color(74, 255, 38);
                graphics.setColor(baseColorSelection);
                graphics.drawRect((int) levelPoint.getX() - width, (int) levelPoint.getY() - height, width, height);

                graphics.setColor(Color.BLACK);

                int inc = 0;
                for (String s : selectionName) {
                    graphics.drawString(s, (int) levelPoint.getX() - width + 5, (int) levelPoint.getY() - height + 10 + inc);
                    inc += 10;
                }
            }
        }
    }

    private void addLink() {
        if (startLevelDrag == endLevelDrag) {
            startLevelDrag = null;
        }

        if (startLevelDrag != null && endLevelDrag != null) {

            boolean existingDimension = false;
            boolean dimV1 = false;
            boolean dimV2 = false;
            boolean dimLevelV1 = false;
            boolean dimLevelV2 = false;
            for (ArrayList<Level> list : links) {
                for (Level l : list) {
                    if ((l.getDimension() == startLevelDrag.getDimension())) {
                        dimV1 = true;
                    } else if ((l.getDimension() == endLevelDrag.getDimension())) {
                        dimV2 = true;
                    }

                    if (l == startLevelDrag) {
                        dimLevelV1 = true;
                    } else if (l == endLevelDrag) {
                        dimLevelV2 = true;
                    }

                }
                //System.out.println("Dim "+dim);
                if (dimV1 && dimV2 && (!dimLevelV1 || !dimLevelV2)) {
                    existingDimension = true;
                    break;
                }
            }

            if (!existingDimension) {
                if (startLevelDrag.getDimension() != endLevelDrag.getDimension()) {
                    boolean add = true;
                    for (ArrayList<Level> levList : links) {
                        if (levList.contains(startLevelDrag) && levList.contains(endLevelDrag)) {
                            add = false;
                            break;
                        }
                    }

                    List<Member> selections = InfoGui.selections;

                    for (Member m : selections) {
                        if ((m.getLevel().getHierarchy() == startLevelDrag.getHierarchy() && m.getLevel().getDepth() > startLevelDrag.getDepth()) || (m.getLevel().getHierarchy() == endLevelDrag.getHierarchy() && m.getLevel().getDepth() > endLevelDrag.getDepth())) {
                            add = false;
                            break;
                        }
                    }

                    if (add) {
                        ArrayList<Level> list = new ArrayList<Level>();
                        list.add(startLevelDrag);
                        list.add(endLevelDrag);
                        links.add(list);
                    }
                }
                startLevelDrag = null;
                endLevelDrag = null;
            }
        }
    }

    private void removeLink() {
        if (startLevelDrag == endLevelDrag) {

            ArrayList<ArrayList<Level>> levelsToRemove = new ArrayList<ArrayList<Level>>();
            for (ArrayList<Level> pSet : links) {
                for (Level l : pSet) {
                    if (l == startLevelDrag) {
                        levelsToRemove.add(pSet);
                        break;
                    }
                }
            }

            links.removeAll(levelsToRemove);
        }
    }

    public static void displayDFM(final Graphics2D graphics, ArrayList<ArrayList<Level>> levelsPerHierarchy, ArrayList<Level> groupBySet, ArrayList<Member> selections, ArrayList<Member> measures, int x0, int y0, int distanceLevel, int radiusLevel, int heightTitleFact, int heightFact, int widthFact, int fontSizeLevel, int fontSizeTitleFact, int fontSizeMeasureFact, int substringLevelSize, int substringMeasureSize, HashMap<Level, Point> points, boolean hierarchyReductionView) {
        int i = 0;
        double angle = 0;

        int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        int fontSize = (int) Math.round(fontSizeLevel * screenRes / 72.0);
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        graphics.setFont(font);

        ArrayList<ArrayList<Level>> organizedLevelsPerHierarchy = new ArrayList<ArrayList<Level>>();

        for (Hierarchy h : hierarchyList) {
            for (ArrayList<Level> levels : levelsPerHierarchy) {
                if (levels.get(0).getHierarchy() == h) {
                    organizedLevelsPerHierarchy.add(levels);
                    break;
                }
            }
        }

        for (ArrayList<Level> levels : organizedLevelsPerHierarchy) {
            int stringXLevel = -radiusLevel + 10;
            int stringYLevel = 0;

            int levelSize = levels.size();

            double x = x0 + (distanceLevel + distanceLevel * levelSize) * Math.cos(angle);
            double y = y0 + (distanceLevel + distanceLevel * levelSize) * Math.sin(angle);

            if (hierarchyReductionView) {
                x = x0 + (2 * distanceLevel) * Math.cos(angle);
                y = y0 + (2 * distanceLevel) * Math.sin(angle);
            }

            graphics.setColor(Color.BLACK);
            graphics.drawLine((int) x, (int) y, x0, y0);

            int radiusSizeInc = 0;

            for (int j = (levelSize - 1); j >= 0; j--) {

                double xLevel = x0 + (distanceLevel + distanceLevel * (levelSize - j)) * Math.cos(angle);
                double yLevel = y0 + (distanceLevel + distanceLevel * (levelSize - j)) * Math.sin(angle);

                graphics.setColor(Color.RED);

                Color baseColor = new Color(255, 192, 32);
                Color baseColorSelection = new Color(74, 255, 38);
                Color bright = baseColor.brighter();
                Color brightTransparent = new Color(bright.getRed(), bright.getGreen(), bright.getBlue(), 0);

                graphics.setPaint(new GradientPaint(0, 0, baseColor, 0, 0, bright));

                boolean isImportant = false;

                for (Member selection : selections) {
                    if (selection.getLevel() == levels.get(j)) {
                        graphics.setPaint(new GradientPaint(0, 0, baseColorSelection.brighter(), 0, 0, brightTransparent));
                        isImportant = true;
                        break;
                    }
                }

                if (groupBySet != null) {
                    if (groupBySet.contains(levels.get(j))) {
                        isImportant = true;
                    }
                }

                int radiusSize = radiusLevel;

                if (hierarchyReductionView && !isImportant) {
                    radiusSize = (int) (0.5 * radiusSize);
                }
                if (hierarchyReductionView && isImportant) {
                    radiusSizeInc = radiusSizeInc + (int) (radiusSize / 2);
                }

                if (hierarchyReductionView) {
                    xLevel = x0 + (2 * distanceLevel + radiusSizeInc) * Math.cos(angle);
                    yLevel = y0 + (2 * distanceLevel + radiusSizeInc) * Math.sin(angle);
                }

                radiusSizeInc += radiusSize;

                int xOval = (int) xLevel - (radiusSize / 2);
                int yOval = (int) yLevel - (radiusSize / 2);

                graphics.fillOval(xOval, yOval, radiusSize, radiusSize);
                graphics.setPaint(new GradientPaint(0, 0, bright, 0, 0, brightTransparent));

                graphics.fillOval((int) xOval + (6 / 2), (int) yOval + (6 / 2), radiusSize - 6, radiusSize - 6);
                graphics.setColor(Color.BLACK);

                if (points != null) {
                    Point levelXY = new Point((int) xLevel, (int) yLevel);
                    levelMap.put(levels.get(j), levelXY);
                }

                if (!(hierarchyReductionView && !isImportant)) {
                    String sl = levels.get(j).getName();
                    if (sl.length() > substringLevelSize) {
                        sl = sl.substring(0, substringLevelSize) + "...";
                    }

                    graphics.drawString(sl, (int) xLevel + stringXLevel, (int) yLevel + stringYLevel);
                }

                if (stringXLevel == -radiusLevel) {
                    stringXLevel = -radiusLevel + 10;
                } else {
                    stringXLevel = -radiusLevel;
                }

                if (stringYLevel == 0) {
                    stringYLevel = 7;
                } else {
                    stringYLevel = 0;
                }
            }
            i++;
            angle += (2 * Math.PI) / (organizedLevelsPerHierarchy.size());
        }

        graphics.setColor(Color.WHITE);
        graphics.fillRect(x0 - (widthFact / 2), y0 - (heightFact / 2), widthFact, heightFact);
        Color baseColor = new Color(255, 192, 32);
        Color bright = baseColor.brighter();
        graphics.setPaint(new GradientPaint(0, 0, baseColor, 0, 0, bright));
        graphics.drawRect(x0 - (widthFact / 2), y0 - (heightFact / 2), widthFact, heightTitleFact);
        graphics.drawRect(x0 - (widthFact / 2), y0 - (heightFact / 2), widthFact, heightFact);
        graphics.setColor(Color.BLACK);

        fontSize = (int) Math.round(fontSizeTitleFact * screenRes / 72.0);
        font = new Font("Arial", Font.PLAIN, fontSize);
        graphics.setFont(font);

        graphics.drawString("FACT", x0 - (widthFact / 2) + 5, y0 - (heightFact / 2) + 15);

        screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        fontSize = (int) Math.round(fontSizeMeasureFact * screenRes / 72.0);
        font = new Font("Arial", Font.PLAIN, fontSize);
        graphics.setFont(font);

        int inc = heightTitleFact + 10;
        int nbMeasure = 0;
        for (Member m : measures) {
            nbMeasure++;
            if (nbMeasure > 5) {
                graphics.drawString("etc...", x0 - (widthFact / 2) + 5, y0 - (heightFact / 2) + inc);
                break;
            } else {
                String sm = m.getName();
                if (sm.length() > substringMeasureSize) {
                    sm = sm.substring(0, substringMeasureSize) + "...";
                }
                graphics.drawString(sm, x0 - (widthFact / 2) + 5, y0 - (heightFact / 2) + inc);
            }
            inc += 10;
        }
    }

    public static void hierarchyList() {
        HashSet<Hierarchy> hierarchies = fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCubeHiers();
        hierarchyList = new ArrayList<Hierarchy>();
        /*for (Hierarchy h : hierarchies) {
         if (!h.getDimension().isMeasures()) {
         hierarchyList.add(h);
         }
         }*/

        /*for (Hierarchy h : hierarchies) {
         if (h.getName().equals("OCCUPATION")) {
         hierarchyList.add(h);
         break;
         }
         }

         for (Hierarchy h : hierarchies) {
         if (h.getName().equals("YEAR")) {
         hierarchyList.add(h);
         break;
         }
         }

         for (Hierarchy h : hierarchies) {
         if (h.getName().equals("CITY")) {
         hierarchyList.add(h);
         break;
         }
         }

         for (Hierarchy h : hierarchies) {
         if (h.getName().equals("SEX")) {
         hierarchyList.add(h);
         break;
         }
         }

         for (Hierarchy h : hierarchies) {
         if (h.getName().equals("RACE")) {
         hierarchyList.add(h);
         break;
         }
         }*/
        for (Hierarchy h : hierarchies) {
            if (!h.getDimension().isMeasures()) {
                hierarchyList.add(h);
            }
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
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
