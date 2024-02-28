package com.transit.mapbox.repository;

import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureRepository extends JpaRepository<FeatureVo, Long> {
    List<FeatureVo> findByShpVo(ShpVo shpVo);

}