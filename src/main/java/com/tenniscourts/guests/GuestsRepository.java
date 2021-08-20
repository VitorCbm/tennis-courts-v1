package com.tenniscourts.guests;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GuestsRepository extends JpaRepository<Guest, Long> {
   
    List<Guest> findAllByNameContaining(String name);           

}
