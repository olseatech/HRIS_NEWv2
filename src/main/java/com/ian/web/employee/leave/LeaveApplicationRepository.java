package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ian.web.employee.leave.LeaveApplication.LeaveStatus;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployeeIdOrderByAppliedDateTimeDesc(Long employeeId);

    List<LeaveApplication> findByStatusOrderByAppliedDateTimeDesc(LeaveStatus status);

    List<LeaveApplication> findAllByOrderByAppliedDateTimeDesc();

    List<LeaveApplication> findByEmployeeIdAndStatusOrderByAppliedDateTimeDesc(Long employeeId, LeaveStatus status);

    @Query("SELECT a FROM LeaveApplication a WHERE a.employee.id = :empId AND a.leaveType.id = :typeId ORDER BY a.dateFrom DESC")
    List<LeaveApplication> findByEmployeeAndLeaveType(@Param("empId") Long employeeId, @Param("typeId") Long leaveTypeId);

    long countByStatus(LeaveStatus status);

    @Query("SELECT a FROM LeaveApplication a WHERE a.status = :#{T(com.ian.web.employee.leave.LeaveApplication.LeaveStatus).PENDING} ORDER BY a.appliedDateTime ASC")
    List<LeaveApplication> findAllPending();

    @Query("SELECT a FROM LeaveApplication a WHERE a.status = :#{T(com.ian.web.employee.leave.LeaveApplication.LeaveStatus).ENDORSED} ORDER BY a.appliedDateTime ASC")
    List<LeaveApplication> findAllEndorsed();

    /** Active applications awaiting action (PENDING or ENDORSED). */
    @Query("SELECT a FROM LeaveApplication a WHERE a.status IN :statuses ORDER BY a.appliedDateTime ASC")
    List<LeaveApplication> findAllByStatusIn(@Param("statuses") java.util.Collection<LeaveStatus> statuses);

    /** Convenience no-arg method — delegates to findAllByStatusIn. */
    default List<LeaveApplication> findAllActionable() {
        return findAllByStatusIn(java.util.Arrays.asList(LeaveStatus.PENDING, LeaveStatus.ENDORSED));
    }

    /**
     * Overlap check — existing non-terminal applications for the same employee
     * whose date range overlaps the proposed [dateFrom, dateTo].
     */
    @Query("SELECT a FROM LeaveApplication a " +
           "WHERE a.employee.id = :empId " +
           "AND a.status NOT IN :terminalStatuses " +
           "AND a.dateFrom <= :dateTo AND a.dateTo >= :dateFrom")
    List<LeaveApplication> findOverlapping(
            @Param("empId")            Long                        employeeId,
            @Param("dateFrom")         LocalDate                   dateFrom,
            @Param("dateTo")           LocalDate                   dateTo,
            @Param("terminalStatuses") java.util.Collection<LeaveStatus> terminalStatuses);

    /** Same overlap check, excluding a specific application ID (for edit scenarios). */
    @Query("SELECT a FROM LeaveApplication a " +
           "WHERE a.employee.id = :empId " +
           "AND a.id <> :excludeId " +
           "AND a.status NOT IN :terminalStatuses " +
           "AND a.dateFrom <= :dateTo AND a.dateTo >= :dateFrom")
    List<LeaveApplication> findOverlappingExcluding(
            @Param("empId")            Long                        employeeId,
            @Param("excludeId")        Long                        excludeId,
            @Param("dateFrom")         LocalDate                   dateFrom,
            @Param("dateTo")           LocalDate                   dateTo,
            @Param("terminalStatuses") java.util.Collection<LeaveStatus> terminalStatuses);

    /** Filter by leave type for reports. */
    @Query("SELECT a FROM LeaveApplication a WHERE a.leaveType.id = :typeId ORDER BY a.appliedDateTime DESC")
    List<LeaveApplication> findByLeaveTypeId(@Param("typeId") Long leaveTypeId);

    /** Applications filed within a date range (by appliedDateTime). */
    @Query("SELECT a FROM LeaveApplication a WHERE a.appliedDateTime >= :from AND a.appliedDateTime <= :to ORDER BY a.appliedDateTime DESC")
    List<LeaveApplication> findByAppliedDateRange(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /** All approved applications for a given calendar year (by leave start date). */
    @Query("SELECT a FROM LeaveApplication a " +
           "WHERE a.status = :#{T(com.ian.web.employee.leave.LeaveApplication.LeaveStatus).APPROVED} " +
           "AND YEAR(a.dateFrom) = :year ORDER BY a.dateFrom DESC")
    List<LeaveApplication> findApprovedForYear(@Param("year") int year);

    /** All applications for a specific employee in a given year. */
    @Query("SELECT a FROM LeaveApplication a " +
           "WHERE a.employee.id = :empId AND YEAR(a.dateFrom) = :year ORDER BY a.dateFrom DESC")
    List<LeaveApplication> findByEmployeeAndYear(@Param("empId") Long employeeId, @Param("year") int year);
}
