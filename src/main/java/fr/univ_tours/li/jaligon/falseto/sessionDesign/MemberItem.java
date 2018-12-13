/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.univ_tours.li.jaligon.falseto.sessionDesign;

import mondrian.olap.Member;

/**
 *
 * @author julien
 */
public class MemberItem {

    private Member m;

    public MemberItem(Member m) {
        this.m = m;
    }

    @Override
    public String toString() {
        return m.getName();

    }

    public Member getMember() {
        return m;
    }
}
