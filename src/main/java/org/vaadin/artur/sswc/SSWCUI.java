package org.vaadin.artur.sswc;

import java.lang.reflect.Field;
import java.util.Optional;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.di.Instantiator;
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
        HasElement wcInstance = (HasElement) Instantiator.get(this)
                .getOrCreate(wcClass);

        getElement().getStateProvider().appendVirtualChild(
                getElement().getNode(), wcInstance.getElement(),
                NodeProperties.INJECT_BY_ID, wcElementId);

    }
}
