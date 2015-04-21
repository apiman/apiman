/// <reference path="../../includes.ts"/>
/// <reference path="apimanGlobals.ts"/>
module Apiman {

    export var _module = angular.module(Apiman.pluginName,
        ['ApimanServices', 'ApimanLogger', 'ApimanConfiguration', 'ApimanTranslation', 'ApimanPageLifecycle',
         'ApimanCurrentUser', 'ApimanDialogs', 'ui.sortable', 'xeditable']);

    var tab = undefined;

    var routes = {
        '/dash'                : { templateUrl: 'dash.html' },
        '/profile'             : { templateUrl: 'profile.html' },

        '/admin/gateways'      : { templateUrl: 'admin/admin-gateways.html' },
        '/admin/plugins'       : { templateUrl: 'admin/admin-plugins.html' },
        '/admin/policyDefs'    : { templateUrl: 'admin/admin-policyDefs.html' },
        '/admin/roles'         : { templateUrl: 'admin/admin-roles.html' },
        '/admin/gateways/:gateway'      : { templateUrl: 'forms/edit-gateway.html' },
        '/admin/plugins/:plugin'        : { templateUrl: 'forms/edit-plugin.html' },
        '/admin/policyDefs/:policyDef'  : { templateUrl: 'forms/edit-policyDef.html' },
        '/admin/roles/:role'            : { templateUrl: 'forms/edit-role.html' },

        '/orgs/:org/:type/:id/:ver/policies/:policy' : { templateUrl: 'forms/edit-policy.html' },
        '/orgs/:org/:type/:id/:ver/new-policy'       : { templateUrl: 'forms/new-policy.html' },

        '/orgs/:org/apps/:app'                      : { templateUrl: 'app/app.html' },
        '/orgs/:org/apps/:app/:version'             : { templateUrl: 'app/app-overview.html' },
        '/orgs/:org/apps/:app/:version/contracts'   : { templateUrl: 'app/app-contracts.html' },
        '/orgs/:org/apps/:app/:version/apis'        : { templateUrl: 'app/app-apis.html' },
        '/orgs/:org/apps/:app/:version/policies'    : { templateUrl: 'app/app-policies.html' },
        '/orgs/:org/apps/:app/:version/activity'    : { templateUrl: 'app/app-activity.html' },
        '/orgs/:org/apps/:app/:version/new-version' : { templateUrl: 'forms/new-appversion.html' },

        '/orgs/:org/plans/:plan'                      : { templateUrl: 'plan/plan.html' },
        '/orgs/:org/plans/:plan/:version'             : { templateUrl: 'plan/plan-overview.html' },
        '/orgs/:org/plans/:plan/:version/policies'    : { templateUrl: 'plan/plan-policies.html' },
        '/orgs/:org/plans/:plan/:version/activity'    : { templateUrl: 'plan/plan-activity.html' },
        '/orgs/:org/plans/:plan/:version/new-version' : { templateUrl: 'forms/new-planversion.html' },

        '/orgs/:org/services/:service'                      : { templateUrl: 'service/service.html' },
        '/orgs/:org/services/:service/:version'             : { templateUrl: 'service/service-overview.html' },
        '/orgs/:org/services/:service/:version/impl'        : { templateUrl: 'service/service-impl.html' },
        '/orgs/:org/services/:service/:version/plans'       : { templateUrl: 'service/service-plans.html' },
        '/orgs/:org/services/:service/:version/policies'    : { templateUrl: 'service/service-policies.html' },
        '/orgs/:org/services/:service/:version/endpoint'    : { templateUrl: 'service/service-endpoint.html' },
        '/orgs/:org/services/:service/:version/contracts'   : { templateUrl: 'service/service-contracts.html' },
        '/orgs/:org/services/:service/:version/activity'    : { templateUrl: 'service/service-activity.html' },
        '/orgs/:org/services/:service/:version/new-version' : { templateUrl: 'forms/new-serviceversion.html' },

        '/browse/orgs'                        : { templateUrl: 'consumer/consumer-orgs.html' },
        '/browse/services'                    : { templateUrl: 'consumer/consumer-services.html' },
        '/browse/orgs/:org'                   : { templateUrl: 'consumer/consumer-org.html' },
        '/browse/orgs/:org/:service'          : { templateUrl: 'consumer/consumer-service-redirect.html' },
        '/browse/orgs/:org/:service/:version' : { templateUrl: 'consumer/consumer-service.html' },

        '/new-app'                  : { templateUrl: 'forms/new-app.html' },
        '/new-contract'             : { templateUrl: 'forms/new-contract.html' },
        '/new-gateway'              : { templateUrl: 'forms/new-gateway.html' },
        '/new-org'                  : { templateUrl: 'forms/new-org.html' },
        '/new-plan'                 : { templateUrl: 'forms/new-plan.html' },
        '/new-plugin'               : { templateUrl: 'forms/new-plugin.html' },
        '/new-role'                 : { templateUrl: 'forms/new-role.html' },
        '/new-service'              : { templateUrl: 'forms/new-service.html' },
        '/import-policyDefs'        : { templateUrl: 'forms/import-policyDefs.html' },

        '/orgs/:org'                : { templateUrl: 'org/org.html' },
        '/orgs/:org/plans'          : { templateUrl: 'org/org-plans.html' },
        '/orgs/:org/services'       : { templateUrl: 'org/org-services.html' },
        '/orgs/:org/apps'           : { templateUrl: 'org/org-apps.html' },
        '/orgs/:org/members'        : { templateUrl: 'org/org-members.html' },
        '/orgs/:org/manage-members' : { templateUrl: 'org/org-manage-members.html' },
        '/orgs/:org/activity'       : { templateUrl: 'org/org-activity.html' },
        '/orgs/:org/new-member'     : { templateUrl: 'org/org-new-member.html' },

        '/users/:user'              : { templateUrl: 'user/user.html' },
        '/users/:user/activity'     : { templateUrl: 'user/user-activity.html' },
        '/users/:user/apps'         : { templateUrl: 'user/user-apps.html' },
        '/users/:user/orgs'         : { templateUrl: 'user/user-orgs.html' },
        '/users/:user/services'     : { templateUrl: 'user/user-services.html' },

        '/errors/400'          : { templateUrl: 'errors/400.html' },
        '/errors/403'          : { templateUrl: 'errors/403.html' },
        '/errors/404'          : { templateUrl: 'errors/404.html' },
        '/errors/409'          : { templateUrl: 'errors/409.html' },
        '/errors/500'          : { templateUrl: 'errors/500.html' }
    };

