
package com.railbook.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railbook.entity.Booking;
import com.railbook.entity.Passenger;
import com.railbook.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // EXISTING SINGLE-PASSENGER BOOKING
    @PostMapping
    public ResponseEntity<?> bookTicket(
            @RequestBody BookingRequest request) {

        try {
            Booking booking = bookingService.bookTicket(
                    request.getTrainId(),
                    request.getPassengerName(),
                    request.getAge(),
                    request.getGender(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getSeatNumber()
            );

            return ResponseEntity.ok(convertToResponse(booking));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // CLASS-WISE GROUP BOOKING
    @PostMapping("/group")
    public ResponseEntity<?> bookGroupTickets(
            @RequestBody GroupBookingRequest request) {

        try {
            if (request.getPassengers() == null) {
                throw new RuntimeException(
                        "Passenger details are required");
            }

            List<Passenger> passengers =
                    request.getPassengers().stream()
                    .map(p -> {
                        Passenger passenger = new Passenger();
                        passenger.setPassengerName(p.getPassengerName());
                        passenger.setAge(p.getAge());
                        passenger.setGender(p.getGender());
                        passenger.setEmail(p.getEmail());
                        passenger.setPhone(p.getPhone());
                        return passenger;
                    })
                    .collect(Collectors.toList());

            List<Booking> bookings =
                    bookingService.bookGroupTickets(
                            request.getTrainId(),
                            passengers,
                            request.getSeatNumbers(),
                            request.getTravelClass()
                    );

            List<BookingResponse> responses =
                    bookings.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET ONE BOOKING USING PNR
    @GetMapping("/{pnr}")
    public ResponseEntity<?> getBooking(@PathVariable String pnr) {

        try {
            Booking booking = bookingService.getBookingByPnr(pnr);
            return ResponseEntity.ok(convertToResponse(booking));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET ALL PASSENGERS USING COMMON PNR
    @GetMapping("/{pnr}/group")
    public ResponseEntity<?> getGroupBooking(
            @PathVariable String pnr) {

        try {
            List<BookingResponse> responses =
                    bookingService.getBookingsByPnr(pnr)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // CANCEL BOOKING OR ENTIRE GROUP USING PNR
    @PostMapping("/{pnr}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable String pnr) {

        try {
            bookingService.cancelBooking(pnr);

            List<BookingResponse> responses =
                    bookingService.getBookingsByPnr(pnr)
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET ALL BOOKINGS
    @GetMapping
    public List<BookingResponse> getAllBookings() {

        return bookingService.getAllBookings()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET BOOKED SEATS - LEGACY ENDPOINT
    @GetMapping("/train/{trainId}/seats")
    public ResponseEntity<?> getBookedSeats(
            @PathVariable Long trainId) {

        try {
            return ResponseEntity.ok(
                    bookingService.getBookedSeats(trainId));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET BOOKED SEATS FOR A PARTICULAR CLASS
    @GetMapping("/train/{trainId}/seats/{travelClass}")
    public ResponseEntity<?> getClassBookedSeats(
            @PathVariable Long trainId,
            @PathVariable String travelClass) {

        try {
            return ResponseEntity.ok(
                    bookingService.getBookedSeats(
                            trainId, travelClass));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // CONVERT BOOKING TO RESPONSE
    private BookingResponse convertToResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setPnr(booking.getPnr());
        response.setStatus(booking.getStatus());
        response.setBookingDate(booking.getBookingDate());
        response.setSeatNumber(booking.getSeatNumber());
        response.setTravelClass(booking.getTravelClass());

        response.setTrainId(booking.getTrain().getId());
        response.setTrainName(booking.getTrain().getTrainName());
        response.setTrainNumber(booking.getTrain().getTrainNumber());
        response.setSource(booking.getTrain().getSource());
        response.setDestination(booking.getTrain().getDestination());

        Passenger passenger = booking.getPassenger();

        response.setPassengerName(passenger.getPassengerName());
        response.setAge(passenger.getAge());
        response.setGender(passenger.getGender());
        response.setEmail(passenger.getEmail());
        response.setPhone(passenger.getPhone());

        return response;
    }

    // SINGLE BOOKING REQUEST
    public static class BookingRequest {

        private Long trainId;
        private String passengerName;
        private int age;
        private String gender;
        private String email;
        private String phone;
        private String seatNumber;

        public Long getTrainId() { return trainId; }
        public void setTrainId(Long trainId) { this.trainId = trainId; }

        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) {
            this.passengerName = passengerName;
        }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }
    }

    // GROUP BOOKING REQUEST
    public static class GroupBookingRequest {

        private Long trainId;
        private String travelClass;
        private List<PassengerDetails> passengers;
        private List<String> seatNumbers;

        public Long getTrainId() { return trainId; }
        public void setTrainId(Long trainId) {
            this.trainId = trainId;
        }

        public String getTravelClass() { return travelClass; }
        public void setTravelClass(String travelClass) {
            this.travelClass = travelClass;
        }

        public List<PassengerDetails> getPassengers() {
            return passengers;
        }

        public void setPassengers(List<PassengerDetails> passengers) {
            this.passengers = passengers;
        }

        public List<String> getSeatNumbers() {
            return seatNumbers;
        }

        public void setSeatNumbers(List<String> seatNumbers) {
            this.seatNumbers = seatNumbers;
        }
    }

    // INDIVIDUAL PASSENGER DETAILS
    public static class PassengerDetails {

        private String passengerName;
        private int age;
        private String gender;
        private String email;
        private String phone;

        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) {
            this.passengerName = passengerName;
        }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

    }

    // BOOKING RESPONSE
    public static class BookingResponse {

        private Long id;
        private String pnr;
        private String status;
        private LocalDateTime bookingDate;
        private String seatNumber;
        private String travelClass;

        private Long trainId;
        private String trainName;
        private String trainNumber;
        private String source;
        private String destination;

        private String passengerName;
        private int age;
        private String gender;
        private String email;
        private String phone;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPnr() { return pnr; }
        public void setPnr(String pnr) { this.pnr = pnr; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getBookingDate() { return bookingDate; }
        public void setBookingDate(LocalDateTime bookingDate) {
            this.bookingDate = bookingDate;
        }

        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getTravelClass() { return travelClass; }
        public void setTravelClass(String travelClass) {
            this.travelClass = travelClass;
        }

        public Long getTrainId() { return trainId; }
        public void setTrainId(Long trainId) { this.trainId = trainId; }

        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) {
            this.trainName = trainName;
        }

        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) {
            this.trainNumber = trainNumber;
        }

        public String getSource() { return source; }
        public void setSource(String source) {
            this.source = source;
        }

        public String getDestination() { return destination; }
        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) {
            this.passengerName = passengerName;
        }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getEmail() { return email; }
        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() { return phone; }
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    // ERROR RESPONSE
    public static class ErrorResponse {

        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
