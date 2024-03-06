package com.transit.mapbox.service;

import com.transit.mapbox.repository.FeatureRepository;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    public FeatureVo saveFeature(FeatureVo feature) {
        return featureRepository.save(feature);
    }

    public List<FeatureVo> getFeatures(ShpVo shpVo) {
        Sort sort = Sort.by(Sort.Direction.ASC, "seq");

        return featureRepository.findByShpVo(shpVo, sort);
    }
}