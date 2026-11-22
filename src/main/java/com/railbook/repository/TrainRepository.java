
package com.railbook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbook.entity.Train;

public interface TrainRepository extends JpaRepository<Train, Long> {

    List<Train> findBySourceIgnoreCaseAndDestinationIgnoreCase(
            String source,
            String destination
    );

}
