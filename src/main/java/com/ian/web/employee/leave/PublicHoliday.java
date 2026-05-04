package com.ian.web.employee.leave;

import java.time.LocalDate;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.*;

/**
 * Philippine public holidays used to exclude non-working days
 * from leave working-day calculations.
 *
 * Holiday types per CSC / Malacañang proclamations:
 *   REGULAR    — Regular holiday (double pay if worked)
 *   SPECIAL_NW — Special Non-Working holiday
 *   SPECIAL_W  — Special Working holiday (no extra pay)
 */
@Entity
@Table(name = "public_holiday",
       uniqueConstraints = @UniqueConstraint(columnNames = "holidayDate"))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate holidayDate;

    @NotBlank
    @Column(length = 200)
    private String holidayName;

    /**
     * "REGULAR" | "SPECIAL_NW" | "SPECIAL_W"
     * Only REGULAR and SPECIAL_NW days are excluded from working-day counts.
     */
    @Column(length = 20)
    private String holidayType = "REGULAR";

    /** Whether this holiday should be excluded from leave working-day computation. */
    private boolean excludeFromLeave = true;

    /** Optional remarks / proclamation reference number. */
    @Column(length = 300)
    private String remarks;

    @Transient
    private String showMode;
}
