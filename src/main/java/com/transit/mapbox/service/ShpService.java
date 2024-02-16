package com.transit.mapbox.service;

import com.transit.mapbox.repository.ShpRepository;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShpService {

    @Autowired
    private ShpRepository shpRepository;

    public void saveShp(ShpVo shp) {
        shpRepository.save(shp);
    }
}