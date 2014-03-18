package org.spbstu.frok.file.upload;

import org.spbstu.frok.file.upload.cexecutor.HelloPrinter;
import org.spbstu.frok.file.upload.scheduler.Scheduler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

@WebServlet(urlPatterns = {"/api"})
@MultipartConfig(location = "D:\\HerFACE\\server\\")
public class TestServlet extends HttpServlet {

    private static final int       maxTasks = 10;
    private static final Scheduler localScheduler = new Scheduler(maxTasks);

    public class GetCommands {
        public static final String DOWNLOAD_PHOTOS = "download_photos";
        public static final String FIND_USERS_ON_PHOTO = "find_users";
        public static final String START_HELLO_WORLDS = "hello";
        public static final String GET_TASK_STATUS = "get_status";
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        for (Part part : request.getParts()) {
            String submittedFileName = part.getSubmittedFileName();
            if (submittedFileName != null) {
                part.write(submittedFileName);
            }
        }
    }

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
            String command = request.getParameter("cmd");

            switch (command) {
                case GetCommands.START_HELLO_WORLDS:
                    HelloPrinter hello = new HelloPrinter();
                    int index = localScheduler.execute(hello);
                    if(index == -1) {
                        out.println("Server is full. Rejecting...\n");
                    }
                    else {
                        out.println("Starting task " + Integer.toString(index) + "\n");
                        out.println("Current tasks: \n");
                        for(int i = 0; i < maxTasks; i++) {
                            out.println("Task №" + Integer.toString(i) + ". Status = "
                                    + Boolean.toString(localScheduler.getTaskStatus(i)) + "\n");
                        }
                    }
                    break;
                case GetCommands.GET_TASK_STATUS:
                    String taskIndex = request.getParameter("index");
                    boolean status = localScheduler.getTaskStatus(Integer.parseInt(taskIndex));
                    if(status)
                        out.println("task №" + taskIndex + " finished\n");
                    else
                        out.println("task №" + taskIndex + " not finished\n");
                    break;
                case GetCommands.DOWNLOAD_PHOTOS:
                case GetCommands.FIND_USERS_ON_PHOTO:
                default:
                    break;

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
        return "TestServlet";
    }
}
