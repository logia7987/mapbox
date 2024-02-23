package com.transit.mapbox.service;

import com.transit.mapbox.repository.CoordinateRepository;
import com.transit.mapbox.vo.CoordinateVo;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

@Service
public class CoordinateService {

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Async("asyncExecutor")
    @Transactional
    public void saveAllCoordinates(List<CoordinateVo> coordinateList) {
        coordinateRepository.saveAll(coordinateList);
    }

}
