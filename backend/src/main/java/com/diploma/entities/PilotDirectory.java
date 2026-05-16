package com.diploma.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pilot_directory", schema = "dbo")
@NoArgsConstructor
public class PilotDirectory {
    @Id
    @Column(name = "tab_no")
    private Integer tabNo;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "rank_name")
    private String rankName;

    @Column(name = "active")
    private Boolean active = true;
}
