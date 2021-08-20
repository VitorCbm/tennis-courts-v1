package com.tenniscourts.guests;

import javax.validation.Valid;

import com.tenniscourts.config.BaseRestController;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
@RequestMapping("/guests")
public class GuestsController extends BaseRestController {
    
    private final GuestService guestService;    

    @PostMapping("/createGuest")
    public ResponseEntity<Void> addGuest(@Valid @RequestBody CreateGuestsDTO createUpdateGuestRequestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.addGuest(createUpdateGuestRequestDTO).getId())).build();
    }

    @PutMapping("/updateGuest")
    public ResponseEntity<GuestsDTO> update(@Valid @RequestBody CreateGuestsDTO createGuestsDTO) {
    	return ResponseEntity.ok(guestService.update(createGuestsDTO));
    }

    @GetMapping(value = "findByName")
    public ResponseEntity<Page<GuestsDTO>> findByName(@RequestParam(value="page", defaultValue="0") Integer page,     
			@RequestParam(value="size", defaultValue="10") Integer size,
			@RequestParam(value="orderBy", defaultValue="id") String orderBy,
			@RequestParam(value="ordination", defaultValue="DESC") String ordination,
			@RequestParam(value="name", defaultValue="") String name) {
        return ResponseEntity.ok(guestService.findByName(name, page, size, orderBy, ordination));
    }

    @GetMapping(value = "listAll")
    public ResponseEntity<Page<GuestsDTO>> listAll(@RequestParam(value="page", defaultValue="0") Integer page, 
			@RequestParam(value="linesPerPage", defaultValue="10") Integer size,
			@RequestParam(value="orderBy", defaultValue="id") String orderBy,
			@RequestParam(value="direction", defaultValue="DESC") String ordination) {
        return ResponseEntity.ok(guestService.findAll(page, size, orderBy, ordination));
    }

    @DeleteMapping(value = "{guestId}")
    public ResponseEntity<Void> delete(Long guestId) {
    	guestService.delete(guestId);
        return ResponseEntity.ok().build();
    } 

}
