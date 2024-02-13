package com.transit.mapbox.service;

import com.transit.mapbox.repository.FeatureRepository;
import com.transit.mapbox.repository.ShpRepository;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShpService {

    @Autowired
    private ShpRepository shpRepository;

    @Transactional
    public Long saveShp(ShpVo shp) {
        ShpVo saveShp = shpRepository.save(shp);
        return saveShp.getShpId();
    }

}
