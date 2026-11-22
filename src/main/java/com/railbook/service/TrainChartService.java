
package com.railbook.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railbook.entity.Booking;
import com.railbook.entity.Train;
import com.railbook.entity.TrainChart;
import com.railbook.repository.BookingRepository;
import com.railbook.repository.TrainChartRepository;
import com.railbook.repository.TrainRepository;

@Service
public class TrainChartService {

    private final TrainChartRepository trainChartRepository;
    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;

    public TrainChartService(
            TrainChartRepository trainChartRepository,
            TrainRepository trainRepository,
            BookingRepository bookingRepository) {

        this.trainChartRepository = trainChartRepository;
        this.trainRepository = trainRepository;
        this.bookingRepository = bookingRepository;
    }

    // Prepare chart for a train
    @Transactional
    public TrainChart prepareChart(Long trainId) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new RuntimeException("Train not found"));

        if (trainChartRepository.existsByTrainId(trainId)) {
            throw new RuntimeException(
                    "Chart has already been prepared for this train");
        }

        TrainChart chart = new TrainChart();

        chart.setTrainId(train.getId());
        chart.setTrainNumber(train.getTrainNumber());
        chart.setSource(train.getSource());
        chart.setDestination(train.getDestination());
        chart.setChartPreparedAt(LocalDateTime.now());
        chart.setStatus("PREPARED");

        return trainChartRepository.save(chart);
    }

    // Get chart by train ID
    public TrainChart getChart(Long trainId) {

        return trainChartRepository.findByTrainId(trainId)
                .orElseThrow(() ->
                        new RuntimeException("Chart not prepared yet"));
    }

    // Get all prepared charts
    public List<TrainChart> getAllCharts() {

        return trainChartRepository.findAll();
    }

    // Check chart preparation status
    public boolean isChartPrepared(Long trainId) {

        return trainChartRepository.existsByTrainId(trainId);
    }

    // Get passenger-wise chart
    public List<Booking> getPassengerChart(Long trainId) {

        // Check whether chart is prepared
        if (!trainChartRepository.existsByTrainId(trainId)) {
            throw new RuntimeException(
                    "Please prepare the chart first");
        }

        // Check whether train exists
        if (!trainRepository.existsById(trainId)) {
            throw new RuntimeException("Train not found");
        }

        List<Booking> passengerChart = new ArrayList<>();

        // Get confirmed passengers
        passengerChart.addAll(
                bookingRepository.findAllByTrain_IdAndStatus(
                        trainId, "CONFIRMED"));

        // Get RAC passengers
        passengerChart.addAll(
                bookingRepository.findAllByTrain_IdAndStatus(
                        trainId, "RAC"));

        // Get waiting-list passengers
        passengerChart.addAll(
                bookingRepository.findAllByTrain_IdAndStatus(
                        trainId, "WAITING"));

        // Sort by booking date and ID
        passengerChart.sort(
                Comparator.comparing(Booking::getBookingDate)
                        .thenComparing(Booking::getId));

        return passengerChart;
    }
}
