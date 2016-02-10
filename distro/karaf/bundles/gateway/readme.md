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

# Test Apiman Gateway API

```
http://localhost:8181/apiman-gateway-api/system/status
```