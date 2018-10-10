package org.vaadin.artur.sswc.internal;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class SSWCServiceInit implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addRequestHandler(new SSWCElementProvider());
        event.addRequestHandler(new SSWCUIBootstrap());
    }

}
