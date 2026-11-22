
package com.railbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbook.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

}
