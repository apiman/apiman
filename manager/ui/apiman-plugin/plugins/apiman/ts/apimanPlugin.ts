/// <reference path="../../includes.ts"/>
/// <reference path="apimanGlobals.ts"/>
module Apiman {

    export var _module = angular.module(Apiman.pluginName,
        ['ApimanServices', 'ApimanLogger', 'ApimanConfiguration', 'ApimanPageLifecycle', 'ApimanCurrentUser', 'ApimanDialogs']);

    var tab = undefined;

    var commonRouteResolves = {
      'CurrentUser' : function(CurrentUser) {
          return CurrentUser.promise;
      }
    };
    var routes = {
        '/apiman/admin-gateways.html'   : { templateUrl: 'admin/admin-gateways.html' },
        '/apiman/admin-plugins.html'    : { templateUrl: 'admin/admin-plugins.html' },
        '/apiman/admin-policyDefs.html' : { templateUrl: 'admin/admin-policyDefs.html' },
        '/apiman/admin-roles.html'      : { templateUrl: 'admin/admin-roles.html' },
        '/apiman/app-overview.html'     : { templateUrl: 'app/app-overview.html' },
        '/apiman/app-contracts.html'    : { templateUrl: 'app/app-contracts.html' },
        '/apiman/app-apis.html'         : { templateUrl: 'app/app-apis.html' },
        '/apiman/app-policies.html'     : { templateUrl: 'app/app-policies.html' },
        '/apiman/app-activity.html'     : { templateUrl: 'app/app-activity.html' },
        '/apiman/consumer-org.html'     : { templateUrl: 'consumer/consumer-org.html' },
        '/apiman/consumer-orgs.html'    : { templateUrl: 'consumer/consumer-orgs.html' },
        '/apiman/consumer-services.html': { templateUrl: 'consumer/consumer-services.html' },
        '/apiman/consumer-service.html' : { templateUrl: 'consumer/consumer-service.html' },
        '/apiman/dash.html'             : { templateUrl: 'dash.html' },
        '/apiman/edit-gateway.html'     : { templateUrl: 'forms/edit-gateway.html' },
        '/apiman/edit-plugin.html'      : { templateUrl: 'forms/edit-plugin.html' },
        '/apiman/edit-policy.html'      : { templateUrl: 'forms/edit-policy.html' },
        '/apiman/edit-policyDef.html'   : { templateUrl: 'forms/edit-policyDef.html' },
        '/apiman/edit-role.html'        : { templateUrl: 'forms/edit-role.html' },
        '/apiman/import-policyDefs.html': { templateUrl: 'forms/import-policyDefs.html' },
        '/apiman/new-app.html'          : { templateUrl: 'forms/new-app.html' },
        '/apiman/new-appversion.html'   : { templateUrl: 'forms/new-appversion.html' },
        '/apiman/new-contract.html'     : { templateUrl: 'forms/new-contract.html' },
        '/apiman/new-gateway.html'      : { templateUrl: 'forms/new-gateway.html' },
        '/apiman/new-org.html'          : { templateUrl: 'forms/new-org.html' },
        '/apiman/new-plan.html'         : { templateUrl: 'forms/new-plan.html' },
        '/apiman/new-planversion.html'  : { templateUrl: 'forms/new-planversion.html' },
        '/apiman/new-plugin.html'       : { templateUrl: 'forms/new-plugin.html' },
        '/apiman/new-policy.html'       : { templateUrl: 'forms/new-policy.html' },
        '/apiman/new-role.html'         : { templateUrl: 'forms/new-role.html' },
        '/apiman/new-service.html'      : { templateUrl: 'forms/new-service.html' },
        '/apiman/new-serviceversion.html'   : { templateUrl: 'forms/new-serviceversion.html' },
        '/apiman/org-plans.html'        : { templateUrl: 'org/org-plans.html' },
        '/apiman/org-services.html'     : { templateUrl: 'org/org-services.html' },
        '/apiman/org-apps.html'         : { templateUrl: 'org/org-apps.html' },
        '/apiman/org-members.html'      : { templateUrl: 'org/org-members.html' },
        '/apiman/org-manage-members.html'   : { templateUrl: 'org/org-manage-members.html' },
        '/apiman/org-new-member.html'  : { templateUrl: 'org/org-new-member.html' },
        '/apiman/org-activity.html'     : { templateUrl: 'org/org-activity.html' },
        '/apiman/plan-overview.html'    : { templateUrl: 'plan/plan-overview.html' },
        '/apiman/plan-policies.html'    : { templateUrl: 'plan/plan-policies.html' },
        '/apiman/plan-activity.html'    : { templateUrl: 'plan/plan-activity.html' },
        '/apiman/service-activity.html' : { templateUrl: 'service/service-activity.html' },
        '/apiman/service-contracts.html': { templateUrl: 'service/service-contracts.html' },
        '/apiman/service-endpoint.html' : { templateUrl: 'service/service-endpoint.html' },
        '/apiman/service-overview.html' : { templateUrl: 'service/service-overview.html' },
        '/apiman/service-impl.html'     : { templateUrl: 'service/service-impl.html' },
        '/apiman/service-plans.html'    : { templateUrl: 'service/service-plans.html' },
        '/apiman/service-policies.html' : { templateUrl: 'service/service-policies.html' },
        '/apiman/user-activity.html'    : { templateUrl: 'user/user-activity.html' },
        '/apiman/user-apps.html'        : { templateUrl: 'user/user-apps.html' },
        '/apiman/user-orgs.html'        : { templateUrl: 'user/user-orgs.html' },
        '/apiman/user-services.html'    : { templateUrl: 'user/user-services.html' },
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
