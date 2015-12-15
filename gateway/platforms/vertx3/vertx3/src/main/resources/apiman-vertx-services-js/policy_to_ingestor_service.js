/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module apiman-vertx-services-js/policy_to_ingestor_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JPolicyToIngestorService = io.apiman.gateway.platforms.vertx3.services.PolicyToIngestorService;
var VertxApiResponse = io.apiman.gateway.platforms.vertx3.io.VertxApiResponse;
var VertxPolicyFailure = io.apiman.gateway.platforms.vertx3.io.VertxPolicyFailure;

/**
 Anything that goes from an ingestor (e.g. HTTP) to policy verticle

 HTTP <=> PolicyVerticle

 @class
*/
var PolicyToIngestorService = function(j_val) {

  var j_policyToIngestorService = j_val;
  var that = this;

  /**
   Write a apiRequest (head)

   @public
   @param apiResponse {Object} the service request 
   @param readyHandler {function} when ready to transmit body 
   */
  this.head = function(apiResponse, readyHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && typeof __args[1] === 'function') {
      j_policyToIngestorService["head(io.apiman.gateway.platforms.vertx3.io.VertxApiResponse,io.vertx.core.Handler)"](apiResponse != null ? new VertxApiResponse(new JsonObject(JSON.stringify(apiResponse))) : null, function(ar) {
      if (ar.succeeded()) {
        readyHandler(null, null);
      } else {
        readyHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Write a body chunks

   @public
   @param chunk {string} the body chunk 
   */
  this.write = function(chunk) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'string') {
      j_policyToIngestorService["write(java.lang.String)"](chunk);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Finished all actions.

   @public
   @param resultHandler {function} the result handler 
   */
  this.end = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_policyToIngestorService["end(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Indicate failure

   @public
   @param policyFailure {Object} the policy failure 
   */
  this.policyFailure = function(policyFailure) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object') {
      j_policyToIngestorService["policyFailure(io.apiman.gateway.platforms.vertx3.io.VertxPolicyFailure)"](policyFailure != null ? new VertxPolicyFailure(new JsonObject(JSON.stringify(policyFailure))) : null);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_policyToIngestorService;
};

/**

 @memberof module:apiman-vertx-services-js/policy_to_ingestor_service
 @param vertx {Vertx} 
 @return {PolicyToIngestorService}
 */
PolicyToIngestorService.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JPolicyToIngestorService["create(io.vertx.core.Vertx)"](vertx._jdel), PolicyToIngestorService);
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:apiman-vertx-services-js/policy_to_ingestor_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {PolicyToIngestorService}
 */
PolicyToIngestorService.createProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JPolicyToIngestorService["createProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), PolicyToIngestorService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = PolicyToIngestorService;