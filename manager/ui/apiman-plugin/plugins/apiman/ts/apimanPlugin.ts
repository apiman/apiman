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
    
    // /apiman/admin/*

    // /apiman/new-org
    // /apiman/new-app
    // /apiman/new-service
    // /apiman/new-plan

    // /apiman/orgs/{orgId} (redirect only)
    // /apiman/orgs/{orgId}/apps
    // /apiman/orgs/{orgId}/services
    // /apiman/orgs/{orgId}/plans

    // /apiman/orgs/{orgId}/apps/{appId}  (redirect only)
    // /apiman/orgs/{orgId}/apps/{appId}/{version}
    // /apiman/orgs/{orgId}/apps/{appId}/{version}/contracts
    // /apiman/orgs/{orgId}/apps/{appId}/{version}/apis
    // /apiman/orgs/{orgId}/apps/{appId}/{version}/policies
    // /apiman/orgs/{orgId}/apps/{appId}/{version}/activity

    // /apiman/orgs/{orgId}/services/{svcId}  (redirect only)
    // /apiman/orgs/{orgId}/services/{svcId}/{version}
    // /apiman/orgs/{orgId}/services/{svcId}/{version}/impl
    // /apiman/orgs/{orgId}/services/{svcId}/{version}/plans
    // /apiman/orgs/{orgId}/services/{svcId}/{version}/policies
    // /apiman/orgs/{orgId}/services/{svcId}/{version}/contracts
    // /apiman/orgs/{orgId}/services/{svcId}/{version}/endpoint
    // /apiman/orgs/{orgId}/services/{svcId}/{version}/activity

    // /apiman/orgs/{orgId}/plans/{planId}  (redirect only)
    // /apiman/orgs/{orgId}/plans/{planId}/{version}
    // /apiman/orgs/{orgId}/plans/{planId}/{version}/policies
    // /apiman/orgs/{orgId}/plans/{planId}/{version}/activity


    var routes = {
        '/dash'                     : { templateUrl: 'dash.html' },

        '/admin-gateways.html'      : { templateUrl: 'admin/admin-gateways.html' },
        '/admin-plugins.html'       : { templateUrl: 'admin/admin-plugins.html' },
        '/admin-policyDefs.html'    : { templateUrl: 'admin/admin-policyDefs.html' },
        '/admin-roles.html'         : { templateUrl: 'admin/admin-roles.html' },

        '/orgs/:org/:type/:id/:ver/policies/:policy' : { templateUrl: 'forms/edit-policy.html' },
        '/orgs/:org/:type/:id/:ver/new-policy'       : { templateUrl: 'forms/new-policy.html' },

        '/orgs/:org/apps/:app'                      : { templateUrl: 'app/app.html' },
        '/orgs/:org/apps/:app/:version'             : { templateUrl: 'app/app-overview.html' },
        '/orgs/:org/apps/:app/:version/contracts'   : { templateUrl: 'app/app-contracts.html' },
        '/orgs/:org/apps/:app/:version/apis'        : { templateUrl: 'app/app-apis.html' },
        '/orgs/:org/apps/:app/:version/policies'    : { templateUrl: 'app/app-policies.html' },
        '/orgs/:org/apps/:app/:version/activity'    : { templateUrl: 'app/app-activity.html' },
        '/orgs/:org/apps/:app/:version/new-version' : { templateUrl: 'forms/new-appversion.html' },

        '/browse/orgs'                        : { templateUrl: 'consumer/consumer-orgs.html' },
        '/browse/services'                    : { templateUrl: 'consumer/consumer-services.html' },
        '/browse/orgs/:org'                   : { templateUrl: 'consumer/consumer-org.html' },
        '/browse/orgs/:org/:service'          : { templateUrl: 'consumer/consumer-service-redirect.html' },
        '/browse/orgs/:org/:service/:version' : { templateUrl: 'consumer/consumer-service.html' },

        '/edit-gateway.html'        : { templateUrl: 'forms/edit-gateway.html' },
        '/edit-plugin.html'         : { templateUrl: 'forms/edit-plugin.html' },
        '/edit-policyDef.html'      : { templateUrl: 'forms/edit-policyDef.html' },
        '/edit-role.html'           : { templateUrl: 'forms/edit-role.html' },
        '/import-policyDefs.html'   : { templateUrl: 'forms/import-policyDefs.html' },
        '/new-app.html'             : { templateUrl: 'forms/new-app.html' },
        '/new-contract.html'        : { templateUrl: 'forms/new-contract.html' },
        '/new-gateway.html'         : { templateUrl: 'forms/new-gateway.html' },
        '/new-org.html'             : { templateUrl: 'forms/new-org.html' },
        '/new-plan.html'            : { templateUrl: 'forms/new-plan.html' },
        '/new-planversion.html'     : { templateUrl: 'forms/new-planversion.html' },
        '/new-plugin.html'          : { templateUrl: 'forms/new-plugin.html' },
        '/new-role.html'            : { templateUrl: 'forms/new-role.html' },
        '/new-service.html'         : { templateUrl: 'forms/new-service.html' },
        '/new-serviceversion.html'  : { templateUrl: 'forms/new-serviceversion.html' },

        '/orgs/:org'                : { templateUrl: 'org/org.html' },
        '/orgs/:org/plans'          : { templateUrl: 'org/org-plans.html' },
        '/orgs/:org/services'       : { templateUrl: 'org/org-services.html' },
        '/orgs/:org/apps'           : { templateUrl: 'org/org-apps.html' },
        '/orgs/:org/members'        : { templateUrl: 'org/org-members.html' },
        '/orgs/:org/manage-members' : { templateUrl: 'org/org-manage-members.html' },
        '/orgs/:org/activity'       : { templateUrl: 'org/org-activity.html' },
        '/orgs/:org/new-member'     : { templateUrl: 'org/org-new-member.html' },
        
        '/plan-overview.html'       : { templateUrl: 'plan/plan-overview.html' },
        '/plan-policies.html'       : { templateUrl: 'plan/plan-policies.html' },
        '/plan-activity.html'       : { templateUrl: 'plan/plan-activity.html' },
        
        '/service-activity.html'    : { templateUrl: 'service/service-activity.html' },
        '/service-contracts.html'   : { templateUrl: 'service/service-contracts.html' },
        '/service-endpoint.html'    : { templateUrl: 'service/service-endpoint.html' },
        '/service-overview.html'    : { templateUrl: 'service/service-overview.html' },
        '/service-impl.html'        : { templateUrl: 'service/service-impl.html' },
        '/service-plans.html'       : { templateUrl: 'service/service-plans.html' },
        '/service-policies.html'    : { templateUrl: 'service/service-policies.html' },
        
        '/users/:user'              : { templateUrl: 'user/user.html' },
        '/users/:user/activity'     : { templateUrl: 'user/user-activity.html' },
        '/users/:user/apps'         : { templateUrl: 'user/user-apps.html' },
        '/users/:user/orgs'         : { templateUrl: 'user/user-orgs.html' },
        '/users/:user/services'     : { templateUrl: 'user/user-services.html' },
        
        '/errors/400.html'          : { templateUrl: 'errors/400.html' },
        '/errors/403.html'          : { templateUrl: 'errors/403.html' },
        '/errors/404.html'          : { templateUrl: 'errors/404.html' },
        '/errors/409.html'          : { templateUrl: 'errors/409.html' },
        '/errors/500.html'          : { templateUrl: 'errors/500.html' }
    };

    _module.config(['$locationProvider', '$routeProvider', 'HawtioNavBuilderProvider',
        ($locationProvider, $routeProvider: ng.route.IRouteProvider, builder: HawtioMainNav.BuilderFactory) => {
            tab = builder.create()
                .id(Apiman.pluginName)
                .title(() => "Apiman")
                .href(() => "/apiman")
                .subPath("Home", "dash", builder.join(Apiman.templatePath, 'dash.html'))
                .build();
            builder.configureRouting($routeProvider, tab);

            // Map all the routes into the route provider.
            angular.forEach(routes, function(config, key) {
                config.templateUrl = builder.join(Apiman.templatePath, config.templateUrl);
                config.resolve = angular.extend({}, config.resolve, commonRouteResolves);
                this.when('/' + Apiman.pluginName + key, config);
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
