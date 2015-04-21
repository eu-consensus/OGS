/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import methods.dbUtils;
import methods.policy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet(name = "UploadERF", urlPatterns = {"/UploadERF"})
@MultipartConfig
public class UploadERF extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Part filePart = request.getPart("file");
        String filename = getFilename(filePart);
        InputStream filecontent = filePart.getInputStream();

        Integer count_of_names = Integer.parseInt(request.getParameter("countofnames"));
        String table_name = request.getParameter("dbname");
//        boolean[] myminmax = methods.minmax(minmax);
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
        String[] criteria_names = new String[count_of_names];
        try {

            //   FileInputStream file = new FileInputStream(new File("C:/Users/anu/Desktop/ERF-IIASA/TEST1.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(filecontent);
            //Get first sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
//TODO get workbook sheet (1)

            //Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {

                int uc = 0;
                Row row = rowIterator.next();
                //For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();

                String[] criteria_of_output = new String[count_of_names];
                policy ppp = new policy(objectivecount, count_of_names);
                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    if (ur == 0) {
                        if (uc < count_of_names) {
                            String wget = cell.getStringCellValue();
                            if (wget.contains(" ")) {
                                wget = wget.replaceAll(" ", "");
                            }
                            criteria_names[uc] = wget;
                        } else {
                            String wget = cell.getStringCellValue();
                            if (wget.contains("%")) {
                                wget = wget.replaceAll("%", "percentage");
                            }
                            if (wget.contains("(")) {
                                wget = wget.replaceAll("[(]", "");
                            }
                            if (wget.contains(")")) {
                                wget = wget.replaceAll("[)]", "");
                            }
                            if (wget.contains(" ")) {
                                wget = wget.replaceAll(" ", "_");
                            }
                            pol_names.add(wget);
                        }
                    } else if (ur > 0) {
                        if (uc < count_of_names) {
                            if (uc == 0) {
                                String wget = Double.toString(cell.getNumericCellValue());
                                if (wget.contains(",")) {
                                    wget = wget.replaceAll(",", ".");
                                }
                                criteria_of_output[uc] = wget;
                            } else {
                                criteria_of_output[uc] = cell.getStringCellValue();
                            }
                        } else {
                            table2.add(cell.getNumericCellValue());
                        }
                    }
                    uc++;
                }

               
                int uobj = 0;
                double[] objValues = new double[table2.size()];
                for (double tab : table2) {
                    objValues[uobj] = tab;
                    uobj++;
                }
                ppp.setPolicyName(criteria_of_output[1]+"_"+criteria_of_output[2]+"_"+criteria_of_output[3]+"_"+criteria_of_output[0]+" euro");
                if (ur > 0) {
                    ppp.setPolicyParameters(criteria_of_output);
                    ppp.setObjectives(objValues);
                    mypol.add(ppp);
                }
                table2.clear();
                ur++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            filecontent.close();
        }
        //    System.out.println(tablename);

        /*    for(int i=0;i<obj_names.length;i++){
         System.out.println(obj_names[i]);
         }
         */
//        for (int i = 0; i < count_of_names; i++) {
//            objp += "," + criteria_names[i] + " VARCHAR(255) ";
//        }
          for (int i = 0; i < count_of_names; i++) {
            objp += ",parameter" + Integer.toString(i + 1) + " VARCHAR(255) ";
        }
        for (String obj : pol_names) {
            objn += "," + obj + " DOUBLE";
        }

        //PERASMA STI VASI TABLE1 
        String sql1 = "CREATE TABLE IF NOT EXISTS " + table_name
                + "(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY "
                + " ,policy VARCHAR(255) "
                + objp
                + objn
                + ")";

        System.out.print(sql1);
        addobjn = createQM(pol_names.size());
        addobjp = createQM(count_of_names);
        Connection conn = dbUtils.getConnection();

        try {
            PreparedStatement statement = conn.prepareStatement(sql1);
            int exec = statement.executeUpdate();

            int pwi = 0;
            stpar += "ID,policy";
//            for (int i = 0; i < count_of_names; i++) {
//                stpar += ","+criteria_names[i];
//            }
            for (int i = 0; i < count_of_names; i++) {
                stpar += ",parameter" + Integer.toString(i + 1) + "";
            }
            for (String obj : pol_names) {
                stpar += "," + obj + "";
            }

            String query = "INSERT INTO " + table_name + " (" + stpar + ") " + "VALUES(?,?" + addobjp + addobjn + ")";


            for (policy pol : mypol) {
                pwi++;

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, pwi);
                stmt.setString(2, pol.getPolicyName());
                for (int num = 0; num < count_of_names; num++) {
                    stmt.setString(num + 3, pol.getPolicyParameters()[num]);
                   
                }
                for (int num = 0; num < pol_names.size(); num++) {
                    stmt.setDouble(num + count_of_names + 3, (double) Math.round(pol.getObjectives()[num] * 10000) / 10000);
                 
                }
             
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
       
        }
            dbUtils.closeConnection(conn);
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/dbLoad.jsp");

        dispatcher.forward(request, response);

    }

    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);
            }
        }
        return null;
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
