/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import fr.univ_tours.li.jaligon.falseto.Generics.Generics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import static fr.univ_tours.li.jaligon.falseto.Generics.Generics.EditDistanceOperatorType.DELETION;
import static fr.univ_tours.li.jaligon.falseto.Generics.Generics.EditDistanceOperatorType.INSERTION;
import static fr.univ_tours.li.jaligon.falseto.Generics.Generics.EditDistanceOperatorType.MATCH_MISMATCH;
import static fr.univ_tours.li.jaligon.falseto.Generics.Generics.EditDistanceOperatorType.STOP;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.Similarity;

/**
 * Extension of the original SmithWaterman algorithm, which adds to each
 * session's query a score based on the alignment (if found).
 * 
 * @author Enrico
 */
public class SmithWatermanWithScore extends SessionComparison {

    protected double[][] similarityMatrix;
    private Matrix matrix;
    //private double[][] costMatrix;
    protected CellInfoEditDistance[][] costMatrix;
    protected double openingGapCost;
    protected double extendingGapCost;
    private double thre;
    
    private boolean[] scoredQuery1;
    private boolean[] scoredQuery2;

    public SmithWatermanWithScore(Matrix similarityMatrix, double openingGapcost, double extendingGapCost, QuerySession s1, QuerySession s2, double thre, double alpha, double beta, double gamma) {
        super(s1, s2, thre, alpha, beta, gamma);
        this.similarityMatrix = similarityMatrix.getScores();
        this.openingGapCost = openingGapcost;
        this.extendingGapCost = extendingGapCost;
        this.row += 1;
        this.column += 1;
        //this.costMatrix=new double[n][m];
        this.costMatrix = new CellInfoEditDistance[row][column];
        this.matrix = similarityMatrix;
        this.thre = thre;
        
        scoredQuery1 = new boolean [row];
        scoredQuery2 = new boolean [column];
    }

    /*
     * NEW METHODS FOR SCORING
     */
    
    private void initializeScores(){
        for(int i=0; i<row; i++){
            scoredQuery1[i] = false;
        }
        for(int i=0; i<column; i++){
            scoredQuery2[i] = false;
        }
    }
    
   
    /**
     * Computes the similarity, then gives a score to each query in each 
     * aligned subsequence that has been found. Since a query can be scored
     * only once, the SW algorithm is computed again excluding the
     * already-scored queries.
     */
    public void computeSimilarityAndScore() {
        this.computeSimilarity();
        
        initializeScores();
        CellInfoEditDistance bestEndPoint = getBestEndPoint();
        while(getBestEndPoint().getSimilarity()!=Integer.MIN_VALUE){
            //Score the subsequence
            normalize(bestEndPoint);
            scoreSubsequenceFromEndPoint(bestEndPoint.getRow_index_prev(),bestEndPoint.getColumn_index_prev(),bestEndPoint.getSimilarity());
            //Get the next one
            this.computeSimilarity();
            bestEndPoint = getBestEndPoint();
        }
        
        //printScores(Generics.SCORE_VECTORS_SMITHWATERMAN_FILE);
    }
    
    /**
     * Browses the costMatrix and extracts the ending point of the best subsequence
     * @return CellInfoEditDistance endPoint
     */
    private CellInfoEditDistance getBestEndPoint() {
        CellInfoEditDistance endPoint = new CellInfoEditDistance(Integer.MIN_VALUE, 0, 0, Generics.EditDistanceOperatorType.NOOPERATION, 0);
        
        for(int i=1; i<row; i++){
            //Skip this row if the corresponding query has already been scored
            if(scoredQuery1[i]) continue; 
            for(int j=1; j<column; j++){
                //Skip this column if the corresponding query has already been scored
                if(scoredQuery2[j]) continue; 
                //Search for the highest similarity with a MATCH_MISMATCH operation
                if(costMatrix[i][j].getSimilarity() >= endPoint.getSimilarity() && costMatrix[i][j].getOperator().equals(Generics.EditDistanceOperatorType.MATCH_MISMATCH)) {
                    endPoint = new CellInfoEditDistance(costMatrix[i][j].getSimilarity(), i, j, 
                            Generics.EditDistanceOperatorType.NOOPERATION, costMatrix[i][j].getQuerySimilarity());
                }
            }
        }
        return endPoint;
    }
    
