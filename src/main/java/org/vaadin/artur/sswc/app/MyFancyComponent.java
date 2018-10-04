package org.vaadin.artur.sswc.app;

import org.vaadin.artur.sswc.internal.WebComponent;
import org.vaadin.artur.sswc.internal.WebComponentProperty;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@WebComponent("my-fancy-component")
public class MyFancyComponent extends HorizontalLayout {

    private Button button;

    @WebComponentProperty(value = "response", defaultValue = "Hello")
    private String response = "Hello";

    public MyFancyComponent() {
        button = new Button("Hello", e -> {
            Notification.show(response);
        });
        add(button);
    }

    @WebComponentProperty("message")
    public void setMessage(String message) {
        this.button.setText(message);
    }

}
