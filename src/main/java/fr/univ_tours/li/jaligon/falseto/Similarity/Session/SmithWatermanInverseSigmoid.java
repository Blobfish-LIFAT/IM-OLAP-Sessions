/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import fr.univ_tours.li.jaligon.falseto.Generics.Generics;
import fr.univ_tours.li.jaligon.falseto.Generics.Generics.EditDistanceOperatorType;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.Qfset;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Similarity.Query.Similarity;

/**
 *
 * @author Elisa
 */
public class SmithWatermanInverseSigmoid extends SessionComparison {
    
    private double[][] similarityMatrix;
    private MatrixInverseSigmoid matrix;
    //private double[][] costMatrix;
    protected CellInfoEditDistance[][] costMatrix;
    protected double openingGapCost;
    protected double extendingGapCost;
    private double thre;
    
    
    public SmithWatermanInverseSigmoid(MatrixInverseSigmoid similarityMatrix, double openingGapcost, double extendingGapCost, QuerySession s1, QuerySession s2, double thre, double alpha, double beta, double gamma) {
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
    }
    
    public Similarity computeSimilarity() {
        int columnIndex = 0;
        int rowIndex = 0;
        
        CellInfoEditDistance ci = new CellInfoEditDistance(Integer.MIN_VALUE, 0, 0, EditDistanceOperatorType.NOOPERATION, 0);
        
        int i, j = 0;
        //Initialize matrix cost
        for (i = 0; i < row; i++) // initialize the first column
        {
            costMatrix[i][0] = new CellInfoEditDistance(0, i, 0, EditDistanceOperatorType.STOP, similarityMatrix[i][0]);
        }
        //costMatrix[i][0]=0;
        for (j = 0; j < column; j++) // initialize the first row
        //costMatrix[0][j]=0;
        {
            costMatrix[0][j] = new CellInfoEditDistance(0, 0, j, EditDistanceOperatorType.STOP, similarityMatrix[0][j]);
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
                    columnIndex = j;
                    rowIndex = i;
                    ci = new CellInfoEditDistance(costMatrix[i][j].getSimilarity(), i, j, EditDistanceOperatorType.NOOPERATION, similarityMatrix[i][j]);
                }
                
            }
        }
        //printMatrix(Generics.MATRIX_SMITHWATERMAN_FILE);

        //System.out.print("Column Index : " + columnIndex+" ");
        //System.out.print("Row Index : " + rowIndex);

        //Normalization
        normalize(ci);
        return ci;
    }
    
    private Similarity normalize(CellInfoEditDistance ci) {
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
                    output.write(String.valueOf(costMatrix[i][j].getQuerySimilarity()) + " " + String.valueOf(costMatrix[i][j].getSimilarity()) + " " + costMatrix[i][j].getOperator() + "\t ");
                }
                //output.write(d[i][j]+", ");
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            System.out.print(e);
        }
    }
    
    public CellInfoEditDistance updateFormula(int row, int column) {
        //CellInfo ci=new CellInfo();
        double diagonal = (double) costMatrix[row - 1][column - 1].getSimilarity() + similarityMatrix[row][column];
        double left = computeMaxOnRow(row, column);
        double up = computeMaxOnColumn(row, column);
        double stop = 0;
        EditDistanceOperatorType op = EditDistanceOperatorType.MATCH_MISMATCH;
        double max = -1;
        
        int fromRow = 0;
        int fromColumn = 0;
        
        if (diagonal >= max) {
            op = EditDistanceOperatorType.MATCH_MISMATCH;
            max = diagonal;
            fromRow = row - 1;
            fromColumn = column - 1;
        }
        if (left > max) {
            op = EditDistanceOperatorType.INSERTION;
            max = left;
            fromRow = row;
            fromColumn = column - 1;
        }
        if (up > max) {
            op = EditDistanceOperatorType.DELETION;
            max = up;
            fromRow = row - 1;
            fromColumn = column;
        }
        if (stop > max) {
            op = EditDistanceOperatorType.STOP;
            max = stop;
        }
        
        CellInfoEditDistance c = new CellInfoEditDistance(max,
                row,
                column,
                op,
                similarityMatrix[row][column]);
        
        c.setFrom_column_index(fromColumn);
        c.setFrom_row_index(fromRow);
        
        return c;
    }
    
    public double getPenalityGap(int numberOfGap) {
        //Affine penality gap
        return (double) (openingGapCost + (double) extendingGapCost * numberOfGap);
    }
    
    public double computeMaxOnRow(int i, int j) {
        double max = -Float.MIN_VALUE;
        for (int k = i; k > 0; k--) {
            double h = (double) (costMatrix[i - k][j].getSimilarity() + getPenalityGap(k));
            if (h > max) {
                max = h;
            }
        }
        return max;
    }
    
    public double computeMaxOnColumn(int i, int j) {
        double max = -Float.MIN_VALUE;
        for (int k = j; k > 0; k--) {
            double h = (double) (costMatrix[i][j - k].getSimilarity() + getPenalityGap(k));
            if (h > max) {
                max = h;
            }
        }
        return max;
    }
    
    public ArrayList<ArrayList<Qfset>> getAlignment() {
        CellInfoEditDistance maxCi = new CellInfoEditDistance(Integer.MIN_VALUE, 0, 0, EditDistanceOperatorType.NOOPERATION, 0);
        
        for (int i = 0; i < row; i++)// for each row
        {
            for (int j = 0; j < column; j++) // for each column
            {
                if (costMatrix[i][j].getSimilarity() > maxCi.getSimilarity()) {
                    maxCi = costMatrix[i][j];
                }
            }
        }
        
        /*for (int i = 0; i < row; i++)// for each row
        {
            for (int j = 0; j < column; j++) // for each column
            {
                System.out.print(costMatrix[i][j].getOperator().name().charAt(0) +" ");
            }
            System.out.println();
        }*/
        
        ArrayList<CellInfoEditDistance> operationList = new ArrayList<CellInfoEditDistance>();
        operationList.add(maxCi.copy());
        
        double sim = maxCi.getSimilarity();
        
        //System.out.println(maxCi.getFrom_row_index()+" "+maxCi.getFrom_column_index());
        
        while (maxCi.getOperator() != EditDistanceOperatorType.STOP) {
            maxCi = costMatrix[maxCi.getFrom_row_index()][maxCi.getFrom_column_index()];
            operationList.add(0, maxCi.copy());
            sim = maxCi.getSimilarity();
        }
        
        ArrayList<Qfset> session1 = new ArrayList<Qfset>();
        ArrayList<Qfset> session2 = new ArrayList<Qfset>();
        
        for (CellInfoEditDistance cied : operationList) {
            //System.out.println(cied.getRow_index_prev()+" "+cied.getColumn_index_prev());
            
            if (cied.getOperator() == EditDistanceOperatorType.MATCH_MISMATCH) {
                session1.add(s1.get(cied.getRow_index_prev() - 1));
                session2.add(s2.get(cied.getColumn_index_prev() - 1));
                //System.out.println(EditDistanceOperatorType.MATCH_MISMATCH);
            } else if (cied.getOperator() == EditDistanceOperatorType.DELETION) {
                session1.add(s1.get(cied.getRow_index_prev() - 1));
                session2.add(null);
                //System.out.println(EditDistanceOperatorType.DELETION);
            } else if (cied.getOperator() == EditDistanceOperatorType.INSERTION) {
                session1.add(null);
                session2.add(s2.get(cied.getColumn_index_prev() - 1));
                //System.out.println(EditDistanceOperatorType.INSERTION);
            }
            
        }
        
        //System.out.println("s1 : "+s1.size());
        //System.out.println(s1);
        //System.out.println("s2 : "+s2.size());
        //System.out.println(s2);
        
        if (session1.size() < session2.size()) {
            for (int i = 0; i < (session2.size() - session1.size()); i++) {
                session1.add(null);
            }  
        }
        else if (session2.size() < session1.size()) {
            for (int i = 0; i < (session1.size() - session2.size()); i++) {
                session2.add(null);
            }  
        }
        
        ArrayList<ArrayList<Qfset>> result = new ArrayList<>();
        result.add(session1);
        result.add(session2);
        
        return result;
    }
    
    /**
     * DISCARD_NOT_ALIGNED_TO_CURRENT_QUERY: if the log session is not aligned to the current query, discard it
     * IGNORE_ALIGNMENT_TO_CURRENT_QUERY: don't care if the log session is aligned to the current query
     * FORCE_ALIGNMENT_TO_CURRENT_QUERY: tricky - though not yet tested: if the log session is not aligned to the 
     * current query, look in the similarity matrix for a not-optimal alignment of the log session to the current
     * query (or to the "tolerance area"); if it exist, consider the log session aligned to the current query
     * (or to the "tolerance area"), otherwise discard it.
     */
    public static enum ALIGNMENT_TYPE {
        DISCARD_NOT_ALIGNED_TO_CURRENT_QUERY, FORCE_ALIGNMENT_TO_CURRENT_QUERY, IGNORE_ALIGNMENT_TO_CURRENT_QUERY
    }

    /** 
     * Tolerance to be adopted upon verification of the alignment of the log session to the current query.
     * The percentage is relative to the length of the current session.
     */
    public static final double TOLERANCE_PERCENTAGE = 0.1; //Between 0 and 1
    
    /**
     * Return the future of the log queries aligned to the current session
     * @param alignmentType Method used to decide if and when a log session is to be considered as "aligned" 
     * @return The future of the aligned log queries
     */
    public QuerySession getFutureLogQueries (ALIGNMENT_TYPE alignmentType) {
        CellInfoEditDistance maxCi = new CellInfoEditDistance(0, 0, 0, EditDistanceOperatorType.NOOPERATION, 0);
        QuerySession futureLogQueries = new QuerySession(s2.id); // Assuming s2 is the log session
        boolean matchFound = false;
        
        for (int i = 1; i < row; i++)// for each row
        {
            for (int j = 1; j < column; j++) // for each column
            {
                if (costMatrix[i][j].getSimilarity() > maxCi.getSimilarity()) {
                    maxCi = costMatrix[i][j];
                    matchFound = true;
                }
            }
        }
        
        if(matchFound){
            int minCurrentQueryToBeAlignedTo = (int)Math.ceil(s1.size()*(1-TOLERANCE_PERCENTAGE))-1;
            switch(alignmentType){
                case FORCE_ALIGNMENT_TO_CURRENT_QUERY:
                    if(maxCi.getFrom_row_index()<minCurrentQueryToBeAlignedTo){ //If maxCi too far from CQ
                        for(int i = maxCi.getRow_index_prev()+1; i<=minCurrentQueryToBeAlignedTo+1; i++){ //Run dowm the column, towards (but not further) the start of the CQ-proximity area
                            if(costMatrix[i][maxCi.getColumn_index_prev()].getQuerySimilarity() > 0){ //If next-maxCi is still positive, keep going
                                maxCi = costMatrix[i][maxCi.getColumn_index_prev()];
                            }
                            else break;
                        }
                    }
                case DISCARD_NOT_ALIGNED_TO_CURRENT_QUERY:
                    if(maxCi.getFrom_row_index()<minCurrentQueryToBeAlignedTo) break;
                case IGNORE_ALIGNMENT_TO_CURRENT_QUERY: 
                    futureLogQueries = (QuerySession) s2.extractSubsequence(maxCi.getFrom_column_index()+1);
                    break;

            }
        }
        return futureLogQueries;
        
    }
    
}
