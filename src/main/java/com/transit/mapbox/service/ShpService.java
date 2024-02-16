package com.transit.mapbox.service;

import com.transit.mapbox.repository.ShpRepository;
import com.transit.mapbox.vo.ShpVo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShpService {

    @Autowired
    private ShpRepository shpRepository;

    @Transactional
    public List<ShpVo> selectShp() {
        return shpRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadDate"));
    }

    public void saveShp(ShpVo shp) {
        shpRepository.save(shp);
    }
}