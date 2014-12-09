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
public class polName {
    private String[] name;

    public String[] getName() {
        return name;
    }

    public void setName(int i,String val) {
        this.name[i] = val;
    }

    public polName(int number) {
        this.name =  new String[number];
    }

}
