package org.vaadin.artur.sswc.app;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.webcomponent.WebComponentDefinition;

public class MyFancyComponent extends HorizontalLayout {

    @Tag("my-fancy-component")
    public static class MyFancyComponentExporter
            implements WebComponentExporter<MyFancyComponent> {

        @Override
        public void define(
                WebComponentDefinition<MyFancyComponent> definition) {
            definition.addProperty("response", "Hello")
                    .onChange((component, value) -> {
                        component.response = value;
                    });
            definition.addProperty("message", "")
                    .onChange((component, value) -> {
                        component.setMessage(value);
                    });
        }

    }

    private Button button;
    public String response;

    public MyFancyComponent() {
        button = new Button("Hello", e -> {
            Notification.show(response);
        });
        add(button);
    }

    public void setMessage(String message) {
        this.button.setText(message);
    }

}
