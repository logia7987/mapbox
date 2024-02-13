package com.transit.mapbox.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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

    @PrePersist
    public void prePersist() {
        uploadDate = LocalDateTime.now();
    }
}
