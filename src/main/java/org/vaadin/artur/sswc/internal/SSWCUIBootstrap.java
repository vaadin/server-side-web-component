package org.vaadin.artur.sswc.internal;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;

public class SSWCUIBootstrap extends BootstrapHandler {

    private static final String PATH_PREFIX = "/sswc/sswcui.html";

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        VaadinServletRequest serlvetRequest = (VaadinServletRequest) request;
        String pathInfo = serlvetRequest.getServletPath()
                + serlvetRequest.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            return false;
        }

        return (pathInfo.equals(PATH_PREFIX));
    }

    @Override
    protected BootstrapContext createAndInitUI(Class<? extends UI> uiClass,
            VaadinRequest request, VaadinResponse response,
            VaadinSession session) {
        BootstrapContext context = super.createAndInitUI(SSWCUI.class, request,
                response, session);
        return context;
    }
}
