package org.vaadin.artur.sswc.app;

import javax.servlet.annotation.WebServlet;

import com.vaadin.flow.server.VaadinServlet;

@WebServlet(urlPatterns = { "/*" })
public class MyVaadinServlet extends VaadinServlet {
}
