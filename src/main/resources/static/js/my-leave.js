/**
 * my-leave.js  —  My Leave self-service page
 *
 * All dynamic values are read from data-* attributes on #ml-config so that
 * this file is a plain static resource and Thymeleaf never processes it as
 * JavaScript (no th:inline="javascript").  That eliminates the entire class
 * of ERR_INCOMPLETE_CHUNKED_ENCODING bugs that happen when a fragment include
 * injects a </script> tag inside a Thymeleaf JS inline block.
 *
 * Consumed data-* attributes (on div#ml-config):
 *   data-msg-code  — uxmessage.code   ("SUCCESS" | "ERROR" | "")
 *   data-msg-text  — uxmessage.message (already HTML-escaped by Thymeleaf)
 *   data-ctx       — servlet context path, e.g. "/hrisp"
 */
(function () {
    'use strict';

    // -----------------------------------------------------------------------
    // Bootstrap
    // -----------------------------------------------------------------------
    document.addEventListener('DOMContentLoaded', function () {
        var cfg     = document.getElementById('ml-config');
        var msgCode = cfg ? (cfg.getAttribute('data-msg-code') || '') : '';
        var msgText = cfg ? (cfg.getAttribute('data-msg-text') || '') : '';
        var ctx     = cfg ? (cfg.getAttribute('data-ctx')      || '') : '';

        // Flash notification via SweetAlert (loaded by common head fragment)
        if (typeof swal === 'function') {
            if (msgCode === 'ERROR') {
                swal({ title: 'Error',   text: msgText, icon: 'error',   buttons: false, timer: 3000 });
            } else if (msgCode === 'SUCCESS') {
                swal({ title: 'Success', text: msgText, icon: 'success', buttons: false, timer: 2200 });
            }
        }

        // DataTables — only init if the table is present (empty-state renders no table)
        if (typeof $ !== 'undefined' && $.fn && $.fn.DataTable) {
            var $tbl = $('#table-my-leave');
            if ($tbl.length) {
                try {
                    $tbl.DataTable({
                        pageLength: 15,
                        order: [[6, 'desc']],
                        columnDefs: [{ orderable: false, targets: [7] }]
                    });
                } catch (e) {
                    console.warn('DataTable init error (my-leave):', e);
                }
            }
        }

        // Re-submit modal — populate form action + fields from data-* on the button
        $(document).on('click', '.btnResubmit', function () {
            var id      = $(this).data('id');
            var reason  = $(this).data('reason')  || '';
            var details = $(this).data('details') || '';
            $('#resubmitForm').attr('action', ctx + '/my-leave-resubmit/' + id);
            $('#resubmitReason').val(reason);
            $('#resubmitDetails').val(details);
            $('#modalResubmit').modal('show');
        });

        // Auto-calculate days when either date input changes
        $('#inputDateFrom, #inputDateTo').on('change', calculateDays);

        // Initialize attachment requirement if a leave type is preselected
        var leaveTypeSelect = document.getElementById('leaveTypeSelect');
        if (leaveTypeSelect && leaveTypeSelect.value) {
            window.updateLeaveSubTypeFields(leaveTypeSelect);
        }
    });

    // -----------------------------------------------------------------------
    // Leave sub-type toggling (called from onchange on the leave-type select)
    // -----------------------------------------------------------------------
    function setAttachmentRequired(isRequired) {
        var input = document.getElementById('supportingDocument');
        var badge = document.getElementById('supportingDocBadge');
        if (!input) {
            return;
        }
        input.required = !!isRequired;
        if (badge) {
            badge.classList.toggle('d-none', !isRequired);
        }
    }

    window.updateLeaveSubTypeFields = function (sel) {
        var opt  = sel.options[sel.selectedIndex];
        var code = opt ? (opt.getAttribute('data-code') || '') : '';
        var req  = opt ? (opt.getAttribute('data-requires-doc') || '') : '';

        // Disable all hidden-row inputs so they are NOT submitted with the form.
        // Without this, all three leaveSubType selects and leaveDetails inputs send
        // values and Spring's @RequestParam picks the first one in document order.
        $('#vlSubTypeRow, #slSubTypeRow, #otherDetailsRow')
            .hide()
            .find('input, select, textarea')
            .prop('disabled', true);

        if (code === 'VL') {
            $('#vlSubTypeRow').show().find('input, select, textarea').prop('disabled', false);
        } else if (code === 'SL') {
            $('#slSubTypeRow').show().find('input, select, textarea').prop('disabled', false);
        } else if (code !== '') {
            $('#otherDetailsRow').show().find('input, select, textarea').prop('disabled', false);
        }
        setAttachmentRequired(req === 'true');
    };

    // -----------------------------------------------------------------------
    // Working-day counter (Mon–Fri, no holiday exclusion — server recalculates)
    // -----------------------------------------------------------------------
    window.calculateDays = function () {
        var from = new Date($('#inputDateFrom').val());
        var to   = new Date($('#inputDateTo').val());
        if (isNaN(from.getTime()) || isNaN(to.getTime()) || to < from) {
            $('#computedDays').val('');
            return;
        }
        var count = 0;
        var cur   = new Date(from);
        while (cur <= to) {
            var dow = cur.getDay();
            if (dow !== 0 && dow !== 6) { count++; }
            cur.setDate(cur.getDate() + 1);
        }
        $('#computedDays').val(count + ' working day(s)');
    };

}());
