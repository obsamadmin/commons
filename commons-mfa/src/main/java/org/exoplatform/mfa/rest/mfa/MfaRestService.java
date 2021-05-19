package org.exoplatform.mfa.rest.mfa;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.mfa.api.MfaService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/mfa")
@Api(value = "/mfa", description = "Manages MFA features")
public class MfaRestService implements ResourceContainer {

  @Path("/settings")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get Activated MFA System", httpMethod = "GET", response = Response.class, produces = MediaType.APPLICATION_JSON)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response getMfaSystem() {
    MfaService mfaService = CommonsUtils.getService(MfaService.class);
    JSONObject result = new JSONObject();
    try {
      result.put("mfaSystem", mfaService.getMfaSystem());
      return Response.ok().entity(result.toString()).build();
    } catch (JSONException e) {
      return Response.serverError().build();

    }

  }
}