/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminRolesController = _module.controller("Apiman.AdminExportImportController",
        ['$q', '$scope', 'SystemSvcs', 'DownloadSvcs', 'PageLifecycle', 'Logger', '$window',
        ($q, $scope, SystemSvcs, DownloadSvcs, PageLifecycle, Logger, $window) => {
            $scope.tab = 'export';
            var pageData = {
            };
            
            $scope.doExport = function() {
                $scope.exportButton.state = 'in-progress';
            	Logger.info('Starting download of export data.');
            	SystemSvcs.exportAsJson(function(download) {
            		Logger.info('Download: {0}', download);
            		var downloadLink = DownloadSvcs.getDownloadLink(download.id);
            		Logger.info('Downloading export data from: {0}', downloadLink);
            		$window.open(downloadLink, "_self");
            	}, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('AdminExport', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-export');
            });
    }])

}
