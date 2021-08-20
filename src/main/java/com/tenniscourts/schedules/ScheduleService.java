package com.tenniscourts.schedules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtDTO;
import com.tenniscourts.tenniscourts.TennisCourtRepository;

@Service
public class ScheduleService {

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    TennisCourtRepository tennisCourtRepository;

    public ScheduleDTO addSchedule(Long tennisCourtId, LocalDateTime startDate) {
        
        Optional<TennisCourt> tennisCourtOpt = tennisCourtRepository.findById(tennisCourtId);
        
        if(!tennisCourtOpt.isPresent()){
            throw new EntityNotFoundException("No tennis court available to create schedule.");
        }  
        
        Optional<Schedule> scheduleOpt =  scheduleRepository.findByTennisCourt_IdAndStartDateTime(tennisCourtId, startDate);
        if(scheduleOpt.isPresent()){
            throw new IllegalArgumentException("There is already a schedule to this court and this hour");
        }

        Schedule schedule = new Schedule();
        schedule.setStartDateTime(startDate);
        schedule.setEndDateTime(startDate.plusHours(1L));
        schedule.setTennisCourt(tennisCourtOpt.get());
        scheduleRepository.save(schedule);
        return mapScheduleToDTO(schedule, tennisCourtOpt.get());        
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<Schedule> schedules = scheduleRepository.findByStartDateTime_GreaterThanEqualAndEndDateTime_LessThanEqual(startDate, endDate);
        List<ScheduleDTO> scheduleDTOList = new ArrayList<>(); 
        for(Schedule schedule: schedules){
            ScheduleDTO scheduleDTO = new ScheduleDTO();
            scheduleDTO = mapScheduleToDTO(schedule, null);
            scheduleDTOList.add(scheduleDTO);
        } 
        return scheduleDTOList;
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
        
        if(!scheduleOpt.isPresent()){
            throw new EntityNotFoundException("No Schedule found related to this id");
        }
        return mapScheduleToDTO(scheduleOpt.get(), null);
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        List<Schedule> schedules = scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId);
        List<ScheduleDTO> scheduleDTOList = new ArrayList<>(); 
        for(Schedule schedule: schedules){
            ScheduleDTO scheduleDTO = new ScheduleDTO();
            scheduleDTO = mapScheduleToDTO(schedule, null);
            scheduleDTOList.add(scheduleDTO);
        } 
        return scheduleDTOList;
    }

    public ScheduleDTO mapScheduleToDTO(Schedule schedule, TennisCourt tennisCourt){

        ScheduleDTO scheduleDTO = new ScheduleDTO();
        TennisCourtDTO tennisCourtDTO = new TennisCourtDTO();
        tennisCourtDTO.setName(tennisCourt != null ? tennisCourtDTO.getName() : null);
        scheduleDTO.setEndDateTime(schedule.getEndDateTime());
        scheduleDTO.setStartDateTime(schedule.getStartDateTime());
        scheduleDTO.setTennisCourt(tennisCourtDTO);

        return scheduleDTO;

    }

}