    /**
     * Browse back the subsequence in the costMatrix, starting from the
     * endPoint and scoring each aligned query on the path
     * @param row
     * @param column
     * @param similarity
     */
    private void scoreSubsequenceFromEndPoint(int row, int column, double similarity){
        boolean next = true;
        if(row<=0 || column<= 0){
            System.out.print("");
        }
        AlignmentIndexes ai1 = new AlignmentIndexes(row-1);
        AlignmentIndexes ai2 = new AlignmentIndexes(column-1);
        
        while(next){
            //Exit if the subsequence "overlaps" with a better (and already scored) subsequence
            if(scoredQuery1[row] || (scoredQuery2[column]))
                break;
            
            double score = similarity;
            scoreAlignedQueries(row,column,score);
            setAlignmentForQueries(row,column,score);
            //Browse back the costMatrix
            switch(costMatrix[row][column].getOperator()){
                case MATCH_MISMATCH:
                    row--;
                    column--;
                    break;
                case INSERTION: 
                    row--;
                    break;
                case DELETION: 
                    column--;
                    break;
                case STOP:
                    next = false;
            }
        }
        ai1.setStart(row);
        ai2.setStart(column);
        s1.addAlignmentIndexes(ai1);
        s2.addAlignmentIndexes(ai2);
    }
    
    /**
     * Gives the score to the aligned query/queries
     * @param row
     * @param column
     * @param score 
     */
    private void scoreAlignedQueries(int row, int column, double score){
        switch(costMatrix[row][column].getOperator()){
            case MATCH_MISMATCH:
                this.s1.get(row-1).addScore(score); 
                scoredQuery1[row] = true;
                this.s2.get(column-1).addScore(score); 
                scoredQuery2[column] = true;
                break;
            case INSERTION: //Score goes to the row query only
                this.s1.get(row-1).addScore(score); 
                scoredQuery1[row] = true;
                break;
            case DELETION: //Score goes to the column query only
                this.s2.get(column-1).addScore(score); 
                scoredQuery2[column] = true;
                break;
        }
    }
    
