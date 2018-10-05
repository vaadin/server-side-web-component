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
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.util.SharedUtil;

import elemental.json.Json;
import elemental.json.JsonObject;

public class SSWCElementProvider extends BootstrapHandler {

    private static final String PATH_PREFIX = "/sswc/";
    private static String template;

    private static String getTemplate() {
        if (true || template == null) {
            try {
                template = IOUtils.toString(SSWCElementProvider.class
                        .getResourceAsStream("wc.html"),
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return template;
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

        String tag = parseTag(pathInfo);
        if (tag == null) {
            return false;
        }

        Class<? extends Component> webComponentClass = WebComponentRegistry
                .getClass(tag);

        if (webComponentClass == null) {
            response.sendError(404, "No such web component");
            return true;
        }

        generateModule(response.getOutputStream(), tag, webComponentClass);

        return true;
    }

    private static String parseTag(String pathInfo) {
        String tag = pathInfo.substring(PATH_PREFIX.length());
        if (!tag.endsWith(".html")) {
            return null;
        }
        tag = tag.substring(0, tag.length() - ".html".length());
        if (!tag.contains("-")) {
            return null;
        }
        return tag;
    }

    private static void generateModule(OutputStream out, String tag,
            Class<? extends Component> webComponentClass) throws IOException {
        Map<String, Field> fieldProperties = SSWCUI
                .getPropertyFields(webComponentClass);
        Map<String, Method> methodProperties = SSWCUI
                .getPropertyMethods(webComponentClass);
        Map<String, String> replacements = new HashMap<>();
        replacements.put("TagDash", tag);
        replacements.put("TagCamel", SharedUtil
                .capitalize(SharedUtil.dashSeparatedToCamelCase(tag)));
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
