
package com.railbook.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbook.entity.Booking;
import com.railbook.entity.Passenger;
import com.railbook.entity.Train;
import com.railbook.repository.BookingRepository;
import com.railbook.repository.PassengerRepository;
import com.railbook.repository.TrainChartRepository;
import com.railbook.repository.TrainRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final TrainRepository trainRepository;
    private final TrainChartRepository trainChartRepository;

    private final SecureRandom random = new SecureRandom();

    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            TrainRepository trainRepository,
            TrainChartRepository trainChartRepository) {

        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.trainRepository = trainRepository;
        this.trainChartRepository = trainChartRepository;
    }

    // Generate unique PNR
    private String generatePNR() {

        String pnr;

        do {
            pnr = String.valueOf(
                    1000000000L + random.nextLong(9000000000L));
        } while (bookingRepository.existsByPnr(pnr));

        return pnr;
    }

    // Validate passenger details
    private void validatePassenger(Passenger passenger) {

        if (passenger.getPassengerName() == null ||
                passenger.getPassengerName().isBlank()) {
            throw new RuntimeException("Passenger name is required");
        }

        if (passenger.getAge() <= 0 || passenger.getAge() > 120) {
            throw new RuntimeException("Invalid passenger age");
        }

        if (passenger.getPhone() == null ||
                !passenger.getPhone().matches("[0-9]{10}")) {
            throw new RuntimeException("Invalid phone number");
        }
    }

    // Validate travel class
    private String validateTravelClass(String travelClass) {

        if (travelClass == null) {
            throw new RuntimeException("Please select a travel class");
        }

        String selectedClass = travelClass.trim().toUpperCase();

        if (!Set.of("1A", "2A", "3A", "3E", "SL")
                .contains(selectedClass)) {
            throw new RuntimeException("Invalid travel class");
        }

        return selectedClass;
    }

    // Get total seats for a class
    private int getClassTotalSeats(Train train, String travelClass) {

        return switch (travelClass) {
            case "1A" -> train.getFirstAcSeats();
            case "2A" -> train.getSecondAcSeats();
            case "3A" -> train.getThirdAcSeats();
            case "3E" -> train.getAcEconomySeats();
            case "SL" -> train.getSleeperSeats();
            default -> throw new RuntimeException("Invalid travel class");
        };
    }

    // Get available seats for a class
    private int getClassAvailableSeats(
            Train train, String travelClass) {

        return switch (travelClass) {
            case "1A" -> train.getFirstAcAvailable();
            case "2A" -> train.getSecondAcAvailable();
            case "3A" -> train.getThirdAcAvailable();
            case "3E" -> train.getAcEconomyAvailable();
            case "SL" -> train.getSleeperAvailable();
            default -> throw new RuntimeException("Invalid travel class");
        };
    }

    // Update available seats for a class
    private void setClassAvailableSeats(
            Train train, String travelClass, int availableSeats) {

        switch (travelClass) {
            case "1A" -> train.setFirstAcAvailable(availableSeats);
            case "2A" -> train.setSecondAcAvailable(availableSeats);
            case "3A" -> train.setThirdAcAvailable(availableSeats);
            case "3E" -> train.setAcEconomyAvailable(availableSeats);
            case "SL" -> train.setSleeperAvailable(availableSeats);
            default -> throw new RuntimeException("Invalid travel class");
        }
    }

    // Get RAC quota
    private int getClassRacLimit(Train train, String travelClass) {

        return switch (travelClass) {
            case "1A" -> train.getFirstAcRacLimit();
            case "2A" -> train.getSecondAcRacLimit();
            case "3A" -> train.getThirdAcRacLimit();
            case "3E" -> train.getAcEconomyRacLimit();
            case "SL" -> train.getSleeperRacLimit();
            default -> throw new RuntimeException("Invalid travel class");
        };
    }

    // Get waiting-list quota
    private int getClassWaitingLimit(Train train, String travelClass) {

        return switch (travelClass) {
            case "1A" -> train.getFirstAcWaitingLimit();
            case "2A" -> train.getSecondAcWaitingLimit();
            case "3A" -> train.getThirdAcWaitingLimit();
            case "3E" -> train.getAcEconomyWaitingLimit();
            case "SL" -> train.getSleeperWaitingLimit();
            default -> throw new RuntimeException("Invalid travel class");
        };
    }

    // Book one ticket - legacy functionality
    @Transactional
    public Booking bookTicket(
            Long trainId,
            String name,
            int age,
            String gender,
            String email,
            String phone,
            String requestedSeat) {

        Passenger passenger = new Passenger();

        passenger.setPassengerName(name);
        passenger.setAge(age);
        passenger.setGender(gender);
        passenger.setEmail(email);
        passenger.setPhone(phone);

        List<Booking> bookings = bookGroupTickets(
                trainId,
                List.of(passenger),
                requestedSeat == null || requestedSeat.isBlank()
                        ? List.of()
                        : List.of(requestedSeat));

        return bookings.get(0);
    }

    // Legacy group booking
    @Transactional
    public List<Booking> bookGroupTickets(
            Long trainId,
            List<Passenger> passengers,
            List<String> requestedSeats) {

        return processGroupBooking(
                trainId, passengers, requestedSeats, null);
    }

    // Class-wise group booking
    @Transactional
    public List<Booking> bookGroupTickets(
            Long trainId,
            List<Passenger> passengers,
            List<String> requestedSeats,
            String travelClass) {

        String selectedClass = validateTravelClass(travelClass);

        return processGroupBooking(
                trainId, passengers, requestedSeats, selectedClass);
    }

    // Common booking process
    private List<Booking> processGroupBooking(
            Long trainId,
            List<Passenger> passengers,
            List<String> requestedSeats,
            String travelClass) {

        if (passengers == null ||
                passengers.isEmpty() ||
                passengers.size() > 6) {
            throw new RuntimeException(
                    "Booking must contain between 1 and 6 passengers");
        }

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new RuntimeException("Train not found"));

        // Prevent new bookings after chart preparation
        if (trainChartRepository.existsByTrainId(trainId)) {
            throw new RuntimeException(
                    "Chart has been prepared. New bookings are not allowed.");
        }

        int count = passengers.size();

        if (requestedSeats == null) {
            requestedSeats = new ArrayList<>();
        }

        if (!requestedSeats.isEmpty() &&
                requestedSeats.size() != count) {
            throw new RuntimeException(
                    "Select one seat for every passenger");
        }

        int availableSeats;
        int totalSeats;

        if (travelClass == null) {

            availableSeats = train.getAvailableSeats();
            totalSeats = train.getTotalSeats();

            if (availableSeats < count) {
                throw new RuntimeException("Not enough seats available");
            }

        } else {

            availableSeats = getClassAvailableSeats(train, travelClass);
            totalSeats = getClassTotalSeats(train, travelClass);
        }

        int confirmedCount = travelClass == null
                ? count
                : Math.min(availableSeats, count);

        int racCount = 0;
        int waitingCount = 0;

        // Check RAC and waiting-list quotas
        if (travelClass != null) {

            racCount = (int) bookingRepository
                    .countByTrain_IdAndTravelClassAndStatus(
                            trainId, travelClass, "RAC");

            waitingCount = (int) bookingRepository
                    .countByTrain_IdAndTravelClassAndStatus(
                            trainId, travelClass, "WAITING");

            int remainingPassengers = count - confirmedCount;

            int racAvailable = Math.max(
                    0, getClassRacLimit(train, travelClass) - racCount);

            int waitingAvailable = Math.max(
                    0, getClassWaitingLimit(train, travelClass) - waitingCount);

            if (remainingPassengers > racAvailable + waitingAvailable) {
                throw new RuntimeException(
                        "Confirmed, RAC and Waiting List quotas are full");
            }
        }

        List<String> assignedSeats = new ArrayList<>();
        Set<String> uniqueSeats = new HashSet<>();

        // Validate passengers and allocate confirmed seats
        for (int i = 0; i < count; i++) {

            Passenger passenger = passengers.get(i);
            validatePassenger(passenger);

            if (i >= confirmedCount) {
                assignedSeats.add(null);
                continue;
            }

            String seat;

            if (requestedSeats.isEmpty()) {

                seat = findAvailableSeat(
                        train, uniqueSeats, travelClass);

            } else {

                seat = requestedSeats.get(i)
                        .trim().toUpperCase();

                if (!seat.matches("S[1-9][0-9]*")) {
                    throw new RuntimeException("Invalid seat number");
                }

                int seatIndex =
                        Integer.parseInt(seat.substring(1));

                if (seatIndex > totalSeats) {
                    throw new RuntimeException("Seat does not exist");
                }

                boolean alreadyBooked;

                if (travelClass == null) {

                    alreadyBooked = bookingRepository
                            .existsByTrain_IdAndSeatNumberAndStatus(
                                    trainId, seat, "CONFIRMED");

                } else {

                    alreadyBooked = bookingRepository
                            .existsByTrain_IdAndTravelClassAndSeatNumberAndStatus(
                                    trainId, travelClass, seat, "CONFIRMED");
                }

                if (alreadyBooked) {
                    throw new RuntimeException(
                            "Seat already booked: " + seat);
                }
            }

            if (!uniqueSeats.add(seat)) {
                throw new RuntimeException(
                        "Duplicate seat selected: " + seat);
            }

            assignedSeats.add(seat);
        }

        // One PNR for the entire group
        String commonPnr = generatePNR();

        List<Booking> groupBookings = new ArrayList<>();
        LocalDateTime bookingTime = LocalDateTime.now();

        for (int i = 0; i < count; i++) {

            Passenger passenger =
                    passengerRepository.save(passengers.get(i));

            Booking booking = new Booking();

            booking.setTrain(train);
            booking.setPassenger(passenger);
            booking.setPnr(commonPnr);
            booking.setBookingDate(bookingTime);
            booking.setTravelClass(travelClass);

            if (i < confirmedCount) {

                booking.setStatus("CONFIRMED");
                booking.setSeatNumber(assignedSeats.get(i));
                booking.setRacNumber(null);
                booking.setWaitingNumber(null);

            } else if (travelClass != null &&
                    racCount < getClassRacLimit(train, travelClass)) {

                racCount++;

                booking.setStatus("RAC");
                booking.setSeatNumber(null);
                booking.setRacNumber(racCount);
                booking.setWaitingNumber(null);

            } else {

                waitingCount++;

                booking.setStatus("WAITING");
                booking.setSeatNumber(null);
                booking.setRacNumber(null);
                booking.setWaitingNumber(waitingCount);
            }

            groupBookings.add(bookingRepository.save(booking));
        }

        // Update confirmed seat availability
        if (travelClass == null) {

            train.setAvailableSeats(
                    train.getAvailableSeats() - confirmedCount);

        } else {

            setClassAvailableSeats(
                    train,
                    travelClass,
                    availableSeats - confirmedCount);
        }

        trainRepository.save(train);

        return groupBookings;
    }

    // Find an available seat
    private String findAvailableSeat(
            Train train,
            Set<String> temporarilySelected,
            String travelClass) {

        int totalSeats = travelClass == null
                ? train.getTotalSeats()
                : getClassTotalSeats(train, travelClass);

        for (int i = 1; i <= totalSeats; i++) {

            String seat = "S" + i;

            boolean booked;

            if (travelClass == null) {

                booked = bookingRepository
                        .existsByTrain_IdAndSeatNumberAndStatus(
                                train.getId(), seat, "CONFIRMED");

            } else {

                booked = bookingRepository
                        .existsByTrain_IdAndTravelClassAndSeatNumberAndStatus(
                                train.getId(), travelClass, seat, "CONFIRMED");
            }

            if (!booked && !temporarilySelected.contains(seat)) {
                return seat;
            }
        }

        throw new RuntimeException("No seats available");
    }

    // Get booked seats
    public List<String> getBookedSeats(Long trainId) {

        if (!trainRepository.existsById(trainId)) {
            throw new RuntimeException("Train not found");
        }

        return bookingRepository
                .findAllByTrain_IdAndStatus(trainId, "CONFIRMED")
                .stream()
                .map(Booking::getSeatNumber)
                .filter(seat -> seat != null)
                .collect(Collectors.toList());
    }

    // Get booked seats for a selected class
    public List<String> getBookedSeats(
            Long trainId, String travelClass) {

        if (!trainRepository.existsById(trainId)) {
            throw new RuntimeException("Train not found");
        }

        String selectedClass = validateTravelClass(travelClass);

        return bookingRepository
                .findAllByTrain_IdAndTravelClassAndStatus(
                        trainId, selectedClass, "CONFIRMED")
                .stream()
                .map(Booking::getSeatNumber)
                .filter(seat -> seat != null)
                .collect(Collectors.toList());
    }

    // Get one booking by PNR
    public Booking getBookingByPnr(String pnr) {

        if (pnr == null || !pnr.matches("[0-9]{10}")) {
            throw new RuntimeException(
                    "Please enter a valid 10-digit PNR");
        }

        List<Booking> bookings =
                bookingRepository.findAllByPnr(pnr);

        if (bookings.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        return bookings.get(0);
    }

    // Get all passengers under one PNR
    public List<Booking> getBookingsByPnr(String pnr) {

        if (pnr == null || !pnr.matches("[0-9]{10}")) {
            throw new RuntimeException(
                    "Please enter a valid 10-digit PNR");
        }

        List<Booking> bookings =
                bookingRepository.findAllByPnr(pnr);

        if (bookings.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        return bookings;
    }

    // Cancel tickets and promote RAC / Waiting List passengers
    @Transactional
    public Booking cancelBooking(String pnr) {

        List<Booking> bookings = getBookingsByPnr(pnr);

        int legacyCancelledSeats = 0;

        Map<String, Integer> classCancelledSeats = new HashMap<>();

        boolean foundActiveBooking = false;

        // Cancel the selected PNR
        for (Booking booking : bookings) {

            String status = booking.getStatus();

            if ("CONFIRMED".equalsIgnoreCase(status) ||
                    "RAC".equalsIgnoreCase(status) ||
                    "WAITING".equalsIgnoreCase(status)) {

                foundActiveBooking = true;

                if ("CONFIRMED".equalsIgnoreCase(status)) {

                    String travelClass = booking.getTravelClass();

                    if (travelClass == null || travelClass.isBlank()) {
                        legacyCancelledSeats++;
                    } else {
                        classCancelledSeats.merge(
                                travelClass, 1, Integer::sum);
                    }
                }

                booking.setStatus("CANCELLED");
                booking.setSeatNumber(null);
                booking.setRacNumber(null);
                booking.setWaitingNumber(null);
            }
        }

        if (!foundActiveBooking) {
            throw new RuntimeException("Ticket is already cancelled");
        }

        Train train = bookings.get(0).getTrain();

        // Return legacy seats
        if (legacyCancelledSeats > 0) {

            train.setAvailableSeats(
                    Math.min(
                            train.getTotalSeats(),
                            train.getAvailableSeats() + legacyCancelledSeats
                    )
            );
        }

        // Return class-wise seats
        for (Map.Entry<String, Integer> entry :
                classCancelledSeats.entrySet()) {

            String travelClass = entry.getKey();

            int currentAvailable =
                    getClassAvailableSeats(train, travelClass);

            int totalSeats =
                    getClassTotalSeats(train, travelClass);

            setClassAvailableSeats(
                    train,
                    travelClass,
                    Math.min(
                            totalSeats,
                            currentAvailable + entry.getValue()
                    )
            );
        }

        bookingRepository.saveAll(bookings);
        trainRepository.save(train);

        // Promote RAC passengers for each affected class
        for (Map.Entry<String, Integer> entry :
                classCancelledSeats.entrySet()) {

            String travelClass = entry.getKey();

            int availablePromotions = entry.getValue();

            List<Booking> racBookings =
                    bookingRepository
                            .findAllByTrain_IdAndTravelClassAndStatus(
                                    train.getId(), travelClass, "RAC");

            Comparator<Booking> bookingOrder =
                    Comparator.comparing(Booking::getBookingDate)
                            .thenComparing(Booking::getId);

            racBookings.sort(bookingOrder);

            Set<String> temporarilySelectedSeats = new HashSet<>();

            int promotedCount = 0;

            for (Booking racBooking : racBookings) {

                if (promotedCount >= availablePromotions) {
                    break;
                }

                String seat = findAvailableSeat(
                        train, temporarilySelectedSeats, travelClass);

                temporarilySelectedSeats.add(seat);

                racBooking.setStatus("CONFIRMED");
                racBooking.setSeatNumber(seat);
                racBooking.setRacNumber(null);
                racBooking.setWaitingNumber(null);

                bookingRepository.save(racBooking);

                int available =
                        getClassAvailableSeats(train, travelClass);

                setClassAvailableSeats(
                        train,
                        travelClass,
                        available - 1
                );

                promotedCount++;
            }

            // Move waiting passengers to RAC
            if (promotedCount > 0) {

                List<Booking> remainingRac =
                        bookingRepository
                                .findAllByTrain_IdAndTravelClassAndStatus(
                                        train.getId(), travelClass, "RAC");

                List<Booking> waitingBookings =
                        bookingRepository
                                .findAllByTrain_IdAndTravelClassAndStatus(
                                        train.getId(), travelClass, "WAITING");

                remainingRac.sort(bookingOrder);
                waitingBookings.sort(bookingOrder);

                int moveToRac = Math.min(
                        promotedCount, waitingBookings.size());

                for (int i = 0; i < moveToRac; i++) {

                    Booking waitingBooking = waitingBookings.get(i);

                    waitingBooking.setStatus("RAC");
                    waitingBooking.setRacNumber(null);
                    waitingBooking.setWaitingNumber(null);

                    bookingRepository.save(waitingBooking);

                    remainingRac.add(waitingBooking);
                }

                // Reassign RAC numbers
                remainingRac.sort(bookingOrder);

                for (int i = 0; i < remainingRac.size(); i++) {

                    Booking racBooking = remainingRac.get(i);

                    racBooking.setRacNumber(i + 1);
                    racBooking.setWaitingNumber(null);

                    bookingRepository.save(racBooking);
                }

                // Reassign waiting-list numbers
                List<Booking> remainingWaiting =
                        bookingRepository
                                .findAllByTrain_IdAndTravelClassAndStatus(
                                        train.getId(), travelClass, "WAITING");

                remainingWaiting.sort(bookingOrder);

                for (int i = 0; i < remainingWaiting.size(); i++) {

                    Booking waitingBooking = remainingWaiting.get(i);

                    waitingBooking.setWaitingNumber(i + 1);
                    waitingBooking.setRacNumber(null);

                    bookingRepository.save(waitingBooking);
                }
            }
        }

        trainRepository.save(train);

        return bookings.get(0);
    }

    // Get booking history
    public List<Booking> getAllBookings() {

        return bookingRepository
                .findAllByOrderByBookingDateDesc();
    }
}
