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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import methods.dbUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/Percentage")
@Stateless
public class Percentage {
String[] parameters={"EU biofuel policies","Source of EU biofuel policies","Solid biomass demand EU", "Bioenergy scenario ROW", " LUC regulations","Level of biodiversity protection","Change in food diets","Yield development"};
    
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
            int param = 0;
            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
           int allobj = columnsNumber - param - 3;
            JSONArray mylist = new JSONArray();

            while (res.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), res.getInt(1));
                policy.put(rsmd.getColumnName(3), res.getString(3));
                for (int i = 0; i < param ; i++) {
                    policy.put(parameters[i], res.getString(i + 4));
                }
                for (int i = 0; i < allobj; i++) {
                    policy.put(rsmd.getColumnName(i + 4 + param), res.getDouble(i + 4 + param));
                }

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
        Response.ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }

    @GET
    @Path("/{table_name}/{policy}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getAll(@Context HttpServletRequest request, @PathParam("table_name") String table_name, @PathParam("policy") String pol) {
        JSONObject result = new JSONObject();
        try {
            Connection conn = dbUtils.getConnection();

            String query = "SELECT * FROM " + table_name + " WHERE policy=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pol);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            int param = 0;
            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
           int allobj = columnsNumber - param - 3;
            JSONArray mylist = new JSONArray();

            while (res.next()) {
                JSONObject policy = new JSONObject();
                policy.put(rsmd.getColumnName(1), res.getInt(1));
                policy.put(rsmd.getColumnName(3), res.getString(3));
                for (int i = 0; i < param ; i++) {
                    policy.put(rsmd.getColumnName(i + 4), res.getString(i + 4));
                }
                for (int i = 0; i < allobj; i++) {
                    policy.put(rsmd.getColumnName(i + 4 + param), res.getDouble(i + 4 + param));
                }
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
        Response.ResponseBuilder builder = Response.ok(result.toString());
        return builder.build();
    }
}
