# Download and install Karaf

```
cd ~/Temp
wget  http://apache.cu.be/karaf/2.4.4/apache-karaf-2.4.4.tar.gz
tar -vxf apache-karaf-2.4.4.tar.gz
cd apache-karaf-2.4.4
```

# Clone Git hub project and build

```
git clone git@github.com:apiman/apiman.git
git checkout karaf
cd distro/karaf
mvn clean install
```

# Copy Apiman Properties config file (from Github repo)

```
cp /Users/chmoulli/Code/jboss/apiman/apiman-core-forked/distro/karaf/bundles/gateway/io.apiman.gateway.cfg ~/Temp/apache-karaf-2.4.4/etc
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

# Scenario Api

- Create an API for the organisation : GatewayOSGIApiTest, apiId : echo and the version 1.0
- Add new versions for the Api (2.0, 3.0)
- Delete the service for the version 3.0
- Get the info of the service version 2.0
- Test the echo service

```
echo '{"organizationId" : "GatewayOSGIApiTest", "apiId" : "echo", "version" : "1.0", "endpointType" : "REST", "endpoint" :"http://localhost:9999/"}' | http --verbose PUT http://localhost:8181/apiman-gateway-api/apis
echo '{"organizationId" : "GatewayOSGIApiTest", "apiId" : "echo", "version" : "2.0", "endpointType" : "REST", "endpoint" :"http://localhost:9999/"}' | http --verbose PUT http://localhost:8181/apiman-gateway-api/apis
echo '{"organizationId" : "GatewayOSGIApiTest", "apiId" : "echo", "version" : "3.0", "endpointType" : "REST", "endpoint" :"http://localhost:9999/"}' | http --verbose PUT http://localhost:8181/apiman-gateway-api/apis
http DELETE http://localhost:8181/apiman-gateway-api/apis/GatewayOSGIApiTest/echo/3.0
http GET http://localhost:8181/apiman-gateway-api/apis/GatewayOSGIApiTest/echo/2.0/endpoint
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, must-revalidate
Content-Type: application/json
Date: Wed, 10 Feb 2016 11:39:10 GMT
Expires: Tue, 09 Feb 2016 11:39:10 GMT
Pragma: no-cache
Server: Jetty(8.1.17.v20150415)
Transfer-Encoding: chunked

{
    "endpoint": "https://localhost:8443/apiman-gateway/GatewayOSGIApiTest/echo/2.0"
}
http GET http://localhost:9999/apiman-echo/sample/path
http --verify=no GET https://localhost:8443/apiman-gateway/GatewayOSGIApiTest/echo/2.0
```
