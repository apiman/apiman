# Apiman Vert.x Gateway 

This is the initial version of an apiman gateway implemented as a [Vert.x](http://www.vertx.io) module.

## Configuration

The following is the standard configuration that should be [provided to the module](http://vertx.io/mods_manual.html) at runtime.

	{
	  "registry": { "class": "io.apiman.gateway.engine.impl.InMemoryRegistry", "config": {} },
	  "connector-factory": { "class": "io.apiman.gateway.vertx.connector.HttpConnectorFactory", "config": {} },
	  "policy-factory": { "class": "io.apiman.gateway.engine.policy.PolicyFactoryImpl", "config": {} },
	  
	  "connectors": {
	    "http-rest": "io.apiman.gateway.connector.HttpConnectorFactory", "config": {}
	  },
	  
	  "components": {
	    "IHttpClientComponent": { "class": "io.apiman.gateway.vertx.components.HttpClientComponentImpl", 
	    	"config": {} },
	
	    "ISharedStateComponent": { "class": "io.apiman.gateway.engine.impl.InMemorySharedStateComponent", 
	    	"config": {} },
	    	
	    "IRateLimiterComponent": { "class": "io.apiman.gateway.engine.impl.InMemoryRateLimiterComponent", 
	    	"config": {} },
	    	
	    "IPolicyFailureFactoryComponent": { "class": "io.apiman.gateway.vertx.components.PolicyFailureFactoryComponent", 
	    	"config": {} }
	  },
	  
	  // Host-name to bind to for this machine.
	  "hostname": "localhost",
	  
	  // You can force a particular endpoint here (e.g. if you have some clustered setup with exotic DNS setup) 
	  "endpoint": "localhost",
	  
	  "routes": {
	    "http-dispatcher": 8200,
	    "http-gateway": 8201,
	    "api": 8202
	  },
	  
	  "auth": {
	    "file-basic": {
	      "example" : "NhWA8jbnUXSGDx0+LsTGFLsC6UHykp7kgnRiWF5X8KU="
	    }
	  },
	
	  "authenticated": true,
	  "realm": "apiman-gateway"
	}
	
For instance, `vert runzip target/<themodule>.zip -conf /path/to/the/conf.json"`. Or you can pass it in programatically with `deployModule(...)`.

### Registries and factories

Unless you are providing your own implementations, you should leave these values as defined above. 

### Routes

You can freely alter which ports a given verticle listens on, and they can be accessed directly or indirectly via the `http-dispatcher`. 

The `http-dispatcher` is a convenient reverse proxy, which forwards traffic to the defined routes. For instance, accessing `localhost:8200/api` will forward the traffic to the `api` verticle on `8202`, whilst `localhost:8200/http-gateway` will be forwarded to the `http-gateway` on `8201`. This allows you to pass all traffic over a single port.

Conversely, if you prefer, the endpoints can be accessed directly.

### Auth

At present, there is only BASIC authentication support for the api. You must provide a string key of your username and a base64 encoded SHA256 hash of the corresponding password. This situation is clearly not very secure, and hence will be superseded shortly using a more comprehensive, secure solution (such as KeyCloak).

Authentication can be turned off entirely by setting: `"authenticated": false`

### Components

These are the various runtime components made available to apiman. 

## Current limitations & future

This first rendition uses mostly simple in-memory components which don't fully exercise the platform's distributed scaling potential. The underlying architecture of the gateway is ready to facilitate significant scaling, hence the next release will implement the necessary distributed components which will enable us to expose clustered, multi-verticle configurations, etc.