 # timeout-policy

A policy that changes the timeouts configuration for the api.

The policy allows the user to adjust the timeout read and connect used by the HTTP Client when execute the query on the backend api.

## How it works

On receiving a request, the policy injects the values in the EndPointProperties. The Http Client read these values and use it. If no value is setting, the default value is used.
The default values are defined in the apiman.properties :
* apiman-gateway.connector-factory.http.timeouts.read
* apiman-gateway.connector-factory.http.timeouts.connect

## Author

William Beck <william.beck.pro@gmail.com>
