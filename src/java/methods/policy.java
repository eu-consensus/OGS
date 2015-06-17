/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author anu
 */
public class policy {

    private Hashtable<String, String> myhash = new Hashtable<>();
    private static int counter = 0;
    private String policyName;
    private double[] objectives;
    private String[] policyParameters;
    private int score;
    private int dominated;
    private double distance;
    private int dominatedbycategory;
    private String order;
    private String Si;
    private String SiR;
    private int rank;
    private int ni;
    private int ID;

    public policy(int number, int number2) {
        this.objectives = new double[number];
        this.score = 0;
        this.dominated = 0;
        this.distance = 0;
        this.dominatedbycategory = 0;
        this.order = "";
        this.Si = "";
        this.SiR = "";
        this.rank = 0;
        this.ni = 0;
        this.policyParameters = new String[number2];
        this.policyName = "";
        this.ID = 0;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String[] getPolicyParameters() {
        return policyParameters;
    }

    public void setPolicyParameters(String[] policyParameters) {
        this.policyParameters = policyParameters;
    }

    public String getSiR() {
        return SiR;
    }

    public void setSiR(String SiR) {
        if (this.SiR.equals("")) {
            this.SiR = SiR;
        } else {
            this.SiR = SiR + " , " + this.SiR;
        }
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public String getSi() {
        return Si;
    }

    public void setSi(String Si) {
        if (this.Si.equals("")) {
            this.Si = Si;
        } else {
            this.Si = Si + " , " + this.Si;
        }
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDominated() {
        return dominated;
    }

    public void setDominated(int dominated) {
        this.dominated = dominated;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public double[] getObjectives() {
        return objectives;
    }

    public void setObjectives(double[] objectives) {
        this.objectives = objectives;
    }

    public void setDistance() {

        //euclidean 
        double sum = 0;
        for (int i = 0; i < objectives.length; i++) {
            sum += Math.pow(objectives[i], 2);
        }
        this.distance = (double) Math.round(Math.sqrt(sum) * 10000) / 10000;
    }

    public int getDominatedbycategory() {
        return dominatedbycategory;
    }

    public void setDominatedbycategory(int dominatedbycategory) {
        this.dominatedbycategory = dominatedbycategory;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder2(String[] params) {     
        String prm = "";
        for (int i = 0; i < params.length; i++) {
            prm += params[i];
        }
        if (myhash.containsKey(prm)) {
           this.order= myhash.get(prm);
        }else{
            counter++;
            myhash.put(prm,Integer.toString(counter));
            this.order=Integer.toString(counter);
        }
    }
public void setOrder(String order){
    this.order=order;
}
    public void setOrder(int objectives_number, double[] optimalValue, double[] worseValue) {
        //in order to create the right order we need to substract from the total number of objectives
        Hashtable<String, List<Integer>> thisorder = new Hashtable<>();
        int[] fthisorder1 = new int[objectives_number];
        int[] fthisorder2 = new int[objectives_number];
        double[] sorted = new double[objectives.length];
        for (int i = 0; i < objectives_number; i++) {
            if (worseValue[i] == optimalValue[i]) {
                sorted[i] = 1;//exeis to kalitero dunato pososto megisti timi
            } else {
                sorted[i] = Math.abs(objectives[i] - worseValue[i]) / Math.abs(optimalValue[i] - worseValue[i]);
            }
        }
        double[] sorted2 = sorted.clone();
        Arrays.sort(sorted2);
        String myorder = "";
        //TODO fix O() add equals in same order value
        for (int j = 0; j < objectives_number; j++) {
            for (int i = 0; i < objectives_number; i++) {

                if (sorted2[j] == sorted[i]) {
                    if (thisorder.containsKey(Integer.toString(j))) {
                        thisorder.get(Integer.toString(j)).add(i);
                    } else {
                        List<Integer> ml = new ArrayList<>();
                        ml.add(i);
                        thisorder.put(Integer.toString(j), ml);
                    }
                }
            }
        }
        int i = 1;
        boolean dble = false;
        for (int u = thisorder.size() - 1; u >= 0; u--) {
            if (thisorder.get(Integer.toString(u)).size() > 1) {
                if (u != thisorder.size() - 1) {//if not first element
                    if (thisorder.get(Integer.toString(u + 1)).equals(thisorder.get(Integer.toString(u))) && dble) {//if the next priority list is the same give the same priority
                        for (int temp : thisorder.get(Integer.toString(u))) {
                            fthisorder1[temp] = i;
                        }
                    } else {//if the next priority list is NOT the same give for 3 elements +3
                        if (dble) {
                            i += thisorder.get(Integer.toString(u + 1)).size();
                        }
                        for (int temp : thisorder.get(Integer.toString(u))) {
                            fthisorder1[temp] = i;
                        }
                    }
                } else {
                    for (int temp : thisorder.get(Integer.toString(u))) {
                        fthisorder1[temp] = i;
                    }
                }
                dble = true;
            } else {
                if (dble) {//if i had {} before  then the next priority += how many elements in {} -1
                    i += thisorder.get(Integer.toString(u + 1)).size();
                    dble = false;
                    for (int temp : thisorder.get(Integer.toString(u))) {
                        fthisorder1[temp] = i;
                    }
                    i++;
                } else {
                    for (int temp : thisorder.get(Integer.toString(u))) {
                        fthisorder1[temp] = i;
                    }
                    i++;
                }

            }
        }
        for (int u = 0; u < fthisorder1.length; u++) {
            myorder += fthisorder1[u];
        }
        this.order = myorder;
    }

 
}
