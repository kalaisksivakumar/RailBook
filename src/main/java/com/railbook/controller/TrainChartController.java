
package com.railbook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railbook.entity.Booking;
import com.railbook.entity.TrainChart;
import com.railbook.service.TrainChartService;

@RestController
@RequestMapping("/api/charts")
public class TrainChartController {

    private final TrainChartService trainChartService;

    public TrainChartController(TrainChartService trainChartService) {
        this.trainChartService = trainChartService;
    }

    // Prepare chart for a train
    @PostMapping("/prepare/{trainId}")
    public ResponseEntity<TrainChart> prepareChart(
            @PathVariable Long trainId) {

        TrainChart chart = trainChartService.prepareChart(trainId);
        return ResponseEntity.ok(chart);
    }

    // Get chart details by train ID
    @GetMapping("/{trainId}")
    public ResponseEntity<TrainChart> getChart(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainChartService.getChart(trainId));
    }

    // Get all prepared charts
    @GetMapping
    public ResponseEntity<List<TrainChart>> getAllCharts() {

        return ResponseEntity.ok(
                trainChartService.getAllCharts());
    }

    // Check whether chart is prepared
    @GetMapping("/status/{trainId}")
    public ResponseEntity<Boolean> getChartStatus(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainChartService.isChartPrepared(trainId));
    }

    // Get passenger-wise chart
    @GetMapping("/{trainId}/passengers")
    public ResponseEntity<List<Booking>> getPassengerChart(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainChartService.getPassengerChart(trainId));
    }
}
