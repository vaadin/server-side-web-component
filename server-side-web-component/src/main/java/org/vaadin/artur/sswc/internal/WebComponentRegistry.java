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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import com.vaadin.flow.component.Component;

/**
 * A registry for exported web components.
 *
 */
@HandlesTypes({ WebComponent.class })
public class WebComponentRegistry implements ServletContainerInitializer {

    private static Map<String, Class<? extends Component>> exportedWebComponents;

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx)
            throws ServletException {
        Stream<Class<? extends Component>> componentClasses = (Stream) c
                .stream().filter(cls -> Component.class.isAssignableFrom(cls));

        exportedWebComponents = componentClasses
                .collect(Collectors.toMap(type -> getTag(type), type -> type));
    }

    public static Class<? extends Component> getClass(String tag) {
        return exportedWebComponents.get(tag.toLowerCase(Locale.ROOT));
    }

    public static String getTag(Class<? extends Component> type) {
        return type.getAnnotation(WebComponent.class).value()
                .toLowerCase(Locale.ROOT);
    }

}
