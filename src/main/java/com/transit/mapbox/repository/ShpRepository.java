package com.transit.mapbox.repository;

import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShpRepository extends JpaRepository<ShpVo, Long> {
}