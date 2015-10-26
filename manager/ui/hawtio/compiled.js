/// <reference path="../typings-custom/tsd.d.ts"/>
/// <reference path="../typings/tsd.d.ts"/>

/// <reference path='../../includes.ts'/>
var Apiman;
(function (Apiman) {
    Apiman.pluginName = 'api-manager';
    Apiman.log = Logger.get(Apiman.pluginName);
    Apiman.templatePath = 'plugins/api-manager/html';
})(Apiman || (Apiman = {}));

/// <reference path='../../includes.ts'/>
/// <reference path='apimanGlobals.ts'/>
var Apiman;
(function (Apiman) {
    Apiman._module = angular.module(Apiman.pluginName, [
        'angular-clipboard',
        'ngRoute',
        'ui.sortable',
        'xeditable',
        'ApimanServices',
        'ApimanLogger',
        'ApimanConfiguration',
        'ApimanTranslation',
        'ApimanPageLifecycle',
        'ApimanCurrentUser',
        'ApimanDialogs'
    ]);
    Apiman._module.config([
        '$locationProvider',
        '$routeProvider', function ($locationProvider, $routeProvider) {
            var path = 'plugins/api-manager/html/';
            var prefix = '/api-manager';
            // Define Routes
            $routeProvider
                .when(prefix + '/', {
                templateUrl: path + 'dash.html'
            })
                .when(prefix + '/about', {
                templateUrl: path + 'about.html'
            })
                .when(prefix + '/profile', {
                templateUrl: path + 'profile.html'
            })
                .when(prefix + '/admin/gateways', {
                templateUrl: path + 'admin/admin-gateways.html'
            })
                .when(prefix + '/admin/plugins', {
                templateUrl: path + 'admin/admin-plugins.html'
            })
                .when(prefix + '/admin/policyDefs', {
                templateUrl: path + 'admin/admin-policyDefs.html'
            })
                .when(prefix + '/admin/roles', {
                templateUrl: path + 'admin/admin-roles.html'
            })
                .when(prefix + '/admin/gateways/:gateway', {
                templateUrl: path + 'forms/edit-gateway.html'
            })
                .when(prefix + '/admin/plugins/:plugin', {
                templateUrl: path + 'forms/edit-plugin.html'
            })
                .when(prefix + '/admin/policyDefs/:policyDef', {
                templateUrl: path + 'forms/edit-policyDef.html'
            })
                .when(prefix + '/admin/roles/:role', {
                templateUrl: path + 'forms/edit-role.html'
            })
                .when(prefix + '/orgs/:org/:type/:id/:ver/policies/:policy', {
                templateUrl: path + 'forms/edit-policy.html'
            })
                .when(prefix + '/orgs/:org/:type/:id/:ver/new-policy', {
                templateUrl: path + 'forms/new-policy.html'
            })
                .when(prefix + '/orgs/:org/apps/:app', {
                templateUrl: path + 'app/app.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version', {
                templateUrl: path + 'app/app-overview.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version/contracts', {
                templateUrl: path + 'app/app-contracts.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version/apis', {
                templateUrl: path + 'app/app-apis.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version/metrics', {
                templateUrl: path + 'app/app-metrics.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version/policies', {
                templateUrl: path + 'app/app-policies.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version/activity', {
                templateUrl: path + 'app/app-activity.html'
            })
                .when(prefix + '/orgs/:org/apps/:app/:version/new-version', {
                templateUrl: path + 'forms/new-appversion.html'
            })
                .when(prefix + '/orgs/:org/plans/:plan', {
                templateUrl: path + 'plan/plan.html'
            })
                .when(prefix + '/orgs/:org/plans/:plan/:version', {
                templateUrl: path + 'plan/plan-overview.html'
            })
                .when(prefix + '/orgs/:org/plans/:plan/:version/policies', {
                templateUrl: path + 'plan/plan-policies.html'
            })
                .when(prefix + '/orgs/:org/plans/:plan/:version/activity', {
                templateUrl: path + 'plan/plan-activity.html'
            })
                .when(prefix + '/orgs/:org/plans/:plan/:version/new-version', {
                templateUrl: path + 'forms/new-planversion.html'
            })
                .when(prefix + '/orgs/:org/services/:service', {
                templateUrl: path + 'service/service.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version', {
                templateUrl: path + 'service/service-overview.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/impl', {
                templateUrl: path + 'service/service-impl.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/def', {
                templateUrl: path + 'service/service-def.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/plans', {
                templateUrl: path + 'service/service-plans.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/policies', {
                templateUrl: path + 'service/service-policies.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/endpoint', {
                templateUrl: path + 'service/service-endpoint.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/contracts', {
                templateUrl: path + 'service/service-contracts.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/metrics', {
                templateUrl: path + 'service/service-metrics.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/activity', {
                templateUrl: path + 'service/service-activity.html'
            })
                .when(prefix + '/orgs/:org/services/:service/:version/new-version', {
                templateUrl: path + 'forms/new-serviceversion.html'
            })
                .when(prefix + '/orgs/:org/import/services', {
                templateUrl: path + 'service/import-services.html'
            })
                .when(prefix + '/browse/orgs', {
                templateUrl: path + 'consumer/consumer-orgs.html'
            })
                .when(prefix + '/browse/services', {
                templateUrl: path + 'consumer/consumer-services.html'
            })
                .when(prefix + '/browse/orgs/:org', {
                templateUrl: path + 'consumer/consumer-org.html'
            })
                .when(prefix + '/browse/orgs/:org/:service', {
                templateUrl: path + 'consumer/consumer-service-redirect.html'
            })
                .when(prefix + '/browse/orgs/:org/:service/:version', {
                templateUrl: path + 'consumer/consumer-service.html'
            })
                .when(prefix + '/browse/orgs/:org/:service/:version/def', {
                templateUrl: path + 'consumer/consumer-service-def.html'
            })
                .when(prefix + '/new-app', {
                templateUrl: path + 'forms/new-app.html'
            })
                .when(prefix + '/new-contract', {
                templateUrl: path + 'forms/new-contract.html'
            })
                .when(prefix + '/new-gateway', {
                templateUrl: path + 'forms/new-gateway.html'
            })
                .when(prefix + '/new-org', {
                templateUrl: path + 'forms/new-org.html'
            })
                .when(prefix + '/new-plan', {
                templateUrl: path + 'forms/new-plan.html'
            })
                .when(prefix + '/new-plugin', {
                templateUrl: path + 'forms/new-plugin.html'
            })
                .when(prefix + '/new-role', {
                templateUrl: path + 'forms/new-role.html'
            })
                .when(prefix + '/new-service', {
                templateUrl: path + 'forms/new-service.html'
            })
                .when(prefix + '/import-policyDefs', {
                templateUrl: path + 'forms/import-policyDefs.html'
            })
                .when(prefix + '/orgs/:org', {
                templateUrl: path + 'org/org.html'
            })
                .when(prefix + '/orgs/:org/plans', {
                templateUrl: path + 'org/org-plans.html'
            })
                .when(prefix + '/orgs/:org/services', {
                templateUrl: path + 'org/org-services.html'
            })
                .when(prefix + '/orgs/:org/apps', {
                templateUrl: path + 'org/org-apps.html'
            })
                .when(prefix + '/orgs/:org/members', {
                templateUrl: path + 'org/org-members.html'
            })
                .when(prefix + '/orgs/:org/manage-members', {
                templateUrl: path + 'org/org-manage-members.html'
            })
                .when(prefix + '/orgs/:org/activity', {
                templateUrl: path + 'org/org-activity.html'
            })
                .when(prefix + '/orgs/:org/new-member', {
                templateUrl: path + 'org/org-new-member.html'
            })
                .when(prefix + '/users/:user', {
                templateUrl: path + 'user/user.html'
            })
                .when(prefix + '/users/:user/activity', {
                templateUrl: path + 'user/user-activity.html'
            })
                .when(prefix + '/users/:user/apps', {
                templateUrl: path + 'user/user-apps.html'
            })
                .when(prefix + '/users/:user/orgs', {
                templateUrl: path + 'user/user-orgs.html'
            })
                .when(prefix + '/users/:user/services', {
                templateUrl: path + 'user/user-services.html'
            })
                .when(prefix + '/errors/invalid_server', {
                templateUrl: path + 'errors/invalid_server.html'
            })
                .when(prefix + '/errors/400', {
                templateUrl: path + 'errors/400.html'
            })
                .when(prefix + '/errors/403', {
                templateUrl: path + 'errors/403.html'
            })
                .when(prefix + '/errors/404', {
                templateUrl: path + 'errors/404.html'
            })
                .when(prefix + '/errors/409', {
                templateUrl: path + 'errors/409.html'
            })
                .when(prefix + '/errors/500', {
                templateUrl: path + 'errors/500.html'
            })
                .otherwise({ redirectTo: prefix + '/' });
            $locationProvider.html5Mode(true);
        }]);
    Apiman._module.factory('authInterceptor', ['$q', '$timeout', 'Configuration', 'Logger',
        function ($q, $timeout, Configuration, Logger) {
            var refreshBearerToken = function () {
                Logger.info('Refreshing bearer token now.');
                // Note: we need to use jquery directly for this call, otherwise we will have
                // a circular dependency in angular.
                $.get('rest/tokenRefresh', function (reply) {
                    Logger.info('Bearer token successfully refreshed: {0}', reply);
                    Configuration.api.auth.bearerToken.token = reply.token;
                    var refreshPeriod = reply.refreshPeriod;
                    if (!refreshPeriod || refreshPeriod < 1) {
                        Logger.info('Refresh period was invalid! (using 60s)');
                        refreshPeriod = 60;
                    }
                    $timeout(refreshBearerToken, refreshPeriod * 1000);
                }).fail(function (error) {
                    Logger.error('Failed to refresh bearer token: {0}', error);
                });
            };
            if (Configuration.api.auth.type == 'bearerToken') {
                var refreshPeriod = Configuration.api.auth.bearerToken.refreshPeriod;
                $timeout(refreshBearerToken, refreshPeriod * 1000);
            }
            var requestInterceptor = {
                request: function (config) {
                    var authHeader = Configuration.getAuthorizationHeader();
                    if (authHeader) {
                        config.headers.Authorization = authHeader;
                    }
                    return config;
                }
            };
            return requestInterceptor;
        }]);
    Apiman._module.config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('authInterceptor');
        }]);
    Apiman._module.run([
        '$rootScope',
        'SystemSvcs',
        'Configuration',
        '$location', function ($rootScope, SystemSvcs, Configuration, $location) {
            $rootScope.isDirty = false;
            $rootScope.$on('$locationChangeStart', function (event, newUrl, oldUrl) {
                if ($rootScope.isDirty) {
                    if (confirm('You have unsaved changes. Are you sure you would like to navigate away from this page? You will lose these changes.') != true) {
                        event.preventDefault();
                    }
                }
            });
            if (Configuration.api
                && Configuration.api.auth
                && Configuration.api.auth.type == 'bearerTokenFromHash') {
                var bearerToken = null;
                var backTo = null;
                var tokenKey = 'Apiman.BearerToken';
                var backToKey = 'Apiman.BackToConsole';
                var hash = $location.hash();
                if (hash) {
                    var settings = JSON.parse(hash);
                    localStorage[tokenKey] = settings.token;
                    localStorage[backToKey] = settings.backTo;
                    $location.hash(null);
                    bearerToken = settings.token;
                    backTo = settings.backTo;
                    console.log('*** Bearer token from hash: ' + bearerToken);
                }
                else {
                    try {
                        bearerToken = localStorage[tokenKey];
                        console.log('*** Bearer token from local storage: ' + bearerToken);
                    }
                    catch (e) {
                        console.log('*** Bearer token from local storage was invalid!');
                        localStorage.removeItem(tokenKey);
                        bearerToken = null;
                    }
                    try {
                        backTo = localStorage[backToKey];
                        console.log('*** Back-to-console link: ' + backTo);
                    }
                    catch (e) {
                        console.log('*** Back-to-console link from local storage was invalid!');
                        localStorage.removeItem(backToKey);
                        backTo = null;
                    }
                }
                if (bearerToken) {
                    Configuration.api.auth.bearerToken = {
                        token: bearerToken
                    };
                }
                if (backTo) {
                    Configuration.ui.backToConsole = backTo;
                }
            }
            $rootScope.pluginName = Apiman.pluginName;
        }]);
    // Load the configuration jsonp script
    $.getScript('apiman/config.js')
        .done(function (script, textStatus) {
        Apiman.log.info('Loaded the config.js config!');
    })
        .fail(function (response) {
        Apiman.log.debug('Error fetching configuration: ', response);
    })
        .always(function () {
        // Load the i18n jsonp script
        $.getScript('apiman/translations.js').done(function (script, textStatus) {
            Apiman.log.info('Loaded the translations.js bundle!');
            angular.element(document).ready(function () {
                angular.bootstrap(document, ['api-manager']);
            });
        }).fail(function (response) {
            Apiman.log.debug('Error fetching translations: ', response);
        });
    });
})(Apiman || (Apiman = {}));

/// <reference path="apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.DashController = Apiman._module.controller("Apiman.AboutController", ['$scope', 'PageLifecycle', 'CurrentUser', 'Configuration',
        function ($scope, PageLifecycle, CurrentUser, Configuration) {
            PageLifecycle.loadPage('About', undefined, $scope, function () {
                $scope.github = "http://github.com/apiman/apiman";
                $scope.site = "http://apiman.io/";
                $scope.userGuide = "http://www.apiman.io/latest/user-guide.html";
                $scope.tutorials = "http://www.apiman.io/latest/tutorials.html";
                $scope.version = Configuration.apiman.version;
                $scope.builtOn = Configuration.apiman.builtOn;
                $scope.apiEndpoint = Configuration.api.endpoint;
                PageLifecycle.setPageTitle('about');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path='../../includes.ts'/>
var ApimanConfiguration;
(function (ApimanConfiguration) {
    ApimanConfiguration._module = angular.module('ApimanConfiguration', []);
    ApimanConfiguration.Configuration = ApimanConfiguration._module.factory('Configuration', ['$window',
        function ($window) {
            var cdata = {};
            if ($window['APIMAN_CONFIG_DATA']) {
                cdata = angular.copy($window['APIMAN_CONFIG_DATA']);
                delete $window['APIMAN_CONFIG_DATA'];
            }
            else {
                console.log('***  Missing variable: APIMAN_CONFIG_DATA  ***');
            }
            cdata.getAuthorizationHeader = function () {
                var authHeader = null;
                if (cdata.api.auth.type == 'basic') {
                    var username = cdata.api.auth.basic.username;
                    var password = cdata.api.auth.basic.password;
                    var enc = btoa(username + ':' + password);
                    authHeader = 'Basic ' + enc;
                }
                else if (cdata.api.auth.type == 'bearerToken' || cdata.api.auth.type == 'bearerTokenFromHash') {
                    if (cdata.api.auth.bearerToken && cdata.api.auth.bearerToken.token) {
                        var token = cdata.api.auth.bearerToken.token;
                        authHeader = 'Bearer ' + token;
                    }
                    else {
                        console.log('** Auth type was ' + cdata.api.auth.type + ' but no bearer-token was found! **');
                    }
                }
                else if (cdata.api.auth.type == 'authToken') {
                    var token = cdata.api.auth.bearerToken.token;
                    authHeader = 'AUTH-TOKEN ' + token;
                }
                return authHeader;
            };
            if (!cdata.ui) {
                cdata.ui = {
                    header: false,
                    metrics: true
                };
            }
            if (cdata.ui.metrics == undefined || cdata.ui.metrics == null) {
                cdata.ui.metrics = true;
            }
            return cdata;
        }]);
})(ApimanConfiguration || (ApimanConfiguration = {}));

/// <reference path='../../includes.ts'/>
var ApimanCurrentUser;
(function (ApimanCurrentUser) {
    ApimanCurrentUser._module = angular.module('ApimanCurrentUser', ['ApimanServices']);
    ApimanCurrentUser.CurrentUser = ApimanCurrentUser._module.factory('CurrentUser', ['$q', '$rootScope', 'CurrentUserSvcs', 'Logger',
        function ($q, $rootScope, CurrentUserSvcs, Logger) {
            return {
                getCurrentUser: function () {
                    return $rootScope.currentUser;
                },
                getCurrentUserOrgs: function () {
                    var orgs = {};
                    var perms = $rootScope.currentUser.permissions;
                    for (var i = 0; i < perms.length; i++) {
                        var perm = perms[i];
                        orgs[perm.organizationId] = true;
                    }
                    var rval = [];
                    angular.forEach(orgs, function (value, key) {
                        this.push(key);
                    }, rval);
                    return rval;
                },
                hasPermission: function (organizationId, permission) {
                    if (organizationId && $rootScope.permissions) {
                        var permid = organizationId + '||' + permission;
                        return $rootScope.permissions[permid];
                    }
                    else {
                        return false;
                    }
                },
                isMember: function (organizationId) {
                    if (organizationId) {
                        return $rootScope.memberships[organizationId];
                    }
                    else {
                        return false;
                    }
                },
                clear: function () {
                    $rootScope.currentUser = undefined;
                }
            };
        }]);
})(ApimanCurrentUser || (ApimanCurrentUser = {}));

/// <reference path="apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.DashController = Apiman._module.controller("Apiman.DashController", ['$scope', 'PageLifecycle', 'CurrentUser',
        function ($scope, PageLifecycle, CurrentUser) {
            PageLifecycle.loadPage('Dash', undefined, $scope, function () {
                $scope.isAdmin = CurrentUser.getCurrentUser().admin;
                $scope.currentUser = CurrentUser.getCurrentUser();
                PageLifecycle.setPageTitle('dashboard');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../../includes.ts"/>
var ApimanDialogs;
(function (ApimanDialogs) {
    ApimanDialogs._module = angular.module("ApimanDialogs", ["ApimanLogger", "ApimanServices"]);
    ApimanDialogs.Dialogs = ApimanDialogs._module.factory('Dialogs', ['Logger', '$compile', '$rootScope', '$timeout', 'ApimanSvcs', 'OrgSvcs',
        function (Logger, $compile, $rootScope, $timeout, ApimanSvcs, OrgSvcs) {
            return {
                // A standard confirmation dialog
                /////////////////////////////////
                confirm: function (title, message, yesCallback, noCallback) {
                    var modalScope = $rootScope.$new(true);
                    modalScope.onYes = function () {
                        if (yesCallback) {
                            yesCallback();
                        }
                    };
                    modalScope.onNo = function () {
                        if (noCallback) {
                            noCallback();
                        }
                        noCallback = null;
                    };
                    modalScope.title = title;
                    modalScope.message = message;
                    $('body').append($compile('<apiman-confirm-modal modal-title="{{ title }}">{{ message }}</apiman-confirm-modal>')(modalScope));
                    $timeout(function () {
                        $('#confirmModal').on('hidden.bs.modal', function () {
                            if (noCallback) {
                                $rootScope.$apply(noCallback);
                            }
                            noCallback = null;
                        });
                        $('#confirmModal')['modal']({ 'keyboard': true, 'backdrop': 'static' });
                    }, 50);
                },
                // A simple "Select a Service" dialog (allows selecting a single service + version
                //////////////////////////////////////////////////////////////////////////////////
                selectService: function (title, handler, publishedOnly) {
                    var modalScope = $rootScope.$new(true);
                    modalScope.selectedService = undefined;
                    modalScope.selectedServiceVersion = undefined;
                    modalScope.title = title;
                    $('body').append($compile('<apiman-select-service-modal modal-title="{{ title }}"></apiman-select-service-modal>')(modalScope));
                    $timeout(function () {
                        $('#selectServiceModal')['modal']({ 'keyboard': true, 'backdrop': 'static' });
                        $('#selectServiceModal').on('shown.bs.modal', function () {
                            $('#selectServiceModal .input-search').focus();
                        });
                    }, 50);
                    modalScope.search = function () {
                        modalScope.selectedService = undefined;
                        if (!modalScope.searchText) {
                            modalScope.criteria = undefined;
                            modalScope.services = undefined;
                        }
                        else {
                            modalScope.searchButton.state = 'in-progress';
                            var body = {};
                            body.filters = [];
                            body.filters.push({
                                'name': 'name',
                                'value': '%' + modalScope.searchText + '%',
                                'operator': 'like'
                            });
                            var searchStr = angular.toJson(body);
                            Logger.log('Searching for services: {0}', modalScope.searchText);
                            ApimanSvcs.save({ entityType: 'search', secondaryType: 'services' }, searchStr, function (reply) {
                                if (reply.beans.length > 0) {
                                    modalScope.services = reply.beans;
                                }
                                else {
                                    modalScope.services = undefined;
                                }
                                modalScope.criteria = modalScope.searchText;
                                Logger.log('Found {0} services.', reply.beans.length);
                                modalScope.searchButton.state = 'complete';
                            }, function (error) {
                                Logger.error(error);
                                // TODO do something interesting with the error
                                modalScope.services = undefined;
                                modalScope.criteria = modalScope.searchText;
                                modalScope.searchButton.state = 'error';
                            });
                        }
                    };
                    modalScope.onServiceSelected = function (service) {
                        if (modalScope.selectedService) {
                            modalScope.selectedService.selected = false;
                        }
                        modalScope.selectedService = service;
                        service.selected = true;
                        modalScope.selectedServiceVersion = undefined;
                        OrgSvcs.query({ organizationId: service.organizationId, entityType: 'services', entityId: service.id, versionsOrActivity: 'versions' }, function (versions) {
                            if (publishedOnly) {
                                var validVersions = [];
                                angular.forEach(versions, function (version) {
                                    if (version.status == 'Published') {
                                        validVersions.push(version);
                                    }
                                });
                                modalScope.serviceVersions = validVersions;
                            }
                            else {
                                modalScope.serviceVersions = versions;
                            }
                            if (modalScope.serviceVersions.length > 0) {
                                modalScope.selectedServiceVersion = modalScope.serviceVersions[0];
                            }
                        }, function (error) {
                            modalScope.serviceVersions = [];
                            modalScope.selectedServiceVersion = undefined;
                        });
                    };
                    modalScope.onOK = function () {
                        if (handler) {
                            handler(modalScope.selectedServiceVersion);
                        }
                    };
                }
            };
        }]);
})(ApimanDialogs || (ApimanDialogs = {}));

/// <reference path="../../includes.ts"/>
var Apiman;
(function (Apiman) {
    Apiman._module.directive('apimanActionBtn', ['Logger', function (Logger) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    var actionVar = attrs.field;
                    var actionText = attrs.placeholder;
                    var icon = attrs.icon;
                    Logger.debug("Action button initializing state variable [{0}].", actionVar);
                    scope[actionVar] = {
                        state: 'ready',
                        html: $(element).html(),
                        actionHtml: '<i class="fa fa-spin ' + icon + '"></i> ' + actionText
                    };
                    scope.$watch(actionVar + '.state', function () {
                        var newVal = scope[actionVar];
                        if (newVal.state == 'in-progress') {
                            $(element).prop('disabled', true);
                            $(element).html(newVal.actionHtml);
                        }
                        else {
                            $(element).prop('disabled', false);
                            $(element).html(newVal.html);
                        }
                    });
                }
            };
        }]);
    Apiman._module.directive('apimanSelectPicker', ['Logger', '$timeout', '$parse', 'TranslationService',
        function (Logger, $timeout, $parse, TranslationService) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    function refresh(newVal) {
                        scope.$applyAsync(function () {
                            $(element)['selectpicker']('refresh');
                        });
                    }
                    $timeout(function () {
                        $(element)['selectpicker']();
                        $(element)['selectpicker']('refresh');
                    });
                    if (attrs.ngOptions && / in /.test(attrs.ngOptions)) {
                        var refreshModel = attrs.ngOptions.split(' in ')[1].split(' ')[0];
                        Logger.debug('Watching model {0} for {1}.', refreshModel, attrs.ngModel);
                        scope.$watch(refreshModel, function () {
                            scope.$applyAsync(function () {
                                Logger.debug('Refreshing {0} due to watch model update.', attrs.ngModel);
                                $(element)['selectpicker']('refresh');
                            });
                        }, true);
                    }
                    if (attrs.apimanSelectPicker) {
                        Logger.debug('Watching {0}.', attrs.apimanSelectPicker);
                        scope.$watch(attrs.apimanSelectPicker + '.length', refresh, true);
                    }
                    if (attrs.ngModel) {
                        scope.$watch(attrs.ngModel, refresh, true);
                    }
                    if (attrs.ngDisabled) {
                        scope.$watch(attrs.ngDisabled, refresh, true);
                    }
                    scope.$on('$destroy', function () {
                        $timeout(function () {
                            $(element)['selectpicker']('destroy');
                        });
                    });
                    $timeout(function () {
                        $(element)['selectpicker']('refresh');
                    }, 200);
                }
            };
        }]);
    Apiman._module.directive('apimanPermission', ['Logger', 'CurrentUser',
        function (Logger, CurrentUser) {
            return {
                restrict: 'A',
                link: function ($scope, element, attrs) {
                    var refresh = function (newValue) {
                        var orgId = $scope.organizationId;
                        if (orgId) {
                            var permission = attrs.apimanPermission;
                            $(element).removeClass('apiman-not-permitted');
                            if (!CurrentUser.hasPermission(orgId, permission)) {
                                $(element).addClass('apiman-not-permitted');
                            }
                        }
                        else {
                            Logger.error('Missing organizationId from $scope - authorization disabled.');
                        }
                    };
                    $scope.$watch('organizationId', refresh);
                    $scope.$watch('permissions', refresh);
                }
            };
        }]);
    Apiman._module.directive('apimanStatus', ['Logger', 'EntityStatusService',
        function (Logger, EntityStatusService) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    scope.$watch(function ($scope) {
                        return EntityStatusService.getEntityStatus();
                    }, function (newValue, oldValue) {
                        var entityStatus = newValue;
                        var elem = element;
                        if (entityStatus) {
                            var validStatuses = attrs.apimanStatus.split(',');
                            var statusIsValid = false;
                            Logger.debug('Checking status {0} against valid statuses {1}:  {2}', entityStatus, '' + validStatuses, element[0].outerHTML);
                            for (var i = 0; i < validStatuses.length; i++) {
                                if (validStatuses[i] == entityStatus) {
                                    statusIsValid = true;
                                    break;
                                }
                            }
                            $(element).removeClass('apiman-wrong-status');
                            if (!statusIsValid) {
                                $(element).addClass('apiman-wrong-status');
                            }
                        }
                        else {
                            Logger.error('Missing entityStatus from $scope - hide/show based on entity status feature is disabled.');
                        }
                    });
                }
            };
        }]);
    Apiman._module.factory('EntityStatusService', ['$rootScope',
        function ($rootScope) {
            var entityStatus = null;
            return {
                setEntityStatus: function (status) {
                    entityStatus = status;
                },
                getEntityStatus: function () {
                    return entityStatus;
                }
            };
        }]);
    Apiman._module.directive('apimanEntityStatus', ['Logger', 'EntityStatusService',
        function (Logger, EntityStatusService) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    scope.$watch(function ($scope) {
                        return EntityStatusService.getEntityStatus();
                    }, function (newValue, oldValue) {
                        var entityStatus = newValue;
                        if (entityStatus) {
                            $(element).html(entityStatus);
                            $(element).removeClass();
                            $(element).addClass('apiman-label');
                            if (entityStatus == 'Created' || entityStatus == 'Ready') {
                                $(element).addClass('apiman-label-warning');
                            }
                            else if (entityStatus == 'Retired') {
                                $(element).addClass('apiman-label-default');
                            }
                            else {
                                $(element).addClass('apiman-label-success');
                            }
                        }
                    });
                }
            };
        }]);
    Apiman.sb_counter = 0;
    Apiman._module.directive('apimanSearchBox', ['Logger', 'TranslationService',
        function (Logger, TranslationService) {
            return {
                restrict: 'E',
                replace: true,
                templateUrl: 'plugins/api-manager/html/directives/searchBox.html',
                scope: {
                    searchFunction: '=function'
                },
                link: function (scope, element, attrs) {
                    scope.placeholder = attrs.placeholder;
                    if (attrs['id']) {
                        scope.filterId = attrs['id'] + '-f';
                        scope.buttonId = attrs['id'] + '-b';
                    }
                    else {
                        var cid = 'search-box-' + Apiman.sb_counter;
                        Apiman.sb_counter = Apiman.sb_counter + 1;
                        scope.filterId = cid + '-filter';
                        scope.buttonId = cid + '-button';
                    }
                    if (attrs.apimanI18nKey) {
                        var translationKey = attrs.apimanI18nKey + ".placeholder";
                        var defaultValue = scope.placeholder;
                        var translatedValue = TranslationService.translate(translationKey, defaultValue);
                        scope.placeholder = translatedValue;
                    }
                    scope.doSearch = function () {
                        $(element).find('button i').removeClass('fa-search');
                        $(element).find('button i').removeClass('fa-close');
                        if (scope.value) {
                            $(element).find('button i').addClass('fa-close');
                        }
                        else {
                            $(element).find('button i').addClass('fa-search');
                        }
                        scope.searchFunction(scope.value);
                    };
                    scope.onClick = function () {
                        if (scope.value) {
                            scope.value = '';
                            $(element).find('button i').removeClass('fa-search');
                            $(element).find('button i').removeClass('fa-close');
                            $(element).find('button i').addClass('fa-search');
                        }
                        scope.searchFunction(scope.value);
                    };
                }
            };
        }]);
    Apiman._module.directive('apimanConfirmModal', ['Logger',
        function (Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/directives/confirmModal.html',
                replace: true,
                restrict: 'E',
                transclude: true,
                link: function (scope, element, attrs) {
                    scope.title = attrs.modalTitle;
                    $(element).on('hidden.bs.modal', function () {
                        $(element).remove();
                    });
                }
            };
        }]);
    Apiman._module.directive('apimanSelectServiceModal', ['Logger',
        function (Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/directives/selectServiceModal.html',
                replace: true,
                restrict: 'E',
                link: function (scope, element, attrs) {
                    scope.title = attrs.modalTitle;
                    $(element).on('hidden.bs.modal', function () {
                        $(element).remove();
                    });
                }
            };
        }]);
    var entryTypeClasses = {
        Organization: 'fa-shield',
        Application: 'fa-gears',
        Plan: 'fa-bar-chart-o',
        Service: 'fa-puzzle-piece'
    };
    Apiman._module.directive('apimanActivity', ['Logger', '$rootScope', 'PageLifecycle',
        function (Logger, $rootScope, PageLifecycle) {
            return {
                templateUrl: 'plugins/api-manager/html/directives/activity.html',
                restrict: 'E',
                replace: true,
                scope: {
                    auditEntries: '=model',
                    next: '=next'
                },
                link: function (scope, element, attrs) {
                    scope.pluginName = $rootScope.pluginName;
                    scope.hasMore = true;
                    scope.getEntryIcon = function (entry) {
                        return entryTypeClasses[entry.entityType];
                    };
                    scope.getMore = function () {
                        scope.getMoreButton.state = 'in-progress';
                        scope.next(function (newEntries) {
                            scope.auditEntries = scope.auditEntries.concat(newEntries);
                            scope.hasMore = newEntries.length >= 20;
                            scope.getMoreButton.state = 'complete';
                        }, PageLifecycle.handleError);
                    };
                }
            };
        }]);
    Apiman._module.directive('apimanAuditEntry', ['Logger', '$rootScope',
        function (Logger, $rootScope) {
            return {
                restrict: 'E',
                scope: {
                    entry: '=model'
                },
                link: function (scope, element, attrs) {
                    scope.pluginName = $rootScope.pluginName;
                    scope.template = 'plugins/api-manager/html/directives/audit/' + scope.entry.entityType + '/audit' + scope.entry.what + '.html';
                    if (scope.entry.data) {
                        scope.data = JSON.parse(scope.entry.data);
                    }
                },
                template: '<div ng-include="template"></div>'
            };
        }]);
    Apiman._module.directive('apimanDropText', ['Logger',
        function (Logger) {
            return {
                restrict: 'A',
                require: 'ngModel',
                scope: {
                    ngModel: '='
                },
                link: function ($scope, $elem, $attrs, ngModel) {
                    $elem.on('dragover', function (e) {
                        e.preventDefault();
                        if (e.dataTransfer) {
                            e.dataTransfer.effectAllowed = 'copy';
                        }
                        if (!$elem.hasClass('dropping')) {
                            $elem.addClass('dropping');
                        }
                        return false;
                    });
                    $elem.on('dragenter', function (e) {
                        e.preventDefault();
                        if (e.dataTransfer) {
                            e.dataTransfer.effectAllowed = 'copy';
                        }
                        $elem.addClass('dropping');
                        return false;
                    });
                    $elem.on('dragleave', function (e) {
                        e.preventDefault();
                        $elem.removeClass('dropping');
                        return false;
                    });
                    $elem.on('drop', function (e) {
                        e.preventDefault();
                        $elem.removeClass('dropping');
                        if (e.originalEvent.dataTransfer && e.originalEvent.dataTransfer.files.length) {
                            if (e.preventDefault)
                                e.preventDefault();
                            if (e.stopPropagation)
                                e.stopPropagation();
                            var firstFile = e.originalEvent.dataTransfer.files[0];
                            var reader = new FileReader();
                            reader.onload = (function (theFile) {
                                return function (result) {
                                    $elem.val(result.target.result);
                                    ngModel.$setViewValue(result.target.result);
                                    $scope.$emit('afterdrop', { element: $elem, value: result.target.result });
                                };
                            })(firstFile);
                            reader.readAsText(firstFile);
                        }
                    });
                }
            };
        }]);
    Apiman._module.directive('apimanPolicyList', ['Logger',
        function (Logger) {
            return {
                restrict: 'E',
                scope: {
                    policies: "=ngModel",
                    remove: "=removeFunction",
                    reorder: "=reorderFunction",
                    type: "@",
                    org: "@orgId",
                    id: "@pageId",
                    version: "@"
                },
                controller: ['$scope', function ($scope) {
                        $scope.policyListOptions = {
                            containerPositioning: 'relative',
                            orderChanged: function (event) {
                                Logger.debug("Reordered as: {0}", $scope.ctrl.policies);
                                $scope.ctrl.reorder($scope.ctrl.policies);
                            }
                        };
                        $scope.pluginName = $scope.$parent.pluginName;
                    }],
                controllerAs: 'ctrl',
                bindToController: true,
                templateUrl: 'plugins/api-manager/html/directives/policyList.html'
            };
        }
    ]);
    Apiman._module.directive('apimanEditableDescription', ['Logger',
        function (Logger) {
            return {
                restrict: 'E',
                scope: {
                    descr: '=description',
                    callback: '='
                },
                controller: ['$scope', function ($scope) {
                    }],
                link: function ($scope, $elem, $attrs) {
                    $scope.defaultValue = $attrs.defaultValue;
                    var elem = null;
                    var previousRows = 1;
                    $scope.topPosition = 0;
                    $scope.leftPosition = 0;
                    $scope.height = 60;
                    /*
                    // If description is updated, call updateFunction.
                    $scope.$watch(function() {
                        return $scope.descr;
                    },
                    function(new_value, old_value) {
                        if (old_value !== new_value && typeof new_value !== 'undefined') {
                            console.log('old_value' + old_value);
                            console.log('new_value: ' + new_value);
                            console.log('callback()');
                             $scope.callback(new_value || '');
                         }
                    });*/
                    $scope.saveDescription = function () {
                        $scope.callback($scope.descr);
                    };
                    $scope.focusOnDescription = function (event) {
                        elem = event.target;
                        elem.value = $scope.descr || '';
                        $(elem).css('height', 'auto');
                        $(elem).height(elem.scrollHeight);
                    };
                    $scope.changeOnDescription = function () {
                        $(elem).css('height', 'auto');
                        $(elem).height(elem.scrollHeight);
                    };
                    $scope.descriptionMouseOver = function (event) {
                        $scope.showPencil = true;
                        var elem = event.target;
                        var position = elem.getBoundingClientRect();
                        // Calculate position of pen
                        // console.log("elem.top " + position.top);
                        // console.log("elem.bottom " + position.bottom);
                        // console.log("elem.left " + position.left);
                        // console.log("elem.right " + position.right);
                        if (position.right != 0) {
                            $scope.leftPosition = (position.right - position.left) - 15;
                            $scope.height = (position.bottom - position.top);
                        }
                    };
                    $scope.descriptionMouseOut = function (event) {
                        $scope.showPencil = false;
                    };
                },
                templateUrl: 'plugins/api-manager/html/directives/editDescription.html'
            };
        }]);
    Apiman._module.run(['editableOptions', 'editableThemes', function (editableOptions, editableThemes) {
            editableOptions.theme = 'default';
            // overwrite templates
            editableThemes['default'].submitTpl = '<button class="btn btn-default inline-save-btn" type="submit"><i class="fa fa-check fa-fw"></i></button>';
            editableThemes['default'].cancelTpl = '<button class="btn btn-default" type="button" ng-click="$form.$cancel()"><i class="fa fa-times fa-fw"></i></button>';
            editableThemes['default'].buttonsTpl = '<div></div>';
            editableThemes['default'].formTpl = '<form class="editable-wrap apiman-inline-edit"></form>';
        }]);
    Apiman._module.directive('apimanI18nKey', ['Logger', 'TranslationService',
        function (Logger, TranslationService) {
            return {
                restrict: 'A',
                link: function (scope, element, attrs) {
                    if (!attrs.apimanI18nKey) {
                        return;
                    }
                    var translationKey, defaultValue, translatedValue;
                    // Process the text of the element only if it has no child elements
                    if ($(element).children().length == 0) {
                        translationKey = attrs.apimanI18nKey;
                        defaultValue = $(element).text();
                        translatedValue = TranslationService.translate(translationKey, defaultValue);
                        $(element).text(translatedValue);
                    }
                    // Now process the placeholder attribute.
                    if ($(element).attr('placeholder')) {
                        translationKey = attrs.apimanI18nKey + '.placeholder';
                        defaultValue = $(element).attr('placeholder');
                        translatedValue = TranslationService.translate(translationKey, defaultValue);
                        Logger.debug('Translating placeholder attr.  Key: {2}  default value: {0}  translated: {1}', defaultValue, translatedValue, translationKey);
                        $(element).prop('placeholder', translatedValue);
                        $(element).attr('placeholder', translatedValue);
                    }
                    // Now process the title attribute.
                    if ($(element).attr('title')) {
                        translationKey = attrs.apimanI18nKey + '.title';
                        defaultValue = $(element).attr('title');
                        translatedValue = TranslationService.translate(translationKey, defaultValue);
                        Logger.debug('Translating title attr.  Key: {2}  default value: {0}  translated: {1}', defaultValue, translatedValue, translationKey);
                        $(element).prop('title', translatedValue);
                        $(element).attr('title', translatedValue);
                    }
                }
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman._module.controller('Apiman.Error400Controller', ['$scope', '$rootScope', 'PageLifecycle',
        function ($scope, $rootScope, PageLifecycle) {
            PageLifecycle.loadErrorPage('Error', $scope, function () {
                PageLifecycle.setPageTitle('error', 400);
            });
        }]);
    Apiman._module.controller('Apiman.Error403Controller', ['$scope', '$rootScope', 'PageLifecycle',
        function ($scope, $rootScope, PageLifecycle) {
            PageLifecycle.loadErrorPage('Error', $scope, function () {
                PageLifecycle.setPageTitle('error', 403);
            });
        }]);
    Apiman._module.controller('Apiman.Error404Controller', ['$scope', '$rootScope', 'PageLifecycle',
        function ($scope, $rootScope, PageLifecycle) {
            PageLifecycle.loadErrorPage('Error', $scope, function () {
                PageLifecycle.setPageTitle('error', 404);
            });
        }]);
    Apiman._module.controller('Apiman.Error409Controller', ['$scope', '$rootScope', 'PageLifecycle',
        function ($scope, $rootScope, PageLifecycle) {
            PageLifecycle.loadErrorPage('Error', $scope, function () {
                PageLifecycle.setPageTitle('error', 409);
            });
        }]);
    Apiman._module.controller('Apiman.Error500Controller', ['$scope', '$rootScope', 'PageLifecycle', 'Logger',
        function ($scope, $rootScope, PageLifecycle, Logger) {
            $scope.error = $rootScope.pageError;
            PageLifecycle.loadErrorPage('Error', $scope, function () {
                PageLifecycle.setPageTitle('error', 500);
            });
        }]);
    Apiman._module.controller('Apiman.ErrorInvalidServerController', ['$scope', '$rootScope', 'PageLifecycle', 'Logger', 'Configuration',
        function ($scope, $rootScope, PageLifecycle, Logger, Configuration) {
            $scope.error = $rootScope.pageError;
            PageLifecycle.loadErrorPage('Error', $scope, function () {
                $scope.installGuide = 'http://www.apiman.io/latest/installation-guide.html';
                $scope.version = Configuration.apiman.version;
                $scope.builtOn = Configuration.apiman.builtOn;
                $scope.apiEndpoint = Configuration.api.endpoint;
                $scope.cors = 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS';
                PageLifecycle.setPageTitle('error', 'Invalid Server');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../../includes.ts"/>
var ApimanPageLifecycle;
(function (ApimanPageLifecycle) {
    ApimanPageLifecycle.pageTitles = {
        "page.title.admin-gateways": "apiman - Admin - Gateways",
        "page.title.admin-plugins": "apiman - Admin - Plugins",
        "page.title.admin-roles": "apiman - Admin - Roles",
        "page.title.admin-policyDefs": "apiman - Admin - Policy Definitions",
        "page.title.app-activity": "apiman - {0} (Activity)",
        "page.title.app-apis": "apiman - {0} (APIs)",
        "page.title.app-contracts": "apiman - {0} (Contracts)",
        "page.title.app-metrics": "apiman - {0} (Metrics)",
        "page.title.app-overview": "apiman - {0} (Overview)",
        "page.title.app-policies": "apiman - {0} (Policies)",
        "page.title.consumer-org": "apiman - Organization {0}",
        "page.title.consumer-orgs": "apiman - Organizations",
        "page.title.consumer-service": "apiman - Service {0}",
        "page.title.consumer-service-def": "apiman - Service {0} - Definition",
        "page.title.consumer-services": "apiman - Services",
        "page.title.dashboard": "apiman - Home",
        "page.title.about": "apiman - About",
        "page.title.edit-gateway": "apiman - Edit Gateway",
        "page.title.edit-policy": "apiman - Edit Policy",
        "page.title.edit-policyDef": "apiman - Edit Policy Definition",
        "page.title.edit-role": "apiman - Edit Role",
        "page.title.import-policyDefs": "apiman - Import Policy Definition(s)",
        "page.title.import-services": "apiman - Import Service(s)",
        "page.title.new-app": "apiman - New Application",
        "page.title.new-app-version": "apiman - New Application Version",
        "page.title.new-contract": "apiman - New Contract",
        "page.title.new-gateway": "apiman - New Gateway",
        "page.title.new-member": "apiman - Add Member",
        "page.title.new-org": "apiman - New Organization",
        "page.title.new-plan": "apiman - New Plan",
        "page.title.new-plan-version": "apiman - New Plan Version",
        "page.title.new-plugin": "apiman - Add Plugin",
        "page.title.new-policy": "apiman - Add Policy",
        "page.title.new-role": "apiman - New Role",
        "page.title.new-service": "apiman - New Service",
        "page.title.new-service-version": "apiman - New Service Version",
        "page.title.org-activity": "apiman - {0} (Activity)",
        "page.title.org-apps": "apiman - {0} (Applications)",
        "page.title.org-manage-members": "apiman - {0} (Manage Members)",
        "page.title.org-members": "apiman - {0} (Members)",
        "page.title.org-plans": "apiman - {0} (Plans)",
        "page.title.org-services": "apiman - {0} (Services)",
        "page.title.plan-activity": "apiman - {0} (Activity)",
        "page.title.plan-overview": "apiman - {0} (Overview)",
        "page.title.plan-policies": "apiman - {0} (Policies)",
        "page.title.plugin-details": "apiman - Plugin Details",
        "page.title.policy-defs": "apiman - Admin - Policy Definitions",
        "page.title.service-activity": "apiman - {0} (Activity)",
        "page.title.service-contracts": "apiman - {0} (Contracts)",
        "page.title.service-endpoint": "apiman - {0} (Endpoint)",
        "page.title.service-metrics": "apiman - {0} (Metrics)",
        "page.title.service-impl": "apiman - {0} (Implementation)",
        "page.title.service-def": "apiman - {0} (Definition)",
        "page.title.service-overview": "apiman - {0} (Overview)",
        "page.title.service-plans": "apiman - {0} (Plans)",
        "page.title.service-policies": "apiman - {0} (Policies)",
        "page.title.user-activity": "apiman - {0} (Activity)",
        "page.title.user-apps": "apiman - {0} (Applications)",
        "page.title.user-orgs": "apiman - {0} (Organizations)",
        "page.title.user-profile": "apiman - User Profile",
        "page.title.user-services": "apiman - {0} (Services)",
        "page.title.error": "apiman - {0} Error",
    };
    var formatMessage = function (theArgs) {
        var now = new Date();
        var msg = theArgs[0];
        if (theArgs.length > 1) {
            for (var i = 1; i < theArgs.length; i++) {
                msg = msg.replace('{' + (i - 1) + '}', theArgs[i]);
            }
        }
        return msg;
    };
    ApimanPageLifecycle._module = angular.module("ApimanPageLifecycle", []);
    ApimanPageLifecycle.PageLifecycle = ApimanPageLifecycle._module.factory('PageLifecycle', ['$q', 'Logger', '$rootScope', '$location', 'CurrentUserSvcs', 'Configuration', 'TranslationService', '$window',
        function ($q, Logger, $rootScope, $location, CurrentUserSvcs, Configuration, TranslationService, $window) {
            var header = 'community';
            if (Configuration.ui && Configuration.ui.header) {
                header = Configuration.ui.header;
            }
            if (header == 'apiman') {
                header = 'community';
            }
            $rootScope.headerInclude = 'plugins/api-manager/html/headers/' + header + '.include';
            console.log('Using header: ' + $rootScope.headerInclude);
            var processCurrentUser = function (currentUser) {
                $rootScope.currentUser = currentUser;
                var permissions = {};
                var memberships = {};
                if (currentUser.permissions) {
                    for (var i = 0; i < currentUser.permissions.length; i++) {
                        var perm = currentUser.permissions[i];
                        var permid = perm.organizationId + '||' + perm.name;
                        permissions[permid] = true;
                        memberships[perm.organizationId] = true;
                    }
                }
                Logger.info('Updating permissions now {0}', permissions);
                $rootScope.permissions = permissions;
                $rootScope.memberships = memberships;
            };
            var handleError = function (error) {
                $rootScope.pageState = 'error';
                $rootScope.pageError = error;
                if (error.status == 400) {
                    Logger.info('Detected an error {0}, redirecting to 400.', error.status);
                    $location.url(Apiman.pluginName + '/errors/400').replace();
                }
                else if (error.status == 401) {
                    Logger.info('Detected an error 401, reloading the page.');
                    $window.location.reload();
                }
                else if (error.status == 403) {
                    Logger.info('Detected an error {0}, redirecting to 403.', error.status);
                    $location.url(Apiman.pluginName + '/errors/403').replace();
                }
                else if (error.status == 404) {
                    Logger.info('Detected an error {0}, redirecting to 404.', error.status);
                    $location.url(Apiman.pluginName + '/errors/404').replace();
                }
                else if (error.status == 409) {
                    Logger.info('Detected an error {0}, redirecting to 409.', error.status);
                    $location.url(Apiman.pluginName + '/errors/409').replace();
                }
                else if (error.status == 0) {
                    Logger.info('Detected an error {0}, redirecting to CORS error page.', error.status);
                    $location.url(Apiman.pluginName + '/errors/invalid_server').replace();
                }
                else {
                    Logger.info('Detected an error {0}, redirecting to 500.', error.status);
                    $location.url(Apiman.pluginName + '/errors/500').replace();
                }
            };
            return {
                setPageTitle: function (titleKey, params) {
                    var key = 'page.title.' + titleKey;
                    var pattern = ApimanPageLifecycle.pageTitles[key];
                    pattern = TranslationService.translate(key, pattern);
                    if (pattern) {
                        var args = [];
                        args.push(pattern);
                        args = args.concat(params);
                        var title = formatMessage(args);
                        document.title = title;
                    }
                    else {
                        document.title = pattern;
                    }
                },
                handleError: handleError,
                forwardTo: function () {
                    var path = '/' + Apiman.pluginName + formatMessage(arguments);
                    Logger.info('Forwarding to page {0}', path);
                    $location.url(path).replace();
                },
                redirectTo: function () {
                    var path = '/' + Apiman.pluginName + formatMessage(arguments);
                    Logger.info('Redirecting to page {0}', path);
                    $location.url(path);
                },
                loadPage: function (pageName, pageData, $scope, handler) {
                    Logger.log("|{0}| >> Loading page.", pageName);
                    $rootScope.pageState = 'loading';
                    $rootScope.isDirty = false;
                    // Every page gets the current user.
                    var allData = undefined;
                    var commonData = {
                        currentUser: $q(function (resolve, reject) {
                            if ($rootScope.currentUser) {
                                Logger.log("|{0}| >> Using cached current user from $rootScope.");
                                resolve($rootScope.currentUser);
                            }
                            else {
                                CurrentUserSvcs.get({ what: 'info' }, function (currentUser) {
                                    processCurrentUser(currentUser);
                                    resolve(currentUser);
                                }, reject);
                            }
                        })
                    };
                    // If some additional page data is requested, merge it into the common data
                    if (pageData) {
                        allData = angular.extend({}, commonData, pageData);
                    }
                    else {
                        allData = commonData;
                    }
                    // Now resolve the data as a promise (wait for all data packets to be fetched)
                    var promise = $q.all(allData);
                    promise.then(function (data) {
                        var count = 0;
                        angular.forEach(data, function (value, key) {
                            Logger.debug("|{0}| >> Binding {1} to $scope.", pageName, key);
                            this[key] = value;
                            count++;
                        }, $scope);
                        $rootScope.pageState = 'loaded';
                        if (handler) {
                            handler();
                        }
                        Logger.log("|{0}| >> Page successfully loaded: {1} data packets loaded", pageName, count);
                    }, function (reason) {
                        Logger.error("|{0}| >> Page load failed: {1}", pageName, reason);
                        handleError(reason);
                    });
                },
                loadErrorPage: function (pageName, $scope, handler) {
                    Logger.log("|{0}| >> Loading error page.", pageName);
                    $rootScope.pageState = 'loading';
                    // Nothing to do asynchronously for the error pages!
                    $rootScope.pageState = 'loaded';
                    if (handler) {
                        handler();
                    }
                    Logger.log("|{0}| >> Error page successfully loaded", pageName);
                }
            };
        }]);
})(ApimanPageLifecycle || (ApimanPageLifecycle = {}));

/// <reference path="../../includes.ts"/>
var ApimanLogger;
(function (ApimanLogger) {
    ApimanLogger._module = angular.module("ApimanLogger", []);
    var stringifyIfObject = function (candidate) {
        return (typeof candidate === 'object') ? angular.toJson(candidate, true) : candidate;
    };
    var _formatMessage = function (theArgs) {
        var now = new Date();
        var msg = theArgs[0];
        if (theArgs.length > 1) {
            for (var i = 1; i < theArgs.length; i++) {
                msg = msg.replace('{' + (i - 1) + '}', stringifyIfObject(theArgs[i]));
            }
        }
        else {
            msg = stringifyIfObject(msg);
        }
        return 'apiman [' + now.toLocaleTimeString() + ']>>  ' + msg;
    };
    ApimanLogger.Logger = ApimanLogger._module.factory('Logger', [
        function () {
            return {
                debug: function () {
                    console.debug(_formatMessage(arguments));
                },
                info: function () {
                    console.info(_formatMessage(arguments));
                },
                log: function () {
                    console.info(_formatMessage(arguments));
                },
                warn: function () {
                    console.warn(_formatMessage(arguments));
                },
                error: function () {
                    console.error(_formatMessage(arguments));
                }
            };
        }]);
})(ApimanLogger || (ApimanLogger = {}));

/// <reference path="apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NavbarController = Apiman._module.controller("Apiman.NavbarController", ['$scope', 'Logger', 'Configuration', function ($scope, Logger, Configuration) {
            Logger.log("Current user is {0}.", Configuration.user.username);
            $scope.username = Configuration.user.username;
            $scope.logoutUrl = Configuration.apiman.logoutUrl;
            $scope.goBack = function () {
                Logger.info('Returning to parent UI: {0}', Configuration.ui.backToConsole);
                window.location.href = Configuration.ui.backToConsole;
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../../includes.ts"/>
var ApimanServices;
(function (ApimanServices_1) {
    ApimanServices_1._module = angular.module("ApimanServices", ['ngResource', 'ApimanConfiguration']);
    var formatEndpoint = function (endpoint, params) {
        return endpoint.replace(/:(\w+)/g, function (match, key) {
            return params[key] ? params[key] : (':' + key);
        });
    };
    ApimanServices_1.ApimanServices = ApimanServices_1._module.factory('ApimanSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/:entityType/:secondaryType';
            return $resource(endpoint, { entityType: '@entityType', secondaryType: '@secondaryType' }, {
                update: {
                    method: 'PUT' // this method issues a PUT request
                } });
        }]);
    ApimanServices_1.UserServices = ApimanServices_1._module.factory('UserSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/users/:user/:entityType';
            return $resource(endpoint, { user: '@user', entityType: '@entityType' });
        }]);
    ApimanServices_1.OrganizationServices = ApimanServices_1._module.factory('OrgSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/organizations/:organizationId/:entityType/:entityId/:versionsOrActivity/:version/:policiesOrActivity/:policyId/:policyChain';
            return $resource(endpoint, {
                organizationId: '@organizationId',
                entityType: '@entityType',
                entityId: '@entityId',
                versionsOrActivity: '@versionsOrActivity',
                version: '@version',
                policiesOrActivity: '@policiesOrActivity',
                policyId: '@policyId',
                chain: '@policyChain',
                page: '@page',
                count: '@count'
            }, {
                update: {
                    method: 'PUT' // update issues a PUT request
                } });
        }]);
    ApimanServices_1.CurrentUserServices = ApimanServices_1._module.factory('CurrentUserSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/currentuser/:what';
            return $resource(endpoint, { entityType: '@what' }, {
                update: {
                    method: 'PUT' // this method issues a PUT request
                } });
        }]);
    ApimanServices_1.ActionServices = ApimanServices_1._module.factory('ActionSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/actions';
            return $resource(endpoint);
        }]);
    ApimanServices_1.AuditServices = ApimanServices_1._module.factory('AuditSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/organizations/:organizationId/:entityType/:entityId/activity';
            return $resource(endpoint, {
                organizationId: '@organizationId',
                entityType: '@entityType',
                entityId: '@entityId',
                page: '@page',
                count: '@count'
            });
        }]);
    ApimanServices_1.UserAuditServices = ApimanServices_1._module.factory('UserAuditSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            var endpoint = Configuration.api.endpoint + '/users/:user/activity';
            return $resource(endpoint, {
                user: '@user',
                page: '@page',
                count: '@count'
            });
        }]);
    ApimanServices_1.PluginServices = ApimanServices_1._module.factory('PluginSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            return {
                getPolicyForm: function (pluginId, policyDefId, handler, errorHandler) {
                    var endpoint = Configuration.api.endpoint + '/plugins/:pluginId/policyDefs/:policyDefId/form';
                    $resource(endpoint, { pluginId: '@pluginId', policyDefId: '@policyDefId' }).get({ pluginId: pluginId, policyDefId: policyDefId }, handler, errorHandler);
                }
            };
        }]);
    ApimanServices_1.ServiceDefinitionServices = ApimanServices_1._module.factory('ServiceDefinitionSvcs', ['$resource', '$http', 'Configuration',
        function ($resource, $http, Configuration) {
            return {
                getServiceDefinitionUrl: function (orgId, serviceId, version) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/definition', { organizationId: orgId, serviceId: serviceId, version: version });
                    return endpoint;
                },
                getServiceDefinition: function (orgId, serviceId, version, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/definition', { organizationId: orgId, serviceId: serviceId, version: version });
                    $http({
                        method: 'GET',
                        url: endpoint,
                        transformResponse: function (value) { return value; }
                    }).success(handler).error(errorHandler);
                },
                updateServiceDefinition: function (orgId, serviceId, version, definition, definitionType, handler, errorHandler) {
                    var ct = 'application/json';
                    if (definitionType == 'SwaggerYAML') {
                        ct = 'application/x-yaml';
                    }
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/definition', { organizationId: orgId, serviceId: serviceId, version: version });
                    $http({
                        method: 'PUT',
                        url: endpoint,
                        headers: { 'Content-Type': ct },
                        data: definition
                    }).success(handler).error(errorHandler);
                }
            };
        }]);
    ApimanServices_1.MetricsServices = ApimanServices_1._module.factory('MetricsSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            return {
                getUsage: function (orgId, serviceId, version, interval, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/usage', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { interval: interval, from: from, to: to }).get({}, handler, errorHandler);
                },
                getUsagePerApp: function (orgId, serviceId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/appUsage', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { from: from, to: to }).get({}, handler, errorHandler);
                },
                getUsagePerPlan: function (orgId, serviceId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/planUsage', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { from: from, to: to }).get({}, handler, errorHandler);
                },
                getResponseStats: function (orgId, serviceId, version, interval, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/responseStats', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { interval: interval, from: from, to: to }).get({}, handler, errorHandler);
                },
                getResponseStatsSummary: function (orgId, serviceId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/summaryResponseStats', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { from: from, to: to }).get({}, handler, errorHandler);
                },
                getResponseStatsPerApp: function (orgId, serviceId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/appResponseStats', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { from: from, to: to }).get({}, handler, errorHandler);
                },
                getResponseStatsPerPlan: function (orgId, serviceId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/services/:serviceId/versions/:version/metrics/planResponseStats', { organizationId: orgId, serviceId: serviceId, version: version });
                    $resource(endpoint, { from: from, to: to }).get({}, handler, errorHandler);
                },
                getAppUsagePerService: function (orgId, applicationId, version, from, to, handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/organizations/:organizationId/applications/:applicationId/versions/:version/metrics/serviceUsage', { organizationId: orgId, applicationId: applicationId, version: version });
                    $resource(endpoint, { from: from, to: to }).get({}, handler, errorHandler);
                },
            };
        }]);
    ApimanServices_1.SystemServices = ApimanServices_1._module.factory('SystemSvcs', ['$resource', 'Configuration',
        function ($resource, Configuration) {
            return {
                getStatus: function (handler, errorHandler) {
                    var endpoint = formatEndpoint(Configuration.api.endpoint + '/system/status', {});
                    $resource(endpoint).get({}, handler, errorHandler);
                }
            };
        }]);
})(ApimanServices || (ApimanServices = {}));

