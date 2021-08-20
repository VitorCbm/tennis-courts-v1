package com.tenniscourts.reservations;

import java.time.LocalDateTime;
import java.util.List;

import com.tenniscourts.config.BaseRestController;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class ReservationController extends BaseRestController {

    @Autowired
    ReservationService reservationService;
    
    @PostMapping(value = "bookReservation")
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
            return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    @PostMapping(value = "bookReservations")
    public ResponseEntity<Void> bookReservationForMultipleCourts(@RequestParam(value = "guestId", required = true) Long id,
                @RequestParam(value = "tennisCourtId", required = true) List<Long> ids,
                @RequestParam(value = "localDate", required = true) 
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date) {                    
        reservationService.bookReservationForMultipleTennisCourts(id, ids, date);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "findReservation")
    public ResponseEntity<ReservationDTO> findReservation(Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }

    @GetMapping(value = "findPreviousReservations")
    public ResponseEntity<Page<ReservationDTO>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @DeleteMapping(value = "cancelReservation")
    public ResponseEntity<ReservationDTO> cancelReservation(@RequestParam(value = "reservationId", required = true) Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservationDTO(reservationId));
    }

    @PutMapping(value = "updateReservation")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@RequestParam(value = "reservationId", required = true)Long reservationId, 
        @RequestParam(value = "startDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
        @RequestParam(value = "tennisCourtId", required = true) Long tennisCourtId){
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, startDate, tennisCourtId));
    }
}
