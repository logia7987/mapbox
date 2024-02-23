package com.transit.mapbox.service;

import com.transit.mapbox.repository.FeatureRepository;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    public void saveFeature(FeatureVo feature) {
        featureRepository.save(feature);
    }
}