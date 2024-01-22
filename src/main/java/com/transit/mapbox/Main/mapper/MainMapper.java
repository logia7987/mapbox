package com.transit.mapbox.Main.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MainMapper {

    @Select("SELECT COUNT(1) FROM TRANSIT.CITYTABLE")
    public String getCount();


}
