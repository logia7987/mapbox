package com.transit.mapbox.vo;

import jakarta.persistence.*;
import lombok.Data;

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

    @Column(name = "SHP_ID")
    private Long shpId;
}
