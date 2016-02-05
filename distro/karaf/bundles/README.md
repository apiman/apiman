# Install

- Download and unzip [JBoss Fuse](https://repository.jboss.org/nexus/content/groups/ea/org/jboss/fuse/jboss-fuse-full/6.2.1.redhat-084/)
- Configure JBoss Fuse to use Apache Logging (Log4j2). Edit the file etc/stratup.properties file and add this line after `org/ops4j/pax/logging/pax-logging-service/1.8.4/pax-logging-service-1.8.4.jar=8`

```
org/ops4j/pax/logging/pax-logging-log4j2/1.8.4/pax-logging-log4j2-1.8.4.jar=8
```
- Configure `org.ops4j.pax.logging.cfg` file 

```
org.ops4j.pax.logging.log4j2.config.file=${karaf.etc}/log4j2.xml
log4j.rootLogger=INFO
```

- Add this file within the etc directory

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
    <Appenders>
        <Console name="console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{ABSOLUTE} | %-5.5p | %-16.16t | %-32.32c{1} | %X{bundle.id} - %X{bundle.name} - %X{bundle.version} | %m%n"/>
        </Console>
        <RollingFile name="out" fileName="${karaf.data}/log/fuse.log" 
              append="true" filePattern="${karaf.data}/log/$${date:yyyy-MM}/fuse-%d{MM-dd-yyyy}-%i.log.gz">
           <PatternLayout>
             <Pattern>%d{ABSOLUTE} | %-5.5p | %-16.16t | %-32.32c{1} | %X{bundle.id} - %X{bundle.name} - %X{bundle.version} | %m%n</Pattern>
           </PatternLayout>
           <Policies>
                <TimeBasedTriggeringPolicy />
                <SizeBasedTriggeringPolicy size="250 MB"/>
            </Policies>
        </RollingFile>
        <PaxOsgi name="paxosgi" filter="VmLogAppender"/>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="console"/>
            <AppenderRef ref="out"/>
            <AppenderRef ref="paxosgi"/>
        </Root>
    </Loggers>
</Configuration>
```

- Disable some features by editing the file `org.apache.karaf.features.cfg`

```
featuresBoot=\
        jasypt-encryption,\
        pax-url-classpath,\
        deployer,\
        config,\
        management,\
        fabric-cxf,\
        fabric,\
        fabric-maven-proxy,\
        patch,\
        transaction,\
        mq-fabric,\
        swagger,\
        camel,\
        camel-cxf,\
        war,\
        fabric-redirect,\
        hawtio-offline,\
        support,\
        hawtio-redhat-fuse-branding,\
        jsr-311
```

- Open a terminal and launch the console

```
./bin.fuse
```
- Install the features file

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.2-SNAPSHOT/xml/features
```

- Deploy the simple Web Apiman Manager project using ES backend

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.2-SNAPSHOT/xml/features
install -s mvn:com.google.guava/guava/18.0
features:install hibernate/4.2.20
features:install pax-cdi-1.1-web-weld
features:install keycloak
features:install apiman-lib
features:install swagger/1.5.4
features:install elasticsearch/1.7.2
features:install apiman-common
features:install apiman-gateway
features:install apiman-manager-api-es
features:install manager-osgi
install -s mvn:org.jboss.weld/weld-osgi-bundle/2.3.0.Final
osgi:shutdown
```

or

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.2-SNAPSHOT/xml/features
features:install apiman-all
```

or with embedded dependencies

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.2-SNAPSHOT/xml/features
features:install manager-osgi-embed
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