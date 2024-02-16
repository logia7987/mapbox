package com.transit.mapbox.vo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "COORDINATES_TABLE")
public class CoordinateVo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COORD_SEQ")
    @SequenceGenerator(name = "COORD_SEQ", sequenceName = "COORD_SEQ", allocationSize = 1)
    @Column(name = "COORDINATES_ID")
    private Long coordinateId;

    @Column(name = "COORDINATE_X")
    private Double coordinateX;

    @Column(name = "COORDINATE_Y")
    private Double coordinateY;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private FeatureVo featureVo;
}