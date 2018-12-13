/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.searching;

import java.util.HashSet;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;

/**
 *
 * @author Salim Igue
 */
public interface I_search {
    
    
    QuerySession sessionGenerationLogSearch(HashSet<QuerySession> question);
    
    
}
