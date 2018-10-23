package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.model.LoadSessions;
import fr.univ_tours.info.im_olap.model.Session;

import java.util.List;

public class Dev {
    public static void main(String[] args) {
        List<Session> sessions = LoadSessions.loadFromDir("data/session_set_1");
        System.out.println(sessions.size());
    }
}
