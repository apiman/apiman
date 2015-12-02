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
features:install manager-osgi
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
[ 334] [Active     ] [Deployed   ]  [   80] [/manager                ] manager-osgi (1.2.0.SNAPSHOT)

```

- Test it using curl or [httpie](httpie.org) tool

```
http get http://localhost:8181/manager/rest/message/user
HTTP/1.1 200 OK
Content-Length: 25
Content-Type: application/json
Server: Jetty(8.1.17.v20150415)

Restful example : user
```

Remarks :

- As Resteasy API has been designed to be deployed within a Java EE container as a war the `resteasy.scan = true` option doesn't work as resteasy scans 
  the classes under `WEB-INF/lib` and `WEB-INF/classes`. This is why, we pass as parameter the classes to be loaded within the web.xml file
- The Java ServiceLocator doesn't work too for the moment. This is also the reasons why the providers classes have been added within the web.xml file
  The Apache Servicemix project has developed a bundle activator to load these classes but that will require additional developments for RESTeasy
- The resteasy project has been packaged as a bundle with the `resteasy-jaxrs`, `resteasy-cdi` & `resteasy-jackson-provider` dependencies. Two classes
  have been created and/or updated to inject the BeanManager and resolve TCCL class loading issue.