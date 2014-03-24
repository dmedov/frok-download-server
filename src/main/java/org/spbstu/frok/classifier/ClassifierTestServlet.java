package org.spbstu.frok.classifier;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


@WebServlet(urlPatterns = {"/rec"})
public class ClassifierTestServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Classifier.executeCommand(new ArrayList<String>() {{
                add("touch");
                add("test2.txt");
            }});
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // for test
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Test get</title>");
            out.println("</head>");
            out.println("<body>");
            String user = request.getParameter("user");

            if (user != null) {
                out.println("hello "+ user);
            }

            out.println("</body>");
            out.println("</html>");
        }

        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "ClassifierTestServlet";
    }
}
