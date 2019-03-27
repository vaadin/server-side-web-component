package org.vaadin.artur.sswc.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.webcomponent.WebComponentDefinition;
import com.vaadin.flow.server.VaadinRequest;

public class HelloWorld extends Div {

    @Tag("hello-world")
    public static class HelloWorldExporter
            implements WebComponentExporter<HelloWorld> {

        @Override
        public void define(WebComponentDefinition<HelloWorld> definition) {
            definition.addProperty("name", "").onChange((component, value) -> {
                component.setMessage(value);
            });
        }

    }

    private Div hello;

    public HelloWorld() {
        Div userAgent = new Div();
        userAgent.setText(VaadinRequest.getCurrent().getHeader("User-Agent"));
        add(userAgent);

        hello = new Div();
        add(hello);
    }

    public void setMessage(String name) {
        hello.setText("Hello " + name + "!");
    }

}
