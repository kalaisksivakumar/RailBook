
package com.railbook.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railbook.entity.Train;
import com.railbook.repository.BookingRepository;
import com.railbook.repository.TrainRepository;

@RestController
@RequestMapping("/api/admin/trains")
public class AdminController {

    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;

    public AdminController(
            TrainRepository trainRepository,
            BookingRepository bookingRepository) {

        this.trainRepository = trainRepository;
        this.bookingRepository = bookingRepository;
    }

    // ADD TRAIN
    @PostMapping
    public ResponseEntity<?> addTrain(@RequestBody Train train) {

        try {
            validateTrain(train);

            boolean duplicate = trainRepository.findAll()
                    .stream()
                    .anyMatch(t -> t.getTrainNumber()
                            .equalsIgnoreCase(train.getTrainNumber()));

            if (duplicate) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Train number already exists!");
            }

            train.setAvailableSeats(train.getTotalSeats());

            Train savedTrain = trainRepository.save(train);

            return ResponseEntity.ok(savedTrain);

        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    // VIEW ALL TRAINS
    @GetMapping
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    // UPDATE TRAIN
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrain(
            @PathVariable Long id,
            @RequestBody Train updatedTrain) {

        Train existingTrain = trainRepository.findById(id)
                .orElse(null);

        if (existingTrain == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Train not found.");
        }

        try {
            validateTrain(updatedTrain);
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest()
                    .body(error.getMessage());
        }

        // Check duplicate train number, excluding this train
        boolean duplicate = trainRepository.findAll()
                .stream()
                .anyMatch(t ->
                        !t.getId().equals(id) &&
                        t.getTrainNumber().equalsIgnoreCase(
                                updatedTrain.getTrainNumber()));

        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Another train already uses this train number.");
        }

        int bookedSeats = existingTrain.getTotalSeats()
                - existingTrain.getAvailableSeats();

        if (updatedTrain.getTotalSeats() < bookedSeats) {
            return ResponseEntity.badRequest()
                    .body("Total seats cannot be less than already booked seats.");
        }

        existingTrain.setTrainNumber(updatedTrain.getTrainNumber());
        existingTrain.setTrainName(updatedTrain.getTrainName());
        existingTrain.setSource(updatedTrain.getSource());
        existingTrain.setDestination(updatedTrain.getDestination());
        existingTrain.setTotalSeats(updatedTrain.getTotalSeats());

        // Preserve booked seats
        existingTrain.setAvailableSeats(
                updatedTrain.getTotalSeats() - bookedSeats
        );

        // Save departure and arrival times
        existingTrain.setDepartureTime(updatedTrain.getDepartureTime());
        existingTrain.setArrivalTime(updatedTrain.getArrivalTime());

        Train savedTrain = trainRepository.save(existingTrain);

        return ResponseEntity.ok(savedTrain);
    }

    // DELETE TRAIN
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrain(@PathVariable Long id) {

        Train train = trainRepository.findById(id)
                .orElse(null);

        if (train == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Train not found.");
        }

        if (bookingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete this train because bookings already exist.");
        }

        trainRepository.delete(train);

        return ResponseEntity.ok("Train deleted successfully!");
    }

    // VALIDATE TRAIN
    private void validateTrain(Train train) {

        if (train.getTrainNumber() == null ||
            train.getTrainNumber().isBlank() ||
            train.getTrainName() == null ||
            train.getTrainName().isBlank() ||
            train.getSource() == null ||
            train.getSource().isBlank() ||
            train.getDestination() == null ||
            train.getDestination().isBlank() ||
            train.getTotalSeats() <= 0 ||
            train.getSource().equalsIgnoreCase(
                    train.getDestination())) {

            throw new IllegalArgumentException(
                    "Please enter valid train details."
            );
        }
    }
}
