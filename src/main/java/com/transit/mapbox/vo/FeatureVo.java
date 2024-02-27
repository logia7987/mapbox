package com.transit.mapbox.vo;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Cacheable
@Entity
@Data
@Table(name = "FEATURE_TABLE")
public class FeatureVo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FEATURE_SEQ")
    @SequenceGenerator(name = "FEATURE_SEQ", sequenceName = "FEATURE_SEQ", allocationSize = 1)
    @Column(name = "FEATURE_ID")
    private Long featureId;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "SEQ")
    private int seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shp_id")
    private ShpVo shpVo;

    @OneToMany(mappedBy = "featureVo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<CoordinateVo> coordinateVos;
}
