/// <reference path="../../includes.ts"/>
module ApimanDialogs {

    export var _module = angular.module("ApimanDialogs", ["ApimanLogger", "ApimanServices"]);

    export var Dialogs = _module.factory('Dialogs', 
        ['Logger', '$compile', '$rootScope', '$timeout', 'ApimanSvcs', 'OrgSvcs',
        function(Logger, $compile, $rootScope, $timeout, ApimanSvcs, OrgSvcs) {
            return {
                // A standard confirmation dialog
                /////////////////////////////////
                confirm: function(title, message, yesCallback, noCallback) {
                    var modalScope = $rootScope.$new(true);
                    
                    modalScope.onYes = function() {
                        if (yesCallback) { yesCallback(); }
                    };
                    
                    modalScope.onNo = function() {
                        if (noCallback) { noCallback(); }
                        noCallback = null;
                    };
                    
                    modalScope.title = title;
                    modalScope.message = message;
                    
                    $('body').append($compile('<apiman-confirm-modal modal-title="{{ title }}">{{ message }}</apiman-confirm-modal>')(modalScope));

                    $timeout(function() {
                        $('#confirmModal').on('hidden.bs.modal', function () {
                            if (noCallback) { $rootScope.$apply(noCallback); }
                            noCallback = null;
                        });
                        
                        $('#confirmModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                    }, 50);
                },
                
                // A simple "Select a Service" dialog (allows selecting a single service + version
                //////////////////////////////////////////////////////////////////////////////////
                selectService: function(title, handler, publishedOnly) {
                    var modalScope = $rootScope.$new(true);

                    modalScope.selectedService = undefined;
                    modalScope.selectedServiceVersion = undefined;
                    modalScope.title = title;

                    $('body').append($compile('<apiman-select-service-modal modal-title="{{ title }}"></apiman-select-service-modal>')(modalScope));

                    $timeout(function() {
                        $('#selectServiceModal')['modal']({'keyboard': true, 'backdrop': 'static'});
                        $('#selectServiceModal').on('shown.bs.modal', function () {
                            $('#selectServiceModal .input-search').focus();
                        });
                    }, 50);

                    modalScope.search = function() {
                        modalScope.selectedService = undefined;

                        if (!modalScope.searchText) {
                            modalScope.criteria = undefined;
                            modalScope.services = undefined;
                        } else {
                            modalScope.searchButton.state = 'in-progress';
                            
                            var body:any = {};
                            body.filters = [];

                            body.filters.push({
                                'name' : 'name',
                                'value' : '%' + modalScope.searchText + '%',
                                'operator' : 'like'
                            });

                            var searchStr = angular.toJson(body);

                            Logger.log('Searching for services: {0}', modalScope.searchText);

                            ApimanSvcs.save({ entityType: 'search', secondaryType: 'services' }, searchStr, function(reply) {
                                if (reply.beans.length > 0) {
                                    modalScope.services = reply.beans;
                                } else {
                                    modalScope.services = undefined;
                                }

                                modalScope.criteria = modalScope.searchText;

                                Logger.log('Found {0} services.', reply.beans.length);

                                modalScope.searchButton.state = 'complete';
                            }, function(error) {
                                Logger.error(error);
                                
                                // TODO do something interesting with the error
                                modalScope.services = undefined;
                                modalScope.criteria = modalScope.searchText;
                                modalScope.searchButton.state = 'error';
                            });
                        }
                    };

                    modalScope.onServiceSelected = function(service) {
                        if (modalScope.selectedService) {
                            modalScope.selectedService.selected = false;
                        }

                        modalScope.selectedService = service;
                        service.selected = true;
                        modalScope.selectedServiceVersion = undefined;

                        OrgSvcs.query({ organizationId: service.organizationId, entityType: 'services', entityId: service.id, versionsOrActivity: 'versions' }, function(versions) {
                            if (publishedOnly) {
                                var validVersions = [];

                                angular.forEach(versions, function(version) {
                                    if (version.status == 'Published') {
                                        validVersions.push(version);
                                    }
                                });

                                modalScope.serviceVersions = validVersions;
                            } else {
                                modalScope.serviceVersions = versions;
                            }

                            if (modalScope.serviceVersions.length > 0) {
                                modalScope.selectedServiceVersion = modalScope.serviceVersions[0];
                            }
                        }, function(error) {
                            modalScope.serviceVersions = [];
                            modalScope.selectedServiceVersion = undefined;
                        });
                    };
                    
                    modalScope.onOK = function() {
                        if (handler) {
                            handler(modalScope.selectedServiceVersion);
                        }
                    };
                }
            };
        }]);

}