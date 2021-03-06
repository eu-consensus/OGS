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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import methods.DBScan;
import methods.Join_keep;
import methods.Kmeans;
import methods.Spectral;
import methods.dbUtils;
import methods.maj;
import methods.methods;
import methods.methods.MajComparator;
import static methods.methods.find_space;
import static methods.methods.maximizationofObjective;
import static methods.methods.merge;
import static methods.methods.minimizationofObjective;
import static methods.methods.paretoG;
import methods.policy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/GameBiofuels")
@Stateless
public class RestGameBiofuels {

    String[] parameters = {"EU_biofuel_policies", "Source_of_EU_biofuel_policies", "Solid_biomass_demand_EU", "Bioenergy_scenario_ROW", " LUC_regulations", "Level_of_biodiversity_protection", "Change_in_food_diets", "Yield_development"};

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
                    + " INNER JOIN " + table_name1
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
                preference3.put(objn[i], new ArrayList<maj>());
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
                policy.put("objscore", resm.getInt(allobj + param + 2));
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

                String joinQuery2 = "SELECT " + table_name2 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".objscore," + table_name2 + ".myorder"
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
                    System.out.println(resm.getString(allobj + param + 3));
                    mylist.put(policy);
                }
                stm.close();
            }

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return mylist;
    }

    @GET
    @Path("/increaseChosen/{table_name1}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response increaseChosen(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("id") int id) {
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
    //returns solutions with wildcard sql query in DB in String Array ?ids=24041&ids=24117
    //returns table_name1 id
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
                        if (cr.contains("-OR-")) {
                            String[] ms = cr.split("-OR-");
                            sentence1 += "(";
                            for (int m = 0; m < ms.length; m++) {
                                if (m < ms.length - 1) {
                                    sentence1 += "" + table_name1 + "." + objp[i] + " ='" + ms[m] + "' OR ";
                                } else {
                                    sentence1 += table_name1 + "." + objp[i] + " ='" + ms[m] + "'";
                                }
                            }
                            sentence1 += ")";
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
    public Response getOptimalbyCategory(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("category_id") String category) {
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
    public Response getOptimalbyPriority(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("priority") String priority) {

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

            Connection conn = dbUtils.getConnection();
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
                whole += "" + objn[first.get(i)] + " BETWEEN 85 AND 100";
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
        //on actual data  boolean[] myminmax = {false, false, false, true};
        boolean[] myminmax = {true, true, true, true};
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
                objp[i] = rsmd.getColumnName(i + 3);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(2 + i + param);
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

    private JSONObject ret7(Connection conn, String table_name1, String table_name2, String parNames, String obNames, List<Integer> idList1, List<Integer> idList2, int polIDer, boolean belongs) throws JSONException {
        int counter = idList1.size();
        JSONArray mylist = new JSONArray();
        JSONArray dominatedbypoolList = new JSONArray();
        JSONArray dominatedbycategoryList = new JSONArray();
        JSONArray dominatedbypooloptimalList = new JSONArray();
        JSONObject finalL = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol1 = new ArrayList<>();
        List<policy> mypol3 = new ArrayList<>();
        List<policy> mypol4 = new ArrayList<>();
        List<policy> mypol5=new ArrayList<>();
        List<policy> optpol=new ArrayList<>();
        int posize = 0;//pool optimal in this category
        int cosize = 0;//category optimal in this category
        //on actual data  boolean[] myminmax = {false, false, false, true};
        boolean[] myminmax = {true, true, true, true};
        try {
            int param = 1;

            String joinQuery = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + parNames + obNames + "," + table_name2 + ".dominatedbypool "
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + table_name1 + ".ID=?";

            //     System.out.println(joinQuery2);
            PreparedStatement stmt = conn.prepareStatement(joinQuery);
            stmt.setInt(1, polIDer);
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
                objp[i] = rsmd.getColumnName(i + 3);
            }
            int allobj = columnsNumber - param - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(2 + i + param);
            }
            //get obj names to perform the join query
            int beforeobj = param + 2;
            policy polID = new policy(allobj, 0);
            while (res.next()) {

                polID.setID(res.getInt(1));
                polID.setPolicyName(res.getString(2));
                double[] obj_values = new double[allobj];

                String[] objparam = new String[param - 1];
                for (int i = 0; i < param - 1; i++) {
                    objparam[i] = res.getString(i + 3);
                }
                for (int i = 0; i < allobj; i++) {
                    obj_values[i] = res.getDouble(beforeobj + i);
                }
                polID.setObjectives(obj_values);
                polID.setPolicyParameters(objparam);
                polID.setDominated(res.getInt(columnsNumber));
            }

            for (int id : idList1) {

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
                    if (resm.getInt(columnsNumber) == 0) {
                        posize++;
                       optpol.add(pol); 
                    }
                    mypol.add(pol);
                }
                stm.close();
            }
//            In list 1 (my prioritization category) i find the dominated list for this specific policy ID
            List<Integer> idList3 = getDominatedbyList(mypol, polID, myminmax);
//           I later add this policyID in the list so as to assign score points
            if (!belongs) {
                mypol.add(polID);
            }
            mypol1 = methods.paretoG(mypol, myminmax);
            List<policy> mypol2 = setScoreCAT(mypol1, belongs);
            for (policy temp : mypol2) {
                if (temp.getDominatedbycategory() == 0) {
                    cosize++;
                }
                if (temp.getID() == polIDer) {
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

            }
            for (int id : idList2) {

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
                    mypol3.add(pol);
                }
                stm.close();
            }
            for (policy temp : mypol3) {

                JSONObject policy = new JSONObject();
                policy.put("ID", temp.getID());
                policy.put("policy", temp.getPolicyName());
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], temp.getPolicyParameters()[i]);
                }
                for (int i = 0; i < allobj; i++) {
                    policy.put(objn[i], temp.getObjectives()[i]);
                }
                dominatedbypoolList.put(policy);

            }
            for (int id : idList3) {

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
                    mypol4.add(pol);
                }
                stm.close();
            }
            for (policy temp : mypol4) {
                JSONObject policy = new JSONObject();
                policy.put("ID", temp.getID());
                policy.put("policy", temp.getPolicyName());
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], temp.getPolicyParameters()[i]);
                }
                for (int i = 0; i < allobj; i++) {
                    policy.put(objn[i], temp.getObjectives()[i]);
                }
                dominatedbycategoryList.put(policy);
            }

            List<Integer> idList5 = getDominatedbyList(optpol, polID, myminmax);
             for (int id : idList5) {

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
                    mypol5.add(pol);
                }
                stm.close();
            }
            for (policy temp : mypol5) {
                JSONObject policy = new JSONObject();
                policy.put("ID", temp.getID());
                policy.put("policy", temp.getPolicyName());
                for (int i = 0; i < param - 1; i++) {
                    policy.put(parameters[i], temp.getPolicyParameters()[i]);
                }
                for (int i = 0; i < allobj; i++) {
                    policy.put(objn[i], temp.getObjectives()[i]);
                }
                dominatedbypooloptimalList.put(policy);
            }
             
            finalL.put("policy", mylist);
            finalL.put("poolDominators", dominatedbypoolList);
            finalL.put("pooloptimalDominators", dominatedbypooloptimalList);
            finalL.put("categoryDominators", dominatedbycategoryList);
            finalL.put("counter", counter);
            finalL.put("belongs", belongs);
            if (dominatedbypoolList.length() == 0 & belongs) {
                finalL.put("pobelongs", true);
            } else {
                finalL.put("pobelongs", false);
            }
            if (dominatedbycategoryList.length() == 0 & belongs) {
                finalL.put("cobelongs", true);
            } else {
                finalL.put("cobelongs", false);
            }
            if (!belongs & dominatedbycategoryList.length() == 0) {
                cosize--;
            }
            finalL.put("cosize", cosize);
            finalL.put("posize", posize);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return finalL;
    }

    @GET
    @Path("/score1/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    //it collects this specific prioritization (4 spots) and collects if not many solutions return more than one category
    //there i check for optimallity within its own category in this mixed category return boolean true and score from this category if inside otherwise return boolean false and closeness score 
    public Response getScorebyID(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("prior") String priority, @QueryParam("id") int id) {
        {
            boolean[] myminmax = {true, true, true, true};
//get achievement_table ID from percentage3
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
            JSONObject mylist = new JSONObject();
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
            List<policy> pool = new ArrayList<>();
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
                while (res.next()) {
                    policy pol = new policy(allobj, 0);
                    pol.setID(res.getInt(1));
//                    pol.setPolicyName(res.getString(2));
                    double[] obj_values = new double[allobj];
                    for (int i = 0; i < allobj; i++) {
                        obj_values[i] = res.getDouble(param + 3 + i);
                    }
                    pol.setObjectives(obj_values);
                    pool.add(pol);
                }

                String query2 = "SELECT * FROM " + table_name1 + " WHERE ID=?";
                PreparedStatement st2 = conn.prepareStatement(query2);
                st2.setInt(1, id);
                ResultSet res2 = st2.executeQuery();
                policy polID = new policy(allobj, 0);
                while (res2.next()) {
                    polID.setID(res2.getInt(1));
//                    pol.setPolicyName(res.getString(2));
                    double[] obj_values = new double[allobj];
                    for (int i = 0; i < allobj; i++) {
                        obj_values[i] = res2.getDouble(param + 3 + i);
                    }
                    polID.setObjectives(obj_values);
                }
                List<Integer> dominatedbypoolList = getDominatedbyList(pool, polID, myminmax);

                String whole = "SELECT " + table_name1 + ".ID " + obNames + " FROM " + table_name1 + " WHERE ";
                for (int i = 0; i < first.size(); i++) {
                    whole += "" + objn[first.get(i)] + " BETWEEN 85 AND 100";
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
//                    if (!belongs) {
//                        firstc.add(id);
//                    }

                    mylist = ret7(conn, table_name1, table_name2, parNames, obNames, firstc, dominatedbypoolList, id, belongs);
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
//                        if (!belongs) {
//                            fourthc.add(id);
//                        }
                        mylist = ret7(conn, table_name1, table_name2, parNames, obNames, fourthc, dominatedbypoolList, id, belongs);
                    } else if (thirdc.size() > 2) {
                        for (int tmp : thirdc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
//                        if (!belongs) {
//                            thirdc.add(id);
//                        }
                        mylist = ret7(conn, table_name1, table_name2, parNames, obNames, thirdc, dominatedbypoolList, id, belongs);
                    } else if (secondc.size() > 2) {
                        for (int tmp : secondc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
//                        if (!belongs) {
//                            secondc.add(id);
//                        }
                        mylist = ret7(conn, table_name1, table_name2, parNames, obNames, secondc, dominatedbypoolList, id, belongs);
                    } else {
                        for (int tmp : firstc) {
                            if (tmp == id) {
                                belongs = true;
                            }
                        }
//                        if (!belongs) {
//                            firstc.add(id);
//                        }
                        mylist = ret7(conn, table_name1, table_name2, parNames, obNames, firstc, dominatedbypoolList, id, belongs);
                    }
                }
                stmt.close();
                conn.close();
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date today = Calendar.getInstance().getTime();
                String reportDate = df.format(today);
                result.put("results", mylist);
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
    //table name1 percentages
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
                    whole += "" + objn[first.get(i)] + " BETWEEN 85 AND 100";
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
//returns euclidean distance from all category and this point ------>mean average value
//percentage data

    private JSONArray ret3(Connection conn, List<Integer> idList, String table_name1, String table_name2, String parNames, String obNames, int idc) throws JSONException {

        JSONArray mylist = new JSONArray();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol1 = new ArrayList<>();
        try {
            int param = 1;

            String joinQuery = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + parNames + obNames + " , " + table_name2 + ".dominatedbypool"
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

                String joinQuery2 = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + parNames + obNames + " , " + table_name2 + ".dominatedbypool"
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
                    pol.setDominated(resm.getInt(columnsNumber));
                    mypol.add(pol);
                }
                stm.close();
            }
//calculate Angle distance
            double top_sum = 0, top_sum1 = 0, top_sum2 = 0;
            int total_optimal_pool = 0;
            int total_optimal_category = 0;
            double euclid = 0;
            List<policy> mypol2 = new ArrayList<>();
            boolean[] myminmax = {true, true, true, true};
            mypol2 = paretoG(mypol, myminmax);
            for (policy temp : mypol2) {
                //ABC angle d- dimensions
                //Euclidean distance d-dimensions
                JSONObject policy = new JSONObject();
                policy.put("ID", temp.getID());
//                for (int i = 0; i < allobj; i++) {
//                    policy.put(objn[i], temp.getObjectives()[i]);
//                }

                for (int i = 0; i < temp.getObjectives().length; i++) {
                    if (temp.getDominated() == 0) {
                        top_sum += Math.pow(comp.getObjectives()[i] - temp.getObjectives()[i], 2);
                        total_optimal_pool++;
                    }
                    if (temp.getDominatedbycategory() == 0) {
                        top_sum2 += Math.pow(comp.getObjectives()[i] - temp.getObjectives()[i], 2);
                        total_optimal_category++;
                    }
//                    top_sum += comp.getObjectives()[i] * temp.getObjectives()[i];
//                    bot_sum1 += Math.pow(comp.getObjectives()[i], 2);
//                    bot_sum2 += Math.pow(temp.getObjectives()[i], 2);
                    top_sum1 += Math.pow(comp.getObjectives()[i] - temp.getObjectives()[i], 2);

                }
                euclid += Math.sqrt(top_sum1);
//                angle += top_sum / (Math.sqrt(bot_sum1) * Math.sqrt(bot_sum2));
//                policy.put("Euclidean distance from policy", euclid);
//                policy.put("Angle distance from policy",top_sum/(Math.sqrt(bot_sum1)*Math.sqrt(bot_sum2)));
//                mylist.put(policy);
            }
            JSONObject policy = new JSONObject();
            policy.put("group distance", euclid / mypol.size());
            if (total_optimal_pool != 0) {
                policy.put("pool optimal distance", Math.sqrt(top_sum) / total_optimal_pool);
            } else {
                policy.put("pool optimal distance", "none");
            }
            if (total_optimal_category != 0) {
                policy.put("category optimal distance", Math.sqrt(top_sum2) / total_optimal_category);
            } else {
                policy.put("category optimal distance", "none");
            }
            mylist.put(policy);
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return mylist;
    }

    @GET
    @Path("/DBSCAN/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDBSCAN(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("eps") double eps, @QueryParam("minPts") int minPts) {

        String query1 = "SELECT * FROM " + table_name1;
        boolean[] myminmax = {false, false, false, true};
        double[] best, worse;
        JSONObject result = new JSONObject();
        JSONObject mylist = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        try {
            Connection conn = dbUtils.getConnection();
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
                objp[i] = rsmd.getColumnName(i + 3);
            }
            int allobj = columnsNumber - param - 1;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(2 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 2 + i) + " ";
            }

            String joinQuery2 = "SELECT " + table_name1 + ".ID" + obNames
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID";
            System.out.println(joinQuery2);
            PreparedStatement st = conn.prepareStatement(joinQuery2);
            ResultSet resm = st.executeQuery();

            best = new double[allobj];
            worse = new double[allobj];
            if (resm.next()) {
                for (int i = 0; i < allobj; i++) {
                    best[i] = resm.getDouble(2 + i);
                    worse[i] = resm.getDouble(2 + i);
                }
            }
            while (resm.next()) {
                double[] obj_valuestest = new double[allobj];
                for (int i = 0; i < allobj; i++) {

                    obj_valuestest[i] = resm.getDouble(2 + i);
                    if (myminmax[i]) {
                        if (best[i] < obj_valuestest[i]) {
                            best[i] = obj_valuestest[i];
                        }
                        if (worse[i] > obj_valuestest[i]) {
                            worse[i] = obj_valuestest[i];
                        }
                    } else {
                        if (best[i] > obj_valuestest[i]) {
                            best[i] = obj_valuestest[i];
                        }
                        if (worse[i] < obj_valuestest[i]) {
                            worse[i] = obj_valuestest[i];
                        }
                    }
                }
            }
            resm.beforeFirst();
            while (resm.next()) {
                policy pol = new policy(objn.length, 0);
                pol.setID(resm.getInt(1));
                double[] obj_values = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    if (worse[i] == best[i]) {
                        obj_values[i] = 1.0;
                    } else {
                        obj_values[i] = Math.abs(resm.getDouble(2 + i) - worse[i]) / Math.abs(best[i] - worse[i]);
                    }
                }
                pol.setObjectives(obj_values);
                mypol.add(pol);
            }
            DBScan myCluster = new DBScan(mypol, eps, minPts);
            myCluster.getClusters();
            Map<Integer, List<Integer>> clusterList = myCluster.cluster;
            Iterator it = clusterList.entrySet().iterator();
            String qq = "SELECT " + table_name2 + ".myorder FROM " + table_name2
                    + " INNER JOIN " + table_name1 + " ON " + table_name2 + ".P_ID= " + table_name1 + ".ID  WHERE "
                    + table_name1 + ".ID=?";
            int counter = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Integer> ml = (List<Integer>) pair.getValue();
                int key = (int) pair.getKey();
                JSONObject item = new JSONObject();
                for (int i : ml) {

                    PreparedStatement stm = conn.prepareStatement(qq);
                    stm.setInt(1, i);
                    ResultSet resmq = stm.executeQuery();
                    while (resmq.next()) {
                        item.put(Integer.toString(i), resmq.getString(1));
                    }
                }
                PreparedStatement stm = conn.prepareStatement(qq);
                stm.setInt(1, key);
                ResultSet resmq = stm.executeQuery();
                while (resmq.next()) {
                    item.put(Integer.toString(key), resmq.getString(1));
                }
                mylist.put("cluster " + Integer.toString(counter), item);
                counter++;

                stm.close();
            }
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
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
    @Path("/percentageDBSCAN/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getpercentageCluster(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("eps") double eps, @QueryParam("minPts") int minPts) {

        String query1 = "SELECT * FROM " + table_name1;
        boolean[] myminmax = {false, false, false, true};
        double[] best, worse;
        JSONObject result = new JSONObject();
        JSONObject mylist = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        try {
            Connection conn = dbUtils.getConnection();
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
//take Id of main table eg.World_2030 -which is the same as achievement_World_2030 aka table_name1.ID
            String joinQuery2 = "SELECT " + table_name1 + ".ID" + obNames
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID";
            System.out.println(joinQuery2);
            PreparedStatement st = conn.prepareStatement(joinQuery2);
            ResultSet resm = st.executeQuery();

            resm.beforeFirst();
            while (resm.next()) {
                policy pol = new policy(objn.length, 0);
                pol.setID(resm.getInt(1));
                double[] obj_values = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    obj_values[i] = resm.getDouble(2 + i);
                }
                pol.setObjectives(obj_values);
                mypol.add(pol);
            }
            DBScan myCluster = new DBScan(mypol, eps, minPts);
            myCluster.getClusters();
            Map<Integer, List<Integer>> clusterList = myCluster.cluster;
            Iterator it = clusterList.entrySet().iterator();
            String qq = "SELECT " + table_name2 + ".myorder FROM " + table_name2
                    + " INNER JOIN " + table_name1 + " ON " + table_name2 + ".P_ID= " + table_name1 + ".P_ID  WHERE "
                    + table_name1 + ".ID=?";
            int counter = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Integer> ml = (List<Integer>) pair.getValue();
                int key = (int) pair.getKey();
                JSONObject item = new JSONObject();
                for (int i : ml) {

                    PreparedStatement stm = conn.prepareStatement(qq);
                    stm.setInt(1, i);
                    ResultSet resmq = stm.executeQuery();
                    while (resmq.next()) {
                        item.put(Integer.toString(i), resmq.getString(1));
                    }
                }
                PreparedStatement stm = conn.prepareStatement(qq);
                stm.setInt(1, key);
                ResultSet resmq = stm.executeQuery();
                while (resmq.next()) {
                    item.put(Integer.toString(key), resmq.getString(1));
                }
                mylist.put("cluster " + Integer.toString(counter), item);
                counter++;

                stm.close();
            }
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
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
    @Path("/prioritizationToCluster/{table_name1}/{table_name2}/{prior}")
    @Produces(MediaType.APPLICATION_JSON)
    //works with achievement_table as table_name1 and game table as table_name2
    public Response prioritizationToCluster(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("prior") String prior, @QueryParam("eps") double eps, @QueryParam("minPts") int minPts) throws JSONException {
        JSONObject mylist = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol2 = new ArrayList<>();
        List<Integer> first = new ArrayList<>();
        List<Integer> second = new ArrayList<>();
        List<Integer> third = new ArrayList<>();
        List<Integer> fourth = new ArrayList<>();
        List<Integer> firstc = new ArrayList<>();
        List<Integer> secondc = new ArrayList<>();
        List<Integer> thirdc = new ArrayList<>();
        List<Integer> fourthc = new ArrayList<>();

        char[] pref = new char[6];
        pref = prior.toCharArray();
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

            Connection conn = dbUtils.getConnection();
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
                whole += "" + objn[first.get(i)] + " BETWEEN 85 AND 100";
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
                mypol2 = retpol(conn, firstc, table_name1, table_name2, obNames);
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
                    mypol2 = retpol(conn, fourthc, table_name1, table_name2, obNames);
                } else if (thirdc.size() > 2) {
                    mypol2 = retpol(conn, thirdc, table_name1, table_name2, obNames);
                } else if (secondc.size() > 2) {
                    mypol2 = retpol(conn, secondc, table_name1, table_name2, obNames);
                } else {
                    mypol2 = retpol(conn, firstc, table_name1, table_name2, obNames);
                }
            }
            stmt.close();
            conn.close();
            DBScan myCluster = new DBScan(mypol2, eps, minPts);
            myCluster.getClusters();
            Map<Integer, List<Integer>> clusterList = myCluster.cluster;
            Iterator it = clusterList.entrySet().iterator();

            int counter = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Integer> ml = (List<Integer>) pair.getValue();
                int key = (int) pair.getKey();
                JSONObject item = new JSONObject();
                for (int i : ml) {
                    item.put(Integer.toString(i), Integer.toString(i));
                }

                item.put(Integer.toString(key), Integer.toString(key));
                mylist.put("cluster " + Integer.toString(counter), item);
                counter++;
            }
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

    private static List<policy> setScoreCAT(List<policy> mypol, boolean belongs) {
        Collections.sort(mypol, new methods.polComparatorCAT());//sorted by category then by pool

        int amount = mypol.size(); //assign points to all solutions
        int top_score = amount * 10;
        int step = 1; //find step to reducing points
        int rank = 1;
        int last = 0;
        int last_dom = 0;
        int last_score = 0;
        int prev_dom_by = 0;
        int b = 75;
        for (policy pol : mypol) {
            if (pol.getDominated() == 0 && pol.getDominatedbycategory() == 0) {
                pol.setScore(top_score);
                last_score = top_score;
            }
            if (pol.getDominatedbycategory() > last_dom) {
                last_dom = pol.getDominatedbycategory();
                last_score -= step * 20;
            }
            if (pol.getDominated() > prev_dom_by) {
                last_score -= 10;
            }
            pol.setScore(last_score);
            prev_dom_by = pol.getDominated();
        }
        int bottom = Math.abs(top_score - last_score);
        int top = 0;
        for (policy pol : mypol) {
            top = Math.abs(pol.getScore() - last_score) * b;
            if (belongs) {
                pol.setScore((top / bottom) + 25);
            } else {
                pol.setScore(top / bottom);
            }
        }
        return mypol;
    }

    private List<policy> retpol(Connection conn, List<Integer> idList, String table_name1, String table_name2, String obNames) {

        JSONArray mylist = new JSONArray();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol1 = new ArrayList<>();
        boolean[] myminmax = {false, false, false, true};
        try {
            int param = 1;

            String joinQuery = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + obNames + "," + table_name2 + ".myorder "
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

            int allobj = columnsNumber - 2;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(2 + i + param);
            }
            //get obj names to perform the join quer

            for (int id : idList) {

                String joinQuery2 = "SELECT " + table_name1 + ".ID," + table_name1 + ".policy" + obNames + "," + table_name2 + ".myorder "
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
//                    pol.setPolicyName(resm.getString(2));
                    double[] obj_values = new double[allobj];
                    for (int i = 0; i < allobj; i++) {
                        obj_values[i] = resm.getDouble(3 + i);
                    }
                    pol.setObjectives(obj_values);
                    pol.setOrder(resm.getString(columnsNumber));
                    mypol.add(pol);
                }
                stm.close();
            }

        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
        return mypol;
    }

    @GET
    @Path("/prioritizationToClusterID/{table_name1}/{table_name2}/{prior}")
    @Produces(MediaType.APPLICATION_JSON)
    //works with achievement_table as table_name1 and game table as table_name2
    //the id is from table_name1 id field as given from orderbypercentage3
    public Response prioritizationToClusterID(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @PathParam("prior") String prior, @QueryParam("eps") double eps, @QueryParam("minPts") int minPts, @QueryParam("id") int id) throws JSONException {
        JSONObject mylist = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        List<policy> mypol2 = new ArrayList<>();
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
        char[] pref = new char[6];
        pref = prior.toCharArray();
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
                whole += "" + objn[first.get(i)] + " BETWEEN 85 AND 100";
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
                mypol2 = retpol(conn, firstc, table_name1, table_name2, obNames);
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
                    mypol2 = retpol(conn, fourthc, table_name1, table_name2, obNames);
                } else if (thirdc.size() > 2) {
                    for (int tmp : thirdc) {
                        if (tmp == id) {
                            belongs = true;
                        }
                    }
                    if (!belongs) {
                        thirdc.add(id);
                    }
                    mypol2 = retpol(conn, thirdc, table_name1, table_name2, obNames);
                } else if (secondc.size() > 2) {
                    for (int tmp : secondc) {
                        if (tmp == id) {
                            belongs = true;
                        }
                    }
                    if (!belongs) {
                        secondc.add(id);
                    }
                    mypol2 = retpol(conn, secondc, table_name1, table_name2, obNames);
                } else {
                    for (int tmp : firstc) {
                        if (tmp == id) {
                            belongs = true;
                        }
                    }
                    if (!belongs) {
                        firstc.add(id);
                    }
                    mypol2 = retpol(conn, firstc, table_name1, table_name2, obNames);
                }
            }
            stmt.close();
            conn.close();
            DBScan myCluster = new DBScan(mypol2, eps, minPts);
            myCluster.getClusters();
            Map<Integer, List<Integer>> clusterList = myCluster.cluster;
            Iterator it = clusterList.entrySet().iterator();

            int counter = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Integer> ml = (List<Integer>) pair.getValue();
                int key = (int) pair.getKey();
                JSONObject item = new JSONObject();
                for (int i : ml) {
                    item.put(Integer.toString(i), Integer.toString(i));
                }

                item.put(Integer.toString(key), Integer.toString(key));
                mylist.put("cluster " + Integer.toString(counter), item);
                counter++;
            }
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

    @GET
    @Path("/KMEANS/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKMEANS(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("kmeans") int kmeans) {

        String query1 = "SELECT * FROM " + table_name1;
        boolean[] myminmax = {false, false, false, true};
        double[] best, worse;
        JSONObject result = new JSONObject();
        JSONObject mylist = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        try {
            Connection conn = dbUtils.getConnection();
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
                objp[i] = rsmd.getColumnName(i + 3);
            }
            int allobj = columnsNumber - param - 1;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(2 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 2 + i) + " ";
            }

            String joinQuery2 = "SELECT " + table_name1 + ".ID" + obNames + ", " + table_name2 + ".myorder"
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID";
            System.out.println(joinQuery2);
            PreparedStatement st = conn.prepareStatement(joinQuery2);
            ResultSet resm = st.executeQuery();

            best = new double[allobj];
            worse = new double[allobj];
            if (resm.next()) {
                for (int i = 0; i < allobj; i++) {
                    best[i] = resm.getDouble(2 + i);
                    worse[i] = resm.getDouble(2 + i);
                }
            }
            while (resm.next()) {
                double[] obj_valuestest = new double[allobj];
                for (int i = 0; i < allobj; i++) {

                    obj_valuestest[i] = resm.getDouble(2 + i);
                    if (myminmax[i]) {
                        if (best[i] < obj_valuestest[i]) {
                            best[i] = obj_valuestest[i];
                        }
                        if (worse[i] > obj_valuestest[i]) {
                            worse[i] = obj_valuestest[i];
                        }
                    } else {
                        if (best[i] > obj_valuestest[i]) {
                            best[i] = obj_valuestest[i];
                        }
                        if (worse[i] < obj_valuestest[i]) {
                            worse[i] = obj_valuestest[i];
                        }
                    }
                }
            }
            resm.beforeFirst();
            while (resm.next()) {
                policy pol = new policy(objn.length, 0);
                pol.setID(resm.getInt(1));
                double[] obj_values = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    if (worse[i] == best[i]) {
                        obj_values[i] = 1.0;
                    } else {
                        obj_values[i] = Math.abs(resm.getDouble(2 + i) - worse[i]) / Math.abs(best[i] - worse[i]);
                    }
                }
                pol.setObjectives(obj_values);
                pol.setOrder(resm.getString(allobj + 2));
                mypol.add(pol);
            }
            Kmeans myCluster = new Kmeans(mypol, kmeans);
            myCluster.reloop(mypol, kmeans);
            Map<Integer, List<policy>> clusterList = myCluster.cluster;
            Iterator it = clusterList.entrySet().iterator();
            int counter = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<policy> ml = (List<policy>) pair.getValue();
                int key = (int) pair.getKey();
                JSONObject item = new JSONObject();
                for (policy i : ml) {
                    item.put(Integer.toString(i.getID()), i.getOrder());
                }
                mylist.put("cluster " + Integer.toString(counter), item);
                counter++;
            }
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
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
    @Path("/SPECTRAL/{table_name1}/{table_name2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpectral(@Context HttpServletRequest request, @PathParam("table_name1") String table_name1, @PathParam("table_name2") String table_name2, @QueryParam("kmeans") int kmeans) {

        String query1 = "SELECT * FROM " + table_name1;
        boolean[] myminmax = {false, false, false, true};
        double[] best, worse;
        JSONObject result = new JSONObject();
        JSONObject mylist = new JSONObject();
        List<policy> mypol = new ArrayList<>();
        try {
            Connection conn = dbUtils.getConnection();
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
                objp[i] = rsmd.getColumnName(i + 3);
            }
            int allobj = columnsNumber - param - 1;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(2 + i + param);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + table_name1 + "." + rsmd.getColumnName(param + 2 + i) + " ";
            }

            String joinQuery2 = "SELECT " + table_name1 + ".ID" + obNames
                    + " FROM " + table_name2
                    + " INNER JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".ID";
            System.out.println(joinQuery2);
            PreparedStatement st = conn.prepareStatement(joinQuery2);
            ResultSet resm = st.executeQuery();

            best = new double[allobj];
            worse = new double[allobj];
            if (resm.next()) {
                for (int i = 0; i < allobj; i++) {
                    best[i] = resm.getDouble(2 + i);
                    worse[i] = resm.getDouble(2 + i);
                }
            }
            while (resm.next()) {
                double[] obj_valuestest = new double[allobj];
                for (int i = 0; i < allobj; i++) {

                    obj_valuestest[i] = resm.getDouble(2 + i);
                    if (myminmax[i]) {
                        if (best[i] < obj_valuestest[i]) {
                            best[i] = obj_valuestest[i];
                        }
                        if (worse[i] > obj_valuestest[i]) {
                            worse[i] = obj_valuestest[i];
                        }
                    } else {
                        if (best[i] > obj_valuestest[i]) {
                            best[i] = obj_valuestest[i];
                        }
                        if (worse[i] < obj_valuestest[i]) {
                            worse[i] = obj_valuestest[i];
                        }
                    }
                }
            }
            resm.beforeFirst();
            while (resm.next()) {
                policy pol = new policy(objn.length, 0);
                pol.setID(resm.getInt(1));
                double[] obj_values = new double[allobj];
                for (int i = 0; i < allobj; i++) {
                    if (worse[i] == best[i]) {
                        obj_values[i] = 1.0;
                    } else {
                        obj_values[i] = Math.abs(resm.getDouble(2 + i) - worse[i]) / Math.abs(best[i] - worse[i]);
                    }
                }
                pol.setObjectives(obj_values);
                mypol.add(pol);
            }
            Spectral myCluster = new Spectral(mypol, kmeans);
            Map<Integer, List<Integer>> clusterList = myCluster.clusterFinal;
            Iterator it = clusterList.entrySet().iterator();
            int counter = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Integer> ml = (List<Integer>) pair.getValue();
                int key = (int) pair.getKey();
                JSONObject item = new JSONObject();
                for (int i : ml) {
                    item.put(Integer.toString(i), Integer.toString(i));
                }
                mylist.put("cluster " + Integer.toString(counter), item);
                counter++;
            }
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            result.put(Integer.toString(mylist.length()) + " results on " + reportDate, mylist);
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

    //Finding pareto frontiers provided the minimization or maximization criteria of each objective O(n^2)
    public static List<Integer> getDominatedbyList(List<policy> theList, policy mpol, boolean[] minmax) {
        double[] data = mpol.getObjectives();
        List<Integer> ret = new ArrayList<>();
        for (int j = 0; j < theList.size(); j++) {
            double[] element = theList.get(j).getObjectives();
            boolean bigger = true;
            boolean smaller = true;
            boolean equal = true;

            for (int w = 0; w < data.length; w++) {
                if (minmax[w]) {
                    int result = maximizationofObjective(data[w], element[w]);
                    if (result == 1) {
                        bigger = true && bigger;
                        smaller = false;
                        equal = false;
                    } else if (result == 0) {
                        equal = true && equal;
                    } else {
                        bigger = false;
                        smaller = true && smaller;
                        equal = false;
                    }
                } else {
                    int result = minimizationofObjective(data[w], element[w]);
                    if (result == 1) {
                        bigger = true && bigger;
                        smaller = false;
                        equal = false;
                    } else if (result == 0) {
                        equal = true && equal;
                    } else {
                        bigger = false;
                        smaller = true && smaller;
                        equal = false;
                    }
                }
            }
            if (!equal) {

                if (smaller) {
                    ret.add(theList.get(j).getID());
                }
            }
        }
        return ret;
    }
}
