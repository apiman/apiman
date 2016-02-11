# Download and install Karaf

```
cd ~/Temp
wget  http://apache.cu.be/karaf/2.4.4/apache-karaf-2.4.4.tar.gz
tar -vxf apache-karaf-2.4.4.tar.gz
cd apache-karaf-2.4.4
```

# Generate keystore for the Web Container

keytool -genkey -alias server-alias -keyalg RSA -keypass apiman -storepass apiman -keystore keystore.jks

Add `localhost` as name for the first and last name field. You can define the value for the other fields as you want.
Here is an example `CN=localhost, OU=apiman, O=apiman, L=fuse, ST=Unknown, C=BE`

# Create the org.ops4j.pax.web.cfg file under the directory /etc of Karaf

- Define the port to be used for the https and https protocols
- Setup the parameters to access the keystore created and the password to be used

```
org.osgi.service.http.secure.enabled=true
org.osgi.service.http.port.secure=8444
org.osgi.service.http.port=8181
org.osgi.service.http.enabled=true
org.ops4j.pax.web.ssl.keystore=/Users/chmoulli/Temp/_apiman/keystore.jks
org.ops4j.pax.web.ssl.password=apiman
org.ops4j.pax.web.ssl.keypassword=apiman
```

# Clone Git hub project and build

```
git clone git@github.com:apiman/apiman.git
git checkout karaf
cd distro/karaf
mvn clean install
```

# Copy Apiman Properties config file & apiman credentials

```
cp /Users/chmoulli/Code/jboss/apiman/apiman-core-forked/distro/karaf/bundles/gateway/io.apiman.gateway.cfg ~/Temp/apache-karaf-2.4.4/etc
cp /Users/chmoulli/Code/jboss/apiman/apiman-core-forked/distro/karaf/bundles/gateway/apiman.properties ~/Temp/apache-karaf-2.4.4/etc
cp /Users/chmoulli/Code/jboss/apiman/apiman-core-forked/distro/karaf/bundles/gateway/apiman-realm.xml ~/Temp/apache-karaf-2.4.4/deploy
```

# Start Karaf

```
./bin/karaf 
```

or
 
```
./bin/karaf debug
```

# Start ElasticSearch

Install and run ElasticSearch 1.7.2 (https://github.com/apiman/apiman-deployer/blob/master/deployer_local.sh - see option 1)


# Deploy the gateway

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.2-SNAPSHOT/xml/features
features:install apiman-gateway
```

# Test Apiman Gateway (using HTTPie tool or curl)

```
http GET http://localhost:8181/apiman-gateway/system/status
```

# Test Apiman Gateway API

```
http://localhost:8181/apiman-gateway-api/system/status
```

# Scenario 1 : Test Api

- Create an API for the organisation : GatewayOSGIApiTest, apiId : echo and the version 1.0
- Add new versions for the Api (2.0, 3.0)
- Delete the service for the version 3.0
- Get the info of the service version 2.0
- Test the echo service

```
echo '{"organizationId" : "GatewayOSGIApiTest", "apiId" : "echo", "version" : "1.0", "endpointType" : "REST", "publicAPI" : "true", "endpoint" :"http://localhost:9999/apiman-echo"}' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/apis
echo '{"organizationId" : "GatewayOSGIApiTest", "apiId" : "echo", "version" : "2.0", "endpointType" : "REST", "publicAPI" : "true", "endpoint" :"http://localhost:9999/apiman-echo"}' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/apis
echo '{"organizationId" : "GatewayOSGIApiTest", "apiId" : "echo", "version" : "3.0", "endpointType" : "REST", "publicAPI" : "true", "endpoint" :"http://localhost:9999/apiman-echo"}' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/apis
http --verify=no -a admin:admin123! DELETE https://localhost:8444/apiman-gateway-api/apis/GatewayOSGIApiTest/echo/3.0
http --verify=no -a admin:admin123! GET https://localhost:8444/apiman-gateway-api/apis/GatewayOSGIApiTest/echo/2.0/endpoint
http GET http://localhost:9999/apiman-echo/sample/path
http --verify=no -a admin:admin123! GET https://localhost:8444/apiman-gateway/GatewayOSGIApiTest/echo/2.0/simple/path
```
# Scenario 2 : Test Client

- Register a client
- Register an invalid client
- Register an retired client
- Remove a client
- Re register it again

```
echo '{"organizationId" : "GatewayOSGIApiTest","clientId" : "test-client", "version" : "1.0", "contracts" : [ {"apiKey" : "12345", "apiOrgId" : "GatewayOSGIApiTest", "apiId" : "echo", "apiVersion" : "1.0"}]}"' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/clients
echo '{"organizationId" : "GatewayOSGIApiTest","clientId" : "invalid-test-client", "version" : "1.0", "contracts" : [ {"apiKey" : "12345_002", "apiOrgId" : "GatewayOSGIApiTest", "apiId" : "invalid-api", "apiVersion" : "1.0"}]}"' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/clients
echo '{"organizationId" : "GatewayOSGIApiTest","clientId" : "retired-test-client", "version" : "1.0", "contracts" : [ {"apiKey" : "12345_003", "apiOrgId" : "GatewayOSGIApiTest", "apiId" : "echo", "apiVersion" : "3.0"}]}"' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/clients
echo '{"organizationId" : "GatewayOSGIApiTest","clientId" : "test-client", "version" : "1.0", "contracts" : [ {"apiKey" : "12345", "apiOrgId" : "GatewayOSGIApiTest", "apiId" : "echo", "apiVersion" : "1.0"}]}"' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/clients
http --verify=no -a admin:admin123! DELETE https://localhost:8444/apiman-gateway-api/clients/GatewayOSGIApiTest/test-client/1.0
echo '{"organizationId" : "GatewayOSGIApiTest","clientId" : "test-client", "version" : "1.0", "contracts" : [ {"apiKey" : "12345", "apiOrgId" : "GatewayOSGIApiTest", "apiId" : "echo", "apiVersion" : "1.0"}]}"' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/clients
```

# Scenario 3 : Test a simple plugin

```
echo '{"organizationId":"PluginTestPolicyTest","apiId":"echo","version":"1.0","publicAPI":true,"endpointType":"REST","endpoint":"http://localhost:9999/apiman-echo","apiPolicies":[{"policyImpl":"plugin:io.apiman:apiman-plugins-test-policy:1.2.0-SNAPSHOT:war/io.apiman.plugins.test_policy.TestPolicy","policyJsonConfig":""}]}' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/apis

http --verify=no -a admin:admin123! GET https://localhost:8444/apiman-gateway/PluginTestPolicyTest/echo/1.0/hello
  
echo '{"organizationId":"PluginTestPolicyTest","apiId":"reload","version":"1.0","publicAPI":true,"endpointType":"REST","endpoint":"http://localhost:9999/apiman-echo","apiPolicies":[{"policyImpl":"plugin:io.apiman.test:custom-fields-plugin:1.0-SNAPSHOT:war/io.apiman.test.plugins.FieldsPolicy","policyJsonConfig":"{ \"field1\" : \"foo\", \"field2\" : \"fighters\" }"}]}' | http --verify=no -a admin:admin123! PUT https://localhost:8444/apiman-gateway-api/apis
http --verify=no -a admin:admin123! --verbose GET https://localhost:8444/apiman-gateway-api/apis/PluginTestPolicyTest/reload/1.0/path/to/app/resource X-RestTest-System-Property:apiman.gateway.m2-repository-path=src/test/resources/test-plan-data/plugins/m2-fields-v1
```


