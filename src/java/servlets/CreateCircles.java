/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

@WebServlet(name = "CreateCircles", urlPatterns = {"/CreateCircles"})
@MultipartConfig
public class CreateCircles extends HttpServlet {

   double circleRadius = 100;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String polid = request.getParameter("polid");
        String prior = request.getParameter("prior");
        String url = "http://localhost:8080/consensusN/rest/GameBiofuels/score1/achievement_World_2030/World_2030_game?prior=" + prior + "&id=" + polid + "";
        try {
            String myjson = IOUtils.toString(new URL(url));
            JSONObject myobj = (JSONObject) JSONValue.parseWithException(myjson);
            JSONObject myarr = (JSONObject) myobj.get("results");
            long total = (long) myarr.get("counter");
            double radius1 = (long)myarr.get("cosize")*circleRadius/total;
            double radius2 = (long) myarr.get("posize")*circleRadius/total;
            ServletContext context = getServletContext();
            RequestDispatcher dispatcher = context.getRequestDispatcher("/circles.jsp");
            request.setAttribute("radius", circleRadius);
            request.setAttribute("radius1", radius1);
            request.setAttribute("radius2", radius2);
            request.setAttribute("belongs",myarr.get("belongs"));
            request.setAttribute("cobelongs",myarr.get("cobelongs"));
            request.setAttribute("pobelongs",myarr.get("pobelongs"));
            request.setAttribute("mypolicy",myarr.get("policy"));
            request.setAttribute("categoryDominators",myarr.get("categoryDominators"));
            dispatcher.forward(request,response);
        } catch (ParseException ex) {
            Logger.getLogger(CreateCircles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
