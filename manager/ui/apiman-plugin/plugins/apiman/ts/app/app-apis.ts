/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppApisController = _module.controller("Apiman.AppApisController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'Logger', 'OrgSvcs', '$rootScope', '$compile', '$timeout', '$routeParams',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, Logger, OrgSvcs, $rootScope, $compile, $timeout, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'apis';
            $scope.version = params.version;
            $scope.downloadAsJson = pluginName + '/proxies/apiman/organizations/' + params.org + '/applications/' + params.app + '/versions/' + params.version + '/apiregistry/json';
            $scope.downloadAsXml = pluginName + '/proxies/apiman/organizations/' + params.org + '/applications/' + params.app + '/versions/' + params.version + '/apiregistry/xml';

            $scope.toggle = function(api) {
                api.expanded = !api.expanded;
            };

            $scope.howToInvoke = function(api) {
                var modalScope = $rootScope.$new(true);
                modalScope.asQueryParam = api.httpEndpoint + '?apikey=' + api.apiKey;
                if (api.httpEndpoint.indexOf('?') > -1) {
                    modalScope.asQueryParam = api.httpEndpoint + '&apikey=' + api.apiKey;
                }
                modalScope.asRequestHeader = 'X-API-Key: ' + api.apiKey;
                $('body').append($compile('<apiman-api-modal></apiman-api-modal>')(modalScope));
                $timeout(function() {
                    $('#apiModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                }, 1);

            };
            
            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                apiRegistry: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'apiregistry', policyId: 'json' }, resolve, reject);
                })
            });
            var promise = $q.all(dataLoad);
            PageLifecycle.loadPage('AppApis', promise, $scope, function() {
                Logger.info("API Registry: {0}", $scope.apiRegistry);
                PageLifecycle.setPageTitle('app-apis', [ $scope.app.name ]);
            });
        }])

    
    _module.directive('apimanApiModal',
        ['Logger', function(Logger) {
            return {
                templateUrl: 'plugins/apiman/html/app/apiModal.html',
                replace: true,
                restrict: 'E',
                link: function(scope, element, attrs) {
                    $(element).on('hidden.bs.modal', function() {
                        $(element).remove();
                    });
                }
            };
        }]);

}
