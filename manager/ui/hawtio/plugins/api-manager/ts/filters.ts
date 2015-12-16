/// <reference path="../../includes.ts"/>
module ApimanFilters {

    export var _module = angular.module('ApimanFilters', ['ApimanConfiguration']);

    export var checklist = _module.filter('urlEncode', function() {
        return encodeURIComponent;
    });

    // Checklist Filter:
    // This filter processes and formats data returned by the /status endpoint.
    export var checklist = _module.filter('checklist', function() {
        // Map the IDs to the Tab Names
        // This is used when checking if the user is on the same tab
        // defined on the checklist.
        var tabMapping = {
            'endpoint': 'impl',
            'gateways': 'impl',
            'plans': 'plans',
            'policies': 'policies'
        };

        return function(input) {
            var filtered = [];

            angular.forEach(input, function(item: ChecklistConfig) {
                if(item && item.id) {
                    // Add path for tabs with more than one path (ie: endpoint and gateways)
                    item.path = tabMapping[item.id];

                    // Check if item is completed (no need to check for this)
                    var status = (item.done === true) ? 'complete' : 'incomplete';

                    // Determine status icon class
                    item.iconClass = (item.done === true) ? 'fa fa-check-circle-o' : 'fa fa-circle-o';

                    // Determine table row class (ie: active complete, inactive incomplete)
                    item.rowClass = status;

                    filtered.push(item);
                }
            });

            return filtered;
        };
    });

}

