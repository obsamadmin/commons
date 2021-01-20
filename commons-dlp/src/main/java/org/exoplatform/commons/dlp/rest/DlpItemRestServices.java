package org.exoplatform.commons.dlp.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.portal.rest.CollectionEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

import java.util.List;

@Path("/dlp/items")
@Api(value = "/dlp/items", description = "Manages Dlp positive items") // NOSONAR
public class DlpItemRestServices implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(DlpItemRestServices.class);

    private DlpPositiveItemService dlpPositiveItemService;

    public DlpItemRestServices(DlpPositiveItemService dlpPositiveItemService) {
        this.dlpPositiveItemService = dlpPositiveItemService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("administrators")
    @ApiOperation(value = "Retrieves the list of dlp positive items", httpMethod = "GET", response = Response.class, produces = "application/json",
            notes = "Return list of dlp positive items in json format")
    @ApiResponses(
            value = {@ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
                    @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
                    @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"),}
    )
    public Response getDlpPositiveItems(@ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                        @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit) {
        try {
            List<DlpPositiveItem> dlpPositiveItems = dlpPositiveItemService.getDlpPositivesItems(offset, limit);
            Long size = dlpPositiveItemService.getDlpPositiveItemsCount();
            CollectionEntity<DlpPositiveItem> collectionDlpPositiveItem = new CollectionEntity<>(dlpPositiveItems, offset, limit, size.intValue());
            return Response.ok(collectionDlpPositiveItem).build();
        } catch (Exception e) {
            LOG.error("Unknown error occurred while getting dlp positive items", e);
            return Response.serverError().build();
        }
    }
}
