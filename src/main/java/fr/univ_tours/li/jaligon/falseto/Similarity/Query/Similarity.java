/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Similarity.Query;

/**
 *
 * @author Julien
 */
public abstract class Similarity {

    protected double similarity;

    public Similarity(double similarity) {
        this.similarity = similarity;
    }

    public Similarity(){
        this.similarity=-1;
    }
    public abstract double getSimilarity();
}