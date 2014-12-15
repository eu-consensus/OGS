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
public class Join_keep {
    private String policy;
    private String myorder;
    private int chosen;
    private double[] data;

    public Join_keep(int number) {
        this.policy="";
        this.myorder="";
        this.chosen=0;
        this.data=new double[number];
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
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

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }
    
}
