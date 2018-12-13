/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import java.util.ArrayList;
import java.util.List;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QfsetForComparison;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel;

/**
 *
 * @author enrico.gallinucci2
 */
public class AllQueriesComparison {
    
    public List<QfsetForComparison> queryList;
    public double[][] similarityMatrix;
    
    private double alpha;
    private double beta;
    private double gamma;
    private double threshold;
    
    public AllQueriesComparison(List<QuerySession> sessionList, double alpha, double beta, double gamma, double threshold){
        queryList = new ArrayList<>();
        for(int i=0;i<sessionList.size();i++){
            for(int j=0;j<sessionList.get(i).size();j++){
                //queryList.add(new QfsetForComparison(sessionList.get(i).getId(),j,sessionList.get(i).getTemplate(),sessionList.get(i).get(j)));
                queryList.add(new QfsetForComparison(""+i,j,sessionList.get(i).getTemplate(),sessionList.get(i).get(j)));
            }
        }
        this.similarityMatrix = new double[queryList.size()][queryList.size()];
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.threshold = threshold;
    }
    
    
    public void computeSimilarities() {

        for (int i = 0; i < queryList.size(); i++) {
            for (int j = 0; j < queryList.size(); j++) {
                QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel queryComparison 
                        = new QueryComparisonByJaccardAndStructureThresholdWithSeveralSelectionPerLevel(queryList.get(i).getQfset(), queryList.get(j).getQfset(), alpha, beta, gamma);
                double similarity = queryComparison.computeSimilarity().getSimilarity();

                if (similarity == threshold) {
                    similarityMatrix[i][j] = (double) 0.01;
                } else if(similarity < threshold){
                    similarityMatrix[i][j] = 0;
                }
                else {
                    similarityMatrix[i][j] = (similarity - threshold) / 0.3;
                }
                //similarityMatrix[i][j] = similarity;
            }
        }
    }
    
}