/// <reference path='apimanPlugin.ts'/>
/// <reference path='services.ts'/>
var Apiman;
(function (Apiman) {
    Apiman.isRegexpValid = function (v) {
        var valid = true;
        try {
            new RegExp(v, '');
        }
        catch (e) {
            valid = false;
        }
        return valid;
    };
    Apiman._module.controller('Apiman.DefaultPolicyConfigFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validateRaw = function (config) {
                var valid = true;
                try {
                    var parsed = JSON.parse(config);
                    $scope.setConfig(parsed);
                }
                catch (e) {
                    valid = false;
                }
                $scope.setValid(valid);
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            if ($scope.getConfig()) {
                $scope.rawConfig = JSON.stringify($scope.getConfig(), null, 2);
            }
            $scope.$watch('rawConfig', validateRaw);
        }]);
    Apiman._module.controller('Apiman.JsonSchemaPolicyConfigFormController', ['$scope', 'Logger', 'PluginSvcs', 'EntityStatusService',
        function ($scope, Logger, PluginSvcs, EntityStatusService) {
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            var initEditor = function (schema) {
                var holder = document.getElementById('json-editor-holder');
                var editor = new window['JSONEditor'](holder, {
                    // Disable fetching schemas via ajax
                    ajax: false,
                    // The schema for the editor
                    schema: schema,
                    // Disable additional properties
                    no_additional_properties: true,
                    // Require all properties by default
                    required_by_default: true,
                    disable_edit_json: true,
                    disable_properties: true,
                    iconlib: 'fontawesome4',
                    theme: 'bootstrap3'
                });
                editor.on('change', function () {
                    $scope.$apply(function () {
                        // Get an array of errors from the validator
                        var errors = editor.validate();
                        // Not valid
                        if (errors.length) {
                            $scope.setValid(false);
                        }
                        else {
                            $scope.setValid(true);
                            $scope.setConfig($scope.editor.getValue());
                        }
                    });
                });
                if ($scope.isEntityDisabled() === true) {
                    editor.disable();
                }
                $scope.editor = editor;
            };
            var destroyEditor = function () {
                if ($scope.editor) {
                    $scope.editor.destroy();
                    $scope.editor = null;
                }
            };
            var loadSchema = function () {
                $scope.schemaState = 'loading';
                var pluginId = $scope.selectedDef.pluginId;
                var policyDefId = $scope.selectedDef.id;
                PluginSvcs.getPolicyForm(pluginId, policyDefId, function (schema) {
                    destroyEditor();
                    initEditor(schema);
                    $scope.editor.setValue($scope.config);
                    $scope.schemaState = 'loaded';
                }, function (error) {
                    // TODO handle the error better here!
                    Logger.error(error);
                    $scope.schemaState = 'loaded';
                });
            };
            // Watch for changes to selectedDef - if the user changes from one schema-based policy
            // to another schema-based policy, then the controller won't change.  The result is that
            // we need to refresh the schema when the selectedDef changes.
            $scope.$watch('selectedDef', function (newValue) {
                if (newValue && newValue.formType == 'JsonSchema') {
                    destroyEditor();
                    loadSchema();
                }
            });
            $scope.$on('$destroy', function () {
                destroyEditor();
            });
            // On first load of this controller, load the schema.
            loadSchema();
        }]);
    Apiman._module.controller('Apiman.RateLimitingFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = true;
                if (config.limit) {
                    config.limit = Number(config.limit);
                }
                if (!config.limit || config.limit < 1) {
                    valid = false;
                }
                if (!config.granularity) {
                    valid = false;
                }
                if (!config.period) {
                    valid = false;
                }
                if (config.granularity == 'User' && !config.userHeader) {
                    valid = false;
                }
                $scope.isEntityDisabled = function () {
                    var status = EntityStatusService.getEntityStatus();
                    return (status !== 'Created' && status !== 'Ready');
                };
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
        }]);
    Apiman._module.controller('Apiman.QuotaFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = true;
                if (config.limit) {
                    config.limit = Number(config.limit);
                }
                if (!config.limit || config.limit < 1) {
                    valid = false;
                }
                if (!config.granularity) {
                    valid = false;
                }
                if (!config.period) {
                    valid = false;
                }
                if (config.granularity == 'User' && !config.userHeader) {
                    valid = false;
                }
                $scope.setValid(valid);
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.$watch('config', validate, true);
        }]);
    Apiman.KB = 1024;
    Apiman.MB = 1024 * 1024;
    Apiman.GB = 1024 * 1024 * 1024;
    Apiman._module.controller('Apiman.TransferQuotaFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            $scope.limitDenomination = 'B';
            if ($scope.config && $scope.config.limit) {
                var limit = Number($scope.config.limit);
                if (limit > Apiman.GB && ((limit % Apiman.GB) == 0)) {
                    $scope.limitAmount = limit / Apiman.GB;
                    $scope.limitDenomination = 'GB';
                }
                else if (limit > Apiman.MB && ((limit % Apiman.MB) == 0)) {
                    $scope.limitAmount = limit / Apiman.MB;
                    $scope.limitDenomination = 'MB';
                }
                else if (limit > Apiman.KB && ((limit % Apiman.KB) == 0)) {
                    $scope.limitAmount = limit / Apiman.KB;
                    $scope.limitDenomination = 'KB';
                }
                else {
                    $scope.limitAmount = limit;
                }
            }
            var validate = function (config) {
                var valid = true;
                if (!config.limit || config.limit < 1) {
                    valid = false;
                }
                if (!config.granularity) {
                    valid = false;
                }
                if (!config.period) {
                    valid = false;
                }
                if (config.granularity == 'User' && !config.userHeader) {
                    valid = false;
                }
                if (!config.direction) {
                    valid = false;
                }
                $scope.setValid(valid);
            };
            var onLimitChange = function () {
                var amt = $scope.limitAmount;
                if (amt) {
                    var den = $scope.limitDenomination;
                    var denFact = 1;
                    if (den == 'KB') {
                        denFact = 1024;
                    }
                    if (den == 'MB') {
                        denFact = 1024 * 1024;
                    }
                    if (den == 'GB') {
                        denFact = 1024 * 1024 * 1024;
                    }
                    try {
                        $scope.config.limit = Number(amt) * denFact;
                    }
                    catch (e) {
                        $scope.config.limit = null;
                    }
                }
                else {
                    $scope.config.limit = null;
                }
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.$watch('config', validate, true);
            $scope.$watch('limitDenomination', onLimitChange, false);
            $scope.$watch('limitAmount', onLimitChange, false);
        }]);
    Apiman._module.controller('Apiman.IPListFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = true;
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
            if (!$scope.config.ipList) {
                $scope.config.ipList = [];
            }
            if (!$scope.config.responseCode) {
                $scope.config.responseCode = '500';
            }
            $scope.add = function (ip) {
                $scope.remove(ip);
                $scope.config.ipList.push(ip);
                $scope.selectedIP = [ip];
                $scope.ipAddress = undefined;
                $('#ip-address').focus();
            };
            $scope.remove = function (ips) {
                angular.forEach(ips, function (ip) {
                    var idx = -1;
                    angular.forEach($scope.config.ipList, function (item, index) {
                        if (item == ip) {
                            idx = index;
                        }
                    });
                    if (idx != -1) {
                        $scope.config.ipList.splice(idx, 1);
                    }
                });
                $scope.selectedIP = undefined;
            };
            $scope.clear = function () {
                $scope.config.ipList = [];
                $scope.selectedIP = undefined;
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
        }]);
    Apiman._module.controller('Apiman.IgnoredResourcesFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = true;
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
            $scope.add = function (path) {
                if (!$scope.config.pathsToIgnore) {
                    $scope.config.pathsToIgnore = [];
                }
                $scope.remove(path);
                $scope.config.pathsToIgnore.push(path);
                $scope.selectedPath = [path];
                $scope.path = undefined;
                $('#path').focus();
            };
            $scope.remove = function (paths) {
                angular.forEach(paths, function (path) {
                    var idx = -1;
                    angular.forEach($scope.config.pathsToIgnore, function (item, index) {
                        if (item == path) {
                            idx = index;
                        }
                    });
                    if (idx != -1) {
                        $scope.config.pathsToIgnore.splice(idx, 1);
                    }
                });
                $scope.selectedPath = undefined;
            };
            $scope.clear = function () {
                $scope.config.pathsToIgnore = [];
                $scope.selectedPath = undefined;
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
        }]);
    Apiman._module.controller('Apiman.BasicAuthFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                if (!config) {
                    return;
                }
                var valid = true;
                if (!config.realm) {
                    valid = false;
                }
                if (!config.staticIdentity && !config.ldapIdentity && !config.jdbcIdentity) {
                    valid = false;
                }
                if (config.staticIdentity) {
                    if (!config.staticIdentity.identities) {
                        valid = false;
                    }
                }
                if (config.ldapIdentity) {
                    if (!config.ldapIdentity.url) {
                        valid = false;
                    }
                    if (!config.ldapIdentity.dnPattern) {
                        valid = false;
                    }
                    if (config.ldapIdentity.bindAs == 'ServiceAccount') {
                        if (!config.ldapIdentity.credentials || !config.ldapIdentity.credentials.username || !config.ldapIdentity.credentials.password) {
                            valid = false;
                        }
                        if (config.ldapIdentity.credentials) {
                            if (config.ldapIdentity.credentials.password != $scope.repeatPassword) {
                                valid = false;
                            }
                        }
                        if (!config.ldapIdentity.userSearch || !config.ldapIdentity.userSearch.baseDn || !config.ldapIdentity.userSearch.expression) {
                            valid = false;
                        }
                    }
                    if (config.ldapIdentity.extractRoles) {
                        if (!config.ldapIdentity.membershipAttribute) {
                            valid = false;
                        }
                        if (!config.ldapIdentity.rolenameAttribute) {
                            valid = false;
                        }
                    }
                }
                if (config.jdbcIdentity) {
                    if (!config.jdbcIdentity.datasourcePath) {
                        valid = false;
                    }
                    if (!config.jdbcIdentity.query) {
                        valid = false;
                    }
                    if (config.jdbcIdentity.extractRoles && !config.jdbcIdentity.roleQuery) {
                        valid = false;
                    }
                }
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
            $scope.$watch('repeatPassword', function () {
                validate($scope.config);
            });
            if ($scope.config) {
                if ($scope.config.staticIdentity) {
                    $scope.identitySourceType = 'static';
                }
                else if ($scope.config.ldapIdentity && $scope.config.ldapIdentity.credentials) {
                    $scope.identitySourceType = 'ldap';
                    $scope.repeatPassword = $scope.config.ldapIdentity.credentials.password;
                }
                else if ($scope.config.jdbcIdentity) {
                    $scope.identitySourceType = 'jdbc';
                }
            }
            $scope.$watch('identitySourceType', function (newValue) {
                if (newValue) {
                    if (newValue == 'static' && !$scope.config.staticIdentity) {
                        $scope.config.staticIdentity = new Object();
                        delete $scope.config.ldapIdentity;
                        delete $scope.config.jdbcIdentity;
                    }
                    else if (newValue == 'jdbc' && !$scope.config.jdbcIdentity) {
                        $scope.config.jdbcIdentity = new Object();
                        $scope.config.jdbcIdentity.hashAlgorithm = 'SHA1';
                        delete $scope.config.staticIdentity;
                        delete $scope.config.ldapIdentity;
                    }
                    else if (newValue == 'ldap' && !$scope.config.ldapIdentity) {
                        $scope.config.ldapIdentity = new Object();
                        $scope.config.ldapIdentity.bindAs = 'UserAccount';
                        delete $scope.config.staticIdentity;
                        delete $scope.config.jdbcIdentity;
                    }
                }
            });
            $scope.add = function (username, password) {
                var item = {
                    username: username,
                    password: password
                };
                if (!$scope.config.staticIdentity.identities) {
                    $scope.config.staticIdentity.identities = [];
                }
                $scope.remove([item]);
                $scope.config.staticIdentity.identities.push(item);
                $scope.selectedIdentity = [item];
                $scope.username = undefined;
                $scope.password = undefined;
                $('#username').focus();
            };
            $scope.remove = function (selectedIdentities) {
                angular.forEach(selectedIdentities, function (identity) {
                    var idx = -1;
                    angular.forEach($scope.config.staticIdentity.identities, function (item, index) {
                        if (item.username == identity.username) {
                            idx = index;
                        }
                    });
                    if (idx != -1) {
                        $scope.config.staticIdentity.identities.splice(idx, 1);
                    }
                });
                $scope.selectedIdentity = undefined;
            };
            $scope.clear = function () {
                $scope.config.staticIdentity.identities = [];
                $scope.selectedIdentity = undefined;
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
        }]);
    Apiman._module.controller('Apiman.AuthorizationFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = config.rules && config.rules.length > 0;
                if (!config.requestUnmatched) {
                    config.requestUnmatched = 'fail';
                }
                if (!config.multiMatch) {
                    config.multiMatch = 'all';
                }
                $scope.setValid(valid);
            };
            $scope.$watch('config', validate, true);
            $scope.currentItemInvalid = function () {
                return !$scope.path || !$scope.verb || !$scope.role || !Apiman.isRegexpValid($scope.path);
            };
            $scope.add = function (path, verb, role) {
                if (!$scope.config.rules) {
                    $scope.config.rules = [];
                }
                var rule = {
                    'verb': verb,
                    'pathPattern': path,
                    'role': role
                };
                $scope.config.rules.push(rule);
                $scope.path = undefined;
                $scope.verb = undefined;
                $scope.role = undefined;
                $('#path').focus();
            };
            $scope.remove = function (selectedRule) {
                var idx = -1;
                angular.forEach($scope.config.rules, function (item, index) {
                    if (item == selectedRule) {
                        idx = index;
                    }
                });
                if (idx != -1) {
                    $scope.config.rules.splice(idx, 1);
                }
            };
            $scope.clear = function () {
                $scope.config.rules = [];
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
        }]);
    Apiman._module.controller('Apiman.CachingFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = false;
                if (config.ttl) {
                    config.ttl = Number(config.ttl);
                    if (config.ttl && config.ttl > 0) {
                        valid = true;
                    }
                }
                $scope.setValid(valid);
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.$watch('config', validate, true);
        }]);
    Apiman._module.controller('Apiman.URLRewritingFormController', ['$scope', 'Logger', 'EntityStatusService',
        function ($scope, Logger, EntityStatusService) {
            var validate = function (config) {
                var valid = true;
                if (!config.fromRegex) {
                    valid = false;
                }
                else {
                    if (!Apiman.isRegexpValid(config.fromRegex)) {
                        valid = false;
                    }
                }
                if (!config.toReplacement) {
                    valid = false;
                }
                if (!config.processBody && !config.processHeaders) {
                    valid = false;
                }
                $scope.setValid(valid);
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.$watch('config', validate, true);
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.UserProfileController = Apiman._module.controller("Apiman.UserProfileController", ['$q', '$rootScope', '$scope', '$location', 'CurrentUserSvcs', 'PageLifecycle',
        function ($q, $rootScope, $scope, $location, CurrentUserSvcs, PageLifecycle) {
            var pageData = {
                user: $q(function (resolve, reject) {
                    CurrentUserSvcs.get({ what: 'info' }, resolve, reject);
                })
            };
            $rootScope.isDirty = false;
            $scope.isValid = true;
            $scope.updatedUser = {
                fullName: undefined,
                email: undefined
            };
            $scope.$watch('updatedUser', function (newValue) {
                var dirty = false;
                var valid = true;
                if (!newValue.fullName) {
                    valid = false;
                }
                if (!newValue.email) {
                    valid = false;
                }
                if (newValue.fullName != $scope.user.fullName) {
                    dirty = true;
                }
                if (newValue.email != $scope.user.email) {
                    dirty = true;
                }
                $rootScope.isDirty = dirty;
                $scope.isValid = valid;
            }, true);
            $scope.save = function () {
                $scope.updateButton.state = 'in-progress';
                CurrentUserSvcs.update({ what: 'info' }, $scope.updatedUser, function () {
                    $scope.updateButton.state = 'complete';
                    $scope.user.fullName = $scope.updatedUser.fullName;
                    $scope.user.email = $scope.updatedUser.email;
                    $scope.isValid = true;
                    $rootScope.isDirty = false;
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('UserProfile', pageData, $scope, function () {
                $scope.updatedUser.fullName = $scope.user.fullName;
                $scope.updatedUser.email = $scope.user.email;
                PageLifecycle.setPageTitle('user-profile');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../../includes.ts"/>
var ApimanTranslation;
(function (ApimanTranslation) {
    ApimanTranslation._module = angular.module("ApimanTranslation", []);
    ApimanTranslation.Translation = ApimanTranslation._module.factory('TranslationService', ['$window',
        function ($window) {
            return {
                translate: function (key, defaultValue) {
                    var translation = undefined;
                    if ($window.APIMAN_TRANSLATION_DATA && $window.APIMAN_TRANSLATION_DATA[key]) {
                        translation = $window.APIMAN_TRANSLATION_DATA[key];
                    }
                    else {
                        translation = defaultValue;
                    }
                    return translation;
                }
            };
        }]);
})(ApimanTranslation || (ApimanTranslation = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AdminGatewaysController = Apiman._module.controller("Apiman.AdminGatewaysController", ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', function ($q, $scope, ApimanSvcs, PageLifecycle) {
            $scope.tab = 'gateways';
            var pageData = {
                gateways: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'gateways' }, function (adminGateways) {
                        resolve(adminGateways);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminGateways', pageData, $scope, function () {
                PageLifecycle.setPageTitle('admin-gateways');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AdminPluginsController = Apiman._module.controller("Apiman.AdminPluginsController", ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Dialogs', 'Logger',
        function ($q, $scope, ApimanSvcs, PageLifecycle, Dialogs, Logger) {
            $scope.tab = 'plugins';
            $scope.filterAvailablePlugins = function (value) {
                if (!value) {
                    $scope.filteredAvailablePlugins = $scope.availablePlugins;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.availablePlugins, function (plugin) {
                        if (plugin.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(plugin);
                        }
                    });
                    $scope.filteredAvailablePlugins = filtered;
                }
            };
            var pageData = {
                plugins: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'plugins' }, function (plugins) {
                        angular.forEach(plugins, function (p) {
                            p.isSnapshot = p.version.indexOf("-SNAPSHOT", this.length - "-SNAPSHOT".length) !== -1;
                        });
                        resolve(plugins);
                    }, reject);
                }),
                availablePlugins: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'plugins', secondaryType: 'availablePlugins' }, function (plugins) {
                        $scope.filteredAvailablePlugins = plugins;
                        resolve(plugins);
                    }, reject);
                })
            };
            var getInstalledPlugin = function (plugin) {
                var rval = null;
                angular.forEach($scope.plugins, function (p) {
                    if (plugin.groupId == p.groupId && plugin.artifactId == p.artifactId) {
                        rval = p;
                    }
                });
                return rval;
            };
            var removePlugin = function (plugin) {
                var index = -1;
                var i = 0;
                angular.forEach($scope.plugins, function (p, i) {
                    if (p === plugin) {
                        index = i;
                    }
                });
                if (index >= 0) {
                    $scope.plugins.splice(index, 1);
                }
            };
            $scope.uninstallPlugin = function (plugin) {
                plugin.deleting = true;
                Dialogs.confirm('Confirm Uninstall Plugin', 'Do you really want to uninstall this plugin?  Any policies it provided will no longer be available.', function () {
                    ApimanSvcs.delete({ entityType: 'plugins', secondaryType: plugin.id }, function (reply) {
                        removePlugin(plugin);
                        refreshPlugins();
                    }, PageLifecycle.handleError);
                }, function () {
                    delete plugin.deleting;
                });
            };
            var refreshPlugins = function () {
                angular.forEach($scope.availablePlugins, function (plugin) {
                    var ip = getInstalledPlugin(plugin);
                    if (ip) {
                        plugin.isInstalled = true;
                        plugin.installedVersion = ip.version;
                        ip.latestVersion = plugin.version;
                        ip.needsUpgrade = plugin.version != ip.version;
                    }
                });
            };
            PageLifecycle.loadPage('AdminPlugins', pageData, $scope, function () {
                PageLifecycle.setPageTitle('admin-plugins');
                refreshPlugins();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AdminPolicyDefsController = Apiman._module.controller("Apiman.AdminPolicyDefsController", ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', function ($q, $scope, ApimanSvcs, PageLifecycle) {
            $scope.tab = 'policyDefs';
            $scope.filterPolicies = function (value) {
                if (!value) {
                    $scope.filteredPolicyDefs = $scope.policyDefs;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.policyDefs, function (policyDef) {
                        if (policyDef.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(policyDef);
                        }
                    });
                    $scope.filteredPolicyDefs = filtered;
                }
            };
            var pageData = {
                policyDefs: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'policyDefs' }, function (policyDefs) {
                        $scope.filteredPolicyDefs = policyDefs;
                        resolve(policyDefs);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminPolicyDefs', pageData, $scope, function () {
                PageLifecycle.setPageTitle('admin-policyDefs');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AdminRolesController = Apiman._module.controller("Apiman.AdminRolesController", ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle',
        function ($q, $scope, ApimanSvcs, PageLifecycle) {
            $scope.tab = 'roles';
            $scope.filterRoles = function (value) {
                if (!value) {
                    $scope.filteredRoles = $scope.roles;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.roles, function (role) {
                        if (role.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(role);
                        }
                    });
                    $scope.filteredRoles = filtered;
                }
            };
            var pageData = {
                roles: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'roles' }, function (adminRoles) {
                        $scope.filteredRoles = adminRoles;
                        resolve(adminRoles);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('AdminRoles', pageData, $scope, function () {
                PageLifecycle.setPageTitle('admin-roles');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppActivityController = Apiman._module.controller("Apiman.AppActivityController", ['$q', '$scope', '$location', 'Logger', 'PageLifecycle', 'AppEntityLoader', 'AuditSvcs', '$routeParams', 'Configuration',
        function ($q, $scope, $location, Logger, PageLifecycle, AppEntityLoader, AuditSvcs, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var getNextPage = function (successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, page: $scope.currentPage, count: 20 }, function (results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                auditEntries: $q(function (resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('AppActivity', pageData, $scope, function () {
                PageLifecycle.setPageTitle('app-activity', [$scope.app.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppApisController = Apiman._module.controller("Apiman.AppApisController", ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'Logger', 'OrgSvcs', '$rootScope', '$compile', '$timeout', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, AppEntityLoader, Logger, OrgSvcs, $rootScope, $compile, $timeout, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'apis';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.downloadAsJson = 'proxies/apiman/organizations/' + params.org + '/applications/' + params.app + '/versions/' + params.version + '/apiregistry/json';
            $scope.downloadAsXml = 'proxies/apiman/organizations/' + params.org + '/applications/' + params.app + '/versions/' + params.version + '/apiregistry/xml';
            $scope.toggle = function (api) {
                api.expanded = !api.expanded;
            };
            $scope.howToInvoke = function (api) {
                var modalScope = $rootScope.$new(true);
                modalScope.asQueryParam = api.httpEndpoint + '?apikey=' + api.apiKey;
                if (api.httpEndpoint.indexOf('?') > -1) {
                    modalScope.asQueryParam = api.httpEndpoint + '&apikey=' + api.apiKey;
                }
                modalScope.asRequestHeader = 'X-API-Key: ' + api.apiKey;
                $('body').append($compile('<apiman-api-modal></apiman-api-modal>')(modalScope));
                $timeout(function () {
                    $('#apiModal')['modal']({ 'keyboard': true, 'backdrop': 'static' });
                }, 50);
            };
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                apiRegistry: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'apiregistry', policyId: 'json' }, resolve, reject);
                })
            });
            PageLifecycle.loadPage('AppApis', pageData, $scope, function () {
                Logger.info("API Registry: {0}", $scope.apiRegistry);
                PageLifecycle.setPageTitle('app-apis', [$scope.app.name]);
            });
        }]);
    Apiman._module.directive('apimanApiModal', ['Logger', function (Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/app/apiModal.html',
                replace: true,
                restrict: 'E',
                link: function (scope, element, attrs) {
                    $(element).on('hidden.bs.modal', function () {
                        $(element).remove();
                    });
                }
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppContractsController = Apiman._module.controller("Apiman.AppContractsController", ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'OrgSvcs', 'Logger', 'Dialogs', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, AppEntityLoader, OrgSvcs, Logger, Dialogs, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                contracts: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function (contracts) {
                        $scope.filteredContracts = contracts;
                        resolve(contracts);
                    }, reject);
                })
            });
            function removeContractFromArray(contract, carray) {
                var idx = -1;
                for (var i = 0; i < carray.length; i++) {
                    if (carray[i].contractId == contract.contractId) {
                        idx = i;
                        break;
                    }
                }
                if (idx > -1) {
                    carray.splice(idx, 1);
                }
            }
            ;
            $scope.filterContracts = function (value) {
                Logger.debug('Called filterContracts!');
                if (!value) {
                    $scope.filteredContracts = $scope.contracts;
                }
                else {
                    var fc = [];
                    angular.forEach($scope.contracts, function (contract) {
                        if (contract.serviceOrganizationName.toLowerCase().indexOf(value.toLowerCase()) > -1 || contract.serviceName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            fc.push(contract);
                        }
                    });
                    $scope.filteredContracts = fc;
                }
            };
            $scope.breakAll = function () {
                Dialogs.confirm('Break All Contracts?', 'Do you really want to break all contracts with all services?', function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts' }, function () {
                        $scope.contracts = [];
                        $scope.filteredContracts = [];
                    }, PageLifecycle.handleError);
                });
            };
            $scope.break = function (contract) {
                Logger.debug("Called break() with {0}.", contract);
                Dialogs.confirm('Break Contract', 'Do you really want to break this contract?', function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', policyId: contract.contractId }, function () {
                        removeContractFromArray(contract, $scope.contracts);
                        removeContractFromArray(contract, $scope.filteredContracts);
                    }, PageLifecycle.handleError);
                });
            };
            PageLifecycle.loadPage('AppContracts', pageData, $scope, function () {
                PageLifecycle.setPageTitle('app-contracts', [$scope.app.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppMetricsController = Apiman._module.controller("Apiman.AppMetricsController", ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', '$routeParams', 'MetricsSvcs', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, AppEntityLoader, $routeParams, MetricsSvcs, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'metrics';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.metricsRange = '7days';
            $scope.metricsType = 'usage';
            var usageByServiceChart;
            var renderServiceUsageChart = function (data) {
                var columns = [];
                var x = ['x'];
                var dataPoints = ['data'];
                angular.forEach(data.data, function (numRequests, serviceName) {
                    x.push(serviceName);
                    dataPoints.push(numRequests);
                });
                if (data.data.length == 0) {
                    $scope.serviceUsageChartNoData = true;
                }
                else {
                    columns.push(x);
                    columns.push(dataPoints);
                    usageByServiceChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            x: 'x',
                            columns: columns,
                            type: 'bar'
                        },
                        axis: {
                            x: {
                                type: 'category'
                            }
                        },
                        bar: {
                            width: {
                                ratio: 0.9
                            }
                        },
                        legend: {
                            hide: true
                        },
                        bindto: '#service-usage-chart'
                    });
                }
            };
            var truncateToDay = function (date) {
                truncateToHour(date);
                date.setHours(0);
                return date;
            };
            var truncateToHour = function (date) {
                date.setMinutes(0);
                date.setSeconds(0);
                date.setMilliseconds(0);
                return date;
            };
            var getChartDateRange = function () {
                var from = new Date();
                var to = new Date();
                if ($scope.metricsRange == '90days') {
                    from = new Date(from.getTime() - Apiman.NINETY_DAYS);
                    truncateToDay(from);
                }
                else if ($scope.metricsRange == '30days') {
                    from = new Date(from.getTime() - Apiman.THIRTY_DAYS);
                    truncateToDay(from);
                }
                else if ($scope.metricsRange == '7days') {
                    from = new Date(from.getTime() - Apiman.SEVEN_DAYS);
                    truncateToDay(from);
                }
                else if ($scope.metricsRange == '24hours') {
                    from = new Date(from.getTime() - Apiman.ONE_DAY);
                    truncateToHour(from);
                }
                else if ($scope.metricsRange == 'hour') {
                    from = new Date(from.getTime() - Apiman.ONE_HOUR);
                }
                return {
                    from: from,
                    to: to
                };
            };
            // *******************************************************
            // Refresh the usage charts
            // *******************************************************
            var refreshUsageCharts = function () {
                $scope.serviceUsageChartLoading = true;
                var range = getChartDateRange();
                var from = range.from;
                var to = range.to;
                var interval = 'day';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours') {
                    interval = 'hour';
                }
                if ($scope.metricsRange == 'hour') {
                    interval = 'minute';
                }
                // Refresh the usage chart
                if (usageByServiceChart) {
                    usageByServiceChart.destroy();
                    usageByServiceChart = null;
                }
                MetricsSvcs.getAppUsagePerService(params.org, params.app, params.version, from, to, function (data) {
                    $scope.serviceUsageChartLoading = false;
                    renderServiceUsageChart(data);
                }, function (error) {
                    Logger.error('Error loading usage chart data: {0}', JSON.stringify(error));
                    $scope.usageChartLoading = false;
                    $scope.usageChartNoData = true;
                });
            };
            var refreshCharts = function () {
                if ($scope.metricsType == 'usage') {
                    refreshUsageCharts();
                }
            };
            $scope.refreshCharts = refreshCharts;
            $scope.$watch('metricsRange', function (newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            $scope.$watch('metricsType', function (newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('AppMetrics', pageData, $scope, function () {
                PageLifecycle.setPageTitle('app-metrics', [$scope.app.name]);
                refreshCharts();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppOverviewController = Apiman._module.controller("Apiman.AppOverviewController", ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, AppEntityLoader, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('AppOverview', pageData, $scope, function () {
                PageLifecycle.setPageTitle('app-overview', [$scope.app.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppPoliciesController = Apiman._module.controller("Apiman.AppPoliciesController", ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, AppEntityLoader, OrgSvcs, Dialogs, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var removePolicy = function (policy) {
                angular.forEach($scope.policies, function (p, index) {
                    if (policy === p) {
                        $scope.policies.splice(index, 1);
                    }
                });
            };
            $scope.removePolicy = function (policy) {
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the application?', function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function (reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };
            $scope.reorderPolicies = function (reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };
                OrgSvcs.save({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' }, policyChainBean, function () {
                    Logger.debug("Reordering POSTed successfully");
                }, function () {
                    Logger.debug("Reordering POST failed.");
                });
            };
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                policies: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function (policies) {
                        resolve(policies);
                    }, reject);
                })
            });
            PageLifecycle.loadPage('AppPolicies', pageData, $scope, function () {
                PageLifecycle.setPageTitle('app-policies', [$scope.app.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.AppRedirectController = Apiman._module.controller("Apiman.AppRedirectController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) {
            var orgId = $routeParams.org;
            var appId = $routeParams.app;
            var pageData = {
                versions: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'applications', entityId: appId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('AppRedirect', pageData, $scope, function () {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                }
                else {
                    PageLifecycle.forwardTo('/orgs/{0}/apps/{1}/{2}', orgId, appId, version);
                }
            });
        }]);
    Apiman.AppEntityLoader = Apiman._module.factory('AppEntityLoader', ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusService',
        function ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusService) {
            return {
                getCommonData: function ($scope, $location) {
                    var params = $routeParams;
                    return {
                        version: $q(function (resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version }, function (version) {
                                $scope.org = version.application.organization;
                                $scope.app = version.application;
                                $rootScope.mruApp = version;
                                EntityStatusService.setEntityStatus(version.status);
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function (resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            };
        }]);
    Apiman.AppEntityController = Apiman._module.controller("Apiman.AppEntityController", ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusService', 'Configuration',
        function ($q, $scope, $location, ActionSvcs, Logger, Dialogs, PageLifecycle, $routeParams, OrgSvcs, EntityStatusService, Configuration) {
            var params = $routeParams;
            $scope.setEntityStatus = function (status) {
                EntityStatusService.setEntityStatus(status);
            };
            $scope.getEntityStatus = function () {
                return EntityStatusService.getEntityStatus();
            };
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.setVersion = function (app) {
                PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}', params.org, params.app, app.version);
            };
            $scope.registerApp = function () {
                $scope.registerButton.state = 'in-progress';
                var registerAction = {
                    type: 'registerApplication',
                    entityId: params.app,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(registerAction, function (reply) {
                    $scope.version.status = 'Registered';
                    $scope.registerButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            };
            $scope.unregisterApp = function () {
                $scope.unregisterButton.state = 'in-progress';
                Dialogs.confirm('Confirm Unregister App', 'Do you really want to unregister the application?  This cannot be undone.', function () {
                    var unregisterAction = {
                        type: 'unregisterApplication',
                        entityId: params.app,
                        organizationId: params.org,
                        entityVersion: params.version
                    };
                    ActionSvcs.save(unregisterAction, function (reply) {
                        $scope.version.status = 'Retired';
                        $scope.unregisterButton.state = 'complete';
                        $scope.setEntityStatus($scope.version.status);
                    }, PageLifecycle.handleError);
                }, function () {
                    $scope.unregisterButton.state = 'complete';
                });
            };
            $scope.updateAppDescription = function (updatedDescription) {
                var updateAppBean = {
                    description: updatedDescription
                };
                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'applications',
                    entityId: $scope.app.id
                }, updateAppBean, function (success) {
                }, function (error) {
                    Logger.error("Unable to update app description: {0}", error);
                });
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ConsumerOrgController = Apiman._module.controller("Apiman.ConsumerOrgController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, CurrentUser, $routeParams) {
            $scope.filterServices = function (value) {
                if (!value) {
                    $scope.filteredServices = $scope.services;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.services, function (service) {
                        if (service.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(service);
                        }
                    });
                    $scope.filteredServices = filtered;
                }
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: '' }, resolve, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'members' }, resolve, reject);
                }),
                services: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'services' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('ConsumerOrg', pageData, $scope, function () {
                $scope.org.isMember = CurrentUser.isMember($scope.org.id);
                $scope.filteredServices = $scope.services;
                PageLifecycle.setPageTitle('consumer-org', [$scope.org.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ConsumerOrgsController = Apiman._module.controller("Apiman.ConsumerOrgsController", ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'CurrentUser',
        function ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger, CurrentUser) {
            var params = $location.search();
            if (params.q) {
                $scope.orgName = params.q;
            }
            $scope.searchOrg = function (value) {
                $location.search('q', value);
            };
            var pageData = {
                orgs: $q(function (resolve, reject) {
                    if (params.q) {
                        var body = {};
                        body.filters = [];
                        body.filters.push({ "name": "name", "value": "*" + params.q + "*", "operator": "like" });
                        var searchStr = angular.toJson(body);
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'organizations' }, searchStr, function (result) {
                            resolve(result.beans);
                        }, reject);
                    }
                    else {
                        resolve([]);
                    }
                })
            };
            PageLifecycle.loadPage('ConsumerOrgs', pageData, $scope, function () {
                PageLifecycle.setPageTitle('consumer-orgs');
                $scope.$applyAsync(function () {
                    angular.forEach($scope.orgs, function (org) {
                        org.isMember = CurrentUser.isMember(org.id);
                    });
                    $('#apiman-search').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ConsumerServiceRedirectController = Apiman._module.controller("Apiman.ConsumerServiceRedirectController", ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, OrgSvcs, PageLifecycle, $routeParams) {
            var orgId = $routeParams.org;
            var serviceId = $routeParams.service;
            var pageData = {
                versions: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'services', entityId: serviceId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('ConsumerServiceRedirect', pageData, $scope, function () {
                var version = $scope.versions[0].version;
                for (var i = 0; i < $scope.versions.length; i++) {
                    var v = $scope.versions[i];
                    if (v.status == 'Published') {
                        version = v;
                        break;
                    }
                }
                PageLifecycle.forwardTo('/browse/orgs/{0}/{1}/{2}', orgId, serviceId, version);
            });
        }]);
    Apiman.ConsumerSvcController = Apiman._module.controller("Apiman.ConsumerSvcController", ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, OrgSvcs, PageLifecycle, $routeParams) {
            $scope.params = $routeParams;
            $scope.chains = {};
            $scope.hasSwagger = false;
            try {
                var swagger = SwaggerUi;
                $scope.hasSwagger = true;
            }
            catch (e) { }
            $scope.getPolicyChain = function (plan) {
                var planId = plan.planId;
                if (!$scope.chains[planId]) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'plans', policyId: plan.planId, policyChain: 'policyChain' }, function (policyReply) {
                        $scope.chains[planId] = policyReply.policies;
                    }, function (error) {
                        $scope.chains[planId] = [];
                    });
                }
            };
            var pageData = {
                version: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                }),
                versions: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions' }, function (versions) {
                        var publishedVersions = [];
                        angular.forEach(versions, function (version) {
                            if (version.version == $routeParams.version) {
                                $scope.selectedServiceVersion = version;
                            }
                            if (version.status == 'Published') {
                                publishedVersions.push(version);
                            }
                        });
                        resolve(publishedVersions);
                    }, reject);
                }),
                publicEndpoint: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'endpoint' }, resolve, function (error) {
                        resolve({
                            managedEndpoint: 'Not available.'
                        });
                    });
                }),
                plans: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'plans' }, resolve, reject);
                })
            };
            $scope.setVersion = function (serviceVersion) {
                PageLifecycle.redirectTo('/browse/orgs/{0}/{1}/{2}', $routeParams.org, $routeParams.service, serviceVersion.version);
            };
            PageLifecycle.loadPage('ConsumerService', pageData, $scope, function () {
                $scope.service = $scope.version.service;
                $scope.org = $scope.service.organization;
                PageLifecycle.setPageTitle('consumer-service', [$scope.service.name]);
            });
        }]);
    Apiman.ConsumerSvcDefController = Apiman._module.controller("Apiman.ConsumerSvcDefController", ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ServiceDefinitionSvcs', 'Configuration',
        function ($q, $scope, OrgSvcs, PageLifecycle, $routeParams, $window, Logger, ServiceDefinitionSvcs, Configuration) {
            $scope.params = $routeParams;
            $scope.chains = {};
            var pageData = {
                version: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('ConsumerServiceDef', pageData, $scope, function () {
                $scope.service = $scope.version.service;
                $scope.org = $scope.service.organization;
                $scope.hasError = false;
                PageLifecycle.setPageTitle('consumer-service-def', [$scope.service.name]);
                var hasSwagger = false;
                try {
                    var swagger = SwaggerUi;
                    hasSwagger = true;
                }
                catch (e) { }
                if ($scope.version.definitionType == 'SwaggerJSON' && hasSwagger) {
                    var url = ServiceDefinitionSvcs.getServiceDefinitionUrl($scope.params.org, $scope.params.service, $scope.params.version);
                    Logger.debug("!!!!! Using definition URL: {0}", url);
                    var authHeader = Configuration.getAuthorizationHeader();
                    $scope.definitionStatus = 'loading';
                    var swaggerOptions = {
                        url: url,
                        dom_id: "swagger-ui-container",
                        validatorUrl: null,
                        sorter: "alpha",
                        authorizations: {
                            apimanauth: new SwaggerClient.ApiKeyAuthorization("Authorization", authHeader, "header")
                        },
                        onComplete: function () {
                            $('#swagger-ui-container a').each(function (idx, elem) {
                                var href = $(elem).attr('href');
                                if (href[0] == '#') {
                                    $(elem).removeAttr('href');
                                }
                            });
                            $('#swagger-ui-container div.sandbox_header').each(function (idx, elem) {
                                $(elem).remove();
                            });
                            $('#swagger-ui-container li.operation div.auth').each(function (idx, elem) {
                                $(elem).remove();
                            });
                            $('#swagger-ui-container li.operation div.access').each(function (idx, elem) {
                                $(elem).remove();
                            });
                            $scope.$apply(function (error) {
                                $scope.definitionStatus = 'complete';
                            });
                        },
                        onFailure: function () {
                            $scope.$apply(function (error) {
                                $scope.definitionStatus = 'error';
                                $scope.hasError = true;
                                $scope.error = error;
                            });
                        }
                    };
                    $window.swaggerUi = new SwaggerUi(swaggerOptions);
                    $window.swaggerUi.load();
                    $scope.hasDefinition = true;
                }
                else {
                    $scope.hasDefinition = false;
                }
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ConsumerSvcsController = Apiman._module.controller("Apiman.ConsumerSvcsController", ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger',
        function ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger) {
            var params = $location.search();
            if (params.q) {
                $scope.serviceName = params.q;
            }
            $scope.searchSvcs = function (value) {
                $location.search('q', value);
            };
            var pageData = {
                services: $q(function (resolve, reject) {
                    if (params.q) {
                        var body = {};
                        body.filters = [];
                        body.filters.push({ "name": "name", "value": "*" + params.q + "*", "operator": "like" });
                        var searchStr = angular.toJson(body);
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'services' }, searchStr, function (reply) {
                            resolve(reply.beans);
                        }, reject);
                    }
                    else {
                        resolve([]);
                    }
                })
            };
            PageLifecycle.loadPage('ConsumerSvcs', pageData, $scope, function () {
                PageLifecycle.setPageTitle('consumer-services');
                $scope.$applyAsync(function () {
                    $('#apiman-search').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.EditGatewayController = Apiman._module.controller("Apiman.EditGatewayController", ['$q', '$rootScope', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs', '$routeParams',
        function ($q, $rootScope, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs, $routeParams) {
            $scope.isValid = false;
            var params = $routeParams;
            var validate = function () {
                $scope.testResult = 'none';
                // First validation
                var valid = true;
                if (!$scope.configuration.endpoint) {
                    valid = false;
                }
                if (!$scope.configuration.username) {
                    valid = false;
                }
                if (!$scope.configuration.password) {
                    valid = false;
                }
                if ($scope.configuration.password != $scope.passwordConfirm) {
                    valid = false;
                }
                $scope.isValid = valid;
                // Now dirty
                var dirty = false;
                if ($scope.gateway.description != $scope.originalGateway.description) {
                    dirty = true;
                }
                if ($scope.configuration.endpoint != $scope.originalConfig.endpoint) {
                    dirty = true;
                }
                if ($scope.configuration.username != $scope.originalConfig.username) {
                    dirty = true;
                }
                if ($scope.configuration.password != $scope.originalConfig.password) {
                    dirty = true;
                }
                $rootScope.isDirty = dirty;
            };
            var Gateway = function () {
                return {
                    description: $scope.gateway.description,
                    type: $scope.gateway.type,
                    configuration: angular.toJson($scope.configuration)
                };
            };
            var pageData = {
                gateway: $q(function (resolve, reject) {
                    ApimanSvcs.get({ entityType: 'gateways', secondaryType: params.gateway }, function (gateway) {
                        $scope.gateway = gateway;
                        $scope.configuration = JSON.parse(gateway.configuration);
                        $scope.passwordConfirm = $scope.configuration.password;
                        $scope.originalGateway = angular.copy(gateway);
                        $scope.originalConfig = angular.copy($scope.configuration);
                        $rootScope.isDirty = false;
                        resolve(gateway);
                    }, reject);
                })
            };
            var testGateway = function () {
                $scope.testButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.update({ entityType: 'gateways' }, gateway, function (reply) {
                    $scope.testButton.state = 'complete';
                    if (reply.success == true) {
                        Logger.info('Connected successfully to Gateway: {0}', reply.detail);
                        $scope.testResult = 'success';
                    }
                    else {
                        Logger.info('Failed to connect to Gateway: {0}', reply.detail);
                        $scope.testResult = 'error';
                        $scope.testErrorMessage = reply.detail;
                    }
                }, function (error) {
                    $scope.testButton.state = 'error';
                    $scope.testResult = 'error';
                    $scope.testErrorMessage = error;
                });
            };
            $scope.updateGateway = function () {
                $scope.updateButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.update({ entityType: 'gateways', secondaryType: $scope.gateway.id }, gateway, function () {
                    $rootScope.isDirty = false;
                    PageLifecycle.redirectTo('/admin/gateways');
                }, PageLifecycle.handleError);
            };
            $scope.deleteGateway = function () {
                $scope.deleteButton.state = 'in-progress';
                Dialogs.confirm('Confirm Delete Gateway', 'Do you really want to permanently delete this gateway?  This can be very destructive to any Service published to it.', function () {
                    ApimanSvcs.delete({ entityType: 'gateways', secondaryType: $scope.gateway.id }, function (reply) {
                        PageLifecycle.redirectTo('/admin/gateways');
                    }, PageLifecycle.handleError);
                }, function () {
                    $scope.deleteButton.state = 'complete';
                });
            };
            $scope.testGateway = testGateway;
            PageLifecycle.loadPage('EditGateway', pageData, $scope, function () {
                PageLifecycle.setPageTitle('edit-gateway');
                $scope.$watch('gateway', validate, true);
                $scope.$watch('configuration', validate, true);
                $scope.$watch('passwordConfirm', validate);
                $('#apiman-gateway-description').focus();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.EditPluginController = Apiman._module.controller("Apiman.EditPluginController", ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs', '$routeParams', 'Logger',
        function ($q, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs, $routeParams, Logger) {
            var params = $routeParams;
            var pageData = {
                plugin: $q(function (resolve, reject) {
                    ApimanSvcs.get({ entityType: 'plugins', secondaryType: params.plugin }, function (plugin) {
                        plugin.isSnapshot = plugin.version.indexOf("-SNAPSHOT", plugin.version.length - "-SNAPSHOT".length) !== -1;
                        resolve(plugin);
                    }, reject);
                })
            };
            $scope.reload = function () {
                Logger.debug("Now reloading the plugin!");
                $scope.reloadButton.state = 'in-progress';
                var body = {
                    groupId: $scope.plugin.groupId,
                    artifactId: $scope.plugin.artifactId,
                    version: $scope.plugin.version,
                    classifier: $scope.plugin.classifier,
                    type: $scope.plugin.type
                };
                ApimanSvcs.save({ entityType: 'plugins' }, body, function (reply) {
                    PageLifecycle.redirectTo('/admin/plugins');
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('EditPlugin', pageData, $scope, function () {
                PageLifecycle.setPageTitle('plugin-details');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.EditPolicyController = Apiman._module.controller("Apiman.EditPolicyController", ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'PageLifecycle', 'Logger', '$routeParams', 'EntityStatusService', 'CurrentUser',
        function ($q, $location, $scope, OrgSvcs, ApimanSvcs, PageLifecycle, Logger, $routeParams, EntityStatusService, CurrentUser) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            var requiredPermissionMap = {
                applications: 'appEdit',
                services: 'svcEdit',
                plans: 'planEdit'
            };
            var etype = params.type;
            if (etype == 'apps') {
                etype = 'applications';
            }
            $scope.getEntityStatus = function () {
                return EntityStatusService.getEntityStatus();
            };
            var pageData = {
                version: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: etype, entityId: params.id, versionsOrActivity: 'versions', version: params.ver }, resolve, reject);
                }),
                policy: $q(function (resolve, reject) {
                    OrgSvcs.get({
                        organizationId: params.org,
                        entityType: etype,
                        entityId: params.id,
                        versionsOrActivity: 'versions',
                        version: params.ver,
                        policiesOrActivity: 'policies',
                        policyId: params.policy
                    }, function (policy) {
                        var config = new Object();
                        try {
                            config = JSON.parse(policy.configuration);
                        }
                        catch (e) {
                        }
                        $scope.config = config;
                        if (policy.definition.formType == 'JsonSchema') {
                            $scope.include = 'plugins/api-manager/html/policyForms/JsonSchema.include';
                        }
                        else {
                            var inc = Apiman.ConfigForms[policy.definition.id];
                            if (!inc) {
                                inc = 'Default.include';
                            }
                            $scope.include = 'plugins/api-manager/html/policyForms/' + inc;
                        }
                        $scope.selectedDef = policy.definition;
                        resolve(policy);
                    }, reject);
                })
            };
            $scope.setValid = function (valid) {
                $scope.isValid = valid;
            };
            $scope.setConfig = function (config) {
                $scope.config = config;
            };
            $scope.getConfig = function () {
                return $scope.config;
            };
            $scope.updatePolicy = function () {
                $scope.updateButton.state = 'in-progress';
                var updatedPolicy = {
                    configuration: angular.toJson($scope.config)
                };
                var etype = params.type;
                if (etype == 'apps') {
                    etype = 'applications';
                }
                OrgSvcs.update({
                    organizationId: params.org,
                    entityType: etype,
                    entityId: params.id,
                    versionsOrActivity: 'versions',
                    version: params.ver,
                    policiesOrActivity: 'policies',
                    policyId: params.policy
                }, updatedPolicy, function () {
                    PageLifecycle.redirectTo('/orgs/{0}/{1}/{2}/{3}/policies', params.org, params.type, params.id, params.ver);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('EditPolicy', pageData, $scope, function () {
                EntityStatusService.setEntityStatus($scope.version.status);
                // Note: not using the apiman-permission directive in the template for this page because
                // we cannot hard-code the required permission.  The required permission changes depending
                // on the entity type of the parent of the policy.  Instead we figure it out and set it here.
                $scope.hasPermission = CurrentUser.hasPermission(params.org, requiredPermissionMap[etype]);
                PageLifecycle.setPageTitle('edit-policy');
                $('#apiman-description').focus();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.EditPolicyDefController = Apiman._module.controller("Apiman.EditPolicyDefController", ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, $location, ApimanSvcs, PageLifecycle, $routeParams) {
            var params = $routeParams;
            var pageData = {
                policyDef: $q(function (resolve, reject) {
                    ApimanSvcs.get({ entityType: 'policyDefs', secondaryType: params.policyDef }, function (policyDef) {
                        resolve(policyDef);
                        $scope.policyDefJSON = angular.toJson(policyDef, true);
                    }, reject);
                })
            };
            $scope.updatePolicyDef = function () {
                var policyDefUpdate = {};
                var policyDef = JSON.parse($scope.policyDefJSON);
                policyDefUpdate.name = policyDef.name;
                policyDefUpdate.description = policyDef.description;
                policyDefUpdate.icon = policyDef.icon;
                ApimanSvcs.update({ entityType: 'policyDefs', secondaryType: $scope.policyDef.id }, policyDefUpdate, function (reply) {
                    PageLifecycle.redirectTo('/admin/policyDefs');
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('EditPolicyDef', pageData, $scope, function () {
                PageLifecycle.setPageTitle('edit-policyDef');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.EditRoleController = Apiman._module.controller("Apiman.EditRoleController", ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'Dialogs', '$routeParams',
        function ($q, $scope, $location, ApimanSvcs, PageLifecycle, Logger, Dialogs, $routeParams) {
            var params = $routeParams;
            var allPermissions = ['orgView', 'orgEdit', 'orgAdmin',
                'planView', 'planEdit', 'planAdmin',
                'svcView', 'svcEdit', 'svcAdmin',
                'appView', 'appEdit', 'appAdmin'];
            $scope.isValid = true;
            $scope.rolePermissions = {};
            angular.forEach(allPermissions, function (value) {
                $scope.rolePermissions[value] = false;
            });
            var validate = function () {
                var atLeastOne = false;
                angular.forEach($scope.rolePermissions, function (value, key) {
                    if (value == true) {
                        atLeastOne = true;
                    }
                });
                return atLeastOne;
            };
            $scope.$watch('rolePermissions', function (newValue) {
                $scope.isValid = validate();
            }, true);
            var pageData = {
                role: $q(function (resolve, reject) {
                    ApimanSvcs.get({ entityType: 'roles', secondaryType: params.role }, function (role) {
                        angular.forEach(role.permissions, function (name) {
                            $scope.rolePermissions[name] = true;
                        });
                        resolve(role);
                    }, reject);
                })
            };
            $scope.updateRole = function () {
                $scope.updateButton.state = 'in-progress';
                var permissions = [];
                angular.forEach($scope.rolePermissions, function (value, key) {
                    if (value == true) {
                        permissions.push(key);
                    }
                });
                var role = {};
                role.name = $scope.role.name;
                role.description = $scope.role.description;
                role.permissions = permissions;
                role.autoGrant = $scope.role.autoGrant;
                ApimanSvcs.update({ entityType: 'roles', secondaryType: $scope.role.id }, role, function (reply) {
                    PageLifecycle.redirectTo('/admin/roles');
                }, PageLifecycle.handleError);
            };
            $scope.deleteRole = function () {
                $scope.deleteButton.state = 'in-progress';
                Dialogs.confirm('Confirm Delete Role', 'Do you really want to delete this role?', function () {
                    ApimanSvcs.delete({ entityType: 'roles', secondaryType: $scope.role.id }, function (reply) {
                        PageLifecycle.redirectTo('/admin/roles');
                    }, PageLifecycle.handleError);
                }, function () {
                    $scope.deleteButton.state = 'complete';
                });
            };
            PageLifecycle.loadPage('EditRole', pageData, $scope, function () {
                PageLifecycle.setPageTitle('edit-role');
                $('#apiman-description').focus();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ImportPolicyDefsController = Apiman._module.controller("Apiman.ImportPolicyDefsController", ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle',
        function ($q, $scope, $location, ApimanSvcs, PageLifecycle) {
            $scope.isData = true;
            $scope.isConfirm = false;
            $scope.isValid = false;
            $scope.parseJSON = function () {
                var policiesImport = JSON.parse($scope.policyDefsJSON);
                var policyDefs = [];
                if (policiesImport.constructor === Array) {
                    policyDefs = policiesImport;
                }
                else {
                    policyDefs.push(policiesImport);
                }
                $scope.policyDefs = policyDefs;
                $scope.isData = false;
                $scope.isConfirm = true;
            };
            $scope.$watch('policyDefsJSON', function (newValue) {
                try {
                    JSON.parse($scope.policyDefsJSON);
                    $scope.isValid = true;
                }
                catch (e) {
                    $scope.isValid = false;
                }
            });
            $scope.importPolicyDefs = function () {
                $scope.yesButton.state = 'in-progress';
                var promises = [];
                angular.forEach($scope.policyDefs, function (def) {
                    promises.push($q(function (resolve, reject) {
                        ApimanSvcs.save({ entityType: 'policyDefs' }, def, resolve, reject);
                    }));
                });
                $q.all(promises).then(function () {
                    PageLifecycle.redirectTo('/admin/policyDefs');
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('ImportPolicyDefs', undefined, $scope, function () {
                PageLifecycle.setPageTitle('import-policyDefs');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewAppController = Apiman._module.controller("Apiman.NewAppController", ['$q', '$location', '$scope', 'CurrentUserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        function ($q, $location, $scope, CurrentUserSvcs, OrgSvcs, PageLifecycle, $rootScope) {
            var recentOrg = $rootScope.mruOrg;
            var pageData = {
                organizations: $q(function (resolve, reject) {
                    CurrentUserSvcs.query({ what: 'apporgs' }, function (orgs) {
                        if (recentOrg) {
                            $scope.selectedOrg = recentOrg;
                        }
                        else if (orgs.length > 0) {
                            $scope.selectedOrg = orgs[0];
                        }
                        resolve(orgs);
                    }, reject);
                }),
            };
            $scope.setOrg = function (org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewApp = function () {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'applications' }, $scope.app, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}', reply.organization.id, reply.id, $scope.app.initialVersion);
                }, PageLifecycle.handleError);
            };
            $scope.app = {
                initialVersion: '1.0'
            };
            PageLifecycle.loadPage('NewApp', pageData, $scope, function () {
                PageLifecycle.setPageTitle('new-app');
                $scope.$applyAsync(function () {
                    $('#apiman-entityname').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewAppVersionController = Apiman._module.controller("Apiman.NewAppVersionController", ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $location, $scope, OrgSvcs, PageLifecycle, $routeParams) {
            var params = $routeParams;
            $scope.appversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewAppVersion = function () {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: '' }, $scope.appversion, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}', params.org, params.app, reply.version);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewAppVersion', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-app-version');
                $scope.$applyAsync(function () {
                    $('#apiman-version').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewContractController = Apiman._module.controller("Apiman.NewContractController", ['$q', '$location', '$scope', 'OrgSvcs', 'CurrentUserSvcs', 'PageLifecycle', 'Logger', '$rootScope', 'Dialogs',
        function ($q, $location, $scope, OrgSvcs, CurrentUserSvcs, PageLifecycle, Logger, $rootScope, Dialogs) {
            var params = $location.search();
            var svcId = params.svc;
            var svcOrgId = params.svcorg;
            var svcVer = params.svcv;
            var planId = params.planid;
            $scope.refreshAppVersions = function (organizationId, appId, onSuccess, onError) {
                OrgSvcs.query({ organizationId: organizationId, entityType: 'applications', entityId: appId, versionsOrActivity: 'versions' }, function (versions) {
                    var plainVersions = [];
                    angular.forEach(versions, function (version) {
                        if (version.status == 'Created' || version.status == 'Ready') {
                            plainVersions.push(version.version);
                        }
                    });
                    $scope.appVersions = plainVersions;
                    if (onSuccess) {
                        onSuccess(plainVersions);
                    }
                }, PageLifecycle.handleError);
            };
            var pageData = {
                apps: $q(function (resolve, reject) {
                    CurrentUserSvcs.query({ what: 'applications' }, function (apps) {
                        if ($rootScope.mruApp) {
                            for (var i = 0; i < apps.length; i++) {
                                var app = apps[i];
                                if (app.organizationId == $rootScope.mruApp.application.organization.id && app.id == $rootScope.mruApp.application.id) {
                                    $scope.selectedApp = app;
                                }
                            }
                        }
                        else {
                            $scope.selectedApp = undefined;
                        }
                        resolve(apps);
                    }, reject);
                }),
                selectedService: $q(function (resolve, reject) {
                    if (svcId && svcOrgId && svcVer) {
                        Logger.debug('Loading service {0}/{1} version {2}.', svcOrgId, svcId, svcVer);
                        OrgSvcs.get({ organizationId: svcOrgId, entityType: 'services', entityId: svcId, versionsOrActivity: 'versions', version: svcVer }, function (serviceVersion) {
                            serviceVersion.organizationName = serviceVersion.service.organization.name;
                            serviceVersion.organizationId = serviceVersion.service.organization.id;
                            serviceVersion.name = serviceVersion.service.name;
                            serviceVersion.id = serviceVersion.service.id;
                            resolve(serviceVersion);
                        }, reject);
                    }
                    else {
                        resolve(undefined);
                    }
                })
            };
            $scope.$watch('selectedApp', function (newValue) {
                Logger.debug("App selected: {0}", newValue);
                $scope.selectedAppVersion = undefined;
                $scope.appVersions = [];
                if (newValue) {
                    $scope.refreshAppVersions(newValue.organizationId, newValue.id, function (versions) {
                        Logger.debug("Versions: {0}", versions);
                        if ($rootScope.mruApp) {
                            if ($rootScope.mruApp.application.organization.id == newValue.organizationId && $rootScope.mruApp.application.id == newValue.id) {
                                $scope.selectedAppVersion = $rootScope.mruApp.version;
                            }
                        }
                        else {
                            if (versions.length > 0) {
                                $scope.selectedAppVersion = versions[0];
                            }
                        }
                    });
                }
            });
            $scope.selectService = function () {
                Dialogs.selectService('Select a Service', function (serviceVersion) {
                    $scope.selectedService = serviceVersion;
                }, true);
            };
            $scope.$watch('selectedService', function (newValue) {
                if (!newValue) {
                    $scope.plans = undefined;
                    $scope.selectedPlan = undefined;
                    return;
                }
                Logger.debug('Service selection made, fetching plans.');
                OrgSvcs.query({ organizationId: newValue.organizationId, entityType: 'services', entityId: newValue.id, versionsOrActivity: 'versions', version: newValue.version, policiesOrActivity: 'plans' }, function (plans) {
                    $scope.plans = plans;
                    Logger.debug("Found {0} plans: {1}.", plans.length, plans);
                    if (plans.length > 0) {
                        if (planId) {
                            for (var i = 0; i < plans.length; i++) {
                                if (plans[i].planId == planId) {
                                    $scope.selectedPlan = plans[i];
                                }
                            }
                        }
                        else {
                            $scope.selectedPlan = undefined;
                        }
                    }
                    else {
                        $scope.plans = undefined;
                    }
                }, PageLifecycle.handleError);
            });
            $scope.createContract = function () {
                Logger.log("Creating new contract from {0}/{1} ({2}) to {3}/{4} ({5}) through the {6} plan!", $scope.selectedApp.organizationName, $scope.selectedApp.name, $scope.selectedAppVersion, $scope.selectedService.organizationName, $scope.selectedService.name, $scope.selectedService.version, $scope.selectedPlan.planName);
                $scope.createButton.state = 'in-progress';
                var newContract = {
                    serviceOrgId: $scope.selectedService.organizationId,
                    serviceId: $scope.selectedService.id,
                    serviceVersion: $scope.selectedService.version,
                    planId: $scope.selectedPlan.planId
                };
                OrgSvcs.save({ organizationId: $scope.selectedApp.organizationId, entityType: 'applications', entityId: $scope.selectedApp.id, versionsOrActivity: 'versions', version: $scope.selectedAppVersion, policiesOrActivity: 'contracts' }, newContract, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apps/{1}/{2}/contracts', $scope.selectedApp.organizationId, $scope.selectedApp.id, $scope.selectedAppVersion);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewContract', pageData, $scope, function () {
                PageLifecycle.setPageTitle('new-contract');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewGatewayController = Apiman._module.controller("Apiman.NewGatewayController", ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'CurrentUser', 'Logger',
        function ($q, $location, $scope, ApimanSvcs, PageLifecycle, CurrentUser, Logger) {
            $scope.isValid = false;
            $scope.gateway = {};
            $scope.configuration = {
                endpoint: 'http://localhost:8080/apiman-gateway-api/'
            };
            var validate = function () {
                $scope.testResult = 'none';
                var valid = true;
                if (!$scope.gateway.name) {
                    valid = false;
                }
                if (!$scope.configuration.endpoint) {
                    valid = false;
                }
                if (!$scope.configuration.username) {
                    valid = false;
                }
                if (!$scope.configuration.password) {
                    valid = false;
                }
                if ($scope.configuration.password != $scope.passwordConfirm) {
                    valid = false;
                }
                $scope.isValid = valid;
            };
            $scope.$watch('gateway', validate, true);
            $scope.$watch('configuration', validate, true);
            $scope.$watch('passwordConfirm', validate);
            var Gateway = function () {
                var gateway = $scope.gateway;
                gateway.configuration = angular.toJson($scope.configuration);
                gateway.type = 'REST';
                return gateway;
            };
            var testGateway = function () {
                $scope.testButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.update({ entityType: 'gateways' }, gateway, function (reply) {
                    $scope.testButton.state = 'complete';
                    if (reply.success == true) {
                        Logger.info('Connected successfully to Gateway: {0}', reply.detail);
                        $scope.testResult = 'success';
                    }
                    else {
                        Logger.info('Failed to connect to Gateway: {0}', reply.detail);
                        $scope.testResult = 'error';
                        $scope.testErrorMessage = reply.detail;
                    }
                }, function (error) {
                    $scope.testButton.state = 'error';
                    $scope.testResult = 'error';
                    $scope.testErrorMessage = error;
                });
            };
            $scope.createGateway = function () {
                $scope.createButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.save({ entityType: 'gateways' }, gateway, function (reply) {
                    PageLifecycle.redirectTo('/admin/gateways');
                }, PageLifecycle.handleError);
            };
            $scope.testGateway = testGateway;
            PageLifecycle.loadPage('NewGateway', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-gateway');
                $('#apiman-gateway-name').focus();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewOrgController = Apiman._module.controller("Apiman.NewOrgController", ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'CurrentUser',
        function ($q, $location, $scope, OrgSvcs, PageLifecycle, CurrentUser) {
            $scope.saveNewOrg = function () {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save($scope.org, function (reply) {
                    CurrentUser.clear();
                    PageLifecycle.redirectTo('/orgs/{0}/plans', reply.id);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewOrg', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-org');
                $scope.$applyAsync(function () {
                    $('#apiman-entityname').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewPlanController = Apiman._module.controller("Apiman.NewPlanController", ['$q', '$location', '$scope', 'CurrentUserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        function ($q, $location, $scope, CurrentUserSvcs, OrgSvcs, PageLifecycle, $rootScope) {
            var recentOrg = $rootScope.mruOrg;
            var pageData = {
                organizations: $q(function (resolve, reject) {
                    CurrentUserSvcs.query({ what: 'planorgs' }, function (orgs) {
                        if (recentOrg) {
                            $scope.selectedOrg = recentOrg;
                        }
                        else if (orgs.length > 0) {
                            $scope.selectedOrg = orgs[0];
                        }
                        resolve(orgs);
                    }, reject);
                })
            };
            $scope.setOrg = function (org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewPlan = function () {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'plans' }, $scope.plan, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/plans/{1}/{2}', reply.organization.id, reply.id, $scope.plan.initialVersion);
                }, PageLifecycle.handleError);
            };
            // Initialize the model - the default initial version for a new plan is always 1.0
            $scope.plan = {
                initialVersion: '1.0'
            };
            PageLifecycle.loadPage('NewPlan', pageData, $scope, function () {
                PageLifecycle.setPageTitle('new-plan');
                $scope.$applyAsync(function () {
                    $('#apiman-entityname').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewPlanVersionController = Apiman._module.controller("Apiman.NewPlanVersionController", ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $location, $scope, OrgSvcs, PageLifecycle, $routeParams) {
            var params = $routeParams;
            $scope.planversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewPlanVersion = function () {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: '' }, $scope.planversion, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/plans/{1}/{2}', params.org, params.plan, reply.version);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewPlanVersion', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-plan-version');
                $scope.$applyAsync(function () {
                    $('#apiman-version').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewPluginController = Apiman._module.controller("Apiman.NewPluginController", ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, $location, ApimanSvcs, PageLifecycle, $routeParams) {
            $scope.plugin = {};
            var params = $routeParams;
            if (params && params.groupId) {
                $scope.plugin = {
                    groupId: params.groupId,
                    artifactId: params.artifactId,
                    version: params.version
                };
                if (params.classifier) {
                    $scope.plugin.classifier = params.classifier;
                }
                if (params.type) {
                    $scope.plugin.type = params.type;
                }
            }
            var validate = function () {
                var valid = true;
                if (!$scope.plugin.groupId) {
                    valid = false;
                }
                if (!$scope.plugin.artifactId) {
                    valid = false;
                }
                if (!$scope.plugin.version) {
                    valid = false;
                }
                $scope.isValid = valid;
            };
            $scope.$watch('plugin', function (newValue) {
                validate();
            }, true);
            $scope.addPlugin = function () {
                $scope.addButton.state = 'in-progress';
                ApimanSvcs.save({ entityType: 'plugins' }, $scope.plugin, function (reply) {
                    PageLifecycle.redirectTo('/admin/plugins');
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewPlugin', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-plugin');
                $('#apiman-group-id').focus();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ConfigForms = {
        BASICAuthenticationPolicy: 'basic-auth.include',
        IgnoredResourcesPolicy: 'ignored-resources.include',
        IPBlacklistPolicy: 'ip-list.include',
        IPWhitelistPolicy: 'ip-list.include',
        RateLimitingPolicy: 'rate-limiting.include',
        QuotaPolicy: 'quota.include',
        TransferQuotaPolicy: 'transfer-quota.include',
        AuthorizationPolicy: 'authorization.include',
        URLRewritingPolicy: 'url-rewriting.include',
        CachingPolicy: 'caching.include'
    };
    Apiman.NewPolicyController = Apiman._module.controller("Apiman.NewPolicyController", ['$q', '$location', '$scope', 'OrgSvcs', 'ApimanSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        function ($q, $location, $scope, OrgSvcs, ApimanSvcs, PageLifecycle, Logger, $routeParams) {
            var params = $routeParams;
            var pageData = {
                policyDefs: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'policyDefs' }, function (policyDefs) {
                        $scope.selectedDefId = '__null__';
                        resolve(policyDefs);
                    }, reject);
                })
            };
            $scope.$watch('selectedDefId', function (newValue) {
                if (newValue) {
                    var newDef = undefined;
                    angular.forEach($scope.policyDefs, function (def) {
                        if (def.id == newValue) {
                            newDef = def;
                        }
                    });
                    $scope.selectedDef = newDef;
                }
            });
            $scope.$watch('selectedDef', function (newValue) {
                if (!newValue) {
                    $scope.include = undefined;
                }
                else {
                    $scope.config = new Object();
                    if ($scope.selectedDef.formType == 'JsonSchema') {
                        $scope.include = 'plugins/api-manager/html/policyForms/JsonSchema.include';
                    }
                    else {
                        var inc = Apiman.ConfigForms[$scope.selectedDef.id];
                        if (!inc) {
                            inc = 'Default.include';
                        }
                        $scope.include = 'plugins/api-manager/html/policyForms/' + inc;
                    }
                }
            });
            $scope.setValid = function (valid) {
                $scope.isValid = valid;
            };
            $scope.setConfig = function (config) {
                $scope.config = config;
            };
            $scope.getConfig = function () {
                return $scope.config;
            };
            $scope.addPolicy = function () {
                $scope.createButton.state = 'in-progress';
                var newPolicy = {
                    definitionId: $scope.selectedDefId,
                    configuration: angular.toJson($scope.config)
                };
                var etype = params.type;
                if (etype == 'apps') {
                    etype = 'applications';
                }
                OrgSvcs.save({ organizationId: params.org, entityType: etype, entityId: params.id, versionsOrActivity: 'versions', version: params.ver, policiesOrActivity: 'policies' }, newPolicy, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/{1}/{2}/{3}/policies', params.org, params.type, params.id, params.ver);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewPolicy', pageData, $scope, function () {
                PageLifecycle.setPageTitle('new-policy');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewRoleController = Apiman._module.controller("Apiman.NewRoleController", ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', 'Logger', 'ApimanSvcs',
        function ($q, $location, $scope, OrgSvcs, PageLifecycle, CurrentUser, Logger, ApimanSvcs) {
            $scope.role = {};
            $scope.rolePermissions = {};
            $scope.isValid = false;
            var validate = function () {
                var valid = true;
                if (!$scope.role.name) {
                    valid = false;
                }
                var atLeastOne = false;
                angular.forEach($scope.rolePermissions, function (value, key) {
                    if (value == true) {
                        atLeastOne = true;
                    }
                });
                if (!atLeastOne) {
                    valid = false;
                }
                $scope.isValid = valid;
            };
            $scope.$watch('role', function (newValue) {
                validate();
            }, true);
            $scope.$watch('rolePermissions', function (newValue) {
                validate();
            }, true);
            $scope.addRole = function () {
                $scope.createButton.state = 'in-progress';
                var permissions = [];
                angular.forEach($scope.rolePermissions, function (value, key) {
                    if (value == true) {
                        permissions.push(key);
                    }
                });
                var role = {};
                role.name = $scope.role.name;
                role.description = $scope.role.description;
                role.permissions = permissions;
                role.autoGrant = $scope.role.autoGrant;
                ApimanSvcs.save({ entityType: 'roles' }, role, function (reply) {
                    PageLifecycle.redirectTo('/admin/roles');
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewRole', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-role');
                $('#apiman-entityname').focus();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewServiceController = Apiman._module.controller("Apiman.NewServiceController", ['$q', '$location', '$scope', 'CurrentUserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        function ($q, $location, $scope, CurrentUserSvcs, OrgSvcs, PageLifecycle, $rootScope) {
            var recentOrg = $rootScope.mruOrg;
            var pageData = {
                organizations: $q(function (resolve, reject) {
                    CurrentUserSvcs.query({ what: 'svcorgs' }, function (orgs) {
                        if (recentOrg) {
                            $scope.selectedOrg = recentOrg;
                        }
                        else if (orgs.length > 0) {
                            $scope.selectedOrg = orgs[0];
                        }
                        resolve(orgs);
                    }, reject);
                }),
            };
            $scope.setOrg = function (org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewService = function () {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'services' }, $scope.service, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/services/{1}/{2}', reply.organization.id, reply.id, $scope.service.initialVersion);
                }, PageLifecycle.handleError);
            };
            $scope.service = {
                initialVersion: '1.0'
            };
            PageLifecycle.loadPage('NewService', pageData, $scope, function () {
                PageLifecycle.setPageTitle('new-service');
                $scope.$applyAsync(function () {
                    $('#apiman-entityname').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NewServiceVersionController = Apiman._module.controller("Apiman.NewServiceVersionController", ['$q', '$location', '$scope', 'OrgSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        function ($q, $location, $scope, OrgSvcs, PageLifecycle, Logger, $routeParams) {
            var params = $routeParams;
            $scope.svcversion = {
                clone: true,
                cloneVersion: params.version
            };
            $scope.saveNewServiceVersion = function () {
                $scope.createButton.state = 'in-progress';
                Logger.info('Creating new version {0} of service {1} / {2}', $scope.svcversion.version, params.service, params.org);
                OrgSvcs.save({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: '' }, $scope.svcversion, function (reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/services/{1}/{2}', params.org, params.service, reply.version);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('NewServiceVersion', undefined, $scope, function () {
                PageLifecycle.setPageTitle('new-service-version');
                $scope.$applyAsync(function () {
                    $('#apiman-version').focus();
                });
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgActivityController = Apiman._module.controller("Apiman.OrgActivityController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'AuditSvcs', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, AuditSvcs, $routeParams) {
            $scope.tab = 'activity';
            var params = $routeParams;
            $scope.organizationId = params.org;
            var getNextPage = function (successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, page: $scope.currentPage, count: 20 }, function (results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        resolve(members);
                    }, reject);
                }),
                auditEntries: $q(function (resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            };
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('OrgActivity', pageData, $scope, function () {
                PageLifecycle.setPageTitle('org-activity', [$scope.org.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgAppsController = Apiman._module.controller("Apiman.OrgAppsController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) {
            $scope.tab = 'applications';
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.filterApps = function (value) {
                if (!value) {
                    $scope.filteredApps = $scope.apps;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.apps, function (app) {
                        if (app.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(app);
                        }
                    });
                    $scope.filteredApps = filtered;
                }
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        resolve(members);
                    }, reject);
                }),
                apps: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications' }, function (apps) {
                        $scope.filteredApps = apps;
                        resolve(apps);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgApps', pageData, $scope, function () {
                PageLifecycle.setPageTitle('org-apps', [$scope.org.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.getRoleIds = function (member) {
        return member.roles.map(function (role) {
            return role.roleId;
        });
    };
    Apiman.OrgManageMembersController = Apiman._module.controller("Apiman.OrgManageMembersController", ['$q', '$scope', '$location', 'ApimanSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'Logger', '$routeParams',
        function ($q, $scope, $location, ApimanSvcs, OrgSvcs, PageLifecycle, $rootScope, $log, $routeParams) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.filteredMembers = [];
            $scope.filterValue = "";
            $scope.selectedRoles = "";
            var containsAnyRoles = function (containsArray) {
                if ($scope.selectedRoles.length === 0) {
                    return true;
                }
                var returnVal = false;
                jQuery.each($scope.selectedRoles, function (index, value) {
                    if (jQuery.inArray(value, containsArray) > -1) {
                        return returnVal = true;
                    }
                });
                return returnVal;
            };
            $scope.filterMembers = function (value) {
                $scope.filterValue = value;
                if (!value) {
                    // Case 1: no filter value and no selected roles
                    // Case 2: no filter value but at least one selected role
                    // Case 3: 
                    if ($scope.selectedRoles.length === 0) {
                        $scope.filteredMembers = $scope.members;
                    }
                    else {
                        $scope.filteredMembers = jQuery.grep($scope.members, function (member, _) {
                            return containsAnyRoles(Apiman.getRoleIds(member));
                        });
                    }
                }
                else {
                    $scope.filteredMembers = jQuery.grep($scope.members, function (m, _) {
                        return ((m.userName.toLowerCase().indexOf(value.toLowerCase()) > -1 || m.userId.toLowerCase().indexOf(value.toLowerCase()) > -1)
                            && containsAnyRoles(Apiman.getRoleIds(m)));
                    });
                }
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        $scope.filteredMembers = members;
                        resolve(members);
                    }, reject);
                }),
                roles: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'roles' }, function (adminRoles) {
                        $scope.filteredRoles = adminRoles;
                        resolve(adminRoles);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgManageMembers', pageData, $scope, function () {
                PageLifecycle.setPageTitle('org-manage-members', [$scope.org.name]);
            });
        }]);
    Apiman.OrgManageMembersController.directive('apimanUserCard', ['OrgSvcs', 'Dialogs', 'Logger', 'PageLifecycle',
        function (OrgSvcs, Dialogs, $log, PageLifecycle) {
            return {
                restrict: 'E',
                scope: {
                    member: '=',
                    roles: '=',
                    orgId: '@'
                },
                template: '<div ng-include="currentTemplate()" ng-show="isCardVisible"></div>',
                link: function ($scope, element, attrs) {
                    // updatedRoles comes from card-back.
                    $scope.updatedRoles = Apiman.getRoleIds($scope.member);
                    $scope.front = 'apiman-user-card-front';
                    $scope.back = 'apiman-user-card-back';
                    $scope.cardFace = $scope.front;
                    $scope.isCardVisible = true;
                    $scope.flipCard = function (face) {
                        $scope.cardFace = face;
                    };
                    $scope.currentTemplate = function () {
                        return 'plugins/api-manager/html/org/' + $scope.cardFace + '.html';
                    };
                    $scope.joinRoles = function (roles) {
                        return roles.map(function (role) {
                            return role.roleName;
                        }).join(', ');
                    };
                    // Update is revoke + grant
                    $scope.updateRoles = function (selectedRoles) {
                        if (!selectedRoles)
                            return $scope.flipCard($scope.front);
                        var grantRolesBean = {
                            userId: $scope.member.userId,
                            roleIds: selectedRoles
                        };
                        _revokeAll($scope.orgId, $scope.member.userId);
                        OrgSvcs.save({ organizationId: $scope.orgId, entityType: 'roles' }, grantRolesBean, function () {
                            $log.info('Successfully Saved: ' + angular.toJson(grantRolesBean));
                            $scope.flipCard($scope.front);
                        }, PageLifecycle.handleError);
                        _reassignRoles(selectedRoles);
                    };
                    // Revoke all permissions with warning
                    $scope.revokeAll = function () {
                        Dialogs.confirm('Confirm Revoke All', 'This will remove ' + $scope.member.userName + ' from all roles in the Organization. Really do this?', function () {
                            _revokeAll($scope.orgId, $scope.member.userId);
                            $scope.isCardVisible = false;
                        });
                    };
                    // Actual revoke function.
                    var _revokeAll = function (orgId, userId) {
                        OrgSvcs.delete({ organizationId: orgId, entityType: 'members', entityId: userId }, function () {
                            $log.debug('Successfully revoked all roles for ' + userId);
                        }, PageLifecycle.handleError);
                    };
                    // Now we've modified the roles, we can update to reflect.
                    var _reassignRoles = function (newRoles) {
                        var matchingRoles = jQuery.grep($scope.roles, function (role, _) {
                            return jQuery.inArray(role.id, newRoles) >= 0;
                        });
                        var assignedRoles = matchingRoles.map(function (elem) {
                            return {
                                roleId: elem.id,
                                roleName: elem.name
                            };
                        });
                        $scope.member.roles = assignedRoles;
                        $scope.updatedRoles = Apiman.getRoleIds($scope.member);
                    };
                }
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgMembersController = Apiman._module.controller("Apiman.OrgMembersController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) {
            $scope.tab = 'members';
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.filterMembers = function (value) {
                if (!value) {
                    $scope.filteredMembers = $scope.members;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.members, function (member) {
                        if (member.userName.toLowerCase().indexOf(value.toLowerCase()) > -1 || member.userId.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(member);
                        }
                    });
                    $scope.filteredMembers = filtered;
                }
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        $scope.filteredMembers = members;
                        resolve(members);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgMembers', pageData, $scope, function () {
                PageLifecycle.setPageTitle('org-members', [$scope.org.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgNewMemberController = Apiman._module.controller("Apiman.OrgNewMemberController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'ApimanSvcs', 'Logger', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, ApimanSvcs, $log, $routeParams) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.selectedUsers = {};
            $scope.selectedRoles = [];
            $scope.queriedUsers = [];
            $scope.searchBoxValue = '';
            $scope.addMembers = function () {
                if ($scope.selectedRoles) {
                    $scope.addMembersButton.state = 'in-progress';
                    // Iterate over object like map (k:v)
                    jQuery.each($scope.selectedUsers, function (k, user) {
                        $log.debug('Adding user: {0}', user);
                        var grantRolesBean = {
                            userId: user.username,
                            roleIds: $scope.selectedRoles
                        };
                        OrgSvcs.save({ organizationId: $scope.organizationId, entityType: 'roles' }, grantRolesBean, function () {
                            $log.debug('Successfully Saved: {0}', grantRolesBean);
                            $scope.addMembersButton.state = 'complete';
                            PageLifecycle.redirectTo('/orgs/{0}/manage-members', params.org);
                        }, PageLifecycle.handleError);
                    });
                }
            };
            $scope.findUsers = function (searchBoxValue) {
                $scope.searchButton.state = 'in-progress';
                $scope.searchBoxValue = searchBoxValue;
                if (!searchBoxValue || searchBoxValue.length == 0) {
                    $scope.queriedUsers = [];
                    $scope.searchButton.state = 'complete';
                    return;
                }
                var queryBean = {
                    filters: [{
                            name: 'username',
                            value: '*' + searchBoxValue + '*',
                            operator: 'like'
                        }],
                    orderBy: {
                        name: 'fullName',
                        ascending: true
                    },
                    paging: {
                        page: 1,
                        pageSize: 50
                    }
                };
                $log.debug('Query: {0}', queryBean);
                ApimanSvcs.save({ entityType: 'users', secondaryType: 'search' }, queryBean, function (reply) {
                    $scope.searchButton.state = 'complete';
                    $log.debug('Reply: {0}', reply);
                    $scope.queriedUsers = reply.beans;
                }, function () {
                    $scope.searchButton.state = 'error';
                });
            };
            $scope.countObjectKeys = function (object) {
                return Object.keys(object).length;
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        $scope.filteredMembers = members;
                        resolve(members);
                    }, reject);
                }),
                roles: $q(function (resolve, reject) {
                    ApimanSvcs.query({ entityType: 'roles' }, function (adminRoles) {
                        $scope.filteredRoles = adminRoles;
                        resolve(adminRoles);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgNewMember', pageData, $scope, function () {
                PageLifecycle.setPageTitle('new-member');
            });
        }]);
    Apiman.OrgNewMemberController.directive('apimanUserEntry', ['Logger', function ($log) {
            return {
                scope: {
                    user: '=',
                    selectedUsers: '='
                },
                replace: true,
                templateUrl: 'plugins/api-manager/html/org/apiman-user-entry.html',
                link: function ($scope) {
                    $scope.isSelectedUser = false;
                    $scope.selectThisUser = function () {
                        $scope.isSelectedUser = !$scope.isSelectedUser;
                        // If selected user then add to map; if deselected remove it.
                        if ($scope.isSelectedUser) {
                            $scope.selectedUsers[$scope.user.username] = $scope.user;
                        }
                        else {
                            delete $scope.selectedUsers[$scope.user.username];
                        }
                        $log.debug("Selected {0}", $scope.user.username);
                        $log.debug("Global $scope.selectedUsers {0}", $scope.selectedUsers);
                    };
                }
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgPlansController = Apiman._module.controller("Apiman.OrgPlansController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) {
            $scope.tab = 'plans';
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.filterPlans = function (value) {
                if (!value) {
                    $scope.filteredPlans = $scope.plans;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.plans, function (plan) {
                        if (plan.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(plan);
                        }
                    });
                    $scope.filteredPlans = filtered;
                }
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        resolve(members);
                    }, reject);
                }),
                plans: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function (plans) {
                        $scope.filteredPlans = plans;
                        resolve(plans);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgPlans', pageData, $scope, function () {
                PageLifecycle.setPageTitle('org-plans', [$scope.org.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgServicesController = Apiman._module.controller("Apiman.OrgServicesController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) {
            $scope.tab = 'services';
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.filterServices = function (value) {
                if (!value) {
                    $scope.filteredServices = $scope.services;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.services, function (service) {
                        if (service.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(service);
                        }
                    });
                    $scope.filteredServices = filtered;
                }
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function (org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function (members) {
                        resolve(members);
                    }, reject);
                }),
                services: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services' }, function (services) {
                        $scope.filteredServices = services;
                        resolve(services);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgSvcs', pageData, $scope, function () {
                PageLifecycle.setPageTitle('org-services', [$scope.org.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgSidebarController = Apiman._module.controller("Apiman.OrgSidebarController", ['Logger', '$scope', 'OrgSvcs', function (Logger, $scope, OrgSvcs) {
            $scope.updateOrgDescription = function (updatedDescription) {
                var updateOrganizationBean = {
                    description: updatedDescription
                };
                OrgSvcs.update({ organizationId: $scope.organizationId }, updateOrganizationBean, function (success) {
                }, function (error) {
                    Logger.error("Unable to update org description: {0}", error);
                });
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.OrgRedirectController = Apiman._module.controller("Apiman.OrgRedirectController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) {
            PageLifecycle.loadPage('OrgRedirect', undefined, $scope, function () {
                var orgId = $routeParams.org;
                var tab = 'members';
                if (CurrentUser.hasPermission(orgId, 'planEdit')) {
                    tab = 'plans';
                }
                else if (CurrentUser.hasPermission(orgId, 'svcEdit')) {
                    tab = 'services';
                }
                else if (CurrentUser.hasPermission(orgId, 'appEdit')) {
                    tab = 'apps';
                }
                PageLifecycle.forwardTo('/orgs/{0}/{1}', orgId, tab);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.PlanActivityController = Apiman._module.controller("Apiman.PlanActivityController", ['$q', '$scope', '$location', 'OrgSvcs', 'AuditSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, AuditSvcs, Logger, PageLifecycle, PlanEntityLoader, $routeParams) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            var getNextPage = function (successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan, page: $scope.currentPage, count: 20 }, function (results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
            var pageData = PlanEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                auditEntries: $q(function (resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('PlanActivity', pageData, $scope, function () {
                PageLifecycle.setPageTitle('plan-activity', [$scope.plan.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.PlanOverviewController = Apiman._module.controller("Apiman.PlanOverviewController", ['$q', '$scope', '$location', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
        function ($q, $scope, $location, PageLifecycle, PlanEntityLoader, $routeParams) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            var pageData = PlanEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('PlanOverview', pageData, $scope, function () {
                PageLifecycle.setPageTitle('plan-overview', [$scope.plan.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.PlanPoliciesController = Apiman._module.controller("Apiman.PlanPoliciesController", ['$q', '$scope', '$location', 'OrgSvcs', 'ApimanSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', 'Dialogs', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, ApimanSvcs, Logger, PageLifecycle, PlanEntityLoader, Dialogs, $routeParams) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;
            var removePolicy = function (policy) {
                angular.forEach($scope.policies, function (p, index) {
                    if (policy === p) {
                        $scope.policies.splice(index, 1);
                    }
                });
            };
            $scope.removePolicy = function (policy) {
                Logger.info('Removing policy: {0}', policy);
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the plan?', function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function (reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };
            $scope.reorderPolicies = function (reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };
                OrgSvcs.save({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' }, policyChainBean, function () {
                    Logger.debug("Reordering POSTed successfully");
                }, function () {
                    Logger.debug("Reordering POST failed.");
                });
            };
            var pageData = PlanEntityLoader.getCommonData($scope, $location);
            angular.extend(pageData, {
                policies: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function (policies) {
                        resolve(policies);
                    }, reject);
                })
            });
            PageLifecycle.loadPage('PlanPolicies', pageData, $scope, function () {
                PageLifecycle.setPageTitle('plan-policies', [$scope.plan.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.PlanRedirectController = Apiman._module.controller("Apiman.PlanRedirectController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) {
            var orgId = $routeParams.org;
            var planId = $routeParams.plan;
            var pageData = {
                versions: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'plans', entityId: planId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('PlanRedirect', pageData, $scope, function () {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                }
                else {
                    PageLifecycle.forwardTo('/orgs/{0}/plans/{1}/{2}', orgId, planId, version);
                }
            });
        }]);
    Apiman.PlanEntityLoader = Apiman._module.factory('PlanEntityLoader', ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusService',
        function ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusService) {
            return {
                getCommonData: function ($scope, $location) {
                    var params = $routeParams;
                    return {
                        version: $q(function (resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version }, function (version) {
                                $scope.org = version.plan.organization;
                                $scope.plan = version.plan;
                                EntityStatusService.setEntityStatus(version.status);
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function (resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            };
        }]);
    Apiman.PlanEntityController = Apiman._module.controller("Apiman.PlanEntityController", ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusService',
        function ($q, $scope, $location, ActionSvcs, Logger, PageLifecycle, $routeParams, OrgSvcs, EntityStatusService) {
            var params = $routeParams;
            $scope.setEntityStatus = function (status) {
                EntityStatusService.setEntityStatus(status);
            };
            $scope.getEntityStatus = function () {
                return EntityStatusService.getEntityStatus();
            };
            $scope.setVersion = function (plan) {
                PageLifecycle.redirectTo('/orgs/{0}/plans/{1}/{2}', params.org, params.plan, plan.version);
            };
            $scope.lockPlan = function () {
                $scope.lockButton.state = 'in-progress';
                var lockAction = {
                    type: 'lockPlan',
                    entityId: params.plan,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(lockAction, function (reply) {
                    $scope.version.status = 'Locked';
                    $scope.lockButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            };
            $scope.updatePlanDescription = function (updatedDescription) {
                var updatePlanBean = {
                    description: updatedDescription
                };
                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'plans',
                    entityId: $scope.plan.id
                }, updatePlanBean, function (success) {
                }, function (error) {
                    Logger.error("Unable to update plan description:  {0}", error);
                });
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    var pages = [
        'overview', 'find-services', 'choose-plans', 'import-services'
    ];
    Apiman.ServiceRedirectController = Apiman._module.controller("Apiman.ImportServicesController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams', 'Logger', 'ApimanSvcs',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams, Logger, ApimanSvcs) {
            var params = $routeParams;
            $scope.params = params;
            $scope.importInfo = {
                services: [],
                isPublic: false,
                plans: []
            };
            var lockedPlans = [];
            var getSelectedPlans = function () {
                var selectedPlans = [];
                for (var i = 0; i < lockedPlans.length; i++) {
                    var plan = lockedPlans[i];
                    if (plan.checked) {
                        var selectedPlan = {};
                        selectedPlan.planId = plan.id;
                        selectedPlan.version = plan.selectedVersion;
                        selectedPlans.push(selectedPlan);
                    }
                }
                return selectedPlans;
            };
            var pageData = {
                org: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, resolve, reject);
                }),
                plans: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function (plans) {
                        //for each plan find the versions that are locked
                        var promises = [];
                        angular.forEach(plans, function (plan) {
                            promises.push($q(function (resolve, reject) {
                                OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: plan.id, versionsOrActivity: 'versions' }, function (planVersions) {
                                    //for each plan find the versions that are locked
                                    var lockedVersions = [];
                                    for (var j = 0; j < planVersions.length; j++) {
                                        var planVersion = planVersions[j];
                                        if (planVersion.status == "Locked") {
                                            lockedVersions.push(planVersion.version);
                                        }
                                    }
                                    // if we found locked plan versions then add them
                                    if (lockedVersions.length > 0) {
                                        plan.lockedVersions = lockedVersions;
                                        lockedPlans.push(plan);
                                    }
                                    resolve(planVersions);
                                }, reject);
                            }));
                        });
                        $q.all(promises).then(function () {
                            lockedPlans.sort(function (a, b) {
                                if (a.id.toLowerCase() < b.id.toLowerCase()) {
                                    return -1;
                                }
                                else if (b.id < a.id) {
                                    return 1;
                                }
                                else {
                                    return 0;
                                }
                            });
                            for (var i = 0; i < lockedPlans.length; i++) {
                                lockedPlans[i].selectedVersion = lockedPlans[i].lockedVersions[0];
                            }
                            resolve(lockedPlans);
                        });
                    }, reject);
                })
            };
            $scope.$watch('plans', function (newValue) {
                $scope.importInfo.plans = getSelectedPlans();
            }, true);
            var validatePage = function () {
                var valid = true;
                if ($scope.currentPage == 'find-services') {
                    Logger.log("Validating find-services page.");
                    valid = $scope.importInfo.services.length > 0;
                }
                if ($scope.currentPage == 'choose-plans') {
                    Logger.log("Validating choose-plans page.");
                    valid = $scope.importInfo.isPublic || $scope.importInfo.plans.length > 0;
                }
                $scope.currentPageValid = valid;
                Logger.log("Current Page Valid: " + valid);
            };
            $scope.$watch('importInfo', validatePage, true);
            $scope.$watch('currentPage', validatePage);
            $scope.currentPage = 'overview';
            $scope.currentPageIdx = 0;
            $scope.currentPageValid = true;
            $scope.services = [];
            $scope.importSources = [
                {
                    id: 'service-catalog',
                    icon: 'search-plus',
                    name: "Service Catalog",
                    disabled: false
                },
                {
                    id: 'wadl',
                    icon: 'file-text-o',
                    name: "WADL File",
                    disabled: true
                },
                {
                    id: 'swagger',
                    icon: 'ellipsis-h',
                    name: "Swagger File",
                    disabled: true
                }
            ];
            $scope.importFrom = 'service-catalog';
            $scope.searchServiceCatalog = function (searchText) {
                $scope.searchButton.state = 'in-progress';
                $scope.searchDisabled = true;
                var body = {};
                body.filters = [];
                body.filters.push({ "name": "name", "value": searchText, "operator": "like" });
                var searchStr = angular.toJson(body);
                Logger.log("Searching service catalogs: {0}", searchStr);
                ApimanSvcs.save({ entityType: 'search', secondaryType: 'serviceCatalogs' }, searchStr, function (reply) {
                    $scope.services = reply.beans;
                    Logger.log("Found {0} services.", reply.beans.length);
                    $scope.searchButton.state = 'complete';
                    $scope.searchDisabled = false;
                }, function (error) {
                    Logger.error(error);
                    // TODO do something interesting with the error
                    $scope.searchButton.state = 'complete';
                    $scope.searchDisabled = false;
                });
            };
            var importServices = function (services) {
                if (services.length == 0) {
                    // We're done - show the "Finish" button. :)
                    $scope.hideImportButton = true;
                    $scope.showFinishButton = true;
                    return;
                }
                var service = services[0];
                services.splice(0, 1);
                Logger.debug("Importing service {0}", service.name);
                Logger.debug("   # Remaining: {0}", services.length);
                service.status = 'importing';
                var newService = {
                    name: service.name,
                    description: service.description,
                    initialVersion: '1.0',
                    endpoint: service.endpoint,
                    endpointType: service.endpointType,
                    publicService: $scope.importInfo.isPublic,
                    plans: $scope.importInfo.plans,
                    definitionUrl: service.definitionUrl,
                    definitionType: service.definitionType
                };
                OrgSvcs.save({ organizationId: params.org, entityType: 'services' }, newService, function (reply) {
                    service.status = 'imported';
                    importServices(services);
                }, function (error) {
                    service.status = 'error';
                    service.error = error;
                    importServices(services);
                });
            };
            $scope.prevPage = function () {
                $scope.currentPageIdx = $scope.currentPageIdx - 1;
                $scope.currentPage = pages[$scope.currentPageIdx];
            };
            $scope.nextPage = function () {
                $scope.currentPageIdx = $scope.currentPageIdx + 1;
                $scope.currentPage = pages[$scope.currentPageIdx];
            };
            $scope.doImport = function () {
                $scope.disableBackButton = true;
                $scope.disableCancelButton = true;
                $scope.importButton.state = 'in-progress';
                var servicesToImport = $scope.importInfo.services.slice(0);
                servicesToImport.sort(function (s1, s2) {
                    if (s1.name.toLowerCase() < s2.name.toLowerCase()) {
                        return -1;
                    }
                    if (s1.name.toLowerCase() > s2.name.toLowerCase()) {
                        return 1;
                    }
                    return 0;
                });
                importServices(servicesToImport);
            };
            $scope.isServiceSelected = function (service) {
                var rval = false;
                angular.forEach($scope.importInfo.services, function (selectedService) {
                    if (service.name == selectedService.name && service.endpoint == selectedService.endpoint) {
                        rval = true;
                    }
                });
                return rval;
            };
            $scope.addService = function (service) {
                if (!$scope.isServiceSelected(service)) {
                    $scope.importInfo.services.push(service);
                }
            };
            $scope.removeService = function (service) {
                angular.forEach($scope.importInfo.services, function (s, idx) {
                    if (service == s) {
                        $scope.importInfo.services.splice(idx, 1);
                    }
                });
            };
            PageLifecycle.loadPage('ImportServices', pageData, $scope, function () {
                PageLifecycle.setPageTitle('import-services');
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceActivityController = Apiman._module.controller("Apiman.ServiceActivityController", ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'AuditSvcs', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, AuditSvcs, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var getNextPage = function (successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, page: $scope.currentPage, count: 20 }, function (results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                auditEntries: $q(function (resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('ServiceActivity', pageData, $scope, function () {
                PageLifecycle.setPageTitle('service-activity', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceContractsController = Apiman._module.controller("Apiman.ServiceContractsController", ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Logger, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'contracts';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var getNextPage = function (successHandler, errorHandler) {
                var maxCount = 10;
                $scope.currentPage = $scope.currentPage + 1;
                OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'contracts', page: $scope.currentPage, count: maxCount }, function (contracts) {
                    if (contracts.length == maxCount) {
                        $scope.hasMore = true;
                    }
                    else {
                        $scope.hasMore = false;
                    }
                    successHandler(contracts);
                }, errorHandler);
            };
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                contracts: $q(function (resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('ServiceContracts', pageData, $scope, function () {
                Logger.debug("::: is public: {0}", $scope.version.publicService);
                if ($scope.version.publicService) {
                    Logger.debug("::: num plans: {0}", $scope.version.plans.length);
                    if ($scope.version.plans.length == 0) {
                        $scope.isPublicOnly = true;
                    }
                }
                PageLifecycle.setPageTitle('service-contracts', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceDefController = Apiman._module.controller('Apiman.ServiceDefController', ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'ServiceDefinitionSvcs', 'Configuration', 'EntityStatusService',
        function ($q, $rootScope, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Logger, $routeParams, ServiceDefinitionSvcs, Configuration, EntityStatusService) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'def';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.typeOptions = [
                { "label": "No Service Definition", "value": "None" },
                { "label": "Swagger (JSON)", "value": "SwaggerJSON" },
                { "label": "Swagger (YAML)", "value": "SwaggerYAML" }
            ];
            var selectType = function (newType) {
                angular.forEach($scope.typeOptions, function (option) {
                    if (option.value == newType) {
                        $scope.selectedDefinitionType = option;
                    }
                });
            };
            selectType('None');
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            jQuery('#service-definition').height(100);
            jQuery('#service-definition').focus(function () {
                jQuery('#service-definition').height(450);
            });
            jQuery('#service-definition').blur(function () {
                jQuery('#service-definition').height(100);
            });
            $scope.$on('afterdrop', function (event, data) {
                var newValue = data.value;
                if (newValue) {
                    if (newValue.lastIndexOf('{', 0) === 0) {
                        $scope.$apply(function () {
                            selectType('SwaggerJSON');
                        });
                    }
                    if (newValue.lastIndexOf('swagger:', 0) === 0) {
                        $scope.$apply(function () {
                            selectType('SwaggerYAML');
                        });
                    }
                }
            });
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            var loadDefinition = function () {
                ServiceDefinitionSvcs.getServiceDefinition(params.org, params.service, params.version, function (definition) {
                    $scope.serviceDefinition = definition;
                    $scope.updatedServiceDefinition = definition;
                }, function (error) {
                    Logger.error("Error loading definition: {0}", error);
                });
            };
            var checkDirty = function () {
                if ($scope.version) {
                    var dirty = false;
                    if ($scope.serviceDefinition != $scope.updatedServiceDefinition) {
                        Logger.debug("**** dirty because of service def");
                        dirty = true;
                    }
                    if ($scope.selectedDefinitionType.value != $scope.definitionType) {
                        Logger.debug("**** dirty because of def type: {0} != {1}", $scope.selectedDefinitionType.value, $scope.definitionType);
                        dirty = true;
                    }
                    $rootScope.isDirty = dirty;
                }
            };
            $scope.$watch('updatedService', checkDirty, true);
            $scope.$watch('updatedServiceDefinition', function (newValue, oldValue) {
                if (!newValue) {
                    return;
                }
                checkDirty();
            });
            $scope.$watch('selectedDefinitionType', checkDirty, true);
            $scope.reset = function () {
                selectType($scope.definitionType);
                $scope.updatedServiceDefinition = $scope.serviceDefinition;
                $rootScope.isDirty = false;
            };
            $scope.saveService = function () {
                $scope.saveButton.state = 'in-progress';
                var update = OrgSvcs.updateJSON;
                if ($scope.selectedDefinitionType.value == 'SwaggerJSON') {
                    update = OrgSvcs.updateYAML;
                }
                ServiceDefinitionSvcs.updateServiceDefinition(params.org, params.service, params.version, $scope.updatedServiceDefinition, $scope.selectedDefinitionType.value, function (definition) {
                    Logger.debug("Updated the service definition!");
                    $scope.serviceDefinition = $scope.updatedServiceDefinition;
                    $rootScope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                }, function (error) {
                    Logger.error("Error updating definition: {0}", error);
                    $scope.saveButton.state = 'error';
                });
            };
            PageLifecycle.loadPage('ServiceDef', pageData, $scope, function () {
                $scope.definitionType = $scope.version.definitionType;
                if (!$scope.definitionType) {
                    $scope.definitionType = 'None';
                }
                if ($scope.version.definitionType && $scope.version.definitionType != 'None') {
                    loadDefinition();
                }
                else {
                    Logger.debug("Skipped loading service definition - None defined.");
                }
                $scope.reset();
                PageLifecycle.setPageTitle('service-def', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceEndpointController = Apiman._module.controller("Apiman.ServiceEndpointController", ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'Configuration', 'EntityStatusService',
        function ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, Configuration, EntityStatusService) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'endpoint';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                managedEndpoint: $q(function (resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'endpoint' }, resolve, reject);
                })
            });
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.success = function () {
                //console.log('Copied!');
            };
            $scope.fail = function (err) {
                //console.error('Error!', err);
            };
            PageLifecycle.loadPage('ServiceEndpoint', pageData, $scope, function () {
                PageLifecycle.setPageTitle('service-endpoint', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceImplController = Apiman._module.controller("Apiman.ServiceImplController", ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusService', 'Logger', 'Configuration',
        function ($q, $rootScope, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusService, Logger, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest", "soap"];
            $scope.updatedService = new Object();
            $scope.apiSecurity = new Object();
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    gateways: $q(function (resolve, reject) {
                        ApimanSvcs.query({ entityType: 'gateways' }, resolve, reject);
                    })
                });
            }
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            var epValue = function (endpointProperties, key) {
                if (endpointProperties && endpointProperties[key]) {
                    return endpointProperties[key];
                }
                else {
                    return null;
                }
            };
            var toApiSecurity = function (version) {
                var rval = {};
                rval.type = version.endpointProperties['authorization.type'];
                if (!rval.type) {
                    rval.type = 'none';
                }
                if (rval.type == 'mssl') {
                    rval.type = 'mtls';
                }
                if (rval.type == 'basic') {
                    rval.basic = {
                        username: epValue(version.endpointProperties, 'basic-auth.username'),
                        password: epValue(version.endpointProperties, 'basic-auth.password'),
                        confirmPassword: epValue(version.endpointProperties, 'basic-auth.password'),
                        requireSSL: 'true' === epValue(version.endpointProperties, 'basic-auth.requireSSL')
                    };
                }
                return rval;
            };
            var toEndpointProperties = function (apiSecurity) {
                var rval = {};
                if (apiSecurity.type == 'none') {
                    return rval;
                }
                rval['authorization.type'] = apiSecurity.type;
                if (apiSecurity.type == 'basic' && apiSecurity.basic) {
                    rval['basic-auth.username'] = apiSecurity.basic.username;
                    rval['basic-auth.password'] = apiSecurity.basic.password;
                    if (apiSecurity.basic.requireSSL) {
                        rval['basic-auth.requireSSL'] = 'true';
                    }
                    else {
                        rval['basic-auth.requireSSL'] = 'false';
                    }
                }
                return rval;
            };
            var checkValid = function () {
                var valid = true;
                if (!$scope.updatedService.endpointType) {
                    valid = false;
                }
                if ($scope.apiSecurity.type == 'basic' && $scope.apiSecurity.basic) {
                    if (!$scope.apiSecurity.basic.password) {
                        valid = false;
                    }
                    if ($scope.apiSecurity.basic.password != $scope.apiSecurity.basic.confirmPassword) {
                        valid = false;
                    }
                }
                else if ($scope.apiSecurity.type == 'basic' && !$scope.apiSecurity.basic) {
                    valid = false;
                }
                $scope.isValid = valid;
            };
            $scope.$watch('updatedService', function (newValue) {
                if ($scope.version) {
                    var dirty = false;
                    if (newValue.endpoint != $scope.version.endpoint || newValue.endpointType != $scope.version.endpointType) {
                        dirty = true;
                    }
                    if (newValue.gateways && newValue.gateways.length > 0) {
                        dirty = true;
                    }
                    if ($scope.version.endpointProperties && newValue.endpointProperties) {
                        if (!angular.equals($scope.version.endpointProperties, newValue.endpointProperties)) {
                            Logger.debug('Dirty due to EP:');
                            Logger.debug('    $scope.version:    {0}', $scope.version);
                            Logger.debug('    $scope.version.EP: {0}', $scope.version.endpointProperties);
                            Logger.debug('    newValue.EP:       {0}', newValue.endpointProperties);
                            dirty = true;
                        }
                    }
                    checkValid();
                    $rootScope.isDirty = dirty;
                }
            }, true);
            $scope.$watch('apiSecurity', function (newValue) {
                if (newValue) {
                    $scope.updatedService.endpointProperties = toEndpointProperties(newValue);
                    checkValid();
                }
            }, true);
            $scope.$watch('selectedGateway', function (newValue) {
                if (newValue) {
                    var alreadySet = false;
                    if ($scope.version.gateways && $scope.version.gateways.length > 0 && $scope.version.gateways[0].gatewayId == newValue.id) {
                        alreadySet = true;
                    }
                    if (!alreadySet) {
                        $scope.updatedService.gateways = [{ gatewayId: newValue.id }];
                    }
                    else {
                        delete $scope.updatedService.gateways;
                    }
                }
            });
            $scope.reset = function () {
                $scope.apiSecurity = toApiSecurity($scope.version);
                $scope.updatedService.endpoint = $scope.version.endpoint;
                $scope.updatedService.endpointType = $scope.version.endpointType;
                $scope.updatedService.endpointProperties = angular.copy($scope.version.endpointProperties);
                delete $scope.updatedService.gateways;
                if ($scope.version.gateways && $scope.version.gateways.length > 0) {
                    angular.forEach($scope.gateways, function (gateway) {
                        // TODO support multiple gateway assignments here
                        if (gateway.id == $scope.version.gateways[0].gatewayId) {
                            $scope.selectedGateway = gateway;
                        }
                    });
                }
                $rootScope.isDirty = false;
            };
            $scope.saveService = function () {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function (reply) {
                    $rootScope.isDirty = false;
                    $scope.autoGateway = false;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    EntityStatusService.setEntityStatus(reply.status);
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('ServiceImpl', pageData, $scope, function () {
                $scope.reset();
                PageLifecycle.setPageTitle('service-impl', [$scope.service.name]);
                // Automatically set the selected gateway if there's only one and the 
                // gateway is not already set.
                if (!$scope.version.gateways || $scope.version.gateways.length == 0) {
                    if ($scope.gateways && $scope.gateways.length == 1) {
                        $scope.selectedGateway = $scope.gateways[0];
                        $scope.autoGateway = true;
                    }
                }
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.NINETY_DAYS = 90 * 24 * 60 * 60 * 1000;
    Apiman.THIRTY_DAYS = 30 * 24 * 60 * 60 * 1000;
    Apiman.SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;
    Apiman.ONE_DAY = 1 * 24 * 60 * 60 * 1000;
    Apiman.ONE_HOUR = 1 * 60 * 60 * 1000;
    Apiman.ServiceMetricsController = Apiman._module.controller("Apiman.ServiceMetricsController", ['$q', 'Logger', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'MetricsSvcs', '$routeParams', '$timeout', 'Configuration',
        function ($q, Logger, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, MetricsSvcs, $routeParams, $timeout, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'metrics';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.metricsRange = '7days';
            $scope.metricsType = 'usage';
            var usageChart, usageByAppChart, usageByPlanChart;
            var responseTypeChart, responseTypeSuccessChart, responseTypeFailuresChart, responseTypeErrorsChart;
            var getTimeSeriesFormat = function () {
                var format = '%Y-%m-%d';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours' || $scope.metricsRange == 'hour') {
                    format = '%H:%M';
                }
                return format;
            };
            var renderUsageChart = function (data) {
                var xcol = [];
                xcol.push('x');
                var reqCol = ['# Requests'];
                angular.forEach(data.data, function (dataPoint) {
                    xcol.push(Date.parse(dataPoint.label));
                    reqCol.push(dataPoint.count);
                });
                if (xcol.length == 1) {
                    $scope.usageChartNoData = true;
                }
                else {
                    Logger.log("======= xcol: " + JSON.stringify(xcol));
                    Logger.log("======= reqs: " + JSON.stringify(reqCol));
                    usageChart = c3.generate({
                        size: {
                            height: 200
                        },
                        data: {
                            x: 'x',
                            columns: [
                                xcol,
                                reqCol
                            ],
                            types: {
                                '# Requests': 'bar'
                            }
                        },
                        bindto: '#usage-chart',
                        legend: {
                            hide: true
                        },
                        axis: {
                            x: {
                                type: 'timeseries',
                                tick: {
                                    format: getTimeSeriesFormat()
                                }
                            },
                            y: {
                                label: 'Total Requests'
                            }
                        }
                    });
                }
            };
            var renderAppUsageChart = function (data) {
                var columns = [];
                angular.forEach(data.data, function (numRequests, appName) {
                    columns.push([appName, numRequests]);
                });
                if (columns.length == 0) {
                    $scope.appUsageChartNoData = true;
                }
                else {
                    usageByAppChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            columns: columns,
                            type: 'pie'
                        },
                        bindto: '#app-usage-chart'
                    });
                }
            };
            var renderPlanUsageChart = function (data) {
                var columns = [];
                angular.forEach(data.data, function (numRequests, planName) {
                    columns.push([planName, numRequests]);
                });
                if (columns.length == 0) {
                    $scope.planUsageChartNoData = true;
                }
                else {
                    usageByPlanChart = c3.generate({
                        size: {
                            height: 250
                        },
                        data: {
                            columns: columns,
                            type: 'pie'
                        },
                        bindto: '#plan-usage-chart'
                    });
                }
            };
            var renderResponseTypeHistogramChart = function (data) {
                var xcol = [];
                xcol.push('x');
                var successCol = ['Success'];
                var failureCol = ['Fail'];
                var errorCol = ['Error'];
                angular.forEach(data.data, function (dataPoint) {
                    xcol.push(Date.parse(dataPoint.label));
                    successCol.push((dataPoint.total - dataPoint.failures - dataPoint.errors).toString());
                    failureCol.push(dataPoint.failures);
                    errorCol.push(dataPoint.errors);
                });
                if (xcol.length == 1) {
                    $scope.responseTypeChartNoData = true;
                }
                else {
                    Logger.log("======= xcol: " + JSON.stringify(xcol));
                    Logger.log("======= successCol: " + JSON.stringify(successCol));
                    Logger.log("======= failureCol: " + JSON.stringify(failureCol));
                    Logger.log("======= errorCol: " + JSON.stringify(errorCol));
                    responseTypeChart = c3.generate({
                        size: {
                            height: 200
                        },
                        data: {
                            x: 'x',
                            columns: [
                                xcol, successCol, failureCol, errorCol
                            ],
                            colors: {
                                'Success': '#71B56E',
                                'Fail': '#E37B4F',
                                'Error': '#E34F4F',
                            },
                            types: {
                                'Success': 'bar',
                                'Fail': 'bar',
                                'Error': 'bar',
                            },
                            groups: [
                                ['Success', 'Fail', 'Error']
                            ]
                        },
                        bindto: '#responseType-chart',
                        axis: {
                            x: {
                                type: 'timeseries',
                                tick: {
                                    format: getTimeSeriesFormat()
                                }
                            },
                            y: {
                                label: 'Responses'
                            }
                        }
                    });
                }
            };
            var renderResponseTypeSummaryCharts = function (data) {
                var total = data.total;
                var success = data.total - data.failures - data.errors;
                var failures = data.failures;
                var errors = data.errors;
                responseTypeSuccessChart = c3.generate({
                    size: {
                        height: 150
                    },
                    data: {
                        columns: [
                            ['data', success]
                        ],
                        colors: {
                            data: '#71B56E'
                        },
                        type: 'gauge'
                    },
                    gauge: {
                        max: total
                    },
                    bindto: '#responseType-chart-success'
                });
                responseTypeFailuresChart = c3.generate({
                    size: {
                        height: 150
                    },
                    data: {
                        columns: [
                            ['data', failures]
                        ],
                        colors: {
                            data: '#E37B4F'
                        },
                        type: 'gauge'
                    },
                    gauge: {
                        max: total
                    },
                    bindto: '#responseType-chart-failed'
                });
                responseTypeErrorsChart = c3.generate({
                    size: {
                        height: 150
                    },
                    data: {
                        columns: [
                            ['data', errors]
                        ],
                        colors: {
                            data: '#E34F4F'
                        },
                        type: 'gauge'
                    },
                    gauge: {
                        max: total
                    },
                    bindto: '#responseType-chart-error'
                });
            };
            var truncateToDay = function (date) {
                truncateToHour(date);
                date.setHours(0);
                return date;
            };
            var truncateToHour = function (date) {
                date.setMinutes(0);
                date.setSeconds(0);
                date.setMilliseconds(0);
                return date;
            };
            var getChartDateRange = function () {
                var from = new Date();
                var to = new Date();
                if ($scope.metricsRange == '90days') {
                    from = new Date(from.getTime() - Apiman.NINETY_DAYS);
                    truncateToDay(from);
                }
                else if ($scope.metricsRange == '30days') {
                    from = new Date(from.getTime() - Apiman.THIRTY_DAYS);
                    truncateToDay(from);
                }
                else if ($scope.metricsRange == '7days') {
                    from = new Date(from.getTime() - Apiman.SEVEN_DAYS);
                    truncateToDay(from);
                }
                else if ($scope.metricsRange == '24hours') {
                    from = new Date(from.getTime() - Apiman.ONE_DAY);
                    truncateToHour(from);
                }
                else if ($scope.metricsRange == 'hour') {
                    from = new Date(from.getTime() - Apiman.ONE_HOUR);
                }
                return {
                    from: from,
                    to: to
                };
            };
            // *******************************************************
            // Refresh the usage charts
            // *******************************************************
            var refreshUsageCharts = function () {
                $scope.usageChartLoading = true;
                $scope.appUsageChartLoading = true;
                $scope.planUsageChartLoading = true;
                var range = getChartDateRange();
                var from = range.from;
                var to = range.to;
                var interval = 'day';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours') {
                    interval = 'hour';
                }
                if ($scope.metricsRange == 'hour') {
                    interval = 'minute';
                }
                // Refresh the usage chart
                if (usageChart) {
                    usageChart.destroy();
                    usageChart = null;
                }
                MetricsSvcs.getUsage(params.org, params.service, params.version, interval, from, to, function (data) {
                    $scope.usageChartLoading = false;
                    renderUsageChart(data);
                }, function (error) {
                    Logger.error('Error loading usage chart data: {0}', JSON.stringify(error));
                    $scope.usageChartLoading = false;
                    $scope.usageChartNoData = true;
                });
                // Refresh the app usage chart
                if (usageByAppChart) {
                    usageByAppChart.destroy();
                    usageByAppChart = null;
                }
                MetricsSvcs.getUsagePerApp(params.org, params.service, params.version, from, to, function (data) {
                    $scope.appUsageChartLoading = false;
                    renderAppUsageChart(data);
                }, function (error) {
                    Logger.error('Error loading app usage chart data: {0}', JSON.stringify(error));
                    $scope.appUsageChartLoading = false;
                    $scope.appUsageChartNoData = true;
                });
                // Refresh the plan usage chart
                if (usageByPlanChart) {
                    usageByPlanChart.destroy();
                    usageByPlanChart = null;
                }
                MetricsSvcs.getUsagePerPlan(params.org, params.service, params.version, from, to, function (data) {
                    $scope.planUsageChartLoading = false;
                    renderPlanUsageChart(data);
                }, function (error) {
                    Logger.error('Error loading plan usage chart data: {0}', JSON.stringify(error));
                    $scope.planUsageChartLoading = false;
                    $scope.planUsageChartNoData = true;
                });
            };
            // *******************************************************
            // Refresh the response type charts
            // *******************************************************
            var refreshResponseTypeCharts = function () {
                $scope.responseTypeChartLoading = true;
                $scope.responseTypeSuccessChartLoading = true;
                $scope.responseTypeFailedChartLoading = true;
                $scope.responseTypeErrorChartLoading = true;
                var range = getChartDateRange();
                var from = range.from;
                var to = range.to;
                var interval = 'day';
                if ($scope.metricsRange == '7days' || $scope.metricsRange == '24hours') {
                    interval = 'hour';
                }
                if ($scope.metricsRange == 'hour') {
                    interval = 'minute';
                }
                // Refresh the response type chart
                if (responseTypeChart) {
                    responseTypeChart.destroy();
                    responseTypeChart = null;
                }
                MetricsSvcs.getResponseStats(params.org, params.service, params.version, interval, from, to, function (data) {
                    $scope.responseTypeChartLoading = false;
                    renderResponseTypeHistogramChart(data);
                }, function (error) {
                    Logger.error('Error loading response type stats histogram data: {0}', JSON.stringify(error));
                    $scope.responseTypeChartLoading = false;
                    $scope.responseTypeChartNoData = true;
                });
                // Refresh the success, failure, and error charts
                if (responseTypeSuccessChart) {
                    responseTypeSuccessChart.destroy();
                    responseTypeSuccessChart = null;
                }
                if (responseTypeFailuresChart) {
                    responseTypeFailuresChart.destroy();
                    responseTypeFailuresChart = null;
                }
                if (responseTypeErrorsChart) {
                    responseTypeErrorsChart.destroy();
                    responseTypeErrorsChart = null;
                }
                MetricsSvcs.getResponseStatsSummary(params.org, params.service, params.version, from, to, function (data) {
                    $scope.responseTypeSuccessChartLoading = false;
                    $scope.responseTypeFailedChartLoading = false;
                    $scope.responseTypeErrorChartLoading = false;
                    renderResponseTypeSummaryCharts(data);
                }, function (error) {
                    Logger.error('Error loading response type summary stats chart data: {0}', JSON.stringify(error));
                    $scope.responseTypeSuccessChartLoading = false;
                    $scope.responseTypeFailedChartLoading = false;
                    $scope.responseTypeErrorChartLoading = false;
                    $scope.responseTypeSuccessChartNoData = true;
                    $scope.responseTypeFailedChartNoData = true;
                    $scope.responseTypeErrorChartNoData = true;
                });
            };
            var refreshCharts = function () {
                Logger.debug("Refreshing charts!");
                if ($scope.metricsType == 'usage') {
                    refreshUsageCharts();
                }
                if ($scope.metricsType == 'responseType') {
                    refreshResponseTypeCharts();
                }
            };
            $scope.refreshCharts = refreshCharts;
            $scope.$watch('metricsRange', function (newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            $scope.$watch('metricsType', function (newValue, oldValue) {
                if (newValue && newValue != oldValue) {
                    refreshCharts();
                }
            });
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('ServiceMetrics', pageData, $scope, function () {
                PageLifecycle.setPageTitle('service-metrics', [$scope.service.name]);
                refreshCharts();
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceOverviewController = Apiman._module.controller("Apiman.ServiceOverviewController", ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', '$routeParams', 'Configuration',
        function ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, $routeParams, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'overview';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            PageLifecycle.loadPage('ServiceOverview', pageData, $scope, function () {
                PageLifecycle.setPageTitle('service-overview', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path='../apimanPlugin.ts'/>
/// <reference path='../services.ts'/>
var Apiman;
(function (Apiman) {
    Apiman.ServicePlansController = Apiman._module.controller('Apiman.ServicePlansController', ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusService', 'Configuration',
        function ($q, $rootScope, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusService, Configuration) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'plans';
            $scope.version = params.version;
            $scope.updatedService = new Object();
            $scope.showMetrics = Configuration.ui.metrics;
            var lockedPlans = [];
            var getSelectedPlans = function () {
                var selectedPlans = [];
                for (var i = 0; i < lockedPlans.length; i++) {
                    var plan = lockedPlans[i];
                    if (plan.checked) {
                        var selectedPlan = {};
                        selectedPlan.planId = plan.id;
                        selectedPlan.version = plan.selectedVersion;
                        selectedPlans.push(selectedPlan);
                    }
                }
                return selectedPlans;
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    plans: $q(function (resolve, reject) {
                        OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function (plans) {
                            //for each plan find the versions that are locked
                            var promises = [];
                            angular.forEach(plans, function (plan) {
                                promises.push($q(function (resolve, reject) {
                                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: plan.id, versionsOrActivity: 'versions' }, function (planVersions) {
                                        //for each plan find the versions that are locked
                                        var lockedVersions = [];
                                        for (var j = 0; j < planVersions.length; j++) {
                                            var planVersion = planVersions[j];
                                            if (planVersion.status == 'Locked') {
                                                lockedVersions.push(planVersion.version);
                                            }
                                        }
                                        // if we found locked plan versions then add them
                                        if (lockedVersions.length > 0) {
                                            plan.lockedVersions = lockedVersions;
                                            lockedPlans.push(plan);
                                        }
                                        resolve(planVersions);
                                    }, reject);
                                }));
                            });
                            $q.all(promises).then(function () {
                                lockedPlans.sort(function (a, b) {
                                    if (a.id.toLowerCase() < b.id.toLowerCase()) {
                                        return -1;
                                    }
                                    else if (b.id < a.id) {
                                        return 1;
                                    }
                                    else {
                                        return 0;
                                    }
                                });
                                resolve(lockedPlans);
                            });
                        }, reject);
                    })
                });
            }
            $scope.$watch('updatedService', function (newValue) {
                $rootScope.isDirty = false;
                if (newValue.publicService != $scope.version.publicService) {
                    $rootScope.isDirty = true;
                }
                if (newValue.plans && $scope.version.plans && newValue.plans.length != $scope.version.plans.length) {
                    $rootScope.isDirty = true;
                }
                else if (newValue.plans && $scope.version.plans) {
                    newValue.plans = _.sortBy(newValue.plans, 'planId');
                    $scope.version.plans = _.sortBy($scope.version.plans, 'planId');
                    for (var i = 0; i < newValue.plans.length; i++) {
                        var p1 = newValue.plans[i];
                        var p2 = $scope.version.plans[i];
                        if (p1.planId != p2.planId || p1.version != p2.version) {
                            $rootScope.isDirty = true;
                        }
                    }
                }
            }, true);
            $scope.$watch('plans', function (newValue) {
                $scope.updatedService.plans = getSelectedPlans();
            }, true);
            $scope.reset = function () {
                $scope.updatedService.publicService = $scope.version.publicService;
                for (var i = 0; i < lockedPlans.length; i++) {
                    lockedPlans[i].selectedVersion = lockedPlans[i].lockedVersions[0];
                    for (var j = 0; j < $scope.version.plans.length; j++) {
                        if (lockedPlans[i].id == $scope.version.plans[j].planId) {
                            lockedPlans[i].checked = true;
                            lockedPlans[i].selectedVersion = $scope.version.plans[j].version;
                            break;
                        }
                    }
                }
                $scope.updatedService.plans = getSelectedPlans();
                $rootScope.isDirty = false;
            };
            $scope.saveService = function () {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function (reply) {
                    $scope.version.publicService = $scope.updatedService.publicService;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    EntityStatusService.setEntityStatus(reply.status);
                    $rootScope.isDirty = false;
                }, PageLifecycle.handleError);
            };
            PageLifecycle.loadPage('ServicePlans', pageData, $scope, function () {
                $scope.reset();
                PageLifecycle.setPageTitle('service-plans', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServicePoliciesController = Apiman._module.controller('Apiman.ServicePoliciesController', ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams', 'Configuration', 'EntityStatusService',
        function ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Dialogs, $routeParams, Configuration, EntityStatusService) {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            var removePolicy = function (policy) {
                angular.forEach($scope.policies, function (p, index) {
                    if (policy === p) {
                        $scope.policies.splice(index, 1);
                    }
                });
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.removePolicy = function (policy) {
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the service?', function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function (reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };
            $scope.reorderPolicies = function (reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };
                OrgSvcs.save({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' }, policyChainBean, function () {
                    Logger.debug("Reordering POSTed successfully");
                }, function () {
                    Logger.debug("Reordering POST failed.");
                });
            };
            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                policies: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, resolve, reject);
                })
            });
            PageLifecycle.loadPage('ServicePolicies', pageData, $scope, function () {
                PageLifecycle.setPageTitle('service-policies', [$scope.service.name]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.ServiceRedirectController = Apiman._module.controller("Apiman.ServiceRedirectController", ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) {
            var orgId = $routeParams.org;
            var serviceId = $routeParams.service;
            var pageData = {
                versions: $q(function (resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'services', entityId: serviceId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('ServiceRedirect', pageData, $scope, function () {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                }
                else {
                    PageLifecycle.forwardTo('/orgs/{0}/services/{1}/{2}', orgId, serviceId, version);
                }
            });
        }]);
    Apiman.ServiceEntityLoader = Apiman._module.factory('ServiceEntityLoader', ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusService',
        function ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusService) {
            return {
                getCommonData: function ($scope, $location) {
                    var params = $routeParams;
                    return {
                        version: $q(function (resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, function (version) {
                                $scope.org = version.service.organization;
                                $scope.service = version.service;
                                $rootScope.mruService = version;
                                EntityStatusService.setEntityStatus(version.status);
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function (resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            };
        }]);
    Apiman.ServiceEntityController = Apiman._module.controller("Apiman.ServiceEntityController", ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusService', 'Configuration',
        function ($q, $scope, $location, ActionSvcs, Logger, Dialogs, PageLifecycle, $routeParams, OrgSvcs, EntityStatusService, Configuration) {
            var params = $routeParams;
            $scope.params = params;
            $scope.setEntityStatus = function (status) {
                EntityStatusService.setEntityStatus(status);
            };
            $scope.getEntityStatus = function () {
                return EntityStatusService.getEntityStatus();
            };
            $scope.isEntityDisabled = function () {
                var status = EntityStatusService.getEntityStatus();
                return (status !== 'Created' && status !== 'Ready');
            };
            $scope.setVersion = function (service) {
                PageLifecycle.redirectTo('/orgs/{0}/services/{1}/{2}', params.org, params.service, service.version);
            };
            $scope.publishService = function () {
                $scope.publishButton.state = 'in-progress';
                var publishAction = {
                    type: 'publishService',
                    entityId: params.service,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(publishAction, function (reply) {
                    $scope.version.status = 'Published';
                    $scope.publishButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            };
            $scope.retireService = function () {
                $scope.retireButton.state = 'in-progress';
                Dialogs.confirm('Confirm Retire Service', 'Do you really want to retire this service?  This action cannot be undone.', function () {
                    var retireAction = {
                        type: 'retireService',
                        entityId: params.service,
                        organizationId: params.org,
                        entityVersion: params.version
                    };
                    ActionSvcs.save(retireAction, function (reply) {
                        $scope.version.status = 'Retired';
                        $scope.retireButton.state = 'complete';
                        $scope.setEntityStatus($scope.version.status);
                    }, PageLifecycle.handleError);
                }, function () {
                    $scope.retireButton.state = 'complete';
                });
            };
            $scope.updateServiceDescription = function (updatedDescription) {
                var updateServiceBean = {
                    description: updatedDescription
                };
                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'services',
                    entityId: $scope.service.id,
                }, updateServiceBean, function (success) {
                    Logger.info("Updated sucessfully");
                }, function (error) {
                    Logger.error("Unable to update service description:  {0}", error);
                });
            };
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.UserActivityController = Apiman._module.controller("Apiman.UserActivityController", ['$q', '$scope', '$location', 'UserSvcs', 'UserAuditSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, $location, UserSvcs, UserAuditSvcs, PageLifecycle, $routeParams) {
            $scope.tab = 'activity';
            var getNextPage = function (successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                UserAuditSvcs.get({ user: $routeParams.user, page: $scope.currentPage, count: 20 }, function (results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
            var pageData = {
                user: $q(function (resolve, reject) {
                    UserSvcs.get({ user: $routeParams.user }, function (user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, reject);
                }),
                auditEntries: $q(function (resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            };
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('UserActivity', pageData, $scope, function () {
                PageLifecycle.setPageTitle('user-activity', [$scope.user.fullName]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.UserAppsController = Apiman._module.controller("Apiman.UserAppsController", ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        function ($q, $scope, $location, UserSvcs, PageLifecycle, Logger, $routeParams) {
            $scope.tab = 'applications';
            $scope.filterApps = function (value) {
                if (!value) {
                    $scope.filteredApps = $scope.applications;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.applications, function (app) {
                        if (app.name.toLowerCase().indexOf(value.toLowerCase()) > -1 || app.organizationName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(app);
                        }
                    });
                    $scope.filteredApps = filtered;
                }
            };
            var pageData = {
                user: $q(function (resolve, reject) {
                    UserSvcs.get({ user: $routeParams.user }, function (user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, reject);
                }),
                applications: $q(function (resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'applications' }, function (userApps) {
                        $scope.filteredApps = userApps;
                        resolve(userApps);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserApps', pageData, $scope, function () {
                PageLifecycle.setPageTitle('user-apps', [$scope.user.fullName]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.UserOrgsController = Apiman._module.controller("Apiman.UserOrgsController", ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, $location, UserSvcs, PageLifecycle, $routeParams) {
            $scope.tab = 'organizations';
            $scope.filterOrgs = function (value) {
                if (!value) {
                    $scope.filteredOrgs = $scope.organizations;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.organizations, function (org) {
                        if (org.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(org);
                        }
                    });
                    $scope.filteredOrgs = filtered;
                }
            };
            var pageData = {
                user: $q(function (resolve, reject) {
                    UserSvcs.get({ user: $routeParams.user }, function (user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, reject);
                }),
                organizations: $q(function (resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'organizations' }, function (userOrgs) {
                        $scope.filteredOrgs = userOrgs;
                        resolve(userOrgs);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserOrgs', pageData, $scope, function () {
                PageLifecycle.setPageTitle('user-orgs', [$scope.user.fullName]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.UserServicesController = Apiman._module.controller("Apiman.UserServicesController", ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', '$routeParams',
        function ($q, $scope, $location, UserSvcs, PageLifecycle, $routeParams) {
            $scope.tab = 'services';
            $scope.filterServices = function (value) {
                if (!value) {
                    $scope.filteredServices = $scope.services;
                }
                else {
                    var filtered = [];
                    angular.forEach($scope.services, function (svc) {
                        if (svc.name.toLowerCase().indexOf(value.toLowerCase()) > -1 || svc.organizationName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(svc);
                        }
                    });
                    $scope.filteredServices = filtered;
                }
            };
            var pageData = {
                user: $q(function (resolve, reject) {
                    UserSvcs.get({ user: $routeParams.user }, function (user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, reject);
                }),
                services: $q(function (resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'services' }, function (userServices) {
                        $scope.filteredServices = userServices;
                        resolve(userServices);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserServices', pageData, $scope, function () {
                PageLifecycle.setPageTitle('user-services', [$scope.user.fullName]);
            });
        }]);
})(Apiman || (Apiman = {}));

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
var Apiman;
(function (Apiman) {
    Apiman.UserRedirectController = Apiman._module.controller("Apiman.UserRedirectController", ['$q', '$scope', '$location', 'PageLifecycle', '$routeParams',
        function ($q, $scope, $location, PageLifecycle, $routeParams) {
            PageLifecycle.loadPage('UserRedirect', undefined, $scope, function () {
                PageLifecycle.forwardTo('/users/{0}/orgs', $routeParams.user);
            });
        }]);
})(Apiman || (Apiman = {}));
