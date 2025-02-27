package com.USWCicrcleLink.server.club.club.domain;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_TABLE")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long clubId;

    @Builder.Default
    @Column(name = "club_uuid", unique = true, nullable = false, updatable = false)
    private UUID clubUUID = UUID.randomUUID();

    @Column(name = "club_name", nullable = false, unique = true)
    @NotBlank(message = "동아리 이름은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 1, max = 10, message = "동아리 이름은 1~10자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "동아리 이름은 영어 또는 한글만 입력 가능합니다.", groups = ValidationGroups.PatternGroup.class)
    private String clubName;

    @Column(name = "leader_name", nullable = false)
    @NotBlank(message = "회장 이름은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 2, max = 30, message = "회장 이름은 2~30자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "회장 이름은 영어 또는 한글만 입력 가능합니다.", groups = ValidationGroups.PatternGroup.class)
    private String leaderName;

    @Column(name = "leader_hp", nullable = false)
    @NotBlank(message = "전화번호는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 11, max = 11, message = "전화번호는 11자여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^01[0-9]{9}$", message = "올바른 휴대전화 번호를 입력하세요.", groups = ValidationGroups.PatternGroup.class)
    private String leaderHp;

    @Column(name = "club_insta")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?instagram\\.com/.+$|^$",
            message = "유효한 인스타그램 링크를 입력해주세요.",
            groups = ValidationGroups.PatternGroup.class)
    private String clubInsta;

    @Column(name = "department", nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(name = "club_room_number", nullable = false)
    @NotBlank(message = "동아리방 호수는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 1, max = 4, message = "동아리방 호수는 1~4자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9A-Za-z]{1,4}$", message = "동아리 방 호수는 1~4자의 숫자 또는 영문이어야 합니다.", groups = ValidationGroups.PatternGroup.class)
    private String clubRoomNumber;

    @PrePersist
    public void prePersist() {
        if (clubUUID == null) {
            this.clubUUID = UUID.randomUUID();
        }
    }

    public void updateClubInfo(String leaderName, String leaderHp, String clubInsta, String clubRoomNumber) {
        this.leaderName = leaderName;
        this.leaderHp = leaderHp;
        this.clubInsta = clubInsta;
        this.clubRoomNumber = clubRoomNumber;
    }
}