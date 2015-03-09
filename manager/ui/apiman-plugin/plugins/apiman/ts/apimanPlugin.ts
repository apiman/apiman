/// <reference path="../../includes.ts"/>
/// <reference path="apimanGlobals.ts"/>
module Apiman {

    export var _module = angular.module(Apiman.pluginName, ['ApimanServices', 'ApimanLogger', 'ApimanConfiguration', 'ApimanPageLifecycle']);

    var tab = undefined;

    _module.config(['$locationProvider', '$routeProvider', 'HawtioNavBuilderProvider',
        ($locationProvider, $routeProvider: ng.route.IRouteProvider, builder: HawtioMainNav.BuilderFactory) => {
            tab = builder.create()
                .id(Apiman.pluginName)
                .title(() => "Apiman")
                .href(() => "/apiman")
                .subPath("Home", "dash.html", builder.join(Apiman.templatePath, 'dash.html'))
                .build();
            builder.configureRouting($routeProvider, tab);

            $routeProvider.when('/apiman/app-overview.html',      { templateUrl: builder.join(Apiman.templatePath, 'app-overview.html') });
            $routeProvider.when('/apiman/app-contracts.html',     { templateUrl: builder.join(Apiman.templatePath, 'app-contracts.html') });
            $routeProvider.when('/apiman/consumer-orgs.html',     { templateUrl: builder.join(Apiman.templatePath, 'consumer-orgs.html') });
            $routeProvider.when('/apiman/dash.html',              { templateUrl: builder.join(Apiman.templatePath, 'dash.html') });
            $routeProvider.when('/apiman/new-app.html',           { templateUrl: builder.join(Apiman.templatePath, 'new-app.html') });
            $routeProvider.when('/apiman/new-appversion.html',    { templateUrl: builder.join(Apiman.templatePath, 'new-appversion.html') });
            $routeProvider.when('/apiman/new-org.html',           { templateUrl: builder.join(Apiman.templatePath, 'new-org.html') });
            $routeProvider.when('/apiman/new-plan.html',          { templateUrl: builder.join(Apiman.templatePath, 'new-plan.html') });
            $routeProvider.when('/apiman/new-policy.html',        { templateUrl: builder.join(Apiman.templatePath, 'new-policy.html') });
            $routeProvider.when('/apiman/org-plans.html',         { templateUrl: builder.join(Apiman.templatePath, 'org-plans.html') });
            $routeProvider.when('/apiman/org-services.html',      { templateUrl: builder.join(Apiman.templatePath, 'org-services.html') });
            $routeProvider.when('/apiman/org-apps.html',          { templateUrl: builder.join(Apiman.templatePath, 'org-apps.html') });
            $routeProvider.when('/apiman/org-members.html',       { templateUrl: builder.join(Apiman.templatePath, 'org-members.html') });
            $routeProvider.when('/apiman/plan-overview.html',     { templateUrl: builder.join(Apiman.templatePath, 'plan-overview.html') });
            $routeProvider.when('/apiman/service-overview.html',  { templateUrl: builder.join(Apiman.templatePath, 'service-overview.html') });
            $routeProvider.when('/apiman/user-apps.html',         { templateUrl: builder.join(Apiman.templatePath, 'user-apps.html') });
            $routeProvider.when('/apiman/user-orgs.html',         { templateUrl: builder.join(Apiman.templatePath, 'user-orgs.html') });
            $routeProvider.when('/apiman/user-services.html',     { templateUrl: builder.join(Apiman.templatePath, 'user-services.html') });
            $routeProvider.when('/apiman/error-409.html',         { templateUrl: builder.join(Apiman.templatePath, 'error-409.html') });
            $locationProvider.html5Mode(true);
        }]);

    _module.factory('authInterceptor', ['$q', 'Configuration', function($q, Configuration) {
        var requestInterceptor = {
            request: function(config) {
                if (Configuration.api.auth.type == 'basic') {
                    var username = Configuration.api.auth.basic.username;
                    var password = Configuration.api.auth.basic.password;
                    var enc = btoa(username + ':' + password);
                    config.headers.Authorization = 'Basic ' + enc;
                } else if (Configuration.api.auth.type == 'bearerToken') {
                    // TBD
                } else if (Configuration.api.auth.type == 'authToken') {
                    // TBD
                }
                return config;
            }
        };
        return requestInterceptor;
    }]);
    _module.config(['$httpProvider', function($httpProvider) {
        $httpProvider.interceptors.push('authInterceptor');
    }]);

    _module.run(['$rootScope', 'HawtioNav', ($rootScope, HawtioNav: HawtioMainNav.Registry) => {
        HawtioNav.add(tab);
        $rootScope.pluginName = Apiman.pluginName;
        log.debug("loaded");
    }]);

    hawtioPluginLoader.addModule(Apiman.pluginName);
}
