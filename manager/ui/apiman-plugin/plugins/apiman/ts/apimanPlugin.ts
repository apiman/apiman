/// <reference path="../../includes.ts"/>
/// <reference path="apimanGlobals.ts"/>
module Apiman {

    export var _module = angular.module(Apiman.pluginName, 
        ['ApimanServices', 'ApimanLogger', 'ApimanConfiguration', 'ApimanPageLifecycle', 'ApimanCurrentUser']);

    var tab = undefined;

    var commonRouteResolves = {
      'CurrentUser' : function(CurrentUser) {
          return CurrentUser.promise;
      }
    };
    var routes = {
        '/apiman/admin-gateways.html'   : { templateUrl: 'admin-gateways.html' },
        '/apiman/admin-plugins.html'    : { templateUrl: 'admin-plugins.html' },
        '/apiman/admin-policyDefs.html' : { templateUrl: 'admin-policyDefs.html' },
        '/apiman/admin-roles.html'      : { templateUrl: 'admin-roles.html' },
        '/apiman/app-overview.html'     : { templateUrl: 'app-overview.html' },
        '/apiman/app-contracts.html'    : { templateUrl: 'app-contracts.html' },
        '/apiman/app-apis.html'         : { templateUrl: 'app-apis.html' },
        '/apiman/app-policies.html'     : { templateUrl: 'app-policies.html' },
        '/apiman/app-activity.html'     : { templateUrl: 'app-activity.html' },
        '/apiman/consumer-org.html'     : { templateUrl: 'consumer-org.html' },
        '/apiman/consumer-orgs.html'    : { templateUrl: 'consumer-orgs.html' },
        '/apiman/consumer-services.html': { templateUrl: 'consumer-services.html' },
        '/apiman/consumer-service.html' : { templateUrl: 'consumer-service.html' },
        '/apiman/dash.html'             : { templateUrl: 'dash.html' },
        '/apiman/edit-gateway.html'     : { templateUrl: 'edit-gateway.html' },
        '/apiman/edit-plugin.html'      : { templateUrl: 'edit-plugin.html' },
        '/apiman/edit-policyDef.html'   : { templateUrl: 'edit-policyDef.html' },
        '/apiman/new-app.html'          : { templateUrl: 'new-app.html' },
        '/apiman/new-appversion.html'   : { templateUrl: 'new-appversion.html' },
        '/apiman/new-org.html'          : { templateUrl: 'new-org.html' },
        '/apiman/new-plan.html'         : { templateUrl: 'new-plan.html' },
        '/apiman/new-policy.html'       : { templateUrl: 'new-policy.html' },
        '/apiman/org-plans.html'        : { templateUrl: 'org-plans.html' },
        '/apiman/org-services.html'     : { templateUrl: 'org-services.html' },
        '/apiman/org-apps.html'         : { templateUrl: 'org-apps.html' },
        '/apiman/org-members.html'      : { templateUrl: 'org-members.html' },
        '/apiman/plan-overview.html'    : { templateUrl: 'plan-overview.html' },
        '/apiman/plan-policies.html'    : { templateUrl: 'plan-policies.html' },
        '/apiman/plan-activity.html'    : { templateUrl: 'plan-activity.html' },
        '/apiman/service-overview.html' : { templateUrl: 'service-overview.html' },
        '/apiman/user-apps.html'        : { templateUrl: 'user-apps.html' },
        '/apiman/user-orgs.html'        : { templateUrl: 'user-orgs.html' },
        '/apiman/user-services.html'    : { templateUrl: 'user-services.html' },
        '/apiman/error-409.html'        : { templateUrl: 'error-409.html' }
    };

    _module.config(['$locationProvider', '$routeProvider', 'HawtioNavBuilderProvider',
        ($locationProvider, $routeProvider: ng.route.IRouteProvider, builder: HawtioMainNav.BuilderFactory) => {
            tab = builder.create()
                .id(Apiman.pluginName)
                .title(() => "Apiman")
                .href(() => "/apiman")
                .subPath("Home", "dash.html", builder.join(Apiman.templatePath, 'dash.html'))
                .build();
            builder.configureRouting($routeProvider, tab);
            
            // Map all the routes into the route provider.
            angular.forEach(routes, function(config, key) {
                config.templateUrl = builder.join(Apiman.templatePath, config.templateUrl);
                config.resolve = angular.extend({}, config.resolve, commonRouteResolves);
                this.when(key, config);
            }, $routeProvider);
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
