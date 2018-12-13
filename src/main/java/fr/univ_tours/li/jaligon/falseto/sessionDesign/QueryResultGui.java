/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.MondrianException;
import mondrian.olap.Query;
import mondrian.rolap.RolapResult;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.ProjectionFragment;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.SelectionFragment;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import org.olap4j.layout.RectangularCellSetFormatter;

/**
 *
 * @author julien
 */
public class QueryResultGui extends JPanel {

    private ArrayList<Hierarchy> hierarchiesOnRow;
    private ArrayList<Hierarchy> hierarchiesOnColumn;
    private ArrayList<Hierarchy> hierarchies;
    //private HashMap<ArrayList<Member>, String> resultMap = new HashMap<ArrayList<Member>, String>();
    private HashMap<Hierarchy, ArrayList<Member>> onRows;
    private HashMap<Hierarchy, ArrayList<Member>> onColumns;
    private ArrayList<ArrayList<String>> data;
    private QueryGui queryGui;
    private ArrayList<Member> filters;
    private ArrayList<Level> levelsToLimit;
    public int crossTableWidth;
    public int crossTableHeight;
    //private int cartesianProduct;

    public QueryResultGui(QueryGui queryGui) throws SQLException, ClassNotFoundException, MondrianException{
        super();

        levelsToLimit = new ArrayList<Level>();
        hierarchiesOnRow = new ArrayList<Hierarchy>();
        hierarchiesOnColumn = new ArrayList<Hierarchy>();
        this.queryGui = queryGui;

        setOpaque(false); // we don't paint all our bits
        setLayout(new BorderLayout());
        setVisible(true);
        setPreferredSize(new Dimension(100, 100));

        queryGui.getQueryResultInfoGui().initializeEmpty();//for initializing QueryResultInfoGui
        Qfset q = queryGui.getQuery();//for initializing QueryResultInfoGui

        int inc = 0;
        for (Hierarchy h : fr.univ_tours.li.jaligon.falseto.Generics.Generics.getHierarchies()) {
            if (inc < 3) {
                if (h.getDefaultMember().isMeasure()) {
                    queryGui.addOnRows(h.getDefaultMember().getLevel());
                    queryGui.getQueryResultInfoGui().addRowList(h.getDefaultMember().getLevel());
                }

                hierarchiesOnRow.add(h);
            } else {
                if (h.getDefaultMember().isMeasure()) {
                    queryGui.addOnColumns(h.getDefaultMember().getLevel());
                    queryGui.getQueryResultInfoGui().addColumnList(h.getDefaultMember().getLevel());
                }

                hierarchiesOnColumn.add(h);
            }
            inc++;

        }

        for (ProjectionFragment pf : q.getAttributes()) {
            boolean loop = true;
            for (Hierarchy h : hierarchiesOnRow) {
                if (h == pf.getHierarchy()) {
                    loop = false;
                    queryGui.addOnRows(pf.getLevel());
                    queryGui.getQueryResultInfoGui().addRowList(pf.getLevel());
                }
            }

            if (loop) {
                for (Hierarchy h : hierarchiesOnColumn) {
                    if (h == pf.getHierarchy()) {
                        queryGui.addOnColumns(pf.getLevel());
                        queryGui.getQueryResultInfoGui().addColumnList(pf.getLevel());
                    }
                }
            }
        }

        hierarchies = new ArrayList<Hierarchy>();
        hierarchies.addAll(hierarchiesOnRow);
        hierarchies.addAll(hierarchiesOnColumn);

        execute();
    }

