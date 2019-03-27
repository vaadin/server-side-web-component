package org.vaadin.artur.sswc.app;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.webcomponent.WebComponentDefinition;

public class Stats extends Div {

    @Tag("server-stats")
    public static class Exporter implements WebComponentExporter<Stats> {
        @Override
        public void define(WebComponentDefinition<Stats> definition) {

        }
    }

    public Stats() {
        update();
        getElement().addEventListener("click", e -> {
            update();
        });
    }

    public void update() {
        String processes;
        try {
            Runtime runtime = Runtime.getRuntime();
            processes = IOUtils
                    .toString(runtime.exec("ps ax").getInputStream());
            String processCount = processes.replaceAll("[^\\n]", "").length()
                    + "";
            String text = "Server status (click to update)";
            text += "<br>";
            text += "<br>";
            text += "Processes running: " + processCount;
            text += "<br>";
            text += "Free memory: " + format(runtime.freeMemory());
            text += "<br>";
            text += "Last updated: "
                    + DateFormat.getDateTimeInstance().format(new Date());

            setInnerHtml(text);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String format(long freeMemory) {
        return freeMemory / 1024 / 1024 + "MB";
    }

    private void setInnerHtml(String html) {
        // Workaround for https://github.com/vaadin/flow/issues/4644
        String current = getElement().getProperty("innerHTML");
        if (!html.equals(current)) {
            getElement().setProperty("innerHTML", html);
        }
    }

}
