package servlets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
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

@WebServlet(name = "Upload", urlPatterns = {"/Upload"})
@MultipartConfig
public class Upload extends HttpServlet {

    static int COUNT_OF_NAMES = 8;//the 9nth is the name of the policy

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Part filePart = request.getPart("file");
        String filename = getFilename(filePart);
        InputStream filecontent = filePart.getInputStream();

//       String minmax = request.getParameter("minmax");
//        boolean[] myminmax = methods.minmax(minmax);
        int ur = 0;
        int objectivecount = 0;
        String tablename = "";

        List<Double> table2 = new ArrayList<>();
        List<String> pol_names = new ArrayList<>();
        List<policy> mypol = new ArrayList<>();
        String objn = "";
        String objp = "";
        String addobjp = "";
        String addobjn = "";
        String stpar = "";
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

                String[] criteria_of_output = new String[COUNT_OF_NAMES];
                policy ppp = new policy(objectivecount, COUNT_OF_NAMES);
                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    if (ur == 0 && uc == COUNT_OF_NAMES + 1 && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                        tablename = cell.getStringCellValue() + "_";
                    }
                    if (ur == 1 && uc == COUNT_OF_NAMES + 1 && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        int smth = ((Double) cell.getNumericCellValue()).intValue();
                        tablename += "" + Integer.toString(smth);
                    }
                    if ((ur == 2 || ur == 3) && cell.getCellType() == Cell.CELL_TYPE_STRING) {

                        String wget = cell.getStringCellValue();
                        if (wget.contains(". ")) {
                            wget = wget.replaceAll(". ", "");
                        }
                        if (wget.contains("/")) {
                            wget = wget.replaceAll("/", "");
                        }
                        if (wget.contains(" ")) {
                            wget =wget.replaceAll(" ", "");
                        }

                        pol_names.add(wget);
                    }
                    if (ur > 3) {

                        if (uc < COUNT_OF_NAMES) {
                            criteria_of_output[uc] = cell.getStringCellValue();
                        } else if (uc == COUNT_OF_NAMES) {
                            ppp.setPolicyName(cell.getStringCellValue());

                        } else {
                            table2.add(cell.getNumericCellValue());

                        }
                        // for (int opip = 0; opip < criteria_of_output.length; opip++) {
                        //     System.out.println(criteria_of_output[opip]);
                        // }
                    }
                    uc++;

                }
                objectivecount = uc - COUNT_OF_NAMES - 1;
                int uobj = 0;
                double[] objValues = new double[objectivecount];
                for (double tab : table2) {
                    objValues[uobj] = tab;
                    uobj++;
                }
                if (ur > 3) {
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
        String[] obj_names = pol_name(pol_names);
        /*    for(int i=0;i<obj_names.length;i++){
         System.out.println(obj_names[i]);
         }
         */

        for (int i = 0; i < COUNT_OF_NAMES; i++) {
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
        addobjp = createQM(COUNT_OF_NAMES);
        Connection conn = dbUtils.getConnection();
        try {
            PreparedStatement statement = conn.prepareStatement(sql1);
            int exec = statement.executeUpdate();

            int pwi = 0;
            stpar += "ID,policy";
            for (int i = 0; i < COUNT_OF_NAMES; i++) {
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
                for (int num = 0; num < COUNT_OF_NAMES; num++) {
                    stmt.setString(num + 3, pol.getPolicyParameters()[num]);
                }
                for (int num = 0; num < obj_names.length; num++) {
                    stmt.setDouble(num + COUNT_OF_NAMES + 3, (double) Math.round(pol.getObjectives()[num] * 10000) / 10000);
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

    private static String readString(InputStream is) throws IOException {
        char[] buf = new char[2048];
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = r.read(buf);
            if (n < 0) {
                break;
            }
            s.append(buf, 0, n);
        }
        return s.toString();
    }
}
