package org.vaadin.artur.sswc;

import javax.servlet.annotation.WebServlet;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet(urlPatterns = { "/*" })
public class MyVaadinServlet extends VaadinServlet {
}
