
package com.railbook.entity;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;

    private int totalSeats;
    private int availableSeats;

    private double fare;

    // Class-wise seat capacity
    private int firstAcSeats;
    private int secondAcSeats;
    private int thirdAcSeats;
    private int acEconomySeats;
    private int sleeperSeats;

    // Class-wise available seats
    private int firstAcAvailable;
    private int secondAcAvailable;
    private int thirdAcAvailable;
    private int acEconomyAvailable;
    private int sleeperAvailable;

    // Class-wise fares
    private double firstAcFare;
    private double secondAcFare;
    private double thirdAcFare;
    private double acEconomyFare;
    private double sleeperFare;

    // Class-wise RAC limits
    private int firstAcRacLimit;
    private int secondAcRacLimit;
    private int thirdAcRacLimit;
    private int acEconomyRacLimit;
    private int sleeperRacLimit;

    // Class-wise Waiting List limits
    private int firstAcWaitingLimit;
    private int secondAcWaitingLimit;
    private int thirdAcWaitingLimit;
    private int acEconomyWaitingLimit;
    private int sleeperWaitingLimit;

    private LocalTime departureTime;
    private LocalTime arrivalTime;

    public Train() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getFirstAcSeats() {
        return firstAcSeats;
    }

    public void setFirstAcSeats(int value) {
        this.firstAcSeats = value;
    }

    public int getSecondAcSeats() {
        return secondAcSeats;
    }

    public void setSecondAcSeats(int value) {
        this.secondAcSeats = value;
    }

    public int getThirdAcSeats() {
        return thirdAcSeats;
    }

    public void setThirdAcSeats(int value) {
        this.thirdAcSeats = value;
    }

    public int getAcEconomySeats() {
        return acEconomySeats;
    }

    public void setAcEconomySeats(int value) {
        this.acEconomySeats = value;
    }

    public int getSleeperSeats() {
        return sleeperSeats;
    }

    public void setSleeperSeats(int value) {
        this.sleeperSeats = value;
    }

    public int getFirstAcAvailable() {
        return firstAcAvailable;
    }

    public void setFirstAcAvailable(int value) {
        this.firstAcAvailable = value;
    }

    public int getSecondAcAvailable() {
        return secondAcAvailable;
    }

    public void setSecondAcAvailable(int value) {
        this.secondAcAvailable = value;
    }

    public int getThirdAcAvailable() {
        return thirdAcAvailable;
    }

    public void setThirdAcAvailable(int value) {
        this.thirdAcAvailable = value;
    }

    public int getAcEconomyAvailable() {
        return acEconomyAvailable;
    }

    public void setAcEconomyAvailable(int value) {
        this.acEconomyAvailable = value;
    }

    public int getSleeperAvailable() {
        return sleeperAvailable;
    }

    public void setSleeperAvailable(int value) {
        this.sleeperAvailable = value;
    }

    public double getFirstAcFare() {
        return firstAcFare;
    }

    public void setFirstAcFare(double value) {
        this.firstAcFare = value;
    }

    public double getSecondAcFare() {
        return secondAcFare;
    }

    public void setSecondAcFare(double value) {
        this.secondAcFare = value;
    }

    public double getThirdAcFare() {
        return thirdAcFare;
    }

    public void setThirdAcFare(double value) {
        this.thirdAcFare = value;
    }

    public double getAcEconomyFare() {
        return acEconomyFare;
    }

    public void setAcEconomyFare(double value) {
        this.acEconomyFare = value;
    }

    public double getSleeperFare() {
        return sleeperFare;
    }

    public void setSleeperFare(double value) {
        this.sleeperFare = value;
    }

    // First AC RAC and Waiting List
    public int getFirstAcRacLimit() {
        return firstAcRacLimit;
    }

    public void setFirstAcRacLimit(int value) {
        this.firstAcRacLimit = value;
    }

    public int getFirstAcWaitingLimit() {
        return firstAcWaitingLimit;
    }

    public void setFirstAcWaitingLimit(int value) {
        this.firstAcWaitingLimit = value;
    }

    // Second AC RAC and Waiting List
    public int getSecondAcRacLimit() {
        return secondAcRacLimit;
    }

    public void setSecondAcRacLimit(int value) {
        this.secondAcRacLimit = value;
    }

    public int getSecondAcWaitingLimit() {
        return secondAcWaitingLimit;
    }

    public void setSecondAcWaitingLimit(int value) {
        this.secondAcWaitingLimit = value;
    }

    // Third AC RAC and Waiting List
    public int getThirdAcRacLimit() {
        return thirdAcRacLimit;
    }

    public void setThirdAcRacLimit(int value) {
        this.thirdAcRacLimit = value;
    }

    public int getThirdAcWaitingLimit() {
        return thirdAcWaitingLimit;
    }

    public void setThirdAcWaitingLimit(int value) {
        this.thirdAcWaitingLimit = value;
    }

    // AC 3 Economy RAC and Waiting List
    public int getAcEconomyRacLimit() {
        return acEconomyRacLimit;
    }

    public void setAcEconomyRacLimit(int value) {
        this.acEconomyRacLimit = value;
    }

    public int getAcEconomyWaitingLimit() {
        return acEconomyWaitingLimit;
    }

    public void setAcEconomyWaitingLimit(int value) {
        this.acEconomyWaitingLimit = value;
    }

    // Sleeper RAC and Waiting List
    public int getSleeperRacLimit() {
        return sleeperRacLimit;
    }

    public void setSleeperRacLimit(int value) {
        this.sleeperRacLimit = value;
    }

    public int getSleeperWaitingLimit() {
        return sleeperWaitingLimit;
    }

    public void setSleeperWaitingLimit(int value) {
        this.sleeperWaitingLimit = value;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}