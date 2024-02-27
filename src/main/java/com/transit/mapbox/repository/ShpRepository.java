package com.transit.mapbox.repository;

import com.transit.mapbox.vo.FeatureVo;
import com.transit.mapbox.vo.ShpVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface ShpRepository extends JpaRepository<ShpVo, Long> {
    @Query("select SHP_ID, SHP_NAME from SHP_TABLE")
    Stream<ShpVo> streamAll();
}