package com.transit.mapbox.Api.mapper;

import com.transit.mapbox.Api.vo.GeometryVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GeoMapper {
    @Insert("INSERT INTO GEOMETRYTABLE " +
            "(GEOMETRY_ID, GEO_X, GEO_Y, SEQ, SHP_ID) " +
            "VALUES (GEO_SEQ.NEXTVAL, #{geoX}, #{geoY}, #{seq}, #{shpId}")
    void insertGeometry(GeometryVo vo);
}
