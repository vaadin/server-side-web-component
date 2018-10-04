package org.vaadin.artur.sswc.app;

import org.vaadin.artur.sswc.internal.WebComponent;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@WebComponent
@Tag("my-fancy-component")
public class MyFancyComponent extends HorizontalLayout {

    public MyFancyComponent() {
        add(new Button("Hello", e -> {
            Notification.show("Hello");
        }));
    }

    @ClientCallable
    public void hello() {
        System.out.println("Hello");
    }
}
