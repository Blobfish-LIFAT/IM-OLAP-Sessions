/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.SUT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import fr.univ_tours.li.jaligon.falseto.QueryStructure.QuerySession;
import fr.univ_tours.li.jaligon.falseto.Recommendation.ASRA;

/**
 *
 * @author 21308124t
 */
public class FalsetoSUT implements I_SUT{
/**
 * The set of QuerySession included in our log
 */    
HashSet<QuerySession> log;
/**
 * The set of QuerySession given by the user
 */
HashSet<QuerySession> question ;

    public FalsetoSUT(){
        this.question = new HashSet<>();
        this.log = new HashSet<>();
    }

    public FalsetoSUT(HashSet<QuerySession> question,HashSet<QuerySession> log) {
        this.question = question;
        this.log = log;
    }

    public HashSet<QuerySession> getLog() {
        return log;
    }

    public void setLog(HashSet<QuerySession> log) {
        this.log = log;
    }

    public HashSet<QuerySession> getQuestion() {
        return question;
    }

    public void setQuestion(HashSet<QuerySession> question) {
        this.question = question;
    }
   

    
    
    
    
@Override
    public QuerySession sessionGenerationRecommendationProposal() {
         //First we are going to check if the set given by the user is empty or not
        if (question.isEmpty()) return this.sessionGenerationRecommendationFirst();
         
         
         
       HashSet<QuerySession> logWithoutRandomSession = new HashSet<>(log);
       QuerySession result = new QuerySession("synthetic");

        List<QuerySession> questionList = new ArrayList<>(question);
        Random r = new Random();
        
        QuerySession randomSession = questionList.get(r.nextInt(questionList.size()));
      
        result.addAll(randomSession.getQueries());
        logWithoutRandomSession.remove(randomSession);

        ASRA recoSys = new ASRA(logWithoutRandomSession, result.getQueries());
        QuerySession reco = null;
        try {
            reco = recoSys.computeASRA();
        } catch (OutOfMemoryError e) {
            reco = null;
        } catch (Exception e) {
            reco = null;
        }


        if (reco != null && !reco.getQueries().isEmpty()) {
            result.addAll(reco.getQueries());
        }

        return result;
    }
    
    
    private QuerySession sessionGenerationRecommendationFirst() {
         HashSet<QuerySession> logWithoutRandomSession = new HashSet<>(log);

        QuerySession result = new QuerySession("synthetic");

        Iterator<QuerySession> it = log.iterator();
        QuerySession randomSession = it.next();

        result.addAll(randomSession.getQueries());
         logWithoutRandomSession.remove(randomSession);

        ASRA recoSys = new ASRA(logWithoutRandomSession, result.getQueries());
        QuerySession reco = null;
        try {
            reco = recoSys.computeASRA();
        } catch (OutOfMemoryError e) {
            reco = null;
        } catch (Exception e) {
            reco = null;
        }

        if (reco != null && !reco.getQueries().isEmpty()) {
            result.addAll(reco.getQueries());
        }

        return result;
    }
        
    }
    

