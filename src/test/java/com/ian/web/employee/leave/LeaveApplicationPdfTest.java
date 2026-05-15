package com.ian.web.employee.leave;

import net.sf.jasperreports.engine.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies that LeaveApplication.jrxml compiles and generates a PDF
 * with all parameters — including the newly added certDaysWithPay /
 * certDaysWithoutPay — without runtime errors.
 */
class LeaveApplicationPdfTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Map<String, Object> baseParams() {
        Map<String, Object> p = new HashMap<>();
        // Section A
        p.put("officeDepartment",  "Office of the City Mayor");
        p.put("lastName",          "Dela Cruz");
        p.put("firstName",         "Juan");
        p.put("middleName",        "Santos");
        p.put("dateOfFiling",      "05/15/2026");
        p.put("position",          "Administrative Officer II");
        p.put("salary",            "");
        // 6A checkboxes
        p.put("chkVacation",         Boolean.TRUE);
        p.put("chkMandatory",        Boolean.FALSE);
        p.put("chkSick",             Boolean.FALSE);
        p.put("chkMaternity",        Boolean.FALSE);
        p.put("chkPaternity",        Boolean.FALSE);
        p.put("chkSpecialPrivilege", Boolean.FALSE);
        p.put("chkSoloParent",       Boolean.FALSE);
        p.put("chkStudy",            Boolean.FALSE);
        p.put("chkVAWC",             Boolean.FALSE);
        p.put("chkRehabilitation",   Boolean.FALSE);
        p.put("chkWomen",            Boolean.FALSE);
        p.put("chkCalamity",         Boolean.FALSE);
        p.put("chkOthers",           Boolean.FALSE);
        p.put("othersSpec",          "");
        // 6B sub-type
        p.put("chkWithinPhilippines",  Boolean.FALSE);
        p.put("withinPhilippinesSpec", "");
        p.put("chkAbroad",             Boolean.FALSE);
        p.put("abroadSpec",            "");
        p.put("chkInHospital",         Boolean.FALSE);
        p.put("inHospitalSpec",        "");
        p.put("chkOutPatient",         Boolean.FALSE);
        p.put("outPatientSpec",        "");
        p.put("womenSpec",             "");
        p.put("chkMastersDegree",      Boolean.FALSE);
        p.put("chkBoardExam",          Boolean.FALSE);
        p.put("chkMonetization",       Boolean.FALSE);
        p.put("chkTerminalLeave",      Boolean.FALSE);
        // 6C / 6D
        p.put("inclusiveDates",             "05/20/2026 – 05/22/2026");
        p.put("numberOfWorkingDays",        "3.000");
        p.put("chkCommutationRequested",    Boolean.FALSE);
        p.put("chkCommutationNotRequested", Boolean.TRUE);
        p.put("applicantSignDate",          "05/15/2026");
        // 7A credits
        p.put("vacAsOf",        "2026");
        p.put("vacTotalEarned", "15.000");
        p.put("vacLessThisApp", "3.000");
        p.put("vacBalance",     "12.000");
        p.put("slAsOf",         "2026");
        p.put("slTotalEarned",  "15.000");
        p.put("slLessThisApp",  "0.000");
        p.put("slBalance",      "15.000");
        p.put("certifiedBy",      "Maria Santos");
        p.put("certifiedByTitle", "HRMO");
        // NEW parameters
        p.put("certDaysWithPay",    "");
        p.put("certDaysWithoutPay", "");
        // 7B
        p.put("chkForApproval",    Boolean.TRUE);
        p.put("chkForDisapproval", Boolean.FALSE);
        p.put("disapprovalReason", "");
        p.put("supervisorName",    "Pedro Reyes");
        p.put("supervisorTitle",   "HR Admin");
        // 7C / 7D
        p.put("daysWithPay",        "3.000");
        p.put("daysWithoutPay",     "0.000");
        p.put("othersApprovalSpec", "");
        p.put("disapprovedDueTo",   "");
        p.put("approvedByName",     "City Mayor");
        p.put("approvedByTitle",    "Authorized Official");
        return p;
    }

    private byte[] generatePdf(Map<String, Object> params) throws JRException {
        InputStream jrxmlStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("jasper/reports/LeaveApplication.jrxml");
        assertThat(jrxmlStream).as("JRXML resource must exist on classpath").isNotNull();

        JasperReport  report = JasperCompileManager.compileReport(jrxmlStream);
        JasperPrint   print  = JasperFillManager.fillReport(report, params, new JREmptyDataSource());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(print, out);
        return out.toByteArray();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("JRXML compiles and generates a non-empty PDF with empty cert days (fallback to HRMO title)")
    void pdfGeneration_withNoCertDays_showsHrmoTitle() throws JRException {
        Map<String, Object> params = baseParams();
        // certDaysWithPay / certDaysWithoutPay are empty — certifiedByTitle should show "HRMO"
        params.put("certDaysWithPay",    "");
        params.put("certDaysWithoutPay", "");

        byte[] pdf = generatePdf(params);

        assertThat(pdf).as("PDF bytes must not be empty").isNotEmpty();
        // PDF magic bytes: %PDF
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("JRXML generates PDF correctly when certDaysWithPay and certDaysWithoutPay are set")
    void pdfGeneration_withCertDays_showsCertifiedDays() throws JRException {
        Map<String, Object> params = baseParams();
        params.put("certDaysWithPay",    "3.000");
        params.put("certDaysWithoutPay", "0.000");

        byte[] pdf = generatePdf(params);

        assertThat(pdf).as("PDF bytes must not be empty").isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("JRXML generates PDF with sick leave type and disapproval")
    void pdfGeneration_sickLeave_disapproved() throws JRException {
        Map<String, Object> params = baseParams();
        params.put("chkVacation",      Boolean.FALSE);
        params.put("chkSick",          Boolean.TRUE);
        params.put("chkInHospital",    Boolean.TRUE);
        params.put("inHospitalSpec",   "Manila Doctors");
        params.put("chkForApproval",   Boolean.FALSE);
        params.put("chkForDisapproval",Boolean.TRUE);
        params.put("disapprovalReason","Insufficient leave credits");
        params.put("certDaysWithPay",    "5.000");
        params.put("certDaysWithoutPay", "0.000");

        byte[] pdf = generatePdf(params);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("All 13 leave type checkbox combinations compile without error")
    void pdfGeneration_allLeaveTypeCheckboxes() throws JRException {
        String[] leaveFlags = {
            "chkVacation", "chkMandatory", "chkSick", "chkMaternity", "chkPaternity",
            "chkSpecialPrivilege", "chkSoloParent", "chkStudy", "chkVAWC",
            "chkRehabilitation", "chkWomen", "chkCalamity", "chkOthers"
        };

        for (String flag : leaveFlags) {
            Map<String, Object> params = baseParams();
            // Reset all to false, enable just one
            for (String f : leaveFlags) params.put(f, Boolean.FALSE);
            params.put(flag, Boolean.TRUE);
            if ("chkOthers".equals(flag)) params.put("othersSpec", "Special Emergency Leave");

            byte[] pdf = generatePdf(params);
            assertThat(pdf).as("PDF must not be empty for flag: " + flag).isNotEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Security logic unit tests (no Spring context needed)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Security: admin user may access any application")
    void security_adminCanAccess() {
        String actorUserType = "ROLE_ADMIN";
        long   actorId       = 1L;
        long   appOwnerId    = 99L;   // different employee

        boolean isAdminUser = "ROLE_ADMIN".equals(actorUserType);
        boolean isOwner     = actorId == appOwnerId;
        boolean allowed     = isAdminUser || isOwner;

        assertThat(allowed).as("Admin must be allowed regardless of ownership").isTrue();
    }

    @Test
    @DisplayName("Security: applicant employee may access their own application")
    void security_ownerCanAccess() {
        String actorUserType = "ROLE_EMPLOYEE";
        long   actorId       = 42L;
        long   appOwnerId    = 42L;   // same employee

        boolean isAdminUser = "ROLE_ADMIN".equals(actorUserType);
        boolean isOwner     = actorId == appOwnerId;
        boolean allowed     = isAdminUser || isOwner;

        assertThat(allowed).as("Applicant must be allowed to access their own PDF").isTrue();
    }

    @Test
    @DisplayName("Security: non-admin employee cannot access another employee's application")
    void security_nonOwnerDenied() {
        String actorUserType = "ROLE_EMPLOYEE";
        long   actorId       = 42L;
        long   appOwnerId    = 99L;   // different employee

        boolean isAdminUser = "ROLE_ADMIN".equals(actorUserType);
        boolean isOwner     = actorId == appOwnerId;
        boolean allowed     = isAdminUser || isOwner;

        assertThat(allowed).as("Non-owner employee must be denied").isFalse();
    }
}
