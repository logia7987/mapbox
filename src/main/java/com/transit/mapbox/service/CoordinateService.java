package com.transit.mapbox.service;

import com.transit.mapbox.repository.CoordinateRepository;
import com.transit.mapbox.vo.CoordinateVo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

@Service
public class CoordinateService {

    @Autowired
    private CoordinateRepository coordinateRepository;
    @Autowired
    private final EntityManager entityManager;

    public CoordinateService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Modifying
    @Transactional
    public void saveAllCoordinates(List<CoordinateVo> coordinateList) {
        int batchSize = 10000;

        for (int i = 0; i < coordinateList.size(); i++) {
            CoordinateVo coordinate = coordinateList.get(i);
            entityManager.merge(coordinate);

            if ((i + 1) % batchSize == 0 || i == coordinateList.size() - 1) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}
