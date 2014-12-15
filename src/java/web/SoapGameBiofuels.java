/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import methods.dbUtils;
import methods.game_db;
import methods.Join_keep;
import methods.maj;
import methods.orderel;

@WebService()
@Stateless(name = "PreferenceDeduction")
public class SoapGameBiofuels {

    @PersistenceContext(unitName = "consensusPU")
    public EntityManager em;

    //an oi times einai sunexeis tupou 0.1-0.2-0.3 ktl tote to diastima tha antistoixei sto 20% tou diastimatos 
    //an den einai tote to diastima tha antistoixei sto 20% twn sinolikwn timwn pou emfanizontai :)
    public static class MajComparator implements Comparator<maj> {

        @Override
        public int compare(maj p1, maj p2) {
            return Double.compare(p1.getValue(), p2.getValue());
        }
    }

    public static double[] find_space(List<maj> test, int total) {

        double[] space = new double[3];
        for (int u = 0; u < space.length; u++) {
            space[u] = 0.0;
        }

        int k = (int) Math.round(test.size() * 0.2);
        if (k < 1) {
            k = 1;
        }
        double threshold = 30;//the threshold we use to verify preference

        for (int w = 0; w < test.size(); w++) {
            int temp_amount = 0;
            for (int i = 0; i < k; i++) {

                if (w + i < test.size()) {//make certain we dont get out of bounds 
                    if (test.get(w).getCount() == 0) {//if the previous value is chosen 0 times then we better start from the next one in creating the spaces in order not to create a space with 100-0-0-0-50
                        break;
                    }
                    temp_amount += test.get(w + i).getCount();
                    if (Double.compare((double) temp_amount * 100 / total, threshold) > 0) {

                        if (space[2] < temp_amount * 100 / total) {
                            space[2] = temp_amount * 100 / total;
                            space[0] = test.get(w).getValue(); //start value 
                            space[1] = test.get(w + i).getValue(); // end value if no one else added then i=0 and it gives the start value
                        }
                    }
                }
            }
            temp_amount = 0;
        }
        return space;
    }

    public static List<maj> merge(List<maj> majList) {
        List<maj> merged = new ArrayList<maj>();
        Hashtable<Double, Integer> hashList = new Hashtable<Double, Integer>();
        for (maj temp : majList) {
            if (hashList.containsKey(temp.getValue())) {
                hashList.put(temp.getValue(), hashList.get(temp.getValue()) + temp.getCount());
            } else {
                hashList.put(temp.getValue(), temp.getCount());
            }
        }
        Enumeration<Double> e = hashList.keys();
        while (e.hasMoreElements()) {
            Double key = e.nextElement();
            maj temp1 = new maj();
            temp1.setValue(key);
            temp1.setCount(hashList.get(key));
            merged.add(temp1);
        }

        return merged;
    }

    public static orderel mergeor(List<orderel> orderList) {
        orderel merged = new orderel();
        Hashtable<String, Integer> hashList = new Hashtable<>();

        for (orderel temp : orderList) {
            if (hashList.containsKey(temp.getValue())) {
                hashList.put(temp.getValue(), hashList.get(temp.getValue()) + temp.getCount());
            } else {
                hashList.put(temp.getValue(), temp.getCount());
            }
        }
        Enumeration<String> e = hashList.keys();
        orderel temp1 = new orderel();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            int count = 0;
            if (count < hashList.get(key)) {
                temp1.setValue(key);
                temp1.setCount(hashList.get(key));
                count = hashList.get(key);
            }
        }
        return temp1;
    }

    @WebMethod(operationName = "PreferencebyObjective")
    public void PreferencebyObjective(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "tablename2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;

        try {
            PreparedStatement stmt = conn.prepareStatement(query1);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            int param = 0;
            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            int allobj = columnsNumber - param;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnLabel(param + 3 + allobj);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames = "" + table_name1 + "." + rsmd.getColumnLabel(param + 3 + allobj) + ", ";
            }
            //i want to have doubles for objective values + the order + how many times each one was selected
            String select = "" + table_name1 + ".policy," + obNames + table_name2 + ".myorder, " + table_name2 + ".chosen";
            String joinQuery = "SELECT" + select
                    + "FROM " + table_name2
                    + "LEFT JOIN " + table_name1
                    + "ON " + table_name2 + ".P_ID=" + table_name1 + ".ID"
                    + "ORDER BY " + table_name2 + ".ID";

            PreparedStatement stmtj = conn.prepareStatement(joinQuery);
            ResultSet resj = stmtj.executeQuery();
            List<Join_keep> mylist = new ArrayList<Join_keep>();
            while (resj.next()) {
                Join_keep mykeep = new Join_keep(allobj);
                mykeep.setPolicy(resj.getString("policy"));
                double[] data = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    data[i] = resj.getDouble(objn[i]);
                }
                mykeep.setData(data);
                mykeep.setChosen(resj.getInt("chosen"));
                mykeep.setMyorder(resj.getString("myorder"));
                mylist.add(mykeep);
            }
            int total = 0;
            for (Join_keep temp : mylist) {
                total += temp.getChosen();
            }
            HashMap<String, HashMap<String, Double>> preferenceOrder = new HashMap<>();
