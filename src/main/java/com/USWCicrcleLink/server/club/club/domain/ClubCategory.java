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
@Table(name = "ClUB_CATEGORY_TABLE")
public class ClubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_category_id")
    private Long clubCategoryId;

    @Column(name = "club_category_name", nullable = false)
    private String clubCategoryName;
}
