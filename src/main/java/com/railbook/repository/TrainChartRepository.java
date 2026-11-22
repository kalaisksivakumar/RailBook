
package com.railbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbook.entity.TrainChart;

public interface TrainChartRepository
        extends JpaRepository<TrainChart, Long> {

    Optional<TrainChart> findByTrainId(Long trainId);

    boolean existsByTrainId(Long trainId);
}
