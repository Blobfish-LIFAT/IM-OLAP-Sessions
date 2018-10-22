package fr.univ_tours.info.im_olap.graph;

import com.google.common.io.Files;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;


enum QueryPartType {
    MEASURE,
    DIMENSION,
    FILTER,
    UNKNOWN
}

enum BlockType {
    TABLE,
    CHART
}

public class WebiQueryPart implements Comparable<WebiQueryPart> {

    public final String parent;
    public final int reportID;
    public final int blockID;
    public final BlockType blockType;
    public final String blockViz;
    public final String objectName;
    public final String objectValue;
    public final String universe;
    public final String objectID;
    public final QueryPartType qualification;

    public static ArrayList<String> removed;

    public static HashMap<String, INDArray> meanTopicByObjectname(){
        return null;
    }


    public static NOGraph<Double, String> makeGraph(List<WebiQueryPart> queryParts){
    	// Hashmaps for fast lookups
        HashMap<String, Set<WebiQueryPart>> blockIdMap = new HashMap<>(); // object id to list of queries in the document

        for (WebiQueryPart webiQueryPart : queryParts){
            blockIdMap.compute(webiQueryPart.uniqueBlockID(), (k,v) -> {
                if (k == null || v == null){
                    v = new HashSet<>();
                }
                v.add(webiQueryPart);
                return v;
            });
        }

        NOGraph<Double, String> graph = new NOGraph<>();

        AtomicInteger progression = new AtomicInteger(0);
        ArrayList<WebiQueryPart> buffer = new ArrayList<>();
        for (Entry<String, Set<WebiQueryPart>> entry : blockIdMap.entrySet()){
             buffer.clear();
             Set<WebiQueryPart> queriesInBlock = entry.getValue();
             buffer.addAll(queriesInBlock); // store bloc elements in an arraylist to allow their indexing

             // cartesian product of elements of the bloc without pair duplicates
             for (int i = 0; i < buffer.size(); i++) 
                 for (int j = i; j < buffer.size(); j++) 
                     graph.safeComputeEdge(buffer.get(i).objectName, buffer.get(j).objectName, x -> {
                         return Optional.of(x.map(y -> y+1).orElse(1.0));
                     });
             progression.incrementAndGet();
        }

        // remove queryparts appearing alone in a bloc

        removed = new ArrayList<>();
        /*
        for ( String obName : graph.getNodes() ){
        	List<CPair<String,Double>> pairs = graph.fromNode(obName);
        	if (pairs.size() == 1){
        		graph.deleteNodeAndItsEdges(obName);
        		removed.add(obName);
        	}
        }

        System.out.println("Removed " + removed.size() + " : "+ removed);
        */
        return graph;
    }

    public static HashMap<String,INDArray> readLDAResults(Path filePath) throws FileNotFoundException, IOException {
        BufferedReader bufferedReader = Files.newReader(filePath.toFile(), StandardCharsets.UTF_8);

        HashMap<String, INDArray> hashMap = new HashMap<>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null){
            String[] splitted = line.split(",");
            double[] vals = new double[splitted.length-1];
            for (int i = 0; i < vals.length; i++) {
                double parsed = Double.parseDouble(splitted[i+1]);
                vals[i] = parsed;
            }
            INDArray vec = Nd4j.create(vals);
            hashMap.put(splitted[0]/*.trim()*/, vec);
        }

        return hashMap;
    }
    
    public WebiQueryPart(String parent, int reportID, int blockID, BlockType blockType, String blockViz, String objectName, String objectValue, String universe, String objectID, QueryPartType qualification) {
        this.parent = parent;
        this.reportID = reportID;
        this.blockID = blockID;
        this.blockType = blockType;
        this.blockViz = blockViz;
        this.objectName = objectName;
        this.objectValue = objectValue;
        this.universe = universe;
        this.objectID = objectID;
        this.qualification = qualification;
    }

    public String strippedObjectName(){
        return this.objectName.trim();
    }

    public String uniqueBlockID(){
        return parent+";"+ String.valueOf(reportID)+";"+ String.valueOf(blockID);
    }

    public String uniqueReportId(){
        return parent+";"+ String.valueOf(reportID);
    }

    public static WebiQueryPart queryFromCSV(String s){
        String[] sp = s.split(";");

        return new WebiQueryPart(
                sp[0],
                Integer.valueOf(sp[1]),
                Integer.valueOf(sp[2]),
                sp[3].equals("TABLE") ? BlockType.TABLE : BlockType.CHART,
                sp[4].equals("null") ? "" : sp[4],
                sp[5].equals("null") ? "" : sp[5],
                sp[6].equals("null") ? "" : sp[6],
                sp[7].equals("null") ? "" : sp[7],
                sp[8].equals("null") ? "" : sp[8],
                sp[9].equals("MEASURE") ? QueryPartType.MEASURE :
                    sp[9].equals("DIMENSION") ? QueryPartType.DIMENSION :
                    sp[9].equals("FILTER") ? QueryPartType.FILTER :
                    QueryPartType.UNKNOWN
        );
    }

    @Override
    public String toString() {
        return "WebiQueryPart{" +
                "parent='" + parent + '\'' +
                ", reportID=" + reportID +
                ", blockID=" + blockID +
                ", blockType=" + blockType +
                ", blockViz='" + blockViz + '\'' +
                ", objectName='" + objectName + '\'' +
                ", objectValue='" + objectValue + '\'' +
                ", universe='" + universe + '\'' +
                ", objectID='" + objectID + '\'' +
                ", qualification=" + qualification +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebiQueryPart queryPart = (WebiQueryPart) o;
        return reportID == queryPart.reportID &&
                blockID == queryPart.blockID &&
                Objects.equals(parent, queryPart.parent) &&
                blockType == queryPart.blockType &&
                Objects.equals(blockViz, queryPart.blockViz) &&
                Objects.equals(objectName, queryPart.objectName) &&
                Objects.equals(objectValue, queryPart.objectValue) &&
                Objects.equals(universe, queryPart.universe) &&
                Objects.equals(objectID, queryPart.objectID) &&
                qualification == queryPart.qualification;
    }

    @Override
    public int hashCode() {

        return Objects.hash(parent, reportID, blockID, blockType, blockViz, objectName, objectValue, universe, objectID, qualification);
    }

    @Override
    public int compareTo(WebiQueryPart o) {
        int comp = parent.compareTo(o.parent);
        if (comp != 0){
            return comp;
        }
        else if ((comp = reportID - o.reportID) != 0){
            return comp;
        }
        else if ((comp = blockID - o.blockID) != 0){
            return comp;
        }
        else if ((comp = blockType.compareTo(o.blockType)) != 0){
            return comp;
        }
        else if ((comp = blockViz.compareTo(o.blockViz)) != 0){
            return comp;
        }
        else if ((comp = objectName.compareTo(o.objectName)) != 0){
            return comp;
        }
        else if ((comp = objectValue.compareTo(o.objectValue)) != 0){
            return comp;
        }
        else if ((comp = universe.compareTo(o.universe)) != 0){
            return comp;
        }
        else {
            return qualification.compareTo(o.qualification);
        }

    }
}
