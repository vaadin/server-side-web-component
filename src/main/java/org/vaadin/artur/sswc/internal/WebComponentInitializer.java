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

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;

@HandlesTypes({ WebComponent.class })
public class WebComponentInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx)
            throws ServletException {
        System.out.println("Classes found: " + c);

        Map<String, Class<? extends HasElement>> tagToWc = c.stream()
                .collect(Collectors.toMap(
                        type -> type.getAnnotation(WebComponent.class).value()
                                .toLowerCase(Locale.ROOT),
                        type -> type.asSubclass(Component.class)));
        ctx.setAttribute(WebComponentInitializer.class.getName(), tagToWc);
    }

    public static Optional<Class<? extends HasElement>> getWebComponentClass(
            ServletContext context, String tagName) {
        @SuppressWarnings("unchecked")
        Map<String, Class<? extends HasElement>> tagToWc = (Map<String, Class<? extends HasElement>>) context
                .getAttribute(WebComponentInitializer.class.getName());

        return Optional.ofNullable(tagToWc.get(tagName.toLowerCase()));
    }
}
