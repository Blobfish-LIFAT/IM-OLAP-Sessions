/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionBuilder;
import fr.univ_tours.li.jaligon.falseto.sessionDesign.SessionGui;

/**
 *
 * @author julien
 */
public class RecommendationGUI extends javax.swing.JPanel {

    public RecommendationGUI() {
        super();
    }

    public RecommendationGUI(QuerySession recommendation) {
        super();
        this.setOpaque(false);

        SessionGui sg = new SessionGui("Recommended Session");
        sg.setSession((ArrayList<Qfset>) recommendation.getQueries());
        sg.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(sg);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(Color.white);

        sg.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionBuilder.nbClickReco++;
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
        
        
        
        this.add(scrollPane);

    }

}
