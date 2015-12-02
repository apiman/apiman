# How to install on JBoss Fuse

* Start JBoss Fuse Server
* when the console is launched, execute these commands

```
features:addurl mvn:io.apiman/apiman-karaf/1.2.0-SNAPSHOT/xml/features
features:install -c apiman-server-manager
uninstall 265
``` 