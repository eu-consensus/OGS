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
import java.util.Arrays;
import java.util.UUID;

/**
 *
 * @author anu
 */
public class policy {

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

    public policy(int number,int number2) {
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
        this.policyParameters=new String[number2];
        this.policyName = "";
        this.ID=0;
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

    public void setOrder(int objectives_number,double[] optimalValue,double[] worseValue) {
        //in order to create the right order we need to substract from the total number of objectives
        int[] thisorder = new int[objectives_number];
        double[] sorted = objectives.clone();
        for(int i=0;i<objectives_number;i++){
            sorted[i]=Math.abs(sorted[i]-worseValue[i])/Math.abs(optimalValue[i]-worseValue[i]);
        }
         double[] sorted2 = sorted.clone();
        Arrays.sort(sorted2);
        String myorder = "";
        //TODO fix O() add equals in same order value
        for (int j = 0; j < objectives_number; j++) {
            for (int i = 0; i < objectives_number; i++) {

                if (sorted2[j] == sorted[i]) {
                    thisorder[j] = objectives_number - i;
                }
            }
        }

        for (int u = 0; u < thisorder.length; u++) {
            myorder += thisorder[u];
        }
        this.order = myorder;
    }

}
