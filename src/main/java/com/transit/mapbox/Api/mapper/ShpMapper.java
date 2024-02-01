package com.transit.mapbox.Api.mapper;

import com.transit.mapbox.Api.vo.ShpVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShpMapper {
    @Insert("INSERT INTO SHPTABLE (SHP_ID, SHP_NAME, UPLOAD_DATE) values (shp_seq.NEXTVAL, #{shpname}, sysdate)")
    void insert(ShpVo vo);


}
