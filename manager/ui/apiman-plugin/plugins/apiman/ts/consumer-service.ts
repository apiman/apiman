/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var ConsumerSvcController = _module.controller("Apiman.ConsumerSvcController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.hasVersions = false;
            var promise = $q.all({
                service: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service }, function(service) {
                        resolve(service);
                    }, function(error) {
                        reject(error);
                    });
                }),
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions' }, function(versions) {
                        resolve(versions);
                        if (versions.length > 0) {
                            $scope.hasVersions = true;
                            if (params.version != null) {
                                for (var i = 0; i < versions.length; i++) {
                                    if (params.version == versions[i].version) {
                                        $scope.setVersion(versions[i]);
                                        break;
                                    }
                                }
                            } else {
                                $scope.setVersion(versions[0]);
                            }
                        }
                    }, function(error) {
                        reject(error);
                    });
                }),
            });
            
            $scope.setVersion = function(service) {
                $scope.selectedServiceVersion = service;
                OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: service.id, versionsOrActivity: 'versions', version: service.version, policiesOrActivity: 'plans' }, function(reply) {
                    for (var i=0; i<reply.length; i++) {
                        var plan = reply[i];
                        OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: service.id, versionsOrActivity: 'versions', version: service.version, policiesOrActivity: 'plans', policyId: plan.planId, policyChain : 'policyChain' }, function(policyReply) {
                            var policies = policyReply;
                            reply[i].policies = policies;  
                        }, function(error) {
                            if (error.status == 409) {
                                $location.path('apiman/error-409.html');
                            } else {
                                alert("ERROR=" + error.status + " " + error.statusText);
                            }
                        });
                    }
                    $scope.plans = reply;
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
                
                $location.path(Apiman.pluginName + "/consumer-service.html").search('org', params.org).search('service', params.service).search('version', service.version);
            };
            
            PageLifecycle.loadPage('ConsumerSvc', promise, $scope);
        }])

}
