# Local file registry implementation

Allows the Gateway to run in 'headless' mode, without an external backing store.

### Use cases

This is particularly useful when immutability is preferred, such as when using a GitOps approach.

It is also particularly valuable when rolling deployments are required, where different instances of the gateway may be running at the same time with different, versioned, configurations, where pointing to a single backing store would be problematic.

### How to use it

Set the registry implementation and path to the registry:

```
apiman-gateway.registry=io.apiman.gateway.engine.filesystem.LocalFileRegistry
apiman-gateway.registry.registry-path=/path/to/registry.json
```

The registry JSON can be created by using [apiman-cli](https://github.com/apiman/apiman-cli), for example;

```
./apiman gateway generate headless --declaration-file ./my-apis.yml --outputFile /path/to/registry.json
```
