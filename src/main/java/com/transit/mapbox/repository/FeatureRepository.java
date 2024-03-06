package com.transit.mapbox.repository;

import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeatureRepository extends JpaRepository<FeatureVo, Long> {
    List<FeatureVo> findByShpVo(ShpVo shpVo, Sort sort);

}