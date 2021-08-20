package com.tenniscourts.reservations;


import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestsRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleRepository;
import com.tenniscourts.schedules.ScheduleService;
import com.tenniscourts.tenniscourts.TennisCourtRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    GuestsRepository guestRepository;

    ScheduleService scheduleService;        

    @Autowired
    TennisCourtRepository tennisCourtRepository;

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        
        // Could not start server properly due to an error on all Mappers. Had to map manually.//
        Optional<Schedule> schedule = scheduleRepository.findById(createReservationRequestDTO.getScheduleId()); 
        return createReservationDTO(schedule, createReservationRequestDTO.getGuestId());
    }

    public List<ReservationDTO> bookReservationForMultipleTennisCourts(Long guestId, List<Long> tennisCourtIds, LocalDateTime localDate) {
        List<ReservationDTO> reservationDTOList = new ArrayList<>();
        for(Long tennisCourtId : tennisCourtIds){
      
            Optional<Schedule> scheduleOpt =  scheduleRepository.findByTennisCourt_IdAndStartDateTime(tennisCourtId, localDate);

            if(!scheduleOpt.isPresent()){
                System.out.println("No Schedule for the related date and or tennis court.");
                continue;
            }

            Long scheduleId = scheduleOpt.get().getId();

            List<Reservation> previousReservations = reservationRepository.findBySchedule_Id(scheduleId);

            if(previousReservations.size() > 0){
                ReservationStatus status = previousReservations.get(previousReservations.size() - 1).getReservationStatus();
                if(status != ReservationStatus.CANCELLED && status != ReservationStatus.RESCHEDULED){
                    continue;
                }                     
            }
           
            ReservationDTO reservDTO = createReservationDTO(scheduleOpt, guestId);           
            reservationDTOList.add(reservDTO);
            
            
        }

        return reservationDTOList;
    }

    private ReservationDTO createReservationDTO(Optional<Schedule> schedule, Long guestId){

        Reservation reservation = new Reservation();

        Long scheduleId = schedule.get().getId();

        List<Reservation> previousReservations = reservationRepository.findBySchedule_Id(scheduleId);

        if(previousReservations.size() > 0){
            ReservationStatus status = previousReservations.get(previousReservations.size() - 1).getReservationStatus();
            if(status != ReservationStatus.CANCELLED && status != ReservationStatus.RESCHEDULED){
                throw new IllegalArgumentException("There is already a reservation for this schedule.");
            }                     
        }

        if(schedule.isPresent()){
            reservation.setSchedule(schedule.get());
        }
        else{
            throw new EntityNotFoundException("Could not find schedule.");
        }
        Optional<Guest> guest = guestRepository.findById(guestId);
        if(guest.isPresent()){
            reservation.setGuest(guest.get());
        }
        else{
            throw new EntityNotFoundException("Could not find guest.");
        }

        reservation.setValue(BigDecimal.valueOf(10L));
        reservation.setRefundValue(reservation.getValue());

        reservation = reservationRepository.save(reservation);
        
        ReservationDTO reservDTO = ReservationDTO.builder()
        .id(reservation.getId())
        .guestId(guestId)
        .scheduledId(reservation.getSchedule().getId())
        .build();

        reservDTO.setReservationStatus(ReservationStatus.READY_TO_PLAY.toString());

        return reservDTO;

    }
    
    public ReservationDTO findReservation(Long reservationId) {

        Optional<Reservation> reservationFound = reservationRepository.findById(reservationId);
        if(!reservationFound.isPresent()){
            throw new EntityNotFoundException("Reservation not found.");
        }
        Reservation reservationDb = reservationFound.get();
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO = mapReservationToDTO(reservationDb);
        return reservationDTO;
        
    }

    public ReservationDTO cancelReservationDTO(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if(reservationOpt.isPresent()){
            Reservation reservationDb = reservationOpt.get();
            this.validateCancellation(reservationDb);
            BigDecimal refundValue = getRefundValue(reservationDb);
            reservationDb = this.updateReservation(reservationDb, refundValue, ReservationStatus.CANCELLED);                     
            return mapReservationToDTO(reservationDb);
        }
        else{
            throw new EntityNotFoundException("Reservation not found");
        }  
    }

    public Reservation cancelReservation(Long reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if(reservationOpt.isPresent()){
            Reservation reservationDb = reservationOpt.get();
            this.validateCancellation(reservationDb);
            BigDecimal refundValue = getRefundValue(reservationDb);
            return this.updateReservation(reservationDb, refundValue, ReservationStatus.CANCELLED);
        }
        else{
            throw new EntityNotFoundException("Reservation not found");
        }  
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    private Schedule validateScheduleSlot(LocalDateTime startDate, Long tennisCourtId){
        Optional<Schedule> scheduleOpt =  scheduleRepository.findByTennisCourt_IdAndStartDateTime(tennisCourtId, startDate);
        if(!scheduleOpt.isPresent()){
            throw new IllegalArgumentException("The tennis court provided and the start date are not available on our schedules.");
        }
        return scheduleOpt.get();
    }

    public Page<ReservationDTO> findAll() {        
    	List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationDTO> reservationDTOList = new ArrayList<>();
        for(Reservation reservation : reservations){
            ReservationDTO reservationDTO = new ReservationDTO();
            reservationDTO = mapReservationToDTO(reservation);
            reservationDTOList.add(reservationDTO);
        }
    	return new PageImpl<>(reservationDTOList);
    }

    public BigDecimal getRefundValue(Reservation reservation) {
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());

        Double fee = 0.0;
        Double reservationTax = reservation.getValue().doubleValue();

        if (hours >= 24) {
            fee = 1.0;            
        } else if (hours >= 12) {
            fee = 0.25;        	
        } else if (hours >= 2) {
            fee = 0.50;        	
        }  else if (hours >= 0) {
            fee = 0.75;        	
        }

        return calculateRefund(reservationTax, fee);
    }

    private BigDecimal calculateRefund(Double value, Double fee){
        
        BigDecimal refundValue = new BigDecimal(value * fee );
        return refundValue;
                
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, LocalDateTime startDate, Long tennisCourtId) {
        Reservation previousReservation = cancelReservation(previousReservationId);
        Long scheduleId = previousReservation.getSchedule().getId();
        Boolean scheduleAlreadyExists = false;
        Schedule scheduleFound = new Schedule();
        
        if (scheduleId.equals(previousReservation.getSchedule().getId())) {
            scheduleAlreadyExists = true;
            scheduleFound = this.validateScheduleSlot(startDate, tennisCourtId);              
        }

        previousReservation.setReservationStatus(ReservationStatus.RESCHEDULED);        

        ReservationDTO reservationBooked = bookReservation(CreateReservationRequestDTO.builder().guestId(previousReservation.getGuest().getId())
        .scheduleId(scheduleAlreadyExists ? scheduleFound.getId() : scheduleId)
        .build());

        reservationRepository.saveAndFlush(previousReservation);

        return reservationBooked;
        
    }

    private ReservationDTO mapReservationToDTO(Reservation reservation){

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setGuestId(reservation.getGuest().getId());
        reservationDTO.setRefundValue(reservation.getRefundValue());
        reservationDTO.setReservationStatus(reservation.getReservationStatus().toString());
        reservationDTO.setScheduledId(reservation.getSchedule().getId());
        reservationDTO.setValue(reservation.getValue());

        return reservationDTO;

    }
}
