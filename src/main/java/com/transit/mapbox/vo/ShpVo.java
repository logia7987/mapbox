package com.transit.mapbox.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "SHP_TABLE")
public class ShpVo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SHP_SEQ")
    @SequenceGenerator(name = "SHP_SEQ", sequenceName = "SHP_SEQ", allocationSize = 1)
    @Column(name = "SHP_ID")
    private Long shpId;

    @Column(name = "SHP_NAME")
    private String shpName;

    @Column(name = "UPLOAD_DATE")
    private LocalDateTime uploadDate;

    @OneToMany(mappedBy = "shpVo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeatureVo> featureVos;

    @PrePersist
    public void prePersist() {
        uploadDate = LocalDateTime.now();
    }
}
