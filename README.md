Uses the new web component export feature of Flow 1.5 to export server side Java classes as web components.

For example
```
@Tag("my-fancy-component")
public static class MyFancyComponentExporter implements WebComponentExporter<MyFancyComponent> {

  @Override
  public void define(WebComponentDefinition<MyFancyComponent> definition) {
    definition.addProperty("response", "Hello").onChange((component, value) -> {
                        component.response = value;
    });
    definition.addProperty("message", "").onChange((component, value) -> {
                        component.setMessage(value);
    });
  }
}
```

exports the component class `MyFancyComponent` and publishes two properties `message` and `response`. If these are changed in the browser, the corresponding field in the component is updated or the corresponding method is called.
