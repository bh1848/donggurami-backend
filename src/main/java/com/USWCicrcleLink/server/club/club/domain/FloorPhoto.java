package com.USWCicrcleLink.server.club.club.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "FLOOR_PHOTO_TABLE")
public class FloorPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "floor_photo_id")
    private Long floorPhotoId;

    @Column(name = "floor_photo_name")
    private String floorPhotoPhotoName;

    @Column(name = "floor_photo_s3_key")
    private String floorPhotoPhotoS3key;

    @Enumerated(EnumType.STRING)
    @Column(name = "floor_photo_floor")
    private FloorPhotoEnum floor;
}
