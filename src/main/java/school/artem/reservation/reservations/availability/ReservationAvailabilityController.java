package school.artem.reservation.reservations.availability;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    private final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvailabilityService service;

    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    @Operation(
            summary = "Check reservation availability",
            description = "Checks whether a room is available for the requested date range"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability checked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @RequestBody @Valid CheckAvailabilityRequest request
    ) {
        log.info("Called checkAvailability: request={}", request);

        boolean isAvailable = service.isReservationAvailable(
                request.roomId(), request.startDate(), request.endDate()
        );

        var message = isAvailable
                ? "Room available to reservation"
                : "Room not available to reservation";

        var status = isAvailable
                ? AvailabilityStatus.AVAILABLE
                : AvailabilityStatus.RESERVED;

        return ResponseEntity.status(200).body(new CheckAvailabilityResponse(message, status));
    }
}
