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
import javax.ws.rs.core.Response.ResponseBuilder;
import methods.dbUtils;
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
    
}
