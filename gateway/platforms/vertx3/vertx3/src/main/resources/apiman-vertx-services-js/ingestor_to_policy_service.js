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

/** @module apiman-vertx-services-js/ingestor_to_policy_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JIngestorToPolicyService = io.apiman.gateway.platforms.vertx3.services.IngestorToPolicyService;
var VertxServiceRequest = io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest;

/**
 From gateway to a policy verticle

 @class
*/
var IngestorToPolicyService = function(j_val) {

  var j_ingestorToPolicyService = j_val;
  var that = this;

  /**
   Write a serviceRequest (head)

   @public
   @param serviceRequest {Object} the service request 
   @param readyHandler {function} when ready to transmit body 
   */
  this.head = function(serviceRequest, readyHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && typeof __args[1] === 'function') {
      j_ingestorToPolicyService["head(io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest,io.vertx.core.Handler)"](serviceRequest != null ? new VertxServiceRequest(new JsonObject(JSON.stringify(serviceRequest))) : null, function(ar) {
      if (ar.succeeded()) {
        readyHandler(ar.result(), null);
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
      j_ingestorToPolicyService["write(java.lang.String)"](chunk);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Finished transmitting body chunks

   @public
   @param resultHandler {function} the result handler 
   */
  this.end = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_ingestorToPolicyService["end(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_ingestorToPolicyService;
};

/**

 @memberof module:apiman-vertx-services-js/ingestor_to_policy_service
 @param vertx {Vertx} 
 @return {IngestorToPolicyService}
 */
IngestorToPolicyService.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JIngestorToPolicyService["create(io.vertx.core.Vertx)"](vertx._jdel), IngestorToPolicyService);
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:apiman-vertx-services-js/ingestor_to_policy_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {IngestorToPolicyService}
 */
IngestorToPolicyService.createProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(JIngestorToPolicyService["createProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address), IngestorToPolicyService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = IngestorToPolicyService;