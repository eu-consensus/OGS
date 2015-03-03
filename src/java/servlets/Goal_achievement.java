package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import methods.*;
import methods.methods.polComparator;
import methods.methods.polComparator2;
import methods.methods.polComparatorRDD;
import static servlets.Upload.COUNT_OF_NAMES;

@WebServlet(name = "Goal_achievement", urlPatterns = {"/Goal_achievement"})
@MultipartConfig
public class Goal_achievement extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        List<policy> mypol = new ArrayList<>();
        String minmax = request.getParameter("minmax");
        String tablename = request.getParameter("table");
        String name = request.getParameter("goal_name");
        boolean[] myminmax = methods.minmax(minmax);

        double[] optimalValues, worseValues;
        String addobjp = "";
        String addobjn = "";
        String stpar = "";
        String query = "SELECT * FROM " + tablename;
        Connection conn = dbUtils.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            ResultSetMetaData rsmd = res.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            int param = 1;
            for (int i = 1; i < columnsNumber + 1; i++) {
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            String[] objp = new String[param-1];
            for (int i = 0; i < param-1; i++) {
                objp[i] = rsmd.getColumnName(i + 3);
            }
            int allobj = columnsNumber - param - 1;

            String[] objn = new String[allobj];
            for (int i = 0; i < allobj; i++) {
                objn[i] = rsmd.getColumnName(param + 2 + i);
            }
            //get obj names to perform the join query
            String obNames = "";
            for (int i = 0; i < allobj; i++) {
                obNames += "," + rsmd.getColumnName(param + 2 + i) + " DOUBLE";
            }
            String parNames = "";
            for (int i = 0; i < param-1; i++) {
                parNames += "," + objp[i] + " VARCHAR(255) ";
            }
            optimalValues = new double[allobj];
            worseValues = new double[allobj];
            if (res.next()) {
                for (int i = 0; i < allobj; i++) {
                    optimalValues[i] = res.getDouble(param + 2 + i);
                    worseValues[i] = res.getDouble(param + 2 + i);
                }
            }
            int beforeobj = param + 2;

            while (res.next()) {
                double[] obj_valuestest = new double[allobj];
                for (int i = 0; i < allobj; i++) {

                    obj_valuestest[i] = res.getDouble(beforeobj + i);
                    if (myminmax[i]) {
                        if (optimalValues[i] < obj_valuestest[i]) {
                            optimalValues[i] = obj_valuestest[i];
                        }
                        if (worseValues[i] > obj_valuestest[i]) {
                            worseValues[i] = obj_valuestest[i];
                        }
                    } else {
                        if (optimalValues[i] > obj_valuestest[i]) {
                            optimalValues[i] = obj_valuestest[i];
                        }
                        if (worseValues[i] < obj_valuestest[i]) {
                            worseValues[i] = obj_valuestest[i];
                        }
                    }
                }
            }
            //res.first();
            res.beforeFirst();
            while (res.next()) {
                policy pol = new policy(objn.length, 0);
                pol.setID(res.getInt(1));
                pol.setPolicyName(res.getString(2));
                double[] obj_values = new double[allobj];
                String[] objn_values=new String[param];
                for (int i = 0; i < param-1; i++) {
                 objn_values[i]=res.getString(i+3);
                }
                 pol.setPolicyParameters(objn_values);
                for (int i = 0; i < allobj; i++) {
                    if (worseValues[i] == optimalValues[i]) {
                        obj_values[i] = 1.0;
                    } else {
                        obj_values[i] = Math.abs(res.getDouble(beforeobj + i) - worseValues[i]) / Math.abs(optimalValues[i] - worseValues[i]);
                    }

                }
                pol.setObjectives(obj_values);
                mypol.add(pol);
            }

            String sql1 = "CREATE TABLE IF NOT EXISTS " + name
                    + "(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "P_ID INTEGER NOT NULL, FOREIGN KEY (P_ID) REFERENCES " + tablename + "(ID)"
                    + " ,policy VARCHAR(255) "
                    + parNames
                    + obNames
                    + ")";
            PreparedStatement statement = conn.prepareStatement(sql1);
            int exec = statement.executeUpdate();
            addobjp = createQM(objp.length);
            addobjn = createQM(objn.length);
            stpar += "ID,P_ID,policy";
            for (int i = 0; i < param-1; i++) {
                stpar += ",parameter" + Integer.toString(i + 1) + "";
            }
            for (String obj : objn) {
                stpar += "," + obj + "";
            }

            String mquery = "INSERT INTO " + name + " (" + stpar + ") " + "VALUES(?,?,?" + addobjp + addobjn + " )";
            PreparedStatement mstmt = conn.prepareStatement(mquery);
            int pwi = 1;
            //   System.out.print(mquery);
            for (policy pol : mypol) {
                mstmt.setInt(1, pwi);
                mstmt.setInt(2, pol.getID());
                mstmt.setString(3, pol.getPolicyName());
                for (int num = 0; num < objp.length; num++) {
                    mstmt.setString(num + 4, (pol.getPolicyParameters()[num]));
                    //   System.out.print((double) Math.round(pol.getObjectives()[num] * 10000) / 10000);
                }
                for (int num = 0; num < objn.length; num++) {
                    mstmt.setDouble(num + param+3, (pol.getObjectives()[num]) * 100);
                    //   System.out.print((double) Math.round(pol.getObjectives()[num] * 10000) / 10000);
                }
//                System.out.print(query);
                mstmt.executeUpdate();
                pwi++;
            }

        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }

        dbUtils.closeConnection(conn);
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/dbLoad.jsp");
        dispatcher.forward(request, response);

    }

    private static String createQM(int number) {
        String qm = "";
        for (int i = 0; i < number; i++) {
            qm += ",?";
        }
        return qm;
    }
}
