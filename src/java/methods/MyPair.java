/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

import java.util.HashMap;

/**
 *
 * @author anu
 */
public class MyPair {
   private HashMap<String,Double> mywrap;
   MyPair(String key, double value){
       this.mywrap=new HashMap<>();
       mywrap.put(key,value);
   }

    public HashMap<String,Double> getMywrap() {
        return mywrap;
    }

   
}
