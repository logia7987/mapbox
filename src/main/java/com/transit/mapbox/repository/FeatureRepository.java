package com.transit.mapbox.repository;

import com.transit.mapbox.vo.CoordinateVo;
import com.transit.mapbox.vo.FeatureVo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<FeatureVo, Long> {
}
