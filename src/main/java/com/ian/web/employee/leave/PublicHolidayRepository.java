package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {

    List<PublicHoliday> findAllByOrderByHolidayDateAsc();

    /** All holidays in a given calendar year. */
    @Query("SELECT h FROM PublicHoliday h WHERE YEAR(h.holidayDate) = :year ORDER BY h.holidayDate ASC")
    List<PublicHoliday> findByYear(@Param("year") int year);

    /** Holidays that should be excluded from leave working-day calculations, within a date range. */
    @Query("SELECT h FROM PublicHoliday h WHERE h.excludeFromLeave = true " +
           "AND h.holidayDate >= :from AND h.holidayDate <= :to ORDER BY h.holidayDate ASC")
    List<PublicHoliday> findExcludedHolidaysBetween(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);

    boolean existsByHolidayDate(LocalDate holidayDate);
}
