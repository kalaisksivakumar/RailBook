
package com.railbook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbook.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByPnr(String pnr);

    boolean existsByTrain_IdAndSeatNumberAndStatus(
            Long trainId, String seatNumber, String status);

    boolean existsByTrain_IdAndTravelClassAndSeatNumberAndStatus(
            Long trainId, String travelClass, String seatNumber, String status);

    List<Booking> findAllByTrain_IdAndStatus(
            Long trainId, String status);

    List<Booking> findAllByTrain_IdAndTravelClassAndStatus(
            Long trainId, String travelClass, String status);

    List<Booking> findByPnr(String pnr);

    List<Booking> findAllByPnr(String pnr);

    List<Booking> findAllByOrderByBookingDateDesc();

    long countByTrain_IdAndTravelClassAndStatus(
            Long trainId, String travelClass, String status);

    List<Booking> findAllByTrain_IdAndTravelClassAndStatusOrderByIdAsc(
            Long trainId, String travelClass, String status);
}