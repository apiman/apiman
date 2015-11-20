/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppApisController = _module.controller('Apiman.AppApisController',
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'Logger', 'OrgSvcs', '$rootScope', '$compile', '$timeout', '$routeParams', 'Configuration', 'ApiRegistrySvcs', 'DownloadSvcs', '$window', '$uibModal', '$log',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, Logger, OrgSvcs, $rootScope, $compile, $timeout, $routeParams, Configuration, ApiRegistrySvcs, DownloadSvcs, $window, $uibModal, $log) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'apis';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            $scope.toggle = function(api) {
                api.expanded = !api.expanded;
            };

            $scope.animationsEnabled = true;

            $scope.howToInvoke = function (size, api) {
                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'invokeModal.html',
                    controller: 'AppApisModalCtrl',
                    size: size,
                    resolve: {
                        api: function() {
                            return api;
                        }
                    }
                });

                modalInstance.result.then(function (selectedItem) {
                    $scope.selected = selectedItem;
                }, function () {
                    $log.info('Modal dismissed at: ' + new Date());
                });
            };

            $scope.toggleAnimation = function () {
                $scope.animationsEnabled = !$scope.animationsEnabled;
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


            // Copy-to-Clipboard

            // Called if copy-to-clipboard functionality was successful
            $scope.copySuccess = function () {
                //console.log('Copied!');
            };

            // Called if copy-to-clipboard functionality was unsuccessful
            $scope.copyFail = function (err) {
                //console.error('Error!', err);
            };


            // Tooltip

            $scope.tooltipTxt = 'Copy to clipboard';

            // Called on clicking the button the tooltip is attached to
            $scope.tooltipChange = function() {
                $scope.tooltipTxt = 'Copied!';
            };

            // Call when the mouse leaves the button the tooltip is attached to
            $scope.tooltipReset = function() {
                setTimeout(function() {
                    $scope.tooltipTxt = 'Copy to clipboard';
                }, 100);
            };

            PageLifecycle.loadPage('AppApis', 'appView', pageData, $scope, function() {
                Logger.info("API Registry: {0}", $scope.apiRegistry);
                PageLifecycle.setPageTitle('app-apis', [ $scope.app.name ]);
            });
        }]);


    export var AppApisModalCtrl = _module.controller('AppApisModalCtrl', function ($scope,
                                                                                   $uibModalInstance,
                                                                                   api) {

        $scope.api = api;

        $scope.asQueryParam = api.httpEndpoint + '?apikey=' + api.apiKey;

        if (api.httpEndpoint.indexOf('?') > -1) {
            $scope.asQueryParam = api.httpEndpoint + '&apikey=' + api.apiKey;
        }

        $scope.asRequestHeader = 'X-API-Key: ' + api.apiKey;


        $scope.ok = function () {
            $uibModalInstance.close();
        };


        // Tooltip

        $scope.tooltipTxt = 'Copy to clipboard';

        // Called on clicking the button the tooltip is attached to
        $scope.tooltipChange = function() {
            $scope.tooltipTxt = 'Copied!';
        };

        // Call when the mouse leaves the button the tooltip is attached to
        $scope.tooltipReset = function() {
            setTimeout(function() {
                $scope.tooltipTxt = 'Copy to clipboard';
            }, 100);
        };


        // Copy-to-Clipboard

        // Called if copy-to-clipboard functionality was successful
        $scope.copySuccess = function () {
            //console.log('Copied!');
        };

        // Called if copy-to-clipboard functionality was unsuccessful
        $scope.copyFail = function (err) {
            //console.error('Error!', err);
        };
    });


}
