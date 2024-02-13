package com.transit.mapbox.service;

import com.transit.mapbox.repository.CoordinateRepository;
import com.transit.mapbox.repository.FeatureRepository;
import com.transit.mapbox.vo.CoordinateVo;
import com.transit.mapbox.vo.FeatureVo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    @Transactional
    public Long saveFeature(FeatureVo feature) {
        FeatureVo saveFeature = featureRepository.save(feature);
        return saveFeature.getFeatureId();
    }

}
