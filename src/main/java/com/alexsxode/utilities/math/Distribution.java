package com.alexsxode.utilities.math;



import com.alexsxode.utilities.collection.MultiSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Discrete probability distribution over a Set X
 * @param <T> the type of x € X
 */
public class Distribution<T> {
    private HashMap<T, Double> map;

    public Distribution() {
        map = new HashMap<>();
    }

    public Distribution(MultiSet<T> from){
        map = new HashMap<>();
        for(T el: from){
            map.put(el, from.count(el)/(double)from.size());
        }
    }

    public boolean isProba(Double epsilon){
        return Math.abs(1 - sumOver()) <= epsilon;
    }

    public boolean isProba(){
        return isProba(10e-20);
    }

    private double sumOver(){
        return map.entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
    }

    public void setProba(T item, double probability){
        map.put(item, probability);
    }

    public double getProba(T item){
        return map.getOrDefault(item, 0.0);
    }

    public Set<T> getSet(){
        return map.keySet();
    }

    public static <E> Distribution<E> average(Distribution<E> a, Distribution<E> b){
        Set<E> universe = a.map.keySet();
        universe.addAll(b.map.keySet());

        Distribution<E> avg = new Distribution<>();
        for (E e : universe){
            avg.setProba(e, 0.5*(a.getProba(e)+b.getProba(e)));
        }

        return avg;
    }

}
