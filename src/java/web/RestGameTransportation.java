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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/GameTransportation")
@Stateless
public class RestGameTransportation {

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
                policy.put("objscore", resm.getInt(allobj +param+ 1));
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

            int allobj = columnsNumber - 3;
            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(4 + i);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "" + table_name1 + "." + rsmd.getColumnName(4 + i) + ", ";
            }

            String select2 = "" + table_name2 + ".ID," + table_name1 + ".policy," + obNames + table_name2 + ".objscore";
            String joinQuery2 = "SELECT " + select2
                    + " FROM " + table_name2
                    + " LEFT JOIN " + table_name1
                    + " ON " + table_name2 + ".P_ID=" + table_name1 + ".P_ID"
                    + " WHERE " + table_name2 + ".myorder=?";
            PreparedStatement stm = conn.prepareStatement(joinQuery2);
            stm.setString(1, id);
            ResultSet resm = stm.executeQuery();

            JSONArray mylist = new JSONArray();
            while (resm.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), resm.getInt(1));
                policy.put(rsmd.getColumnName(3), resm.getString(2));
                for (int i = 4; i < allobj + 4; i++) {
                    policy.put(rsmd.getColumnName(i), resm.getDouble(i-1));
                }
                policy.put("objscore", resm.getInt(allobj + 3));
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
}
