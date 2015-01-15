package servlets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import methods.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet(name = "UploadBiofuelsIBM", urlPatterns = {"/UploadBiofuelsIBM"})
@MultipartConfig
public class UploadBiofuelsIBM extends HttpServlet {

    static int COUNT_OF_NAMES = 8;//the 9nth is the name of the policy

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        URL oracle = new URL(request.getParameter("url").toString());
        Integer count_of_names = Integer.parseInt(request.getParameter("countofnames"));
        String tablename = request.getParameter("dbname");

        int ur = 0;
        int objectivecount = 0;

        List<Double> table2 = new ArrayList<>();
        List<String> pol_names = new ArrayList<>();
        List<policy> mypol = new ArrayList<>();
        String objn = "";
        String objp = "";
        String addobjp = "";
        String addobjn = "";
        String stpar = "";
        String line = "";
        String splitBy = ",";

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(oracle.openStream(), "UTF-8"));
            while ((line = r.readLine()) != null) {
                while (line.contains(",,")) {
                    line = line.replaceAll(",,", ",");
                }
                String[] policy = line.split(splitBy);
                String[] criteria_of_output = new String[count_of_names];
                policy ppp = new policy(objectivecount, count_of_names);
                if (ur == 0) {
                    if (policy[2] != null) {
                        tablename = policy[2] + "_";
                    }
                }
                if (ur == 1) {
                    if (policy[2] != null) {
                        tablename += policy[2];
                    }
                }
                if ((ur == 2)) {
                    for (int i = 0; i < policy.length; i++) {
                        if (!policy[i].equals("")) {
                            String wget = policy[i];
                            if (wget.contains(". ")) {
                                wget = wget.replaceAll(". ", "");
                            }
                            if (wget.contains("/")) {
                                wget = wget.replaceAll("/", "");
                            }
                            if (wget.contains(" ")) {
                                wget = wget.replaceAll(" ", "");
                            }

                            pol_names.add(wget);
                        }
                    }
                }
                if (ur > 2) {
                    for (int i = 0; i < policy.length; i++) {
                        if (i == 0) {
                            ppp.setPolicyName(policy[0]);
                        } else if (i < count_of_names + 1) {
                            criteria_of_output[i - 1] = policy[i];
                        } else {
                            table2.add(Double.parseDouble(policy[i]));
                        }
                        // for (int opip = 0; opip < criteria_of_output.length; opip++) {
                        //     System.out.println(criteria_of_output[opip]);
                        // }
                    }
                    objectivecount = policy.length - COUNT_OF_NAMES - 1;
                    int uobj = 0;
                    double[] objValues = new double[objectivecount];
                    for (double tab : table2) {
                        objValues[uobj] = tab;
                        uobj++;
                    }
                    ppp.setPolicyParameters(criteria_of_output);
                    ppp.setObjectives(objValues);
                    mypol.add(ppp);
                    table2.clear();
                }

                ur++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //    System.out.println(tablename);
        String[] obj_names = new String[pol_names.size()];
        int i365 = 0;
        for (String pol : pol_names) {
            obj_names[i365] = pol + "";
            i365++;
        }

        for (int i = 0;i < count_of_names;i++) {
            objp += ",parameter" + Integer.toString(i + 1) + " VARCHAR(255) ";
        }
        for (String obj : obj_names) {
            objn += "," + obj + " DOUBLE";
        }

        //PERASMA STI VASI TABLE1 
        String sql1 = "CREATE TABLE IF NOT EXISTS " + tablename
                + "(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY "
                + " ,policy VARCHAR(255) "
                + objp
                + objn
                + ")";

        System.out.print(sql1);
        addobjn = createQM(obj_names.length);
        addobjp = createQM(count_of_names);
        Connection conn = dbUtils.getConnection();

        try {
            PreparedStatement statement = conn.prepareStatement(sql1);
            int exec = statement.executeUpdate();

            int pwi = 0;
            stpar += "ID,policy";
            for (int i = 0; i < count_of_names; i++) {
                stpar += ",parameter" + Integer.toString(i + 1) + "";
            }
            for (String obj : obj_names) {
                stpar += "," + obj + "";
            }

            String query = "INSERT INTO " + tablename + " (" + stpar + ") " + "VALUES(?,?" + addobjp + addobjn + " )";

            for (policy pol : mypol) {
                pwi++;

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, pwi);
                stmt.setString(2, pol.getPolicyName());
                for (int num = 0; num < count_of_names; num++) {
                    stmt.setString(num + 3, pol.getPolicyParameters()[num]);
                }
                for (int num = 0; num < obj_names.length; num++) {
                    stmt.setDouble(num + count_of_names + 3, (double) Math.round(pol.getObjectives()[num] * 10000) / 10000);
                    //   System.out.print((double) Math.round(pol.getObjectives()[num] * 10000) / 10000);
                }
//                System.out.print(query);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
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

    private static String[] pol_name(List<String> tpol) {
        int psize = tpol.size() / 2;
        String[] names = new String[psize];
        for (int i = 0; i < psize; i++) {
            names[i] = "";
        }
        int i = 0;
        for (String pol : tpol) {
            names[i] += pol + "";
            i++;
            if (i == (psize)) {
                i = 0;
            }
        }
        return names;
    }
}
