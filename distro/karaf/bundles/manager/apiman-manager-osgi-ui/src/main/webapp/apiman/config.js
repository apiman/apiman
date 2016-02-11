var APIMAN_CONFIG_DATA = {
    "platform" : "standalone",
    "apiman" : {
        "version" : "${project.version}",
        "builtOn" : "${timestamp}",
        "logoutUrl" : "logout"
    },
    "user" : {
        "username" : "admin"
    },
    "ui" : {
        "header" : "apiman",
        "metrics" : true
    },
    "api" : {
        "endpoint" : "/apiman",
        "auth" : {
            "type" : "basic",
            "basic" : {
                "username" : "admin",
                "password" : "admin123!"
            }
        }
    }
};