    private void printScores(String fileName) {
        Writer output = null;

        try {
            File file = new File(fileName);
            output = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < row-1; i++) {
                output.write(String.valueOf((double)Math.round(this.s1.get(i).getScore()*10000)/10000) + "\t");
            }
            output.write("\n");
            for (int j = 0; j < column-1; j++) {
                output.write(String.valueOf((double)Math.round(this.s2.get(j).getScore()*1000)/1000) + "\t");
            }
            output.close();
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    private void setAlignmentForQueries(int row, int column,double score) {
        switch(costMatrix[row][column].getOperator()){
            case MATCH_MISMATCH:
                this.s1.get(row-1).addAlignment("Match:S"+s2.id+"_Q"+(column-1)+"_"
                        +String.valueOf((double)Math.round(score*1000)/1000)+"\t"); 
                this.s2.get(column-1).addAlignment("Match:S"+s1.id+"_Q"+(row-1)+"_"
                        +String.valueOf((double)Math.round(score*1000)/1000)+"\t"); 
                break;
            case INSERTION: //Score goes to the row query only
                this.s1.get(row-1).addAlignment("Gap:S"+s2.id+"_Q"+(column-1)+"_"
                        +String.valueOf((double)Math.round(score*1000)/1000)+"\t"); 
                break;
            case DELETION: //Score goes to the column query only
                this.s2.get(column-1).addAlignment("Gap:S"+s1.id+"_Q"+(row-1)+"_"
                        +String.valueOf((double)Math.round(score*1000)/1000)+"\t"); 
                break;
        }
    }
    
    /*
     * OVERRIDDEN METHODS FROM ORIGINAL SMITHWATERMAN
     */
    public CellInfoEditDistance updateFormula(int row, int column) {
        Generics.EditDistanceOperatorType op = Generics.EditDistanceOperatorType.MATCH_MISMATCH;
        double diagonal = -1, left = -1, up = -1, stop = 0, max = -1;
        
        //If the row-query or column-query has been scored, this cell goes to -infinite
        if(scoredQuery1[row] || scoredQuery2[column]){
            return new CellInfoEditDistance(Integer.MIN_VALUE,
                row,
                column,
                op,
                similarityMatrix[row][column]);
        }
        
        if(scoredQuery1[row-1] && scoredQuery2[column-1]){
            diagonal = similarityMatrix[row][column];
        }
        else if(!scoredQuery1[row-1] && scoredQuery2[column-1]){
            diagonal = similarityMatrix[row][column];
            left = computeMaxOnRow(row, column);
        }
        else if(scoredQuery1[row-1] && !scoredQuery2[column-1]){
            diagonal = similarityMatrix[row][column];
            up = computeMaxOnColumn(row, column);
        }
        else if(!scoredQuery1[row-1] && !scoredQuery2[column-1]){
            diagonal = (double) costMatrix[row - 1][column - 1].getSimilarity() + similarityMatrix[row][column];
            left = computeMaxOnRow(row, column);
            up = computeMaxOnColumn(row, column);
        }

        if (diagonal >= max) {
            op = Generics.EditDistanceOperatorType.MATCH_MISMATCH;
            max = diagonal;
        }
        if (left > max) {
            op = Generics.EditDistanceOperatorType.INSERTION;
            max = left;
        }
        if (up > max) {
            op = Generics.EditDistanceOperatorType.DELETION;
            max = up;
        }
        if (stop > max) {
            op = Generics.EditDistanceOperatorType.STOP;
            max = stop;
        }


        return new CellInfoEditDistance(max,
                row,
                column,
                op,
                similarityMatrix[row][column]);
    }
    
    /**
     * VERTICAL
     * Keeps the column fixed and looks for the max value changing the rows
     */
    public double computeMaxOnRow(int i, int j) {
        double max = -Float.MIN_VALUE;
        for (int k = i; k > 0; k--) {
            if(scoredQuery1[k]) return max;
            double h = (double) (costMatrix[i - k][j].getSimilarity() + getPenalityGap(k));
            if (h > max) {
                max = h;
            }
        }
        return max;
    }

    /**
     * HORIZONTAL
     * Keeps the row fixed and looks for the max value changing the columns
     */
    public double computeMaxOnColumn(int i, int j) {
        double max = -Float.MIN_VALUE;
        for (int k = j; k > 0; k--) {
            if(scoredQuery2[k]) return max;
            double h = (double) (costMatrix[i][j - k].getSimilarity() + getPenalityGap(k));
            if (h > max) {
                max = h;
            }
        }
        return max;
    }
    
    /*
     * ORIGINAL METHODS OF SMITHWATERMAN
     */
    public Similarity computeSimilarity() {
        CellInfoEditDistance ci = new CellInfoEditDistance(Integer.MIN_VALUE, 0, 0, Generics.EditDistanceOperatorType.NOOPERATION, 0);

        int i, j = 0;
        //Initialize matrix cost
        for (i = 0; i < row; i++) // initialize the first column
        {
            costMatrix[i][0] = new CellInfoEditDistance(0, i, 0, Generics.EditDistanceOperatorType.STOP, similarityMatrix[i][0]);
        }
        //costMatrix[i][0]=0;
        for (j = 0; j < column; j++) // initialize the first row
        //costMatrix[0][j]=0;
        {
            costMatrix[0][j] = new CellInfoEditDistance(0, 0, j, Generics.EditDistanceOperatorType.STOP, similarityMatrix[0][j]);
        }
        for (i = 1; i < row; i++)// for each row
        {
            for (j = 1; j < column; j++) // for each column
            {
                /*costMatrix[i][j]=Math.max(
                 costMatrix[i-1][j-1] + similarityMatrix[i][j],
                 Math.max(computeMaxOnRow(i,j),
                 Math.max(computeMaxOnColumn(i,j),0)));*/
                costMatrix[i][j] = updateFormula(i, j);
                if (costMatrix[i][j].getSimilarity() >= ci.getSimilarity()) {
                    ci = new CellInfoEditDistance(costMatrix[i][j].getSimilarity(), i, j, Generics.EditDistanceOperatorType.NOOPERATION, similarityMatrix[i][j]);
                }

            }
        }
        //printMatrix(Generics.MATRIX_SMITHWATERMAN_FILE);
        //printMatrixLight(Generics.MATRIX_SMITHWATERMAN_FILE);


        //Normalization
        normalize(ci);
        return ci;
    }

    protected Similarity normalize(CellInfoEditDistance ci) {
        double sumRho = 0;
        int minLength = Math.min(this.s1.size(), this.s2.size());
        for (int k = 0; k < minLength; k++) {
            sumRho += this.matrix.increasing_function(k, k);
        }
        //System.out.println(ci.getSimilarity() + " " + sumRho);
        double normalizedSimilarity = ci.getSimilarity() / ((1.0 - thre) * sumRho);

        //System.out.println(normalizedSimilarity);
        ci.setSimilarity(normalizedSimilarity);

        return ci;

    }

    private void printMatrix(String fileName) {
        Writer output = null;

        try {
            File file = new File(fileName);
            output = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    output.write(String.valueOf(costMatrix[i][j].getQuerySimilarity()) + " " + String.valueOf(costMatrix[i][j].getSimilarity()) + " " + costMatrix[i][j].getOperator() + ", ");
                }
                //output.write(d[i][j]+", ");
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            System.out.print(e);
        }
    }
    
    private void printMatrixLight(String fileName) {
        Writer output = null;

        try {
            File file = new File(fileName);
            output = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    output.write(String.valueOf((double)Math.round(costMatrix[i][j].getQuerySimilarity()*10000)/10000) 
                            + " " + String.valueOf((double)Math.round(costMatrix[i][j].getSimilarity()*10000)/10000) 
                            + " " + costMatrix[i][j].getOperator().toString().substring(0, 3) + "\t");
                }
                //output.write(d[i][j]+", ");
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    public double getPenalityGap(int numberOfGap) {
        //Affine penality gap
        return (double) (openingGapCost + (double) extendingGapCost * numberOfGap);
    }

}
