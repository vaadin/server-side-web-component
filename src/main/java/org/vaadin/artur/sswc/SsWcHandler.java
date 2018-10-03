/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.artur.sswc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.util.SharedUtil;

public class SsWcHandler extends BootstrapHandler {

    private static final String PATH_PREFIX = "/sswc/";
    private String template;
    private ThreadLocal<Class<?>> currentWebComponentClass = new ThreadLocal<>();
    private ThreadLocal<String> currentWebComponentId = new ThreadLocal<>();
    private static AtomicInteger sswcId = new AtomicInteger(1);

    private String getTemplate() {
        if (true || this.template == null) {
            try {
                this.template = IOUtils.toString(
                        getClass().getResourceAsStream("wc.html"),
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.template;
    }

    @Override
    protected BootstrapContext createAndInitUI(Class<? extends UI> uiClass,
            VaadinRequest request, VaadinResponse response,
            VaadinSession session) {
        BootstrapContext context = super.createAndInitUI(SSWCUI.class, request,
                response, session);

        Class<?> wcClass = currentWebComponentClass.get();
        String tag = getTag(wcClass);
        String wcElementId = currentWebComponentId.get();
        ((SSWCUI) context.getUI()).mapWc(currentWebComponentClass.get(), tag,
                wcElementId);

        return context;
    }

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session,
            VaadinRequest request, VaadinResponse response) throws IOException {
        VaadinServletRequest serlvetRequest = (VaadinServletRequest) request;
        String pathInfo = serlvetRequest.getServletPath()
                + serlvetRequest.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            return false;
        }

        if (!pathInfo.startsWith(PATH_PREFIX)) {
            return false;
        }

        // This is a prototype. Real code should not depend on servlet details
        // here
        ServletContext context = ((VaadinServletRequest) request)
                .getServletContext();

        String tail = pathInfo.substring(PATH_PREFIX.length());

        if (!tail.endsWith(".html")) {
            response.sendError(404, "No such file");
            return true;
        }

        if (tail.startsWith("element/")) {
            tail = tail.substring("element/".length());
            String id = tail.substring(0, tail.indexOf("/"));
            tail = tail.substring(id.length() + 1);
            currentWebComponentId.set(id);
            String tagName = tail.substring(0,
                    tail.length() - ".html".length());
            Optional<Class<? extends HasElement>> webComponentClass = WebComponentInitializer
                    .getWebComponentClass(context, tagName);
            if (!webComponentClass.isPresent()) {
                response.sendError(404, "No such web component");
                return true;
            }
            generateModule(response.getOutputStream(), tagName,
                    webComponentClass.get());
            return true;
        }

        String tagName = tail.substring(0, tail.length() - ".html".length());

        Optional<Class<? extends HasElement>> webComponentClass = WebComponentInitializer
                .getWebComponentClass(context, tagName);
        if (!webComponentClass.isPresent()) {
            response.sendError(404, "No such web component");
            return true;
        }
        currentWebComponentClass.set(webComponentClass.get());
        currentWebComponentId.set("sswc_" + sswcId.incrementAndGet());

        super.synchronizedHandleRequest(session, request, response);

        currentWebComponentClass.remove();
        currentWebComponentId.remove();
        return true;
    }

    private void generateModule(OutputStream out, String tagName,
            Class<? extends HasElement> webComponentClass) throws IOException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("Id", currentWebComponentId.get());
        String tag = getTag(webComponentClass);
        replacements.put("TagDash", tag);
        replacements.put("TagCamel", SharedUtil
                .capitalize(SharedUtil.dashSeparatedToCamelCase(tag)));
        String template = getTemplate();
        for (Entry<String, String> replacement : replacements.entrySet()) {
            template = template.replace("_" + replacement.getKey() + "_",
                    replacement.getValue());
        }
        IOUtils.write(template, out, StandardCharsets.UTF_8);

    }

    private String getTag(Class<?> webComponentClass) {
        Tag ann = webComponentClass.getAnnotation(Tag.class);
        return ann.value();
    }
}
