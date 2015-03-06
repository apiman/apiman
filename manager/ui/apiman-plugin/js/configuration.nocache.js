var APIMAN_CONFIG_DATA = {
  "apiman" : {
    "version" : "1.1.0-SNAPSHOT",
    "builtOn" : "${maven.build.timestamp}",
    "logoutUrl" : "logout"
  },
  "user" : {
    "username" : "admin"
  },
  "api" : {
    "endpoint" : "http://127.0.0.1:80",
    "auth" : {
      "type" : "none",
      "basic" : {
        "username" : "admin",
        "password" : "admin123!"
      }
    }
  }
};
