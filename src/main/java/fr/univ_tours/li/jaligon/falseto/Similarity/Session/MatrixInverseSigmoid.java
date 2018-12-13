/*
 * $Id: Matrix.java,v 1.2 2005/04/14 14:44:42 ahmed Exp $
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import java.io.Serializable;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparison;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;

/**
 * Scoring matrix.
 * 
 * @author Elisa Turricchia
 */
public class MatrixInverseSigmoid implements Serializable {

    /**
     * Matrix id (or name)
     */
    private String id = null;
    /**
     * Scores
     */
    private double[][] scores = null;
    private double[][] increasing_function_matrix = null;
    private int n;
    private int m;
    private QueryComparison queryComparison;
    /*Weight for group by set*/
    private double alpha;
    /*Weight for selection*/
    private double beta;
    /*Weight for measure*/
    private double gamma;

    public int columnCount() {
        return m;
    }

    public int rowCount() {
        return n;
    }
    private QuerySession session1;
    private QuerySession session2;

    public MatrixInverseSigmoid(QuerySession s1, QuerySession s2, double alpha, double beta, double gamma) {
        this.session1 = s1;
        this.session2 = s2;
        n = s1.size() + 1;
        m = s2.size() + 1;
        scores = new double[n][m];
        increasing_function_matrix = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                increasing_function_matrix[i][j] = 1;
            }
        }
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;

    }

    private void setQueryComparison(Qfset qi, Qfset qj) {
        queryComparison = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(qi, qj, alpha, beta, gamma);
    }

    public void fillMatrix_MatchMisMatch(double threshold) {

        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                double similarity = queryComparison.computeSimilarity().getSimilarity();

                /*if(similarity>=threshold) //calcolo similarità
                scores[i+1][j+1]=match;
                else
                scores[i+1][j+1]=mismatch;*/
                if (similarity == threshold) {
                    scores[i + 1][j + 1] = (double) 0.01;
                } else {
                    scores[i + 1][j + 1] = similarity - threshold;
                }
            }
        }
    }

    public void fillMatrix_Similarity() {
        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                //Get negative value
                double sim = queryComparison.computeSimilarity().getSimilarity();
                scores[i + 1][j + 1] = sim;
            }
        }
    }

    public void fillMatrix_Threshold(double threshold) {
        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                double similarity = queryComparison.computeSimilarity().getSimilarity();
                if (similarity >= threshold) //calcolo similarità
                {
                    scores[i + 1][j + 1] = similarity;
                } else {
                    scores[i + 1][j + 1] = similarity - threshold;
                }
            }
        }
    }

    public void fillMatrix_Threshold_Opposite(double threshold) {
        for (int i = 0; i < n; i++) {
            scores[i][0] = 0;
        }
        for (int j = 0; j < m; j++) {
            scores[0][j] = 0;
        }
        for (int i = 0; i < session1.size(); i++) {
            for (int j = 0; j < session2.size(); j++) {
                setQueryComparison((Qfset) session1.get(i), (Qfset) session2.get(j));
                double similarity = queryComparison.computeSimilarity().getSimilarity();
                if (similarity >= threshold) {
                    scores[i + 1][j + 1] = similarity;
                } else {
                    scores[i + 1][j + 1] = -(1 - similarity);
                }
            }
        }
    }


    //Two-dimensional logistic sigmoid
    //Maximum time discount=0.66
    //Slope=4

    public void applySymmetricIncreasingFunction() {
        double MaxTimeDiscount = 0;
        //double slope = 5;
        double slope = 10.0/(double)session1.size();
        
        for (int k = 0; k < n - 1; k++) {
            int i = k;
            for (int t = 0; t < m - 1; t++) {
                int j = t;
                //if(scores[i][j]>0)
                increasing_function_matrix[k][t] = (double) (1 - ((1 - MaxTimeDiscount) / (1 + Math.exp(slope + (5/session1.size())*k - (20/session2.size())*t))));
                //System.out.println((double) (1 - ((1 - MaxTimeDiscount) / (1 + Math.exp(slope + (5/session1.size())*k - (15/session2.size())*t)))));
                scores[i][j] *= increasing_function_matrix[k][t];
            }
        }
    }

    public MatrixInverseSigmoid(String id, double[][] scores) {
        this.id = id;
        this.scores = scores;
    }

    public QuerySession getSession1() {
        return session1;
    }

    public void setSession1(QuerySession session1) {
        this.session1 = session1;
    }

    public QuerySession getSession2() {
        return session2;
    }

    public void setSession2(QuerySession session2) {
        this.session2 = session2;
    }

    public double increasing_function(int i, int j) {
        return this.increasing_function_matrix[i][j];
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return Returns the scores.
     */
    public double[][] getScores() {
        return this.scores;
    }

    /**
     * 
     * @param a
     * @param b
     * @return score
     */
    public double getScore(int a, int b) {
        return this.scores[a][b];
    }
}