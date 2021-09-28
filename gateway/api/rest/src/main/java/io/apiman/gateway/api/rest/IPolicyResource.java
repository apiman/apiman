// package io.apiman.gateway.api.rest;
//
// import io.apiman.gateway.engine.beans.IPolicyProbeRequest;
// import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
//
// import javax.annotation.Resource;
// import javax.ws.rs.Consumes;
// import javax.ws.rs.GET;
// import javax.ws.rs.Produces;
// import javax.ws.rs.container.AsyncResponse;
// import javax.ws.rs.container.Suspended;
// import javax.ws.rs.core.MediaType;
// import javax.ws.rs.core.Request;
//
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiImplicitParam;
// import io.swagger.annotations.ApiImplicitParams;
// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiResponse;
// import io.swagger.annotations.ApiResponses;
//
// /**
//  * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
//  */
// @Api(tags = "policy")
// @Resource(name = "policy", description = "Probe information relating to the state of policies")
// public interface IPolicyResource {
//
//     @GET
//     @ApiOperation(value = "Probe the state of a policy")
//     @ApiResponses(
//          @ApiResponse(code = 200, message = "OK", response = IPolicyProbeResponse.class)
//     )
//     @ApiImplicitParams(
//          @ApiImplicitParam(name = "request", dataTypeClass = IPolicyProbeRequest.class)
//     )
//     @Consumes(MediaType.APPLICATION_JSON)
//     @Produces(MediaType.APPLICATION_JSON)
//     void probePolicy(final Request request, @Suspended final AsyncResponse response);
// }