    private HashMap<ArrayList<Member>, String> queryResult(RolapResult result) {

        HashMap<ArrayList<Member>, String> resultM = new HashMap<ArrayList<Member>, String>();

        ArrayList<Integer> position = new ArrayList<Integer>();
        for (int i = 0; i < result.getAxes().length; i++) {
            position.add(0);
        }

        ArrayList<ArrayList<Integer>> positionList = new ArrayList<ArrayList<Integer>>();

        positionList(result, 0, position, positionList);

        for (ArrayList<Integer> pos : positionList) {
            int[] positions = new int[result.getAxes().length];
            for (int j = 0; j < pos.size(); j++) {
                //System.out.print(pos.get(j) + " ");
                positions[j] = pos.get(j);
            }

            ArrayList<Member> members = new ArrayList<Member>();

            for (Hierarchy h : hierarchies) {
                Member m = result.getMember(positions, h.getHierarchy());
                members.add(m);
            }

            String value = result.getCell(positions).getFormattedValue();

            resultM.put(members, value);
        }

        return resultM;
    }

    private void positionList(RolapResult result, int axis, ArrayList<Integer> position, ArrayList<ArrayList<Integer>> positionList) {
        if (axis == result.getAxes().length) {
            //System.out.println("Position : "+position);
            positionList.add(position);
            position = new ArrayList<Integer>();
            for (int i = 0; i < result.getAxes().length; i++) {
                position.add(0);
            }
        } else {
            for (int i = 0; i < result.getAxes()[axis].getPositions().size(); i++) {
                position.set(axis, i);
                int dim = axis + 1;
                positionList(result, dim, position, positionList);
                ArrayList<Integer> positionTemp = new ArrayList<Integer>();
                positionTemp.addAll(position);
                position = positionTemp;
            }
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponents(g);
        Graphics2D graphics = (Graphics2D) g;

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 11);
        g.setFont(font);

        int heightCell = 20;

        int cartesianProductOnRows = 1;

        for (Hierarchy h : onRows.keySet()) {
            cartesianProductOnRows *= onRows.get(h).size();
        }

        int coordX = 0;
        int coordY = heightCell * onColumns.keySet().size();

        int idH = 1;

        for (Hierarchy h : hierarchiesOnRow) {
            int height = heightCell;
            int width = 0;
            for (Member m : onRows.get(h)) {
                if (m.getName().length() > width) {
                    width = m.getName().length();
                }
            }
            width = width * 8;

            int heightH = ((cartesianProductOnRows / onRows.get(h).size()) / idH) * height;
            for (int i = 0; i < idH; i++) {
                for (Member m : onRows.get(h)) {
                    graphics.setPaint(Color.lightGray);
                    graphics.fillRect(coordX, coordY, width, heightH);
                    graphics.setPaint(Color.black);
                    graphics.drawRect(coordX, coordY, width, heightH);
                    graphics.drawString(m.getName(), coordX + 5, coordY + 15);
                    coordY += heightH;
                }
            }

            idH *= onRows.get(h).size();

            coordX += width;
            coordY = heightCell * onColumns.keySet().size();

        }

        int cartesianProductOnColumns = 1;

        for (Hierarchy h : onColumns.keySet()) {
            cartesianProductOnColumns *= onColumns.get(h).size();
        }

        int initialCoordXColumn = coordX;
        coordY = 0;

        boolean isWidthOptimal = false;
        int widthOptimal = 0;

        if (!hierarchiesOnColumn.isEmpty()) {
            while (!isWidthOptimal) {
                int nbTrue = 0;
                idH = 1;
                for (Hierarchy h : hierarchiesOnColumn) {

                    int widthH = ((cartesianProductOnColumns / onColumns.get(h).size()) / idH) * widthOptimal;
                    int localMaxWidth = 0;
                    for (int i = 0; i < idH; i++) {
                        for (Member m : onColumns.get(h)) {
                            if (m.getName().length() > localMaxWidth) {
                                localMaxWidth = m.getName().length();
                            }
                        }
                    }

                    if (widthH >= localMaxWidth) {
                        nbTrue++;
                    } else {
                        break;
                    }

                    idH *= onColumns.get(h).size();

                }

                if (nbTrue == hierarchiesOnColumn.size()) {
                    isWidthOptimal = true;
                } else {
                    widthOptimal++;
                }

            }
        }

        int maxWidth = widthOptimal * 8;
        /*for (ArrayList<Member> mList : onColumns.values()) {
         for (Member m : mList) {
         if (m.getName().length() > maxWidth) {
         maxWidth = m.getName().length();
         }
         }
         }*/
        
        //if (maxWidth == 0) {
            for (ArrayList<String> dataStringList : data) {
                for (String dataString : dataStringList) {
                    if (dataString.length()*8 > maxWidth) {
                        maxWidth = dataString.length()*8;
                    }
                }
            }
            //maxWidth *= 8;
        //}
        
        
        int height = heightCell;

        idH = 1;
        for (Hierarchy h : hierarchiesOnColumn) {

            int width = maxWidth;

            int widthH = ((cartesianProductOnColumns / onColumns.get(h).size()) / idH) * width;
            for (int i = 0; i < idH; i++) {
                for (Member m : onColumns.get(h)) {
                    graphics.setPaint(Color.lightGray);
                    graphics.fillRect(coordX, coordY, widthH, height);
                    graphics.setPaint(Color.black);
                    graphics.drawRect(coordX, coordY, widthH, height);
                    graphics.drawString(m.getName(), coordX + 5, coordY + 15);
                    coordX += widthH;
                }
            }

            idH *= onColumns.get(h).size();

            coordX = initialCoordXColumn;
            coordY += heightCell;

        }

        

        for (ArrayList<String> dataStringList : data) {
            for (String dataString : dataStringList) {
                graphics.setPaint(Color.white);
                graphics.fillRect(coordX, coordY, maxWidth, height);
                graphics.setPaint(Color.black);
                graphics.drawRect(coordX, coordY, maxWidth, height);
                graphics.drawString(dataString, coordX + 5, coordY + 15);
                coordX += maxWidth;
            }
            coordX = initialCoordXColumn;
            coordY += heightCell;
        }

        graphics.setPaint(Color.white);
        graphics.fillRect(0, coordY+1, cartesianProductOnColumns * maxWidth + initialCoordXColumn, filters.size() * 15 + 15 * 3);
        graphics.setPaint(Color.black);
        
        if (!filters.isEmpty()) {
            graphics.drawString("Filters:", 5, coordY + 15);
        }

        int inc = 15;
        for (Member m : filters) {
            graphics.drawString(m.getName(), 5, coordY + 15 + inc);
            inc += 15;
        }

        int widthSize = cartesianProductOnColumns * maxWidth + initialCoordXColumn;
        int heightSize = (cartesianProductOnRows + hierarchiesOnColumn.size()) * heightCell + filters.size() * 15 + 15 * 3;

        crossTableWidth = widthSize;
        crossTableHeight = heightSize;
        
        setPreferredSize(new Dimension(widthSize, heightSize));
        this.revalidate();
    }

