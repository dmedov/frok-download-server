package org.spbstu.frok.file.upload;

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
import java.util.Iterator;

@Deprecated
@WebServlet(urlPatterns = {"/links"})
@MultipartConfig(location = "/tmp")
public class PhotoLinksUploadServlet extends HttpServlet {
    private static final String UPLOAD_DIRECTORY = "/tmp/";
    private static final String PHOTOS_EXTENSION = ".jpg";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String filename = null;

        for (Part part : request.getParts()) {
            String submittedFileName = part.getSubmittedFileName();
            if (submittedFileName != null) {
                part.write(submittedFileName);
                filename = submittedFileName;
            }
        }

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("/tmp/" + filename));

            JSONObject jsonObject = (JSONObject) obj;

            String userId = (String) jsonObject.get("user_id");

            // parse photo links
            JSONArray photos = (JSONArray) jsonObject.get("photos");
            Iterator<String> iterator = photos.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                String newFilename = next.substring(next.indexOf("?") + 9, next.indexOf("&"));
                // save file by url
                FileUtils.copyURLToFile(new URL(next), new File(UPLOAD_DIRECTORY + userId + File.separator + newFilename + PHOTOS_EXTENSION));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "ImageUploadServlet";
    }
}
