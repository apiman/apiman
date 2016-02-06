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
install -s mvn:io.apiman/gateway-osgi-servlet/1.2.2-SNAPSHOT
install -s mvn:io.apiman/gateway-osgi/1.2.2-SNAPSHOT
install -s mvn:io.apiman/gateway-osgi-api/1.2.2-SNAPSHOT
```

# Test Apiman Gateway (using HTTPie tool or curl)

```
http GET http://localhost:8181/apiman-gateway/system/status
```

# Test Apiman Gateway API (does not work as the servlet is not started)

```
http GET http://localhost:8181/apiman-gateway-api/system/status
```

Error 

```
2016-02-06 10:49:44,559 | ERROR | l Console Thread | HttpServiceStarted               | 90 - org.ops4j.pax.web.pax-web-runtime - 3.2.4 | Could not start the servlet context for context path []
java.lang.RuntimeException: No IRegistry class configured.
	at io.apiman.gateway.platforms.war.WarEngineConfig.loadConfigClass(WarEngineConfig.java:246)
	at io.apiman.gateway.platforms.war.WarEngineConfig.getRegistryClass(WarEngineConfig.java:96)
	at io.apiman.gateway.engine.impl.ConfigDrivenEngineFactory.createRegistry(ConfigDrivenEngineFactory.java:83)
	at io.apiman.gateway.engine.impl.AbstractEngineFactory.createEngine(AbstractEngineFactory.java:51)
	at io.apiman.gateway.platforms.war.WarGateway.init(WarGateway.java:46)
	at io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper.contextInitialized(WarGatewayBootstrapper.java:42)
	at org.eclipse.jetty.server.handler.ContextHandler.callContextInitialized(ContextHandler.java:782)[83:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	at org.eclipse.jetty.servlet.ServletContextHandler.callContextInitialized(ServletContextHandler.java:424)[83:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	at org.eclipse.jetty.server.handler.ContextHandler.startContext(ContextHandler.java:774)[83:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	at org.eclipse.jetty.servlet.ServletContextHandler.startContext(ServletContextHandler.java:249)[83:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	at org.eclipse.jetty.server.handler.ContextHandler.doStart(ContextHandler.java:717)[83:org.eclipse.jetty.aggregate.jetty-all-server:8.1.17.v20150415]
	at org.ops4j.pax.web.service.jetty.internal.HttpServiceContext.doStart(HttpServiceContext.java:222)
	at org.eclipse.jetty.util.component.AbstractLifeCycle.start(AbstractLifeCycle.java:64)
	at org.ops4j.pax.web.service.jetty.internal.JettyServerImpl$1.start(JettyServerImpl.java:204)
	at org.ops4j.pax.web.service.internal.HttpServiceStarted.registerServlet(HttpServiceStarted.java:215)
	at org.ops4j.pax.web.service.internal.HttpServiceStarted.registerServlet(HttpServiceStarted.java:349)
	at org.ops4j.pax.web.service.internal.HttpServiceStarted.registerServlet(HttpServiceStarted.java:317)
	at org.ops4j.pax.web.service.internal.HttpServiceProxy.registerServlet(HttpServiceProxy.java:124)
	at io.apiman.gateway.api.osgi.Activator.start(Activator.java:123)
	at org.apache.felix.framework.util.SecureAction.startActivator(SecureAction.java:645)
	at org.apache.felix.framework.Felix.activateBundle(Felix.java:2154)
	at org.apache.felix.framework.Felix.startBundle(Felix.java:2072)
	at org.apache.felix.framework.BundleImpl.start(BundleImpl.java:976)
	at org.apache.felix.framework.BundleImpl.start(BundleImpl.java:963)
	at org.apache.karaf.shell.osgi.InstallBundle.doExecute(InstallBundle.java:51)
	at org.apache.karaf.shell.console.OsgiCommandSupport.execute(OsgiCommandSupport.java:38)
	at org.apache.felix.gogo.commands.basic.AbstractCommand.execute(AbstractCommand.java:35)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_45]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_45]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_45]
	at java.lang.reflect.Method.invoke(Method.java:497)[:1.8.0_45]
	at org.apache.aries.proxy.impl.ProxyHandler$1.invoke(ProxyHandler.java:54)[11:org.apache.aries.proxy.impl:1.0.4]
	at org.apache.aries.proxy.impl.ProxyHandler.invoke(ProxyHandler.java:119)[11:org.apache.aries.proxy.impl:1.0.4]
	at org.apache.karaf.shell.console.commands.$BlueprintCommand2042410452.execute(Unknown Source)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.CommandProxy.execute(CommandProxy.java:78)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.Closure.executeCmd(Closure.java:477)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.Closure.executeStatement(Closure.java:403)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.Pipe.run(Pipe.java:108)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.Closure.execute(Closure.java:183)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.Closure.execute(Closure.java:120)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.felix.gogo.runtime.CommandSessionImpl.execute(CommandSessionImpl.java:92)[17:org.apache.karaf.shell.console:2.4.4]
	at org.apache.karaf.shell.console.jline.Console.run(Console.java:197)[17:org.apache.karaf.shell.console:2.4.4]
	at java.lang.Thread.run(Thread.java:745)[:1.8.0_45]
``	