    public void initializeOnColumnsOnRows(){
        List<Level> levelsOnColumns = queryGui.getOnColumns();

        hierarchiesOnRow = new ArrayList<Hierarchy>();
        hierarchiesOnColumn = new ArrayList<Hierarchy>();

        for (Level l : levelsOnColumns) {
            hierarchiesOnColumn.add(l.getHierarchy());
            //System.out.println("ON COLUMN "+l.getHierarchy());
        }

        List<Level> levelsOnRows = queryGui.getOnRows();

        for (Level l : levelsOnRows) {
            hierarchiesOnRow.add(l.getHierarchy());
            //System.out.println("ON ROW "+l.getHierarchy());
        }

        hierarchies = new ArrayList<Hierarchy>();
        hierarchies.addAll(hierarchiesOnRow);
        hierarchies.addAll(hierarchiesOnColumn);

        execute();
        //repaint();
    }

    private void execute() throws MondrianException{
        Qfset qf = queryGui.getQuery();

        HashSet<ProjectionFragment> projectionOnRows = new HashSet<ProjectionFragment>();
        HashSet<ProjectionFragment> projectionOnColumns = new HashSet<ProjectionFragment>();

        for (Hierarchy h : hierarchiesOnColumn) {
            for (ProjectionFragment pf : qf.getAttributes()) {
                if (h == pf.getLevel().getHierarchy()) {
                    projectionOnColumns.add(pf);
                }
            }
        }

        for (Hierarchy h : hierarchiesOnRow) {
            for (ProjectionFragment pf : qf.getAttributes()) {
                if (h == pf.getLevel().getHierarchy()) {
                    projectionOnRows.add(pf);
                }
            }
        }

        Query q = queryGui.getQuery().toMDX(projectionOnRows, projectionOnColumns);

        RolapResult result = null;

        //try {

        result = (RolapResult) fr.univ_tours.li.jaligon.falseto.Generics.Connection.getCnx().execute(q);

        HashMap<ArrayList<Member>, String> resultMap = queryResult(result);

        ArrayList<String> stringLevelSort = new ArrayList<String>();
        for (ArrayList<Member> list : resultMap.keySet()) {
            stringLevelSort.add(list.toString());
        }
        //System.out.println(stringLevelSort);
        Collections.sort(stringLevelSort);

        //System.out.println(stringLevelSort);
        ArrayList<ArrayList<Member>> levelSort = new ArrayList<ArrayList<Member>>();

        for (String s : stringLevelSort) {
            for (ArrayList<Member> list : resultMap.keySet()) {
                if (list.toString().equals(s)) {
                    //resultMap.put(list, resultMapTemp.get(list));
                    levelSort.add(list);
                }
            }
        }

        onRows = new HashMap<Hierarchy, ArrayList<Member>>();
        onColumns = new HashMap<Hierarchy, ArrayList<Member>>();

        for (Hierarchy h : hierarchiesOnRow) {
            onRows.put(h, new ArrayList<Member>());
        }

        for (Hierarchy h : hierarchiesOnColumn) {
            onColumns.put(h, new ArrayList<Member>());
        }

        for (ArrayList<Member> memberList : levelSort) {
            for (Member m : memberList) {
                for (Hierarchy h : hierarchies) {
                    if (m.getHierarchy() == h) {
                        if (hierarchiesOnRow.contains(h)) {
                            ArrayList<Member> temp = onRows.get(h);
                            if (!temp.contains(m)) {
                                temp.add(m);
                                onRows.put(h, temp);
                            }
                        } else {
                            ArrayList<Member> temp = onColumns.get(h);
                            if (!temp.contains(m)) {
                                temp.add(m);
                                onColumns.put(h, temp);
                            }
                        }
                    }
                }
            }
        }

        data = new ArrayList<ArrayList<String>>();

        //int cartesianProductOnRows = 1;
        int cartesianProductOnColumns = 1;

        /*for (Hierarchy h : onRows.keySet()) {
         cartesianProductOnRows *= onRows.get(h).size();
         }*/
        filters = new ArrayList<Member>();

        ArrayList<Hierarchy> hierarchiesToRemove = new ArrayList<Hierarchy>();

        for (Hierarchy h : hierarchies) {
            if (onColumns.get(h) != null) {
                if (onColumns.get(h).size() == 1) {
                    filters.add(onColumns.get(h).get(0));
                    onColumns.remove(h);
                    hierarchiesToRemove.add(h);
                    hierarchiesOnColumn.remove(h);
                }
            } else {
                if (onRows.get(h).size() == 1) {
                    filters.add(onRows.get(h).get(0));
                    onRows.remove(h);
                    hierarchiesToRemove.add(h);
                    hierarchiesOnRow.remove(h);
                }
            }
        }

        hierarchies.removeAll(hierarchiesToRemove);

        for (Hierarchy h : onColumns.keySet()) {
            cartesianProductOnColumns *= onColumns.get(h).size();
        }
        //System.out.println(onRows.toString());
        //System.out.println(onColumns.toString());
        int i = 1;
        ArrayList<String> stringList = new ArrayList<String>();

        for (ArrayList<Member> memberList : levelSort) {
            String s = resultMap.get(memberList);
            //System.out.println(memberList.toString() + " " + s);
            if (i % cartesianProductOnColumns == 0) {
                stringList.add(s);
                data.add(stringList);

                stringList = new ArrayList<String>();
            } else {
                stringList.add(s);
            }
            i++;
        }
    }

}
