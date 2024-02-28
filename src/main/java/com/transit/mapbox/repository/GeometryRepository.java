package com.transit.mapbox.repository;

import com.transit.mapbox.vo.GeometryVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeometryRepository extends JpaRepository<GeometryVo, Long> {

}