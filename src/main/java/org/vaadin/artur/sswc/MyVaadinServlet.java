package org.vaadin.artur.sswc;

import javax.servlet.annotation.WebServlet;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;

@WebServlet(urlPatterns = { "/*" })
public class MyVaadinServlet extends VaadinServlet {
    @Override
    protected VaadinServletService createServletService(
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        VaadinServletService service = new MyVaadinService(this,
                deploymentConfiguration);
        service.init();
        return service;
    }
}
