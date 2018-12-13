/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.Generics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import mondrian.olap.Dimension;
import mondrian.olap.Hierarchy;

/**
 *
 * @author Elisa
 */
public class Generics {

    public enum QueryComparisonType {

        EDITDISTANCE, BY_STRUCTURE, BY_STRUCTURE_PARAMETER, BY_JACCARD_STRUCTURE, BY_JACCARD_STRUCTURE_THRESHOLD, BY_LOG, BY_INCLUSION, BY_EQUALITY, BY_ATTRIBUTE;
    }

    public enum SessionSimilarityType {

        DICE, EDITDISTANCE, HAMMING, TFIDF, SMITHWATERMAN_AVGMATCH, SSTI_NOT_SYMMETRIC, SSTI_SYMMETRIC
    }

    public enum AggregateFunction {

        AVG, SUM, MIN, MAX, COUNT
    }

    public enum SelectionCriteriaOperator {

        EQUALITY, GREATER, LOWER
    }

    public enum EditDistanceOperatorType {

        INSERTION, DELETION, SUBSTITUTION, TRANSPOSITION, NOOPERATION, STOP, MATCH_MISMATCH
    }

    public enum Directions {

        STOP, LEFT, DIAGONAL, UP
    }

    public enum MatrixType {

        MATCH_MISMATCH, SIMILARITY_MATRIX, EXAMPLE, MATCH_THRESHOLD, MATCH_THRESHOLD_INVERSE
    }

    public enum PatternType {

        PARALLEL, CROSSED, SAMEINITIAL_DIFFERENTFINAL, DIFFERENTINITIAL_SAMEFINAL, INVERSE, OLAP_SEPARATION
    }

    public static void printMatrix(String fileName, double[][] matrixToPrint, int numRow, int numColumn) {
        Writer output;

        try {
            File file = new File(fileName);
            output = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < numRow; i++) {
                for (int j = 0; j < numColumn; j++) {
                    output.write(String.valueOf(matrixToPrint[i][j]) + ", ");
                }
                //output.write(d[i][j]+", ");
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    /**
     * Found the hierarchy of the cube.
     */
    public static HashSet<Hierarchy> getHierarchies() {
        Dimension[] cubeDims = Connection.getCube().getDimensions();

        for (int i = 0; i < cubeDims.length; i++) {
            Hierarchy[] tmpH = cubeDims[i].getHierarchies();
            hierachies.addAll(Arrays.asList(tmpH));
        }

        return hierachies;
    }
    public static String MEASURES_DIMENSION = "MEASURES";
    public static String MEASURES_LEVEL = "MeasuresLevel";
    public static String CONNECTION_PROPERTIES = "olapConnection.properties";
    public static String CONNECTION_PROPERTIES_STUDENTTEST = "olapConnectionStudentTest.properties";
    public static String MATRIX_EDITDISTANCE_FILE = "matrixEditDistance.txt";
    public static String MATRIX_SMITHWATERMAN_FILE = "matrixSmithWaterman.txt";
    public static String MATRIX_SMITHWATERMAN_INCREASE_FILE = "matrixSmithWatermanIncrease.txt";
    public static String MATRIX_SIMILARITY_FILE = "matrixSimilarity.txt";
    public static String MATRIX_COST_FILE = "matrix_cost.txt";
    public static String MATRIX_CLUSTER = "cluster_matrix.txt";
    public static String webPath = "/Users/julien/Documents/Netbeans Projects/WebApplicationSimilarity/web/";
    //public static String webPath = "D:/users/aligon_j/Documents/WebApplicationSimilarity/web/";
    //public static String webPath = "C:/Users/Julien/Documents/NetBeansProjects/WebApplicationSimilarity/web/";
    private static HashSet<Hierarchy> hierachies = new HashSet<Hierarchy>();

    public static String date() {
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        return format.format(new Date());
    }
}
