package org.vaadin.artur.sswc.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.internal.nodefeature.NodeProperties;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.theme.ThemeDefinition;
import com.vaadin.flow.theme.lumo.Lumo;

public class SSWCUI extends UI {
    private Class<?> theme = Lumo.class;;

    public SSWCUI() {
        assignTheme();
    }

    private void assignTheme() {
        try {
            Field field = UIInternals.class.getDeclaredField("theme");
            field.setAccessible(true);
            field.set(getInternals(), ReflectTools.createInstance(theme));
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Router getRouter() {
        return null;
    }

    @Override
    public Optional<ThemeDefinition> getThemeFor(Class<?> navigationTarget,
            String path) {
        return Optional.empty();
    }

    public void mapWc(Class<?> wcClass, String tag, String wcElementId) {
        getPage().addHtmlImport(
                "../sswc/element/" + wcElementId + "/" + tag + ".html");
        Component wcInstance = (Component) Instantiator.get(this)
                .getOrCreate(wcClass);

        Wrapper wrapper = new Wrapper(tag);
        Element wrapperElement = wrapper.getElement();
        wrapper.setChild(wcInstance);

        getElement().getStateProvider().appendVirtualChild(
                getElement().getNode(), wrapperElement,
                NodeProperties.INJECT_BY_ID, wcElementId);
        getPage().executeJavaScript("$0.serverConnected()", wrapperElement);

    }

    public static Map<String, Method> getPropertyMethods(Class<?> cls) {
        Map<String, Method> methods = new HashMap<>();

        for (Method method : cls.getDeclaredMethods()) {
            WebComponentProperty ann = method
                    .getAnnotation(WebComponentProperty.class);
            if (ann != null) {
                methods.put(ann.value(), method);
            }
        }
        return methods;
    }

    public static Map<String, Field> getPropertyFields(Class<?> cls) {
        Map<String, Field> fields = new HashMap<>();

        for (Field field : cls.getDeclaredFields()) {
            WebComponentProperty ann = field
                    .getAnnotation(WebComponentProperty.class);
            if (ann != null) {
                fields.put(ann.value(), field);
            }
        }
        return fields;
    }

    public static class Wrapper extends Component {
        private Component child;
        private Map<String, Field> fields;
        private Map<String, Method> methods;

        Wrapper(String tag) {
            super(new Element(tag));
        }

        public void setChild(Component child) {
            this.child = child;
            getElement().appendChild(child.getElement());

            this.fields = getPropertyFields(child.getClass());
            this.methods = getPropertyMethods(child.getClass());
        }

        @ClientCallable
        public void sync(String property, String newValue) {
            try {
                if (methods.containsKey(property)) {
                    Method method = methods.get(property);
                    method.setAccessible(true);
                    method.invoke(child, newValue);
                } else if (fields.containsKey(property)) {
                    Field field = fields.get(property);
                    field.setAccessible(true);
                    field.set(child, newValue);
                } else {
                    System.err.println("No method found for " + property);
                }
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
