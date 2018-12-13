/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import fr.univ_tours.li.jaligon.falseto.Generics.Connection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.RecommendationGUI;
import fr.univ_tours.li.jaligon.falseto.summaryBrowsingDesign.SummaryGUI;

/**
 *
 * @author julien
 */
public class SessionBuilder extends JFrame {

    /**
     * Creates new form LogMiner
     */
    public static DFMGui dfm;
    public static JTabbedPane tabbedPane;
    public static SessionGui sessionsGui;
    public static InfoGui info;
    public static SessionBuilder frame;
    public static String id = fr.univ_tours.li.jaligon.falseto.Generics.Generics.date();
    public static long timeBegin = System.currentTimeMillis();
    public static long timeEnd = System.currentTimeMillis();
    public static String idQuestionnaire = null;
    public static String random = null;
    public static JScrollPane js = null;

    public static int nbClickReco = 0;
    public static int nbClickSummary = 0;
    //public static ArrayList<Qfset> session = new ArrayList<Qfset>();
    //public static Qfset selectedQuery;

    public SessionBuilder() {
        super("FALSETO (Former AnalyticaL Sessions for lEss Tedious Olap) v0.7");

        frame = this;
        boolean display = true;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                int confirm = JOptionPane.showConfirmDialog(
                        SessionBuilder.frame,
                        "Are you sure to close the application?",
                        "Close the application",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        setPreferredSize(new Dimension(1280, 710));
        setLocation(100, 100);
        //setResizable(false);

        if (display) {
            sessionsGui = new SessionGui("Session Viewer");
            //sessionsGui.setPreferredSize(new Dimension(704, 190));

            js = new JScrollPane(sessionsGui);
            js.setOpaque(false); // we don't paint all our bits
            js.getViewport().setBackground(Color.white);
            js.setPreferredSize(new Dimension(604, 200));
            js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

            js.setVisible(false);

            tabbedPane = new JTabbedPane();

            dfm = new DFMGui();
            dfm.setPreferredSize(new Dimension(604, 568));

            tabbedPane.addTab("Query Designer", dfm);
            //tabbedPane.setEnabledAt(0, false);

            SummaryGUI sg = new SummaryGUI();

            tabbedPane.addTab("Log Browsing", sg);

            tabbedPane.addTab("Recommendation", new RecommendationGUI());
            tabbedPane.setEnabledAt(2, false);
            tabbedPane.setSelectedIndex(1);

            tabbedPane.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (tabbedPane.getSelectedIndex() == 0) {

                        if (info.getActiveQueryPanel().isVisible() || info.getSessionPanel().isVisible()) {
                            info.setVisible(true);
                        } else {
                            info.setVisible(false);
                        }
                        info.summaryDisable();
                    } else if (tabbedPane.getSelectedIndex() == 1) {
                        info.setVisible(true);
                        info.summaryEnable();
                        info.measureSetDisable();
                        info.selectionSetDisable();
                    } else if (tabbedPane.getSelectedIndex() == 2) {
                        info.summaryDisable();
                        info.measureSetDisable();
                        info.selectionSetDisable();
                    }
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

            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.setOpaque(false);
            p.add(js, BorderLayout.NORTH);
            p.add(tabbedPane, BorderLayout.CENTER);
            p.setVisible(true);

            getContentPane().add(p, BorderLayout.CENTER);

            info = new InfoGui();
            info.setPreferredSize(new Dimension(420, 768));
            getContentPane().add(info, BorderLayout.EAST);
            getContentPane().setBackground(Color.WHITE);
            info.setVisible(false);

            pack();

            setLocationRelativeTo(null);
            setVisible(true);

            while (idQuestionnaire == null || idQuestionnaire.equals("")) {
                idQuestionnaire = JOptionPane.showInputDialog(this, "Please enter the ID of your questionnaire");
            }
            Random r = new Random();
            random = "" + r.nextInt(1000000);

            info.setVisible(true);
            info.summaryEnable();
        }
    }

    public static void configureSessionGui() {
        sessionsGui.repaint();
        js.setVisible(true);
    }

    public static void configureSessionGuiInvisible() {
        //sessionsGui.repaint();
        js.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        optionMenu = new javax.swing.JMenu();
        configurationItem = new javax.swing.JMenuItem();
        logItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        optionMenu.setText("Options");

        configurationItem.setText("configuration");
        optionMenu.add(configurationItem);

        logItem.setText("analyze a log...");
        optionMenu.add(logItem);

        menuBar.add(optionMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 278, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {
        Locale.setDefault(Locale.US);

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SessionBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SessionBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SessionBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SessionBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        Thread t = new Thread() {
            @Override
            public void run() {
                final JFrame waiting = new JFrame("Waiting");
                waiting.setSize(new Dimension(100, 100));
                waiting.setLocationRelativeTo(null);
                waiting.setUndecorated(true);
                ImageIcon icone = new ImageIcon("pictures/waiting.gif");
                JLabel image = new JLabel(icone);
                waiting.add(image);
                waiting.setVisible(true);
            }
        };
        t.start();

        Connection c;

        try {
            c = new Connection();
            c.open();
            SessionBuilder lg = new SessionBuilder();
            //display = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            //configurationActionPerformed();
            //display = false;
        }

        

        /*Runnable gui = new Runnable() {
         @Override
         public void run() {

         Connection c;

         try {
         c = new Connection();
         c.open();
         //display = true;
         } catch (Exception ex) {
         System.out.println(ex.getMessage());
         //configurationActionPerformed();
         //display = false;
         }

         SessionBuilder lg = new SessionBuilder();

         }
         };
         //GUI must start on EventDispatchThread:
         SwingUtilities.invokeLater(gui);*/
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem configurationItem;
    private javax.swing.JMenuItem logItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu optionMenu;
    // End of variables declaration//GEN-END:variables
}
