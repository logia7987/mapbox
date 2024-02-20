package com.transit.mapbox.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@Table(name = "COORDINATES_TABLE")
public class CoordinateVo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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