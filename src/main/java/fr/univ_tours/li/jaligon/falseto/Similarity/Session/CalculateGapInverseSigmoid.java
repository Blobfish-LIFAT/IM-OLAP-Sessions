/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

/**
 * Class that implements different method to calculate the Smith-Waterman gap
 * @author Elisa
 */
public class CalculateGapInverseSigmoid {
    /*Similarity Matrix*/
    private MatrixInverseSigmoid similarityMatrix;

    public CalculateGapInverseSigmoid(MatrixInverseSigmoid simMatrix) {
        this.similarityMatrix=simMatrix;
    }
    
    public double calculateExtGap_AvgMatch()
    {
        double extGap=0;
        int count=0;
        for(int i=0; i< similarityMatrix.rowCount(); i++)
            for(int j=0; j<similarityMatrix.columnCount(); j++)
            {
                double score=similarityMatrix.getScore(i, j);
                if(score>0)
                {
                    extGap+=score;
                    count++;
                }
            }

        return -(double)(extGap/count);
    }

    public double calculateExtGap_AvgMisMatch()
    {
        double extGap=0;
        int count=0;
        for(int i=0; i< similarityMatrix.rowCount(); i++)
            for(int j=0; j<similarityMatrix.columnCount(); j++)
            {
                double score=similarityMatrix.getScore(i, j);
                if(score<0)
                {
                    extGap+=score;
                    count++;
                }
            }

        return (double)(extGap/count);
    }



}