//            create hash map for each priority
            for (int i = 0; i < allobj; i++) {
                preferenceOrder.put("objective" + Integer.toString(i), new HashMap<String, Double>());
            }
            //put values in tables
            for (Join_keep temp : mylist) {

                char[] chr = temp.getMyorder().toCharArray();
                for (int i = 0; i < chr.length; i++) {
                    if (preferenceOrder.get("objective" + Integer.toString(i)).containsKey(chr[i] + "")) {
                        preferenceOrder.get("objective" + Integer.toString(i)).put(chr[i] + "", preferenceOrder.get("prior" + Integer.toString(i)).get(chr[i] + "") + temp.getChosen());
                    } else {
                        preferenceOrder.get("objective" + Integer.toString(i)).put(chr[i] + "", (double) temp.getChosen());
                    }
                }
            }
//            make percentages
            for (int i = 0; i < allobj; i++) {

                for (Map.Entry<String, Double> entry : preferenceOrder.get("objective" + Integer.toString(i)).entrySet()) {
                    entry.setValue(entry.getValue() / total);
                }
            }

            stmtj.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            Logger.getLogger(SoapGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @WebMethod(operationName = "increaseChosen")
    public void increaseChosen(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
        System.out.print(query1);
        try {
            PreparedStatement stmt = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, id);

            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                System.out.print(res.getInt(8));
                Integer chosen = res.getInt(8);
                chosen++;
                res.updateInt(8, chosen);
                res.updateRow();
            }
        } catch (Exception exception) {
            System.out.printf(exception.getMessage());
        }
    }

    @WebMethod(operationName = "increaseLiked")
    public void increaseLiked(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id) {

        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, id);

            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                System.out.print(res.getInt(9));
                Integer chosen = res.getInt(9);
                chosen++;
                res.updateInt(9, chosen);
                res.updateRow();
            }
        } catch (Exception exception) {
        }
    }

    @WebMethod(operationName = "decreaseChosen")
    public void decreaseChosen(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1 + " WHERE ID=?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, id);

            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                System.out.print(res.getInt(8));
                Integer chosen = res.getInt(8);
                chosen--;
                res.updateInt(8, chosen);
                res.updateRow();
            }
        } catch (Exception exception) {
        }
    }

    @WebMethod(operationName = "decreaseLiked")
    public void decreaseLiked(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id) {

        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, id);

            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                System.out.print(res.getInt(9));
                Integer chosen = res.getInt(9);
                chosen--;
                res.updateInt(9, chosen);
                res.updateRow();
            }
        } catch (Exception exception) {
        }
    }
//   Preference by priority
//    for (int i = 0; i < allobj; i++) {
//                int top = 0;
//                String objmap = "";
//                for (Map.Entry<String, Integer> entry : preferenceOrder.get("objective" + Integer.toString(i)).entrySet()) {
//                    if (top < entry.getValue()) {
//                        top = entry.getValue();
//                        objmap = entry.getKey();
//                    }
//                }
//                preferenceOrder.get("objective" + Integer.toString(i)).put(objmap, top/total);
//            }

}
