# Install

- Download and unzip [JBoss Fuse](https://repository.jboss.org/nexus/content/groups/ea/org/jboss/fuse/jboss-fuse-full/6.2.1.redhat-084/)
- Open a terminal and launch the console

```
./bin.fuse
```
- Install the features file

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.0-SNAPSHOT/xml/features
```

- Deploy the simple Web Apiman Manager project

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.0-SNAPSHOT/xml/features
features:install -c apiman-lib
features:install -c apiman-common
features:install -c apiman-manager-api
features:install -c apiman-gateway
features:install -c manager-osgi
#Remove this bundle due to a dep chaining - org.apache.geronimo.specs.geronimo-jpa_2.0_spec [265.0]
uninstall 265
```

- Verify that the WebContext is well registered

```
web:list
   ID   State         Web-State       Level  Web-ContextPath           Name
[ 253] [Active     ] [Deployed   ]  [   80] [/hawtio                 ] hawtio :: hawtio-web (1.4.0.redhat-621071)
[ 255] [Active     ] [Deployed   ]  [   80] [/hawtio-karaf-terminal  ] hawtio :: Karaf terminal plugin (1.4.0.redhat-621071)
[ 259] [Active     ] [Deployed   ]  [   80] [/rhaccess-web           ] Tooling for support (1.2.0.redhat-621071)
[ 260] [Active     ] [Deployed   ]  [   80] [/rhaccess-plugin        ] hawtio :: project (1.4.0.redhat-621071)
[ 262] [Active     ] [Deployed   ]  [   80] [/redhat-branding        ] hawtio :: Red Hat Fuse Branding (1.4.0.redhat-621071)
[ 334] [Active     ] [Deployed   ]  [   80] [/apiman                ] manager-osgi (1.2.0.SNAPSHOT)

```

- Test it using curl or [httpie](httpie.org) tool

```
http get http://localhost:8181/apiman/rest/message/user
HTTP/1.1 200 OK
Content-Length: 25
Content-Type: application/json
Server: Jetty(8.1.17.v20150415)

Restful example : user
```

- Test Apiman System Satus

```
http -v GET http://localhost:8181/apiman/rest/system/status
GET /manager/rest/system/status HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Host: localhost:8181
User-Agent: HTTPie/0.9.2



HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.1.17.v20150415)
Transfer-Encoding: chunked

{
    "builtOn": null,
    "description": "The API Manager REST API is used by the API Manager UI to get stuff done.  You can use it to automate any apiman task you wish.  For example, create new Organizations, Plans, Applications, and Services.",
    "id": "apiman-manager-api",
    "moreInfo": "http://www.apiman.io/latest/api-manager-restdocs.html",
    "name": "API Manager REST API",
    "up": false,
    "version": null
}
``

Remarks :

- As Resteasy API has been designed to be deployed within a Java EE container as a war the `resteasy.scan = true` option doesn't work as resteasy scans 
  the classes under `WEB-INF/lib` and `WEB-INF/classes`. This is why, we pass as parameter the classes to be loaded within the web.xml file
- The Java ServiceLocator doesn't work too for the moment. This is also the reasons why the providers classes have been added within the web.xml file
  The Apache Servicemix project has developed a bundle activator to load these classes but that will require additional developments for RESTeasy
- The resteasy project has been packaged as a bundle with the `resteasy-jaxrs`, `resteasy-cdi` & `resteasy-jackson-provider` dependencies. Two classes
  have been created and/or updated to inject the BeanManager and resolve TCCL class loading issue.