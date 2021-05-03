/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

/// <reference path="apimanPlugin.ts"/>

module Apiman {

    export var SwaggerUIContractService = _module.service('SwaggerUIContractService', function () {
        var key;

        var setXAPIKey = function (XAPIKey) {
            key = XAPIKey;
        };

        var getXAPIKey = function () {
            return key;
        };

        var removeXAPIKey = function () {
            key = "";
        };

        //return functions
        return {
            setXAPIKey: setXAPIKey,
            getXAPIKey: getXAPIKey,
            removeXAPIKey: removeXAPIKey
        }
    });
}
