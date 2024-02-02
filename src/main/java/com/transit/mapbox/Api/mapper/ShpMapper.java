package com.transit.mapbox.Api.mapper;

import com.transit.mapbox.Api.vo.ShpVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

@Mapper
public interface ShpMapper {
    @Insert("INSERT INTO SHPTABLE (SHP_ID, SHP_NAME, UPLOAD_DATE) VALUES (SHP_SEQ.NEXTVAL, #{shpname}, sysdate+9/24)")
    @SelectKey(statement = "SELECT SHP_SEQ.currval as shpid FROM dual", keyProperty = "shpid", before = false, resultType = Long.class)
    void insert(ShpVo vo);
}
