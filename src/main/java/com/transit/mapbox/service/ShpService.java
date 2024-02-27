package com.transit.mapbox.service;

import com.transit.mapbox.repository.CoordinateRepository;
import com.transit.mapbox.repository.FeatureRepository;
import com.transit.mapbox.repository.ShpRepository;
import com.transit.mapbox.vo.CoordinateVo;
import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShpService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ShpRepository shpRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Transactional
    public List<ShpVo> selectShp() {
        return shpRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadDate"));
    }

    @Transactional(readOnly = true)
    public ShpVo getShpDataById(Long shpId) {
        ShpVo shpVo = shpRepository.findById(shpId).orElse(null);

        if (shpVo != null) {
            for (FeatureVo featureVo : shpVo.getFeatureVos()) {
                Hibernate.initialize(featureVo.getCoordinateVos());
            }
        }

        return shpVo;
    }

    public void saveShp(ShpVo shp) {
        shpRepository.save(shp);
    }

}