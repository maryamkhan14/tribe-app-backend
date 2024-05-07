package com.savvato.tribeapp.controllers.annotations.controllers.ConnectAPIController;

import com.savvato.tribeapp.controllers.annotations.responses.BadRequest;
import com.savvato.tribeapp.controllers.annotations.responses.Success;
import com.savvato.tribeapp.dto.ConnectOutgoingMessageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

/**
 * Documentation for getting a QR code string
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "Get connections for a user",
        description = "Provided a user ID, get Connections for a user. ")
@Success(description = "A list of the user's connection", array = @ArraySchema(schema = @Schema(implementation = ConnectOutgoingMessageDTO.class), minItems = 2))
@BadRequest(description = "Connections could not be retrieved", noContent = true)
public @interface GetConnections {
}
