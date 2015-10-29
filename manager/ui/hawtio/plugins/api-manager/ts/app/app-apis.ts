/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppApisController = _module.controller("Apiman.AppApisController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'Logger', 'OrgSvcs', '$rootScope', '$compile', '$timeout', '$routeParams', 'Configuration', 'ApiRegistrySvcs', 'DownloadSvcs', '$window',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, Logger, OrgSvcs, $rootScope, $compile, $timeout, $routeParams, Configuration, ApiRegistrySvcs, DownloadSvcs, $window) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'apis';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

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
                }, 50);

            };

            $scope.doExportAsJson = function() {
                $scope.exportAsJsonButton.state = 'in-progress';
            	Logger.info('Starting download of api registry (json).');
            	ApiRegistrySvcs.exportApiRegistryAsJson(params.org, params.app, params.version, function(download) {
            		Logger.info('Download: {0}', download);
            		var downloadLink = DownloadSvcs.getDownloadLink(download.id);
            		Logger.info('Downloading api registry from: {0}', downloadLink);
            		$window.open(downloadLink, "_self");
                    $scope.exportAsJsonButton.state = 'complete';
            	}, PageLifecycle.handleError);
            };

            $scope.doExportAsXml = function() {
                $scope.exportAsXmlButton.state = 'in-progress';
            	Logger.info('Starting download of api registry (xml).');
            	ApiRegistrySvcs.exportApiRegistryAsXml(params.org, params.app, params.version, function(download) {
            		Logger.info('Download: {0}', download);
            		var downloadLink = DownloadSvcs.getDownloadLink(download.id);
            		Logger.info('Downloading api registry from: {0}', downloadLink);
            		$window.open(downloadLink, "_self");
                    $scope.exportAsXmlButton.state = 'complete';
            	}, PageLifecycle.handleError);
            };
            
            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                apiRegistry: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'apiregistry', policyId: 'json' }, resolve, reject);
                })
            });
            PageLifecycle.loadPage('AppApis', pageData, $scope, function() {
                Logger.info("API Registry: {0}", $scope.apiRegistry);
                PageLifecycle.setPageTitle('app-apis', [ $scope.app.name ]);
            });
        }])

    
    _module.directive('apimanApiModal',
        ['Logger', function(Logger) {
            return {
                templateUrl: 'plugins/api-manager/html/app/apiModal.html',
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
