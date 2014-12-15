/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

/**
 *
 * @author anu
 */
public class game_db {
    private int ID;
    private int p_id;
    private double distance;
    private int dominatedbycategory;
    private int dominatedbypool;
    private int rank;
    private String myorder;
    private int chosen;
    private int liked;
    private int objscore;
    private int prefscore;

    public game_db() {
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getP_id() {
        return p_id;
    }

    public void setP_id(int p_id) {
        this.p_id = p_id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getDominatedbycategory() {
        return dominatedbycategory;
    }

    public void setDominatedbycategory(int dominatedbycategory) {
        this.dominatedbycategory = dominatedbycategory;
    }

    public int getDominatedbypool() {
        return dominatedbypool;
    }

    public void setDominatedbypool(int dominatedbypool) {
        this.dominatedbypool = dominatedbypool;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getMyorder() {
        return myorder;
    }

    public void setMyorder(String myorder) {
        this.myorder = myorder;
    }

    public int getChosen() {
        return chosen;
    }

    public void setChosen(int chosen) {
        this.chosen = chosen;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    public int getObjscore() {
        return objscore;
    }

    public void setObjscore(int objscore) {
        this.objscore = objscore;
    }

    public int getPrefscore() {
        return prefscore;
    }

    public void setPrefscore(int prefscore) {
        this.prefscore = prefscore;
    }
    
}
