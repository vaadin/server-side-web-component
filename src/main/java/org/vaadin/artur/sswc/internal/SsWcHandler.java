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
package org.vaadin.artur.sswc.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.util.SharedUtil;

import elemental.json.Json;
import elemental.json.JsonObject;

public class SsWcHandler extends BootstrapHandler {

    private static final String PATH_PREFIX = "/sswc/";
    private static String template;

    private ThreadLocal<RequestInfo> currentInfo = new ThreadLocal<>();

    private static AtomicInteger sswcId = new AtomicInteger(1);

    private static String getTemplate() {
        if (true || template == null) {
            try {
                template = IOUtils.toString(
                        SsWcHandler.class.getResourceAsStream("wc.html"),
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return template;
    }

    @Override
    protected BootstrapContext createAndInitUI(Class<? extends UI> uiClass,
            VaadinRequest request, VaadinResponse response,
            VaadinSession session) {
        BootstrapContext context = super.createAndInitUI(SSWCUI.class, request,
                response, session);

        RequestInfo info = currentInfo.get();
        ((SSWCUI) context.getUI()).mapWc(info.webComponentClass, info.tag,
                info.id);

        return context;
    }

    public static class RequestInfo {
        private String tag;
        private String id;
        private boolean elementRequest;
        Class<? extends HasElement> webComponentClass;
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

        RequestInfo info = parseRequestInfo(request, pathInfo);
        if (info == null) {
            response.sendError(404, "No such web component");
            return true;
        }

        currentInfo.set(info);
        if (info.elementRequest) {
            generateModule(response.getOutputStream(), info);
            return true;
        } else {
            super.synchronizedHandleRequest(session, request, response);
        }
        currentInfo.remove();

        return true;

    }

    private static RequestInfo parseRequestInfo(VaadinRequest request,
            String pathInfo) {
        ServletContext context = ((VaadinServletRequest) request)
                .getServletContext();

        String tail = pathInfo.substring(PATH_PREFIX.length());
        if (!tail.endsWith(".html")) {
            return null;
        }

        RequestInfo info = new RequestInfo();
        if (tail.startsWith("element/")) {
            info.elementRequest = true;

            tail = tail.substring("element/".length());
            info.id = tail.substring(0, tail.indexOf("/"));
            tail = tail.substring(info.id.length() + 1);
            info.tag = tail.substring(0, tail.length() - ".html".length());
        } else {
            info.tag = tail.substring(0, tail.length() - ".html".length());
            info.id = "sswc_" + sswcId.incrementAndGet();
        }
        Optional<Class<? extends HasElement>> webComponentClass = WebComponentInitializer
                .getWebComponentClass(context, info.tag);
        if (!webComponentClass.isPresent()) {
            return null;
        }

        info.webComponentClass = webComponentClass.get();

        return info;
    }

    private static void generateModule(OutputStream out, RequestInfo info)
            throws IOException {
        Map<String, Field> fieldProperties = SSWCUI
                .getPropertyFields(info.webComponentClass);
        Map<String, Method> methodProperties = SSWCUI
                .getPropertyMethods(info.webComponentClass);
        Map<String, String> replacements = new HashMap<>();
        replacements.put("Id", info.id);
        replacements.put("TagDash", info.tag);
        replacements.put("TagCamel", SharedUtil
                .capitalize(SharedUtil.dashSeparatedToCamelCase(info.tag)));
        replacements.put("PropertyMethods",
                getPropertyMethods(fieldProperties, methodProperties));
        replacements.put("Properties",
                getPropertyDefinitions(fieldProperties, methodProperties));
        String template = getTemplate();
        for (Entry<String, String> replacement : replacements.entrySet()) {
            template = template.replace("_" + replacement.getKey() + "_",
                    replacement.getValue());
        }
        IOUtils.write(template, out, StandardCharsets.UTF_8);

    }

    private static String getPropertyDefinitions(
            Map<String, Field> fieldProperties,
            Map<String, Method> methodProperties) {
        JsonObject props = Json.createObject();

        for (Entry<String, Field> entry : fieldProperties.entrySet()) {
            String property = entry.getKey();
            Field value = entry.getValue();
            String defaultValue = value
                    .getAnnotation(WebComponentProperty.class).defaultValue();
            JsonObject prop = getPropertyDefinition(property, defaultValue);
            props.put(property, prop);
        }
        for (Entry<String, Method> entry : methodProperties.entrySet()) {
            String property = entry.getKey();
            Method value = entry.getValue();
            String defaultValue = value
                    .getAnnotation(WebComponentProperty.class).defaultValue();
            JsonObject prop = getPropertyDefinition(property, defaultValue);
            props.put(property, prop);
        }
        return props.toJson();
    }

    private static JsonObject getPropertyDefinition(String property,
            String defaultValue) {
        JsonObject prop = Json.createObject();
        prop.put("type", "String");
        prop.put("value", defaultValue);
        prop.put("observer", getSyncMethod(property));
        prop.put("notify", true);
        prop.put("reflectToAttribute", false);
        return prop;

    }

    private static String getSyncMethod(String property) {
        return "_sync_" + property;
    }

    private static String getPropertyMethods(Map<String, Field> fieldProperties,
            Map<String, Method> methodProperties) {
        StringBuilder methods = new StringBuilder();
        Stream.concat(fieldProperties.keySet().stream(),
                methodProperties.keySet().stream()).forEach(property -> {
                    methods.append("    " + getSyncMethod(property)
                            + "(newValue, oldValue) { this._sync('" + property
                            + "', newValue);}\n");
                });
        return methods.toString();
    }

}
