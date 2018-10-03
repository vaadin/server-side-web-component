package org.vaadin.artur.sswc;

import java.util.List;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServletService;

public class MyVaadinService extends VaadinServletService {
    public MyVaadinService(MyVaadinServlet myVaadinServlet,
            DeploymentConfiguration deploymentConfiguration) {
        super(myVaadinServlet, deploymentConfiguration);
    }

    @Override
    protected List<RequestHandler> createRequestHandlers()
            throws ServiceException {
        List<RequestHandler> handlers = super.createRequestHandlers();
        handlers.add(new SsWcHandler());
        return handlers;
    }
}