    _module.config(['$locationProvider', '$routeProvider', 'HawtioNavBuilderProvider',
        ($locationProvider, $routeProvider: ng.route.IRouteProvider, builder: HawtioMainNav.BuilderFactory) => {
            tab = builder.create()
                .id(Apiman.pluginName)
                .title(() => "Apiman")
                .href(() => "/api-manager")
                .subPath("Home", "dash", builder.join(Apiman.templatePath, 'dash.html'))
                .build();
            builder.configureRouting($routeProvider, tab);

            // Map all the routes into the route provider.
            angular.forEach(routes, function(config, key) {
                config.templateUrl = builder.join(Apiman.templatePath, config.templateUrl);
                this.when('/' + Apiman.pluginName + key, config);
            }, $routeProvider);
            $locationProvider.html5Mode(true);
        }]);

    _module.factory('authInterceptor', 
        ['$q', '$timeout', 'Configuration', 'Logger',
        ($q, $timeout, Configuration, Logger) => {
            var refreshBearerToken = function() {
                Logger.info('Refreshing bearer token now.');
                // Note: we need to use jquery directly for this call, otherwise we will have
                // a circular dependency in angular.
                $.get('rest/tokenRefresh', function(reply) {
                    Logger.info('Bearer token successfully refreshed: {0}', reply);
                    Configuration.api.auth.bearerToken.token = reply.token;
                    var refreshPeriod = reply.refreshPeriod;
                    $timeout(refreshBearerToken, refreshPeriod * 1000);
                }).fail(function(error) {
                    Logger.error('Failed to refresh bearer token: {0}', error);
                });
            };
            if (Configuration.api.auth.type == 'bearerToken') {
                var refreshPeriod = Configuration.api.auth.bearerToken.refreshPeriod;
                $timeout(refreshBearerToken, refreshPeriod * 1000);
            }
            var requestInterceptor = {
                request: function(config) {
                    if (Configuration.api.auth.type == 'basic') {
                        var username = Configuration.api.auth.basic.username;
                        var password = Configuration.api.auth.basic.password;
                        var enc = btoa(username + ':' + password);
                        config.headers.Authorization = 'Basic ' + enc;
                    } else if (Configuration.api.auth.type == 'bearerToken') {
                        var token = Configuration.api.auth.bearerToken.token;
                        config.headers.Authorization = 'Bearer ' + token;
                    } else if (Configuration.api.auth.type == 'authToken') {
                        var token = Configuration.api.auth.bearerToken.token;
                        config.headers.Authorization = 'AUTH-TOKEN ' + token;
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
