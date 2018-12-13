/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import fr.univ_tours.li.jaligon.falseto.Similarity.Query.Similarity;

/**
 *
 * @author Julien
 */
public class CellInfo extends Similarity{
    
    private int row_index_prev;
    private int column_index_prev;
    private double querySimilarity;
    
    public CellInfo(double similarity, int row_index, int column_index, double querySimilarity)
    {
        super(similarity);
        this.row_index_prev=row_index;
        this.column_index_prev=column_index;
        this.querySimilarity=querySimilarity;
    }

    public CellInfo()
    {
        super();
        this.row_index_prev=-1;
        this.column_index_prev=-1;
        this.querySimilarity=-1;
    }

    public CellInfo copy()
    {
        return new CellInfo(this.querySimilarity, this.row_index_prev, this.column_index_prev, this.similarity);
    }

    public double getQuerySimilarity() {
        return querySimilarity;
    }

    public void setQuerySimilarity(double cost) {
        this.querySimilarity = cost;
    }

        public int getColumn_index_prev() {
        return column_index_prev;
    }

    public void setColumn_index_prev(int column_index_prev) {
        this.column_index_prev = column_index_prev;
    }

    public int getRow_index_prev() {
        return row_index_prev;
    }

    public void setRow_index_prev(int row_index_prev) {
        this.row_index_prev = row_index_prev;
    }

    public double getSimilarity() {
        return similarity;
    }
    public void setSimilarity(double sim) {
        similarity=sim;
    }
    
}
