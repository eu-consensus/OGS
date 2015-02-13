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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import methods.Join_keep;
import methods.dbUtils;
import methods.maj;
import methods.methods.MajComparator;
import static methods.methods.find_space;
import static methods.methods.merge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/GameBiofuels")
@Stateless
public class RestGameBiofuels {

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
            stmt.close();
            conn.close();

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
            stmt.close();
            conn.close();

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
                preferenceOrder.put(objn[i], new HashMap<String, Double>());
            }
            //put values in tables
            for (Join_keep temp : mylist) {

                char[] chr = temp.getMyorder().toCharArray();
                for (int i = 0; i < chr.length; i++) {
                    if (preferenceOrder.get(objn[i]).containsKey(chr[i] + "")) {
                        preferenceOrder.get(objn[i]).put(chr[i] + "", preferenceOrder.get(objn[i]).get(chr[i] + "") + temp.getChosen());
                    } else {
                        preferenceOrder.get(objn[i]).put(chr[i] + "", (double) temp.getChosen());
                    }
                }
            }
//            make percentages
            for (int i = 0; i < allobj; i++) {
                for (Map.Entry<String, Double> entry : preferenceOrder.get(objn[i]).entrySet()) {
                    entry.setValue(entry.getValue() / total);
                }
            }
//System.out.print(preferenceOrder.get("objective" +Integer.toString(objective_number)).toString());
//                result.put("objective" +Integer.toString(objective_number), preferenceOrder.get("objective" +Integer.toString(objective_number)).toString());
            for (int i = 0; i < allobj; i++) {
                JSONObject myjson = new JSONObject();
                for (Map.Entry<String, Double> entry : preferenceOrder.get(objn[i]).entrySet()) {
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
            HashMap<String, List<maj>> preferenceOrder = new HashMap<>();
//            create hash map for each priority
            for (int i = 0; i < allobj; i++) {
                preferenceOrder.put(objn[i], new ArrayList<>());
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
                Collections.sort(temp2, new MajComparator());
                double[] spaces = new double[3];
                spaces = find_space(temp2, total);
                tjson.put("begin", spaces[0]);
                tjson.put("end", spaces[1]);
                tjson.put("percentage", spaces[2]);
                result.put(entry.getKey(), tjson);
            }
            conn.close();
            stmtj.close();
            stmt.close();

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/PreferencebyID/{table_name2}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response PreferencebyID(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("id") int id) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT chosen FROM " + table_name2;
        String query2 = "SELECT chosen FROM " + table_name2 + " WHERE P_ID=?";
        JSONObject result = new JSONObject();
        int mychosen = 0;
        int total = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement(query2);
            stmt.setInt(1, id);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                mychosen = res.getInt("chosen");
            }
            PreparedStatement stmtj = conn.prepareStatement(query1);
            ResultSet resj = stmtj.executeQuery();

            while (resj.next()) {
                total += resj.getInt("chosen");
            }
            result.put("percentage", (double) mychosen / total);
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
    @Path("/PreferencebyOrder/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response PreferencebyOrder(@Context HttpServletRequest request, @PathParam("table_name2") String table_name2) {
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT chosen,myorder FROM " + table_name2;
        JSONObject result = new JSONObject();
        int mychosen = 0;
        int total = 0;
        HashMap<String, Integer> myHash = new HashMap<>();

        try {
            PreparedStatement stmt = conn.prepareStatement(query1);
            ResultSet res = stmt.executeQuery();
            while (res.next()) {
                total += res.getInt("chosen");
                if (myHash.containsKey(res.getString("myorder"))) {
                    myHash.put(res.getString("myorder"), myHash.get(res.getString("myorder")) + res.getInt("chosen"));
                } else {
                    myHash.put(res.getString("myorder"), res.getInt("chosen"));
                }
            }

            for (Map.Entry<String, Integer> entry : myHash.entrySet()) {
                result.put(entry.getKey(), (double) entry.getValue() / total);
            }
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
    @Path("/prefscore/{table_name1}/{table_name2}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response prefscore(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("id") int id) {
        double score = 0;
        double THRESHOLD = 0.4;
        Connection conn = dbUtils.getConnection();
        String query1 = "SELECT * FROM " + table_name1;
        JSONObject retjson = new JSONObject();
        try {
            int total = 0;
            HashMap<String, Integer> myHash = new HashMap<>();
            HashMap<String, HashMap<String, Double>> preferenceOrder = new HashMap<>();
            HashMap<String, List<maj>> preference3 = new HashMap<>();
            JSONObject result = new JSONObject();
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
            String select2 = "" + table_name1 + ".policy," + obNames + table_name2 + ".myorder, " + table_name2 + ".chosen";
            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " LEFT JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID"
                    + " WHERE " + table_name1 + ".ID=?";
            PreparedStatement stm = conn.prepareStatement(joinQuery2);
            stm.setInt(1, id);
            ResultSet resm = stm.executeQuery();
            Join_keep userkeep = new Join_keep(allobj);
            if (resm.next()) {

                userkeep.setPolicy(resm.getString("policy"));
                double[] data = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    data[i] = resm.getDouble(objn[i]);
                }
                userkeep.setData(data);
                userkeep.setChosen(resm.getInt("chosen"));
                userkeep.setMyorder(resm.getString("myorder"));
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

            for (Join_keep temp : mylist) {
                total += temp.getChosen();
            }

//            create hash map for each priority
            for (int i = 0; i < allobj; i++) {
                preferenceOrder.put(objn[i], new HashMap<String, Double>());
            }
            //put values in tables
            for (Join_keep temp : mylist) {

                char[] chr = temp.getMyorder().toCharArray();
                for (int i = 0; i < chr.length; i++) {
                    if (preferenceOrder.get(objn[i]).containsKey(chr[i] + "")) {
                        preferenceOrder.get(objn[i]).put(chr[i] + "", preferenceOrder.get(objn[i]).get(chr[i] + "") + temp.getChosen());
                    } else {
                        preferenceOrder.get(objn[i]).put(chr[i] + "", (double) temp.getChosen());
                    }
                }
                if (myHash.containsKey(temp.getMyorder())) {
                    myHash.put(temp.getMyorder(), myHash.get(temp.getMyorder()) + temp.getChosen());
                } else {
                    myHash.put(temp.getMyorder(), temp.getChosen());
                }
            }
//            make percentages

            for (int i = 0; i < allobj; i++) {
                double max_value = 0;
                Iterator<Map.Entry<String, Double>> iterator = preferenceOrder.get(objn[i]).entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Double> myentry = iterator.next();
                    if (max_value < myentry.getValue()) {
                        max_value = myentry.getValue();
                    } else {
                        iterator.remove();
                    }
                }
                iterator = preferenceOrder.get(objn[i]).entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Double> myentry = iterator.next();
                    if (max_value > myentry.getValue()) {
                        iterator.remove();
                    }
                }
            }
            for (int i = 0; i < allobj; i++) {
                Iterator<Map.Entry<String, Double>> iterator = preferenceOrder.get(objn[i]).entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Double> myentry = iterator.next();
                    if ((userkeep.getMyorder().toCharArray()[i] + "").equals(myentry.getKey())) {
                        score += 0.25;
                        System.out.print("got 0.25 from priority in this objective");
                    }
                }
            }
            Iterator it = preferenceOrder.entrySet().iterator();
            int max_order = 0;
            it = myHash.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (max_order < (int) pairs.getValue()) {
                    max_order = (int) pairs.getValue();
                } else {
                    it.remove();
                }
            }
            //remove entries smaller since we now have the biggest
            it = myHash.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (max_order > (int) pairs.getValue()) {
                    it.remove();
                }
            }
            it = myHash.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                int val = (int) pairs.getValue();
                double perc = (double) val / total;
                if (userkeep.getMyorder().equals(pairs.getKey()) && perc >= THRESHOLD) {
                    score += 0.5;
                    System.out.print("got 0.5 from complete order");
                }
            }
