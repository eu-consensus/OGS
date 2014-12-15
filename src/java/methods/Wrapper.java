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
public class Wrapper {
    private HashMap<String,MyPair> wrap;
    Wrapper(String key, MyPair value){
        this.wrap=new HashMap<>();
        wrap.put(key,value);
    }

    public HashMap<String, MyPair> getWrap() {
        return wrap;
    }
    
}
