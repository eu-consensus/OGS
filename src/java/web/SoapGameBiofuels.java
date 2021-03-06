/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import methods.dbUtils;
import methods.Join_keep;
import methods.MapWrapper;
import methods.OuterWrapper;
import methods.maj;
import methods.orderel;

@WebService()
@Stateless(name = "PreferenceDeduction")
public class SoapGameBiofuels {

    @PersistenceContext(unitName = "consensusPU")
    public EntityManager em;

    @WebMethod
    public OuterWrapper hello() {
        HashMap<String, Double> mapOne = new HashMap<>();
        mapOne.put("one", 1.1111);
        mapOne.put("two", 2.2222);
        MapWrapper mrone = new MapWrapper();
        mrone.map = mapOne;
        HashMap<String, Double> mapTwo = new HashMap<>();
        mapTwo.put("three", 3.33333);
        mapTwo.put("four", 4.44444);
        MapWrapper mrtwo = new MapWrapper();
        mrtwo.map = mapTwo;
        OuterWrapper result = new OuterWrapper();
        HashMap<String, MapWrapper> outerWrapper = new HashMap<>();
        outerWrapper.put("mrone", mrone);
        outerWrapper.put("mrtwo", mrtwo);
        result.outerWrapper = outerWrapper;
        return result;
    }

    @WebMethod(operationName = "PreferencebyObjective")
    public OuterWrapper PreferencebyObjective(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "tablename2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        OuterWrapper ret = new OuterWrapper();
        try {
            PreparedStatement stmt = conn.prepareStatement(query1);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            int param = 1;

            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            int allobj = columnsNumber - param - 1;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(param + 2 + i);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "" + table_name1 + "." + rsmd.getColumnName(param + 2 + i) + ", ";
            }
            //i want to have doubles for objective values + the order + how many times each one was selected
            String select = "" + table_name1 + ".policy," + obNames + table_name2 + ".myorder, " + table_name2 + ".chosen";
            String joinQuery = "SELECT " + select
                    + " FROM " + table_name2
                    + " LEFT JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID"
                    + " ORDER BY " + table_name2 + ".ID";
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
                        preferenceOrder.get("objective" + Integer.toString(i)).put(chr[i] + "", preferenceOrder.get("objective" + Integer.toString(i)).get(chr[i] + "") + temp.getChosen());
                    } else {
                        preferenceOrder.get("objective" + Integer.toString(i)).put(chr[i] + "", (double) temp.getChosen());
                    }
                }
            }
//            make percentages
            HashMap<String, MapWrapper> outerWrapper = new HashMap<>();
            for (int i = 0; i < allobj; i++) {

                MapWrapper mrone = new MapWrapper();
                mrone.map = preferenceOrder.get("objective" + Integer.toString(i));
//                    entry.getValue() / total;                
                outerWrapper.put("objective" + Integer.toString(i), mrone);
            }
            ret.outerWrapper = outerWrapper;
            stmtj.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return ret;
    }

    @WebMethod(operationName = "PreferencebyPriority")
    public OuterWrapper PreferencebyPriority(@WebParam(name = "table_name1") String table_name1, @WebParam(name = "table_name2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        OuterWrapper result = new OuterWrapper();
        try {
            PreparedStatement stmt = conn.prepareStatement(query1);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            int param = 1;

            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            int allobj = columnsNumber - param - 1;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(param + 2 + i);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "" + table_name1 + "." + rsmd.getColumnName(param + 2 + i) + ", ";
            }
            //i want to have doubles for objective values + the order + how many times each one was selected
            String select = "" + table_name1 + ".policy," + obNames + table_name2 + ".myorder, " + table_name2 + ".chosen";
            String joinQuery = "SELECT " + select
                    + " FROM " + table_name2
                    + " LEFT JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID"
                    + " ORDER BY " + table_name2 + ".ID";
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
                preferenceOrder.put("priority" + Integer.toString(i + 1), new HashMap<String, Double>());
            }
            //put values in tables
            for (Join_keep temp : mylist) {

                char[] chr = temp.getMyorder().toCharArray();
                for (int i = 0; i < chr.length; i++) {
                    if (preferenceOrder.get("priority" + chr[i]).containsKey("objective " + Integer.toString(i + 1))) {
                        preferenceOrder.get("priority" + chr[i]).put("objective " + Integer.toString(i + 1), preferenceOrder.get("priority" + chr[i]).get("objective " + Integer.toString(i + 1)) + temp.getChosen());
                    } else {
                        preferenceOrder.get("priority" + chr[i]).put("objective " + Integer.toString(i + 1), (double) temp.getChosen());
                    }
                }
            }
//            make percentages
            for (int i = 0; i < allobj; i++) {
                for (Map.Entry<String, Double> entry : preferenceOrder.get("priority" + Integer.toString(i + 1)).entrySet()) {
                    entry.setValue(entry.getValue() / total);
                }
            }

            HashMap<String, MapWrapper> outerWrapper = new HashMap<>();
            for (int i = 0; i < allobj; i++) {
                MapWrapper mrone = new MapWrapper();
                mrone.map = preferenceOrder.get("priority" + Integer.toString(i + 1));
                outerWrapper.put("priority" + Integer.toString(i + 1), mrone);
            }
            result.outerWrapper = outerWrapper;
            stmtj.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return result;
    }

    @WebMethod(operationName = "increaseChosen")
    public void increaseChosen(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id
    ) {
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
    public void increaseLiked(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id
    ) {

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
    public void decreaseChosen(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id
    ) {
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
    public void decreaseLiked(@WebParam(name = "tablename1") String table_name1, @WebParam(name = "id") int id
    ) {

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
