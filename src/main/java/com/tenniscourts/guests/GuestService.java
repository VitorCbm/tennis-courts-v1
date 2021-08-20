package com.tenniscourts.guests;

import java.util.ArrayList;
import java.util.List;

import com.tenniscourts.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;


@Service
public class GuestService {

    @Autowired
    GuestsRepository guestsRepository;

    public Guest findById(Long guestId) {
   	 return guestsRepository.findById(guestId).orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found on database.");
        });
   }

    public GuestsDTO addGuest(CreateGuestsDTO guestDTO) {
        Guest guest =  guestsRepository.saveAndFlush(Guest.builder().name(guestDTO.getName()).build());
        return mapGuestToDTO(guest);       
    }
    
    public GuestsDTO update(CreateGuestsDTO createOrUpdateGuestDTO) {
    	Guest guest = findById(createOrUpdateGuestDTO.getId());
    	guest.setName(createOrUpdateGuestDTO.getName());
        guestsRepository.saveAndFlush(guest);
    	return mapGuestToDTO(guest);
    }

    public void delete(Long id){
        findById(id);
        guestsRepository.deleteById(id);
    }

    public Page<GuestsDTO> findByName(String guestName, Integer page, Integer linesPerPage, String orderBy, String direction) {            	
        List<Guest> guests = guestsRepository.findAllByNameContaining(guestName);
        List<GuestsDTO> guestsDTOList = new ArrayList<>();
        for(Guest guest : guests){
            GuestsDTO guestDTO = new GuestsDTO();
            guestDTO = mapGuestToDTO(guest);
            guestsDTOList.add(guestDTO);        }
        
    	return new PageImpl<>(guestsDTOList);
   }

   public Page<GuestsDTO> findAll(Integer page, Integer linesPerPage, String orderBy, String direction) {
    List<Guest> guests = guestsRepository.findAll();
    List<GuestsDTO> guestsDTOList = new ArrayList<>();
        for(Guest guest : guests){
            GuestsDTO guestDTO = new GuestsDTO();
            guestDTO = mapGuestToDTO(guest);
            guestsDTOList.add(guestDTO);        }
        
    	return new PageImpl<>(guestsDTOList);
   }

   private GuestsDTO mapGuestToDTO(Guest guest){

    GuestsDTO guestsDTO = new GuestsDTO();

    guestsDTO.setName(guest.getName());

    return guestsDTO;

   }


    
}
