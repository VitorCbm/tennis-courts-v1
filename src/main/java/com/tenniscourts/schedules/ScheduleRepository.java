package com.tenniscourts.schedules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByTennisCourt_IdOrderByStartDateTime(Long id);

    Optional<Schedule> findByTennisCourt_IdAndStartDateTime(Long id, LocalDateTime date);  
    
    List<Schedule> findByStartDateTime_GreaterThanEqualAndEndDateTime_LessThanEqual(LocalDateTime startDate, LocalDateTime endDate);

}