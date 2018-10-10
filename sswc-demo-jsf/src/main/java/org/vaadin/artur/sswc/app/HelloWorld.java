package org.vaadin.artur.sswc.app;

import org.vaadin.artur.sswc.internal.WebComponent;
import org.vaadin.artur.sswc.internal.WebComponentProperty;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.VaadinRequest;

@WebComponent("hello-world")
public class HelloWorld extends Div {

    private Div hello;

    public HelloWorld() {
        Div userAgent = new Div();
        userAgent.setText(VaadinRequest.getCurrent().getHeader("User-Agent"));
        add(userAgent);

        hello = new Div();
        add(hello);
    }

    @WebComponentProperty("name")
    public void setMessage(String name) {
        hello.setText("Hello " + name + "!");
    }

}
