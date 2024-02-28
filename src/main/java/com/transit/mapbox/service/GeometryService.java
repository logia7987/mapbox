package com.transit.mapbox.service;

import com.transit.mapbox.repository.GeometryRepository;
import com.transit.mapbox.vo.GeometryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeometryService {

    @Autowired
    private GeometryRepository geometryRepository;

    public void saveGeometry(GeometryVo geometry) {
        geometryRepository.save(geometry);
    }
}
