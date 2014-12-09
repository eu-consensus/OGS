package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
        PrintWriter out = response.getWriter();
        List<policy> mypol = new ArrayList<>();
        String minmax = request.getParameter("minmax");
        String tablename = request.getParameter("table");
        String gamename = request.getParameter("game");
        boolean[] myminmax = methods.minmax(minmax);
        String name = tablename + "_" + gamename;

//TODO CHECK FUNCTION REMOVE REGEX 
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
                System.out.print(rsmd.getColumnName(i));
                if (rsmd.getColumnName(i).contains("parameter")) {
                    param++;
                }
            }
            int allobj = columnsNumber - param - 1;
            System.out.print("the objectives are");
            System.out.print(allobj);
            int beforeobj = param + 2;
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
                pol.setOrder(allobj);
                mypol.add(pol);
            }
            int pwi = 0;

//TODO ADD ALL IN pol so i can do math
            Collections.sort(mypol, new polComparator());
            List<policy> mypol1 = methods.paretoM(mypol, myminmax);
            List<policy> mypol2 = methods.dominationBYcategory(mypol1, myminmax);
            Collections.sort(mypol2, new polComparator2());
            List<policy> mypol3 = methods.nsga2FH(mypol2, myminmax);
            List<policy> mypol4 = setScore(mypol3);

            String mquery = "INSERT INTO " + name + " (ID,P_ID,distance,dominatedbycategory,dominatedbypool,rank,myorder,chosen,liked,objscore,prefscore) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement mstmt = conn.prepareStatement(mquery);
            //   System.out.print(mquery);
            for (policy pol : mypol4) {
                pwi++;
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
            }

        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        dbUtils.closeConnection(conn);
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/dbLoad.jsp");

        dispatcher.forward(request, response);

    }

    private static List<policy> createdata(InputStream is, int number, int number2) throws IOException {

        List<policy> mypolicy = new ArrayList<>();
        char[] buf = new char[2048];
        String line = "";
        String splitBy = ",";

        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((line = r.readLine()) != null) {
            //minimization (-) maximization(+)
            String[] policy = line.split(splitBy);
            policy newpol = new policy(number, number2);
            int u = 0;
            double[] data = new double[number];
            for (int i = 0; i < policy.length; i++) {
                //if a policy name is provided then add it else create a Unique id
                if (i == 0) {
                    newpol.setPolicyName(policy[0]);
                    continue;
                }

                data[u] = Double.parseDouble(policy[i]);
                u++;
            }
            newpol.setObjectives(data);
            newpol.setDistance();
            newpol.setOrder(number);
            mypolicy.add(newpol);
        }
        return mypolicy;
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
                    last_score -= 10;
                }
                prev_dom_by = pol.getDominatedbycategory();

                 if (last_score < 0) {
                pol.setScore(0);
            } else {pol.setScore(last_score);
            }
        }
        return mypol;
    }
}
