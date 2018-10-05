package org.vaadin.artur.sswc.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet(urlPatterns = { "/*" })
public class MyVaadinServlet extends VaadinServlet {

    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String uri = request.getPathInfo();
        if (uri.equals("/index.html")
                && request.getParameter("source") != null) {
            PrintWriter writer = response.getWriter();
            writer.write(IOUtils.toString(
                    getServletContext().getResourceAsStream("/index.html"),
                    StandardCharsets.UTF_8));

            writer.write(
                    "\n\n---------------------------------------------------------------\n\n");

            String javaFile = MyFancyComponent.class.getSimpleName() + ".java";
            writer.write(IOUtils.toString(
                    MyFancyComponent.class.getResource(javaFile),
                    StandardCharsets.UTF_8));

            response.setContentType("text/plain");
            return;
        }

        super.service(request, response);
    }
}
