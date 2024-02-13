package com.transit.mapbox.service;

import com.transit.mapbox.repository.CoordinateRepository;
import com.transit.mapbox.vo.CoordinateVo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoordinateService {

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Transactional
    public void batchInsertCoordinates(List<CoordinateVo> coordinateList) {
        coordinateRepository.saveAll(coordinateList);
    }

}
