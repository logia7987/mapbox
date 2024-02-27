package com.transit.mapbox.repository;

import com.transit.mapbox.vo.CoordinateVo;
import com.transit.mapbox.vo.FeatureVo;
import org.apache.ibatis.annotations.Param;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.cdi.JpaRepositoryExtension;

import java.util.List;
import java.util.stream.Stream;

public interface CoordinateRepository extends JpaRepository<CoordinateVo, Long> {
    List<CoordinateVo> findByFeatureVo(FeatureVo featureVo);

    @Query(value = "SELECT * FROM COORDINATES_TABLE WHERE FEATURE_ID = :featureId", nativeQuery = true)
    List<CoordinateVo> findByFeatureIdNative(@Param("featureId") Long featureId);

    @Query("select COORDINATE_X, COORDINATE_Y from COORDINATES_TABLE")
    Stream<CoordinateVo> streamAllCoordinate();
}