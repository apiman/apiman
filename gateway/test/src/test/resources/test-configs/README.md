To execute the tests with your specific JSON config, set its name (without file suffix) via the `apiman.gateway-test.config` system property. It should reside within the `test-configs` directory.

For example: 
  - `apiman.gateway-test.config=servlet-es`
  - `apiman.gateway-test.config=vertx3-mem`
  
If left unset, `default` will be assumed.