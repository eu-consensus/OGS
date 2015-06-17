package web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import methods.Join_keep;
import methods.dbUtils;
import methods.maj;
import methods.methods;
import static methods.methods.find_space;
import static methods.methods.merge;
import methods.policy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/GameTransportation")
@Stateless
public class RestGameTransportation {

    String[] parameters = {"Basic Toll", "Road pricing type", "Toll collection technique", "Operation Authority"};

    @GET
    @Path("/{table_name}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getAll(@Context HttpServletRequest request, @PathParam("table_name") String table_name) {
        JSONObject result = new JSONObject();
        try {
            Connection conn = dbUtils.getConnection();

            String query = "SELECT * FROM " + table_name;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            JSONArray mylist = new JSONArray();

            while (res.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), res.getInt(1));
                policy.put(rsmd.getColumnName(2), res.getInt(2));
                policy.put(rsmd.getColumnName(3), res.getInt(3));
                policy.put(rsmd.getColumnName(4), res.getInt(4));
                policy.put(rsmd.getColumnName(5), res.getInt(5));
                policy.put(rsmd.getColumnName(6), res.getInt(6));
                policy.put(rsmd.getColumnName(7), res.getString(7));
                policy.put(rsmd.getColumnName(8), res.getInt(8));
                policy.put(rsmd.getColumnName(9), res.getInt(9));
                policy.put(rsmd.getColumnName(10), res.getInt(10));
                policy.put(rsmd.getColumnName(11), res.getInt(11));
                mylist.put(policy);
            }
            dbUtils.closeConnection(conn);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put("result on " + reportDate, mylist);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(RestBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/{table_name}/{id}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getByID(@Context HttpServletRequest request, @PathParam("table_name") String table_name, @PathParam("id") int id) {
        JSONObject result = new JSONObject();
        try {
            Connection conn = dbUtils.getConnection();
            String query = "SELECT * FROM " + table_name + " WHERE ID = ?";
            System.out.print(query);
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
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
            JSONArray mylist = new JSONArray();

            while (res.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), res.getInt(1));
                policy.put(rsmd.getColumnName(2), res.getString(2));
                for (int i = 3; i < param + 3; i++) {
                    policy.put(rsmd.getColumnName(i), res.getString(i));
                }
                for (int i = param + 2; i < allobj + param + 2; i++) {
                    policy.put(rsmd.getColumnName(i), res.getDouble(i));
                }

                mylist.put(policy);
            }

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put("result on " + reportDate, mylist);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(RestBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/PreferencebyObjective/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response PreferencebyObjective(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
                    + " INNER JOIN " + table_name1
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
            for (int i = 0; i < allobj; i++) {
                for (Map.Entry<String, Double> entry : preferenceOrder.get("objective" + Integer.toString(i)).entrySet()) {
                    entry.setValue(entry.getValue() / total);
                }
            }
//System.out.print(preferenceOrder.get("objective" +Integer.toString(objective_number)).toString());
//                result.put("objective" +Integer.toString(objective_number), preferenceOrder.get("objective" +Integer.toString(objective_number)).toString());
            for (int i = 0; i < allobj; i++) {
                JSONObject myjson = new JSONObject();
                for (Map.Entry<String, Double> entry : preferenceOrder.get("objective" + Integer.toString(i)).entrySet()) {
                    myjson.put(entry.getKey(), entry.getValue());
                }
                result.put(objn[i], myjson);
            }
            stmtj.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/PreferencebyPriority/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response PreferencebyPriority(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
                    + " INNER JOIN " + table_name1
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
                    if (preferenceOrder.get("priority" + chr[i]).containsKey(objn[i])) {
                        preferenceOrder.get("priority" + chr[i]).put(objn[i], preferenceOrder.get("priority" + chr[i]).get(objn[i]) + temp.getChosen());
                    } else {
                        preferenceOrder.get("priority" + chr[i]).put(objn[i], (double) temp.getChosen());
                    }
                }
            }
//            make percentages
            for (int i = 0; i < allobj; i++) {
                for (Map.Entry<String, Double> entry : preferenceOrder.get("priority" + Integer.toString(i + 1)).entrySet()) {
                    entry.setValue(entry.getValue() / total);
                }
            }
//System.out.print(preferenceOrder.get("objective" +Integer.toString(objective_number)).toString());
//                result.put("objective" +Integer.toString(objective_number), preferenceOrder.get("objective" +Integer.toString(objective_number)).toString());
            for (int i = 0; i < allobj; i++) {
                JSONObject myjson = new JSONObject();
                for (Map.Entry<String, Double> entry : preferenceOrder.get("priority" + Integer.toString(i + 1)).entrySet()) {
                    myjson.put(entry.getKey(), entry.getValue());
                }
                result.put("priority" + Integer.toString(i + 1), myjson);
            }
            stmtj.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/PreferencebyRange/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response PreferencebyRange(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
                    + " INNER JOIN " + table_name1
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
            HashMap<String, List<maj>> preferenceOrder = new HashMap<>();
//            create hash map for each priority
            for (int i = 0; i < allobj; i++) {
                preferenceOrder.put(objn[i], new ArrayList<maj>());
            }
            //put values in tables
            for (Join_keep temp : mylist) {

                double[] dt = temp.getData();
                for (int i = 0; i < dt.length; i++) {
                    maj tmaj = new maj();
                    tmaj.setValue(dt[i]);
                    tmaj.setCount(temp.getChosen());
                    preferenceOrder.get(objn[i]).add(tmaj);
                }
            }

            for (Map.Entry<String, List<maj>> entry : preferenceOrder.entrySet()) {
                JSONObject tjson = new JSONObject();
                List<maj> temp2 = entry.getValue();
                temp2 = merge(temp2);
                Collections.sort(temp2, new methods.MajComparator());
                double[] spaces = new double[3];
                spaces = find_space(temp2, total);
                tjson.put("begin", spaces[0]);
                tjson.put("end", spaces[1]);
                tjson.put("percentage", spaces[2]);
                result.put(entry.getKey(), tjson);
            }

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/order/{table_name1}/{table_name2}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getbyOrder(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("id") String id) {

        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
            String[] objp = new String[param];
            for (int i = 0; i < param; i++) {
                objp[i] = rsmd.getColumnName(i + 1);
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
            String parNames = "";
            for (int i = 1; i < param; i++) {
                parNames += "" + table_name1 + "." + rsmd.getColumnName(2 + i) + ", ";
            }
            String select2 = "" + table_name2 + ".ID," + table_name1 + ".policy," + parNames + obNames + table_name2 + ".objscore";
            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID"
                    + " WHERE " + table_name2 + ".myorder=?";
            PreparedStatement stm = conn.prepareStatement(joinQuery2);
            stm.setString(1, id);
            ResultSet resm = stm.executeQuery();

            JSONArray mylist = new JSONArray();
            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), resm.getInt(1));
                policy.put(rsmd.getColumnName(2), resm.getString(2));
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], resm.getString(i + 3));
                }
                for (int i = param + 2; i < allobj + param + 2; i++) {
                    policy.put(rsmd.getColumnName(i), resm.getDouble(i));
                }
                policy.put("objscore", resm.getInt(allobj + param + 1));
                mylist.put(policy);
            }
            stmt.close();
            conn.close();

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put("result on " + reportDate, mylist);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/orderbypercentage/{table_name1}/{table_name2}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getbyOrderPercentage(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("id") String id) {

        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "" + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + ", ";
            }
            String parNames = "";
            for (int i = 1; i < param; i++) {
                parNames += "" + table_name1 + "." + rsmd.getColumnName(3 + i) + ", ";
            }
            String select2 = "" + table_name2 + ".ID," + table_name1 + ".policy," + parNames + obNames + table_name2 + ".objscore";
            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + table_name2 + ".myorder=?";
            PreparedStatement stm = conn.prepareStatement(joinQuery2);
            stm.setString(1, id);
            ResultSet resm = stm.executeQuery();
            ResultSetMetaData rsmd2 = resm.getMetaData();
            JSONArray mylist = new JSONArray();
            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd2.getColumnName(1), resm.getInt(1));
                policy.put(rsmd2.getColumnName(2), resm.getString(2));
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], resm.getString(i + 3));
                }
                for (int i = param + 2; i < allobj + param + 2; i++) {
                    policy.put(rsmd2.getColumnName(i), resm.getDouble(i));
                }
                policy.put("objscore", resm.getInt(allobj + param + 2));
                mylist.put(policy);
            }
            stmt.close();
            conn.close();

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/orderbypercentage2/{table_name1}/{table_name2}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getbyOrderPercentage2(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("order") String order) {

        Connection conn = dbUtils.getConnection();
        List<Integer> first = new ArrayList<>();
        List<Integer> second = new ArrayList<>();
        List<Integer> third = new ArrayList<>();
        List<Integer> fourth = new ArrayList<>();
        List<Integer> firstc = new ArrayList<>();
        List<Integer> secondc = new ArrayList<>();
        List<Integer> thirdc = new ArrayList<>();
        List<Integer> fourthc = new ArrayList<>();
        JSONArray mylist = new JSONArray();
        char[] pref = new char[6];
        pref = order.toCharArray();
        //find 1s 2s 3s 4s
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '1') {
                first.add(i);
            }
        }
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '2') {
                second.add(i);
            }
        }
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '3') {
                third.add(i);
            }
        }
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '4') {
                fourth.add(i);
            }
        }

        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + " ";
            }
            String parNames = "";
            for (int i = 1; i < param; i++) {
                parNames += "," + table_name1 + "." + rsmd.getColumnName(3 + i) + " ";
            }

            String whole = "SELECT " + table_name1 + ".ID " + obNames + " FROM " + table_name1 + " WHERE ";
            for (int i = 0; i < first.size(); i++) {
                whole += "" + objn[first.get(i)] + " BETWEEN 90 AND 100";
                if (first.size() - i > 1) {
                    whole += " AND ";
                }
            }
            System.out.println(whole);
            PreparedStatement stmf = conn.prepareStatement(whole);
            ResultSet resmf = stmf.executeQuery();
            while (resmf.next()) {
                firstc.add(resmf.getInt(1));
            }
            if (firstc.size() < 3) {
                mylist = ret(conn, firstc, table_name1, table_name2, parNames, obNames, param, allobj);
            } else {
                resmf.beforeFirst();
                while (resmf.next()) {
                    double min = resmf.getDouble(first.get(0) + 2);
                    for (int temp1 : first) {
                        if (min > resmf.getDouble(temp1 + 2)) {
                            min = resmf.getDouble(temp1 + 2);
                        }
                    }
                    if (!second.isEmpty()) {
                        boolean issmaller = true;
                        double min2 = resmf.getDouble(second.get(0) + 2);
                        for (int temp2 : second) {
                            if (min2 > resmf.getDouble(temp2 + 2)) {
                                min2 = resmf.getDouble(temp2 + 2);
                            }
                            if (resmf.getDouble(temp2 + 2) < min) {
                                issmaller = issmaller & true;
                            } else {
                                issmaller = issmaller & false;
                            }
                        }
                        if (issmaller) {
                            secondc.add(resmf.getInt(1));

                            if (!third.isEmpty()) {

                                double min3 = resmf.getDouble(third.get(0) + 2);
                                for (int temp3 : third) {
                                    if (min3 > resmf.getDouble(temp3 + 2)) {
                                        min3 = resmf.getDouble(temp3 + 2);
                                    }
                                    if (resmf.getDouble(temp3 + 2) < min2) {
                                        issmaller = issmaller & true;
                                    } else {
                                        issmaller = issmaller & false;
                                    }
                                }
                                if (issmaller) {
                                    thirdc.add(resmf.getInt(1));

                                    if (!fourth.isEmpty()) {

                                        double min4 = resmf.getDouble(fourth.get(0) + 2);
                                        for (int temp4 : fourth) {
                                            if (min4 > resmf.getDouble(temp4 + 2)) {
                                                min4 = resmf.getDouble(temp4 + 2);
                                            }
                                            if (resmf.getDouble(temp4 + 2) < min3) {
                                                issmaller = issmaller & true;
                                            } else {
                                                issmaller = issmaller & false;
                                            }
                                        }
                                        if (issmaller) {
                                            fourthc.add(resmf.getInt(1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (fourthc.size() > 2) {
                    mylist = ret(conn, fourthc, table_name1, table_name2, parNames, obNames, param, allobj);
                } else if (thirdc.size() > 2) {
                    mylist = ret(conn, thirdc, table_name1, table_name2, parNames, obNames, param, allobj);
                } else if (secondc.size() > 2) {
                    mylist = ret(conn, secondc, table_name1, table_name2, parNames, obNames, param, allobj);
                } else {
                    mylist = ret(conn, firstc, table_name1, table_name2, parNames, obNames, param, allobj);
                }
            }
            stmt.close();
            conn.close();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    private JSONArray ret(Connection conn, List<Integer> idList, String table_name1, String table_name2, String parNames, String obNames, int param, int allobj) throws JSONException {

        JSONArray mylist = new JSONArray();
        try {
            for (int id : idList) {

                String joinQuery2 = "SELECT " + table_name2 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".objscore"
                        + " FROM " + table_name2
                        + " INNER JOIN " + table_name1
                        + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                        + " WHERE " + table_name1 + ".ID=?";
//               System.out.println(joinQuery2);
                PreparedStatement stm = conn.prepareStatement(joinQuery2);
                stm.setInt(1, id);
                ResultSet resm = stm.executeQuery();
                ResultSetMetaData rsmd2 = resm.getMetaData();

                while (resm.next()) {
                    JSONObject policy = new JSONObject();
                    policy.put(rsmd2.getColumnName(1), resm.getInt(1));
                    policy.put(rsmd2.getColumnName(2), resm.getString(2));
                    for (int i = 0; i < param - 1; i++) {
                        policy.put(parameters[i], resm.getString(i + 3));
                    }
                    for (int i = param + 2; i < allobj + param + 2; i++) {
                        policy.put(rsmd2.getColumnName(i), resm.getDouble(i));
                    }
                    policy.put("objscore", resm.getInt(allobj + param + 2));
                    mylist.put(policy);
                }
                stm.close();
            }

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return mylist;
    }

    private double min(List<Double> tominimize) {
        double temp;
        if (tominimize.size() < 2) {
            return tominimize.get(0);
        } else {
            temp = tominimize.get(0);
            for (double it : tominimize) {
                if (it < temp) {
                    temp = it;
                }
            }
            return temp;
        }
    }

    @GET
    @Path("/increaseChosen/{table_name1}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getbyOrderPercentage(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("id") int id) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
        JSONObject result = new JSONObject();
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
            String query2 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
            PreparedStatement stmt2 = conn.prepareStatement(query2, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt2.setInt(1, id);
            ResultSet resm = stmt2.executeQuery();
            ResultSetMetaData rsmd = resm.getMetaData();
            int columnsNumber = rsmd.getColumnCount();

            JSONArray mylist = new JSONArray();
            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), res.getInt(1));
                policy.put(rsmd.getColumnName(2), res.getInt(2));
                policy.put(rsmd.getColumnName(3), res.getInt(3));
                policy.put(rsmd.getColumnName(4), res.getInt(4));
                policy.put(rsmd.getColumnName(5), res.getInt(5));
                policy.put(rsmd.getColumnName(6), res.getInt(6));
                policy.put(rsmd.getColumnName(7), res.getString(7));
                policy.put(rsmd.getColumnName(8), res.getInt(8));
                policy.put(rsmd.getColumnName(9), res.getInt(9));
                policy.put(rsmd.getColumnName(10), res.getInt(10));
                policy.put(rsmd.getColumnName(11), res.getInt(11));
                mylist.put(policy);
            }

            dbUtils.closeConnection(conn);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put("result on " + reportDate, mylist);
            stmt.close();
            stmt2.close();

        } catch (Exception exception) {
            System.out.printf(exception.getMessage());
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/decreaseChosen/{table_name1}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response decreaseChosen(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("id") int id) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
        JSONObject result = new JSONObject();
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
            String query2 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
            PreparedStatement stmt2 = conn.prepareStatement(query2, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt2.setInt(1, id);
            ResultSet resm = stmt2.executeQuery();
            ResultSetMetaData rsmd = resm.getMetaData();
            int columnsNumber = rsmd.getColumnCount();

            JSONArray mylist = new JSONArray();
            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), res.getInt(1));
                policy.put(rsmd.getColumnName(2), res.getInt(2));
                policy.put(rsmd.getColumnName(3), res.getInt(3));
                policy.put(rsmd.getColumnName(4), res.getInt(4));
                policy.put(rsmd.getColumnName(5), res.getInt(5));
                policy.put(rsmd.getColumnName(6), res.getInt(6));
                policy.put(rsmd.getColumnName(7), res.getString(7));
                policy.put(rsmd.getColumnName(8), res.getInt(8));
                policy.put(rsmd.getColumnName(9), res.getInt(9));
                policy.put(rsmd.getColumnName(10), res.getInt(10));
                policy.put(rsmd.getColumnName(11), res.getInt(11));
                mylist.put(policy);
            }

            dbUtils.closeConnection(conn);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put("result on " + reportDate, mylist);
            stmt.close();
            stmt2.close();

        } catch (Exception exception) {
            System.out.printf(exception.getMessage());
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/orderbypercentage3/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    //TODO change in String Array ?ids=24041&ids=24117
    public Response getbyOrderPercentage3(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("par") List<String> crit) {

        Connection conn = dbUtils.getConnection();
        JSONArray mylist = new JSONArray();

        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
            //objective parameters -criteria 
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + " ";
            }
            String parNames = "";
            for (int i = 1; i < param; i++) {
                parNames += "," + table_name1 + "." + rsmd.getColumnName(3 + i) + " ";
            }

            String select2 = "" + table_name1 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".objscore ," + table_name2 + ".dominatedbycategory ," + table_name2 + ".dominatedbypool ";
            String sentence1 = "";
            Iterator<String> myit = crit.iterator();
            for (int i = 0; i < objp.length; i++) {
                if (myit.hasNext()) {
                    String cr = myit.next();

                    if (!cr.equals("NULL")) {
                        if (cr.contains("-BETWEEN-")) {
                            String[] ms = cr.split("-BETWEEN-");
                            sentence1 += "" + table_name1 + "." + objp[i] + " BETWEEN " + ms[0] + " AND " + ms[1] + " ";
                            if ((i < objp.length - 1)) {
                                if (!crit.get(i + 1).equals("NULL")) {
                                    sentence1 += " AND ";
                                }
                            }
                        } else {
                            sentence1 += "" + table_name1 + "." + objp[i] + "='" + cr + "'";
                            if ((i < objp.length - 1)) {
                                if (!crit.get(i + 1).equals("NULL")) {
                                    sentence1 += " AND ";
                                }
                            }
                        }
                    } else {
                        if ((i < objp.length - 1)) {
                            if (!crit.get(i + 1).equals("NULL") & !sentence1.isEmpty()) {
                                sentence1 += " AND ";
                            }
                        }
                    }
                }
            }

            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + sentence1;
            System.out.println(joinQuery2);
            PreparedStatement stm = conn.prepareStatement(joinQuery2);
            ResultSet resm = stm.executeQuery();
            ResultSetMetaData rsmd2 = resm.getMetaData();

            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd2.getColumnName(1), resm.getInt(1));
                policy.put(rsmd2.getColumnName(2), resm.getString(2));
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], resm.getString(i + 3));
                }
                for (int i = param + 2; i < allobj + param + 2; i++) {
                    policy.put(rsmd2.getColumnName(i), resm.getDouble(i));
                }
//                policy.put("objscore", resm.getInt(allobj + param + 2));
//                policy.put("dominatedbycategory", resm.getInt(allobj + param + 3));
//                policy.put("dominatedbypool", resm.getInt(allobj + param + 4));
                mylist.put(policy);
            }

            stmt.close();
            conn.close();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(result.toString());

        return builder.build();
    }
//returns optimal solutions only based on the category id 

    @GET
    @Path("/optimal/{table_name1}/{table_name2}/{category_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOptimalbyCategory(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("category_id") String category
    ) {
        Connection conn = dbUtils.getConnection();
        JSONArray mylist = new JSONArray();

        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
            //objective parameters -criteria 
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + " ";
            }
            String parNames = "";
            for (int i = 1; i < param; i++) {
                parNames += "," + table_name1 + "." + rsmd.getColumnName(3 + i) + " ";
            }
            String select2 = "" + table_name2 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".objscore ," + table_name2 + ".dominatedbycategory ," + table_name2 + ".dominatedbypool ";

            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + table_name2 + ".myorder=?"
                    + " AND " + table_name2 + ".dominatedbycategory=0";
            //"AND "+ table_name2+ ".dominatedbypool=0";
            System.out.println(joinQuery2);
            PreparedStatement stm = conn.prepareStatement(joinQuery2);
            stm.setString(1, category);
            ResultSet resm = stm.executeQuery();
            ResultSetMetaData rsmd2 = resm.getMetaData();

            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd2.getColumnName(1), resm.getInt(1));
                policy.put(rsmd2.getColumnName(2), resm.getString(2));
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], resm.getString(i + 3));
                }
                for (int i = param + 2; i < allobj + param + 2; i++) {
                    policy.put(rsmd2.getColumnName(i), resm.getDouble(i));
                }
                policy.put("objscore", resm.getInt(allobj + param + 2));
                policy.put("dominatedbycategory", resm.getInt(allobj + param + 3));
                policy.put("dominatedbypool", resm.getInt(allobj + param + 4));
                mylist.put(policy);
            }

            stmt.close();
            conn.close();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(result.toString());

        return builder.build();
    }

    @GET
    @Path("/optimal1/{table_name1}/{table_name2}/{priority}")
    @Produces(MediaType.APPLICATION_JSON)
    //it collects this specific prioritization (4 spots) and collects if not many solutions return more than one category
    //there i check for optimallity within its own category in this mixed category 
    //TODO check h
    public Response getOptimalbyPriority(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("priority") String priority
    ) {

        Connection conn = dbUtils.getConnection();
        List<Integer> first = new ArrayList<>();
        List<Integer> second = new ArrayList<>();
        List<Integer> third = new ArrayList<>();
        List<Integer> fourth = new ArrayList<>();
        List<Integer> firstc = new ArrayList<>();
        List<Integer> secondc = new ArrayList<>();
        List<Integer> thirdc = new ArrayList<>();
        List<Integer> fourthc = new ArrayList<>();
        JSONArray mylist = new JSONArray();
        char[] pref = new char[6];
        pref = priority.toCharArray();
        //find 1s 2s 3s 4s
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '1') {
                first.add(i);
            }
        }
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '2') {
                second.add(i);
            }
        }
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '3') {
                third.add(i);
            }
        }
        for (int i = 0; i < pref.length; i++) {
            if (pref[i] == '4') {
                fourth.add(i);
            }
        }

        String query1 = "SELECT * FROM " + table_name1;
        JSONObject result = new JSONObject();
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
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + " ";
            }
            String parNames = "";
            for (int i = 1; i < param; i++) {
                parNames += "," + table_name1 + "." + rsmd.getColumnName(3 + i) + " ";
            }

            String whole = "SELECT " + table_name1 + ".ID " + obNames + " FROM " + table_name1 + " WHERE ";
            for (int i = 0; i < first.size(); i++) {
                whole += "" + objn[first.get(i)] + " BETWEEN 90 AND 100";
                if (first.size() - i > 1) {
                    whole += " AND ";
                }
            }
            System.out.println(whole);
            PreparedStatement stmf = conn.prepareStatement(whole);
            ResultSet resmf = stmf.executeQuery();
            while (resmf.next()) {
                firstc.add(resmf.getInt(1));
            }
            if (firstc.size() < 3) {
                mylist = ret2(conn, firstc, table_name1, table_name2, parNames, obNames);
            } else {
                resmf.beforeFirst();
                while (resmf.next()) {
                    double min = resmf.getDouble(first.get(0) + 2);
                    for (int temp1 : first) {
                        if (min > resmf.getDouble(temp1 + 2)) {
                            min = resmf.getDouble(temp1 + 2);
                        }
                    }
                    if (!second.isEmpty()) {
                        boolean issmaller = true;
                        double min2 = resmf.getDouble(second.get(0) + 2);
                        for (int temp2 : second) {
                            if (min2 > resmf.getDouble(temp2 + 2)) {
                                min2 = resmf.getDouble(temp2 + 2);
                            }
                            if (resmf.getDouble(temp2 + 2) < min) {
                                issmaller = issmaller & true;
                            } else {
                                issmaller = issmaller & false;
                            }
                        }
                        if (issmaller) {
                            secondc.add(resmf.getInt(1));

                            if (!third.isEmpty()) {

                                double min3 = resmf.getDouble(third.get(0) + 2);
                                for (int temp3 : third) {
                                    if (min3 > resmf.getDouble(temp3 + 2)) {
                                        min3 = resmf.getDouble(temp3 + 2);
                                    }
                                    if (resmf.getDouble(temp3 + 2) < min2) {
                                        issmaller = issmaller & true;
                                    } else {
                                        issmaller = issmaller & false;
                                    }
                                }
                                if (issmaller) {
                                    thirdc.add(resmf.getInt(1));

                                    if (!fourth.isEmpty()) {

                                        double min4 = resmf.getDouble(fourth.get(0) + 2);
                                        for (int temp4 : fourth) {
                                            if (min4 > resmf.getDouble(temp4 + 2)) {
                                                min4 = resmf.getDouble(temp4 + 2);
                                            }
                                            if (resmf.getDouble(temp4 + 2) < min3) {
                                                issmaller = issmaller & true;
                                            } else {
                                                issmaller = issmaller & false;
                                            }
                                        }
                                        if (issmaller) {
                                            fourthc.add(resmf.getInt(1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (fourthc.size() > 2) {
                    mylist = ret2(conn, fourthc, table_name1, table_name2, parNames, obNames);
                } else if (thirdc.size() > 2) {
                    mylist = ret2(conn, thirdc, table_name1, table_name2, parNames, obNames);
                } else if (secondc.size() > 2) {
                    mylist = ret2(conn, secondc, table_name1, table_name2, parNames, obNames);
                } else {
                    mylist = ret2(conn, firstc, table_name1, table_name2, parNames, obNames);
                }
            }
            stmt.close();
            conn.close();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    private JSONArray ret2(Connection conn, List<Integer> idList, String table_name1, String table_name2, String parNames, String obNames) throws JSONException {

        JSONArray mylist = new JSONArray();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol1 = new ArrayList<>();
        boolean[] myminmax = {true, false, false, false, true, true};
        try {
            int param = 1;

            String joinQuery = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".dominatedbypool "
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + table_name1 + ".ID=?";

            //     System.out.println(joinQuery2);
            PreparedStatement stmt = conn.prepareStatement(joinQuery);
            stmt.setInt(1, idList.get(0));
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            //objective parameters -criteria 
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query

            int beforeobj = param + 2;

            for (int id : idList) {

                String joinQuery2 = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".dominatedbypool "
                        + " FROM " + table_name2
                        + " INNER JOIN " + table_name1
                        + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                        + " WHERE " + table_name1 + ".ID=?";

                //     System.out.println(joinQuery2);
                PreparedStatement stm = conn.prepareStatement(joinQuery2);
                stm.setInt(1, id);
                ResultSet resm = stm.executeQuery();

                while (resm.next()) {
                    policy pol = new policy(allobj, 0);
                    pol.setID(resm.getInt(1));
                    pol.setPolicyName(resm.getString(2));
                    double[] obj_values = new double[allobj];

                    String[] objparam = new String[param - 1];
                    for (int i = 0; i < param - 1; i++) {
                        objparam[i] = resm.getString(i + 3);
                    }
                    for (int i = 0; i < allobj; i++) {
                        obj_values[i] = resm.getDouble(beforeobj + i);
                    }
                    pol.setObjectives(obj_values);
                    pol.setPolicyParameters(objparam);
                    pol.setDominated(resm.getInt(columnsNumber));
                    mypol.add(pol);
                }
                stm.close();
            }
            mypol1 = methods.paretoG(mypol, myminmax);
            List<policy> mypol2 = setScore(mypol1);
            for (policy temp : mypol1) {
                JSONObject policy = new JSONObject();
                policy.put("ID", temp.getID());
                policy.put("policy", temp.getPolicyName());
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], temp.getPolicyParameters()[i]);
                }
                for (int i = 0; i < allobj; i++) {
                    policy.put(objn[i], temp.getObjectives()[i]);
                }
                policy.put("dominatedByCategory", temp.getDominatedbycategory());
                policy.put("dominatedbypool", temp.getDominated());
                policy.put("objscore", temp.getScore());
                mylist.put(policy);
            }

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return mylist;
    }

    private static List<policy> setScore(List<policy> mypol) {
        Collections.sort(mypol, new methods.polComparatorRDD());//sorted by rank-domination count-domination by category

        double amount = mypol.size() * 0.5; //assign points to 60% of solutions
        int top_score = (int) (amount);
        int step = 1; //find step to reducing points
        int rank = 1;
        int last = 0;
        int last_dom = 0;
        int last_score = 0;
        int prev_dom_by = 0;
        int b = 100;
        for (policy pol : mypol) {
            if (pol.getDominated() == 0 && pol.getDominatedbycategory() == 0) {
                pol.setScore(top_score);
                last_score = top_score;
            }

            if (pol.getDominated() > last_dom) {
                last_dom = pol.getDominated();
                last_score -= step * 60;
            }

            if (pol.getDominatedbycategory() > prev_dom_by) {
                last_score -= 40;
            }
            pol.setScore(last_score);
            prev_dom_by = pol.getDominatedbycategory();
        }

        int bottom = Math.abs(top_score - last_score);
        int top = 0;
        for (policy pol : mypol) {
            top = Math.abs(pol.getScore() - last_score) * b;
            pol.setScore(top / bottom);
        }
        return mypol;
    }

    @GET
    @Path("/score1/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    //it collects this specific prioritization (4 spots) and collects if not many solutions return more than one category
    //there i check for optimallity within its own category in this mixed category return boolean true and score from this category if inside otherwise return boolean false and closeness score 

    public Response getScorebyID(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("prior") String priority, @QueryParam("id") int id) {
        {

            Connection conn = dbUtils.getConnection();
            List<Integer> first = new ArrayList<>();
            List<Integer> second = new ArrayList<>();
            List<Integer> third = new ArrayList<>();
            List<Integer> fourth = new ArrayList<>();
            List<Integer> firstc = new ArrayList<>();
            List<Integer> secondc = new ArrayList<>();
            List<Integer> thirdc = new ArrayList<>();
            List<Integer> fourthc = new ArrayList<>();
            boolean belongs = false;
            JSONArray mylist = new JSONArray();
            char[] pref = new char[6];
            pref = priority.toCharArray();
            //find 1s 2s 3s 4s
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '1') {
                    first.add(i);
                }
            }
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '2') {
                    second.add(i);
                }
            }
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '3') {
                    third.add(i);
                }
            }
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '4') {
                    fourth.add(i);
                }
            }

            String query1 = "SELECT * FROM " + table_name1;
            JSONObject result = new JSONObject();
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
                String[] objp = new String[param - 1];
                for (int i = 0; i < param - 1; i++) {
                    objp[i] = rsmd.getColumnName(i + 4);
                }
                int allobj = columnsNumber - param - 2;
                String[] objn = new String[allobj];
                for (int i = 0; i < allobj; i++) {
                    objn[i] = rsmd.getColumnName(3 + i + param);
                }
                //get obj names to perform the join query
                String obNames = "";
                for (int i = 0; i < allobj; i++) {
                    obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + " ";
                }
                String parNames = "";
                for (int i = 1; i < param; i++) {
                    parNames += "," + table_name1 + "." + rsmd.getColumnName(3 + i) + " ";
                }

                String whole = "SELECT " + table_name1 + ".ID " + obNames + " FROM " + table_name1 + " WHERE ";
                for (int i = 0; i < first.size(); i++) {
                    whole += "" + objn[first.get(i)] + " BETWEEN 90 AND 100";
                    if (first.size() - i > 1) {
                        whole += " AND ";
                    }
                }
                System.out.println(whole);
                PreparedStatement stmf = conn.prepareStatement(whole);
                ResultSet resmf = stmf.executeQuery();
                while (resmf.next()) {
                    firstc.add(resmf.getInt(1));
                }
                if (firstc.size() < 3) {
                    for (int tmp : firstc) {
                        if (tmp == id) {
                            belongs = true;
                        }
                    }
                    if (!belongs) {
                        firstc.add(id);
                    }
                    mylist = ret2(conn, firstc, table_name1, table_name2, parNames, obNames);
                } else {
                    resmf.beforeFirst();
                    while (resmf.next()) {
                        double min = resmf.getDouble(first.get(0) + 2);
                        for (int temp1 : first) {
                            if (min > resmf.getDouble(temp1 + 2)) {
                                min = resmf.getDouble(temp1 + 2);
                            }
                        }
                        if (!second.isEmpty()) {
                            boolean issmaller = true;
                            double min2 = resmf.getDouble(second.get(0) + 2);
                            for (int temp2 : second) {
                                if (min2 > resmf.getDouble(temp2 + 2)) {
                                    min2 = resmf.getDouble(temp2 + 2);
                                }
                                if (resmf.getDouble(temp2 + 2) < min) {
                                    issmaller = issmaller & true;
                                } else {
                                    issmaller = issmaller & false;
                                }
                            }
                            if (issmaller) {
                                secondc.add(resmf.getInt(1));

                                if (!third.isEmpty()) {

                                    double min3 = resmf.getDouble(third.get(0) + 2);
                                    for (int temp3 : third) {
                                        if (min3 > resmf.getDouble(temp3 + 2)) {
                                            min3 = resmf.getDouble(temp3 + 2);
                                        }
                                        if (resmf.getDouble(temp3 + 2) < min2) {
                                            issmaller = issmaller & true;
                                        } else {
                                            issmaller = issmaller & false;
                                        }
                                    }
                                    if (issmaller) {
                                        thirdc.add(resmf.getInt(1));

                                        if (!fourth.isEmpty()) {

                                            double min4 = resmf.getDouble(fourth.get(0) + 2);
                                            for (int temp4 : fourth) {
                                                if (min4 > resmf.getDouble(temp4 + 2)) {
                                                    min4 = resmf.getDouble(temp4 + 2);
                                                }
                                                if (resmf.getDouble(temp4 + 2) < min3) {
                                                    issmaller = issmaller & true;
                                                } else {
                                                    issmaller = issmaller & false;
                                                }
                                            }
                                            if (issmaller) {
                                                fourthc.add(resmf.getInt(1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (fourthc.size() > 2) {
                        for (int tmp : fourthc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
                        if (!belongs) {
                            fourthc.add(id);
                        }
                        mylist = ret2(conn, fourthc, table_name1, table_name2, parNames, obNames);
                    } else if (thirdc.size() > 2) {
                        for (int tmp : thirdc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
                        if (!belongs) {
                            thirdc.add(id);
                        }
                        mylist = ret2(conn, thirdc, table_name1, table_name2, parNames, obNames);
                    } else if (secondc.size() > 2) {
                        for (int tmp : secondc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
                        if (!belongs) {
                            secondc.add(id);
                        }
                        mylist = ret2(conn, secondc, table_name1, table_name2, parNames, obNames);
                    } else {
                        for (int tmp : firstc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
                        if (!belongs) {
                            firstc.add(id);
                        }
                        mylist = ret2(conn, firstc, table_name1, table_name2, parNames, obNames);
                    }
                }
                stmt.close();
                conn.close();
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date today = Calendar.getInstance().getTime();
                String reportDate = df.format(today);
                result.put("belongs", Boolean.toString(belongs));
                result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);

            } catch (SQLException ex) {
                System.out.print(ex.getMessage());
            } catch (JSONException ex) {
                Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
            }
            ResponseBuilder builder = Response.ok(result.toString());
            return builder.build();
        }
    }

    @GET
    @Path("/distance1/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDistance(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("prior") String priority, @QueryParam("id") int id) {
        {

            Connection conn = dbUtils.getConnection();
            List<Integer> first = new ArrayList<>();
            List<Integer> second = new ArrayList<>();
            List<Integer> third = new ArrayList<>();
            List<Integer> fourth = new ArrayList<>();
            List<Integer> firstc = new ArrayList<>();
            List<Integer> secondc = new ArrayList<>();
            List<Integer> thirdc = new ArrayList<>();
            List<Integer> fourthc = new ArrayList<>();
            boolean belongs = false;
            JSONArray mylist = new JSONArray();
            char[] pref = new char[6];
            pref = priority.toCharArray();
            //find 1s 2s 3s 4s
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '1') {
                    first.add(i);
                }
            }
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '2') {
                    second.add(i);
                }
            }
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '3') {
                    third.add(i);
                }
            }
            for (int i = 0; i < pref.length; i++) {
                if (pref[i] == '4') {
                    fourth.add(i);
                }
            }

            String query1 = "SELECT * FROM " + table_name1;
            JSONObject result = new JSONObject();
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
                String[] objp = new String[param - 1];
                for (int i = 0; i < param - 1; i++) {
                    objp[i] = rsmd.getColumnName(i + 4);
                }
                int allobj = columnsNumber - param - 2;
                String[] objn = new String[allobj];
                for (int i = 0; i < allobj; i++) {
                    objn[i] = rsmd.getColumnName(3 + i + param);
                }
                //get obj names to perform the join query
                String obNames = "";
                for (int i = 0; i < allobj; i++) {
                    obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 3 + i) + " ";
                }
                String parNames = "";
                for (int i = 1; i < param; i++) {
                    parNames += "," + table_name1 + "." + rsmd.getColumnName(3 + i) + " ";
                }

                String whole = "SELECT " + table_name1 + ".ID " + obNames + " FROM " + table_name1 + " WHERE ";
                for (int i = 0; i < first.size(); i++) {
                    whole += "" + objn[first.get(i)] + " BETWEEN 90 AND 100";
                    if (first.size() - i > 1) {
                        whole += " AND ";
                    }
                }
                System.out.println(whole);
                PreparedStatement stmf = conn.prepareStatement(whole);
                ResultSet resmf = stmf.executeQuery();
                while (resmf.next()) {
                    firstc.add(resmf.getInt(1));
                }
                if (firstc.size() < 3) {
                    mylist = ret3(conn, firstc, table_name1, table_name2, parNames, obNames, id);
                } else {
                    resmf.beforeFirst();
                    while (resmf.next()) {
                        double min = resmf.getDouble(first.get(0) + 2);
                        for (int temp1 : first) {
                            if (min > resmf.getDouble(temp1 + 2)) {
                                min = resmf.getDouble(temp1 + 2);
                            }
                        }
                        if (!second.isEmpty()) {
                            boolean issmaller = true;
                            double min2 = resmf.getDouble(second.get(0) + 2);
                            for (int temp2 : second) {
                                if (min2 > resmf.getDouble(temp2 + 2)) {
                                    min2 = resmf.getDouble(temp2 + 2);
                                }
                                if (resmf.getDouble(temp2 + 2) < min) {
                                    issmaller = issmaller & true;
                                } else {
                                    issmaller = issmaller & false;
                                }
                            }
                            if (issmaller) {
                                secondc.add(resmf.getInt(1));

                                if (!third.isEmpty()) {

                                    double min3 = resmf.getDouble(third.get(0) + 2);
                                    for (int temp3 : third) {
                                        if (min3 > resmf.getDouble(temp3 + 2)) {
                                            min3 = resmf.getDouble(temp3 + 2);
                                        }
                                        if (resmf.getDouble(temp3 + 2) < min2) {
                                            issmaller = issmaller & true;
                                        } else {
                                            issmaller = issmaller & false;
                                        }
                                    }
                                    if (issmaller) {
                                        thirdc.add(resmf.getInt(1));

                                        if (!fourth.isEmpty()) {

                                            double min4 = resmf.getDouble(fourth.get(0) + 2);
                                            for (int temp4 : fourth) {
                                                if (min4 > resmf.getDouble(temp4 + 2)) {
                                                    min4 = resmf.getDouble(temp4 + 2);
                                                }
                                                if (resmf.getDouble(temp4 + 2) < min3) {
                                                    issmaller = issmaller & true;
                                                } else {
                                                    issmaller = issmaller & false;
                                                }
                                            }
                                            if (issmaller) {
                                                fourthc.add(resmf.getInt(1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (fourthc.size() > 2) {
                        mylist = ret3(conn, fourthc, table_name1, table_name2, parNames, obNames, id);
                    } else if (thirdc.size() > 2) {
                        mylist = ret3(conn, thirdc, table_name1, table_name2, parNames, obNames, id);
                    } else if (secondc.size() > 2) {
                        mylist = ret3(conn, secondc, table_name1, table_name2, parNames, obNames, id);
                    } else {
                        mylist = ret3(conn, firstc, table_name1, table_name2, parNames, obNames, id);
                    }
                }
                stmt.close();
                conn.close();
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date today = Calendar.getInstance().getTime();
                String reportDate = df.format(today);
                result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
            } catch (SQLException ex) {
                System.out.print(ex.getMessage());
            } catch (JSONException ex) {
                Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
            }

            ResponseBuilder builder = Response.ok(result.toString());
            return builder.build();
        }
    }

    private JSONArray ret3(Connection conn, List<Integer> idList, String table_name1, String table_name2, String parNames, String obNames, int idc) throws JSONException {

        JSONArray mylist = new JSONArray();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol1 = new ArrayList<>();
        boolean[] myminmax = {true, false, false, false, true, true};
        try {
            int param = 1;

            String joinQuery = "SELECT " + table_name2 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".dominatedbypool "
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + table_name1 + ".ID=?";

            //     System.out.println(joinQuery2);
            PreparedStatement stmt = conn.prepareStatement(joinQuery);
            stmt.setInt(1, idc);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            //objective parameters -criteria 
            String[] objp = new String[param - 1];
            for (int i = 0; i < param - 1; i++) {
                objp[i] = rsmd.getColumnName(i + 4);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(3 + i + param);
            }
            //get obj names to perform the join query

            int beforeobj = param + 2;
            policy comp = new policy(allobj, 0);
            while (res.next()) {
                comp.setID(res.getInt(1));
                double[] obj_values = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    obj_values[i] = res.getDouble(beforeobj + i);
                }
                comp.setObjectives(obj_values);
            }
            stmt.close();

            for (int id : idList) {

                String joinQuery2 = "SELECT " + table_name2 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".dominatedbypool "
                        + " FROM " + table_name2
                        + " INNER JOIN " + table_name1
                        + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                        + " WHERE " + table_name1 + ".ID=?";

                System.out.println(joinQuery2);
                PreparedStatement stm = conn.prepareStatement(joinQuery2);
                stm.setInt(1, id);
                ResultSet resm = stm.executeQuery();

                while (resm.next()) {
                    policy pol = new policy(allobj, 0);
                    pol.setID(resm.getInt(1));
                    double[] obj_values = new double[allobj];
                    for (int i = 0; i < allobj; i++) {
                        obj_values[i] = resm.getDouble(beforeobj + i);
                    }
                    pol.setObjectives(obj_values);
                    mypol.add(pol);
                }
                stm.close();
            }
//calculate Angle distance
            double top_sum = 0, top_sum1 = 0;
            double bot_sum1 = 0, bot_sum2 = 0;

            for (policy temp : mypol) {
                //ABC angle d- dimensions
                //Euclidean distance d-dimensions
                JSONObject policy = new JSONObject();
                policy.put("ID", temp.getID());
//                for (int i = 0; i < allobj; i++) {
//                    policy.put(objn[i], temp.getObjectives()[i]);
//                }
                for (int i = 0; i < temp.getObjectives().length; i++) {
                    top_sum += comp.getObjectives()[i] * temp.getObjectives()[i];
                    bot_sum1 += Math.pow(comp.getObjectives()[i], 2);
                    bot_sum2 += Math.pow(temp.getObjectives()[i], 2);
                    top_sum1 += Math.pow(comp.getObjectives()[i] - temp.getObjectives()[i], 2);

                }
                policy.put("Euclidean distance from policy", Math.sqrt(top_sum));
                policy.put("Angle distance from policy", top_sum / (Math.sqrt(bot_sum1) * Math.sqrt(bot_sum2)));
                mylist.put(policy);
            }

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return mylist;
    }

}
