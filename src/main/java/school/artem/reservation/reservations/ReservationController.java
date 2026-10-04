package school.artem.reservation.reservations;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import school.artem.reservation.reservations.availability.CreateReservationRequest;
import school.artem.reservation.reservations.availability.UpdateReservationRequest;

import java.util.List;

@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get reservation by ID",
            description = "Returns a reservation by ID if the authenticated user is the owner or an admin"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to access this reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation or user not found")
    })
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id,
            Authentication authentication
    ){

        String username = authentication.getName();

        log.info("Called getReservationById: id={}", id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id, username));
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get user's reservations",
            description = "Returns all reservations belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations returned successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<Reservation>> getAllReservations(
            Authentication authentication
    ){

        String username = authentication.getName();

        log.info("Called getAllReservations: username={}", username);

        return ResponseEntity.ok(reservationService.getAllReservations(username));
    }

    @GetMapping
    @Operation(
            summary = "Search reservations",
            description = "Searches reservations. Available only to admins"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations returned successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to access all reservations"),
    })
    public ResponseEntity<List<Reservation>> searchAllByFilter(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ){
        log.info("Called searchAllByFilter");

        var filter = new ReservationSearchFilter(
                roomId,
                userId,
                pageSize,
                pageNumber
        );

        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));
    }

    @PostMapping
    @Operation(
            summary = "Create reservation",
            description = "Creates a new reservation for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid CreateReservationRequest reservationToCreate,
            Authentication authentication
    ) {
        String username = authentication.getName();

        log.info("Creating reservation for user {}", username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate, username));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update reservation",
            description = "Updates a reservation if the authenticated user is the owner or an admin"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data or reservation cannot be updated"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to update this reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation or user not found")
    })
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateReservationRequest updateReservationRequest,
            Authentication authentication
    ) {

        String username = authentication.getName();

        log.info("User {} updating reservation id={}", username, id);
        var updated = reservationService.updateReservation(id, updateReservationRequest, username);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel reservation",
            description = "Cancels a reservation. An admin can also cancel an approved reservation"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Reservation cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to cancel this reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation or user not found")
    })
    public ResponseEntity<String> cancelReservation(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {

        String username = authentication.getName();

        log.info("Called cancelReservation id={}", id);

        reservationService.cancelReservation(id, username);

        return ResponseEntity.ok("Reservation cancelled successfully");
    }

    @PostMapping("/{id}/approve")
    @Operation(
            summary = "Approve reservation",
            description = "Approves a pending reservation. Available only to admins"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation approved successfully"),
            @ApiResponse(responseCode = "400", description = "Reservation cannot be approved or has an availability conflict"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to approve reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
    })
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);

        var reservation = reservationService.approveReservation(id);

        return ResponseEntity.ok(reservation);
    }
}