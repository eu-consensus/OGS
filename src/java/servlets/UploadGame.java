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

@WebServlet(name = "UploadGame", urlPatterns = {"/UploadGame"})
@MultipartConfig
public class UploadGame extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        List<policy> mypol = new ArrayList<>();
        String minmax = request.getParameter("minmax");
        String tablename = request.getParameter("table");
        String gamename = request.getParameter("game");
        boolean[] myminmax = methods.minmax(minmax);
        String name = tablename + "_" + gamename;
        double[] optimalValues, worseValues;
//TODO CHECK FUNCTION REMOVE REGEX from table name -game name (form correct name for sql implementation!)
        String sql = "CREATE TABLE IF NOT EXISTS " + name
                + "(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "P_ID INTEGER NOT NULL, FOREIGN KEY (P_ID) REFERENCES " + tablename + "(ID),"
                + " distance DOUBLE,"
                + " dominatedbycategory int(30),"
                + " dominatedbypool int(30),"
                + " rank int(30),"
                + " myorder varchar(12),"
                + " chosen int(30) DEFAULT 0,"
                + " liked int(30)DEFAULT 0, "
                + " objscore int(30),"
                + " prefscore int(30))";
        System.out.print(sql);
        String query = "SELECT * FROM " + tablename;
        System.out.print(query);
        Connection conn = dbUtils.getConnection();
        try {

            PreparedStatement statement = conn.prepareStatement(sql);
            int exec = statement.executeUpdate();
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
            int allobj = columnsNumber - param - 1;
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
                policy pol = new policy(allobj, 0);
                pol.setID(res.getInt(1));
                pol.setPolicyName(res.getString(2));
                double[] obj_values = new double[allobj];

                for (int i = 0; i < allobj; i++) {
                    obj_values[i] = res.getDouble(beforeobj + i);
                }
                pol.setObjectives(obj_values);
                pol.setDistance();
                pol.setOrder(allobj, optimalValues, worseValues);
                mypol.add(pol);
            }

//TODO ADD ALL IN pol so i can do math
            Collections.sort(mypol, new polComparator());
            List<policy> mypol12 = null;
            List<policy>mypol1=null;
            try {
                mypol1 = methods.paretoM(mypol, myminmax);
              
            } catch (Exception exc) {
                System.out.println(exc.getMessage());
            }
            List<policy> mypol2 = null;
            List<policy> mypol4 = null;
            try {
                mypol2 = methods.dominationBYcategory(mypol1, myminmax);

                Collections.sort(mypol2, new polComparator2());
                List<policy> mypol3 = methods.nsga2FH(mypol2, myminmax);

                mypol4 = setScore(mypol3);
            } catch (Exception exc) {
                System.out.println(exc.getMessage());
            }
            String mquery = "INSERT INTO " + name + " (ID,P_ID,distance,dominatedbycategory,dominatedbypool,rank,myorder,chosen,liked,objscore,prefscore) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement mstmt = conn.prepareStatement(mquery);
            int pwi = 1;

            //   System.out.print(mquery);
            for (policy pol : mypol4) {
                mstmt.setInt(1, pwi);
                mstmt.setInt(2, pol.getID());
                mstmt.setDouble(3, pol.getDistance());
                mstmt.setInt(4, pol.getDominatedbycategory());
                mstmt.setInt(5, pol.getDominated());
                mstmt.setInt(6, pol.getRank());
                mstmt.setString(7, pol.getOrder());
                mstmt.setInt(8, 0);
                mstmt.setInt(9, 0);
                mstmt.setInt(10, pol.getScore());
                mstmt.setInt(11, 0);
                mstmt.executeUpdate();
                pwi++;
            }
//            mypol.clear();
//            mypol1.clear();
//            mypol2.clear();
//            mypol3.clear();
//            mypol4.clear();
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }

        dbUtils.closeConnection(conn);
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/dbLoad.jsp");
        dispatcher.forward(request, response);

    }

    private static List<policy> setScore(List<policy> mypol) {
        Collections.sort(mypol, new polComparatorRDD());//sorted by rank-domination count-domination by category

        double amount = mypol.size() * 0.5; //assign points to 60% of solutions
        int top_score = (int) (amount);
        int step = 1; //find step to reducing points
        int rank = 1;
        int last = 0;
        int last_dom = 0;
        int last_score = 0;
        int prev_dom_by = 0;
        int b = 500;
        for (policy pol : mypol) {
            if (pol.getRank() == 1 && pol.getDominated() == 0 && pol.getDominatedbycategory() == 0) {
                pol.setScore(top_score);
                last_score = top_score;
            }

            if (pol.getDominated() > last_dom) {
                last_dom = pol.getDominated();
                last_score -= step * 60;
                rank = pol.getRank();
            }
            if (pol.getRank() > rank) {
                rank = pol.getRank();
                last_score -= step * 40;
            }
            if (pol.getDominatedbycategory() > prev_dom_by) {
                last_score -= 20;
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
}