//            create hash map for each priority
            for (int i = 0; i < allobj; i++) {
                preference3.put(objn[i], new ArrayList<>());
            }
            //put values in tables
            for (Join_keep temp : mylist) {

                double[] dt = temp.getData();
                for (int i = 0; i < dt.length; i++) {
                    maj tmaj = new maj();
                    tmaj.setValue(dt[i]);
                    tmaj.setCount(temp.getChosen());
                    preference3.get(objn[i]).add(tmaj);
                }
            }

            for (Map.Entry<String, List<maj>> entry : preference3.entrySet()) {
                JSONObject tjson = new JSONObject();
                List<maj> temp2 = entry.getValue();
                temp2 = merge(temp2);
                Collections.sort(temp2, new MajComparator());
                double[] spaces = new double[3];
                spaces = find_space(temp2, total);
                tjson.put("begin", spaces[0]);
                tjson.put("end", spaces[1]);
                tjson.put("percentage", spaces[2]);
                result.put(entry.getKey(), tjson);
            }

            for (int i = 0; i < allobj; i++) {
                if (userkeep.getData()[i] >= result.getJSONObject(objn[i]).getDouble("begin") && userkeep.getData()[i] <= result.getJSONObject(objn[i]).getDouble("end") && result.getJSONObject(objn[i]).getDouble("percentage") >= THRESHOLD) {
                    score += 0.25;
                    System.out.print("got 0.25 for being within range in " + objn[i]);
                }
            }

            stmtj.close();
            stmt.close();
            conn.close();

            retjson.put("prefscore", score * 10);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        } catch (JSONException ex) {
            Logger.getLogger(RestGameBiofuels.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResponseBuilder builder = Response.ok(retjson.toString());
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
            for(int i=0;i<param;i++){
                objp[i]=rsmd.getColumnName(i+1);
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
            String select2 = "" + table_name1 + ".ID,"+ table_name1 + ".policy," + parNames +obNames + table_name2 + ".chosen";
            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " LEFT JOIN " + table_name1
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
                for (int i = 3; i < param + 3; i++) {
                    policy.put(rsmd.getColumnName(i), resm.getString(i));
                }
                for (int i = param + 2; i < allobj + param + 2; i++) {
                    policy.put(rsmd.getColumnName(i), resm.getDouble(i));
                }

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
}
