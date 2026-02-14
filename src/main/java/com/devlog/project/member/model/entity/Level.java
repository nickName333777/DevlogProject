package com.devlog.project.member.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LEVELS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
public class Level {

    @Id
    @Column(name = "LEVEL_NO")
    private Integer levelNo;
 
    @Column(name = "REQUIRED_TOTAL_EXP", nullable = false)
    private Integer requiredTotalExp;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;
}

