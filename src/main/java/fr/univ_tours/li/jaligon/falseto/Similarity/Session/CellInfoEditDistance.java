/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.univ_tours.li.jaligon.falseto.Similarity.Session;

import fr.univ_tours.li.jaligon.falseto.Generics.Generics.EditDistanceOperatorType;



/**
 *
 * @author Elisa
 */
public class CellInfoEditDistance extends CellInfo{
    
    private EditDistanceOperatorType operator;
    
    private int from_row_index;
    private int from_column_index;

    public CellInfoEditDistance(double similarity, int row_index, int column_index, EditDistanceOperatorType op,double querySimilarity)
    {
        super(similarity, row_index, column_index, querySimilarity);
        this.operator=op;
    }

    public CellInfoEditDistance()
    {
        super();
        this.operator=EditDistanceOperatorType.NOOPERATION;
        
    }

    public int getFrom_row_index() {
        return from_row_index;
    }

    public int getFrom_column_index() {
        return from_column_index;
    }

    public void setFrom_row_index(int from_row_index) {
        this.from_row_index = from_row_index;
    }

    public void setFrom_column_index(int from_column_index) {
        this.from_column_index = from_column_index;
    }
    
    
    
    

    @Override
    public CellInfoEditDistance copy()
    {
        return new CellInfoEditDistance(this.getSimilarity(), this.getRow_index_prev(), this.getColumn_index_prev(), this.operator, this.getSimilarity());
    }

    public EditDistanceOperatorType getOperator() {
        return operator;
    }

    public void setOperator(EditDistanceOperatorType operator) {
        this.operator = operator;
    }
}
