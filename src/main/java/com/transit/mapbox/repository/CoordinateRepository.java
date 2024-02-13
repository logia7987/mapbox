package com.transit.mapbox.repository;

import com.transit.mapbox.vo.CoordinateVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.cdi.JpaRepositoryExtension;

public interface CoordinateRepository extends JpaRepository<CoordinateVo, Long> {
}
