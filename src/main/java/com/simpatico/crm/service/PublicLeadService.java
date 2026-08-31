package com.simpatico.crm.service;

import com.simpatico.crm.dto.PublicLeadResponse;
import com.simpatico.crm.dto.PublicLeadSubmission;

/**
 * Service interface defining operations for processing public website lead submissions.
 */
public interface PublicLeadService {

    /**
     * Process a public website form submission: identifies/creates Buyer, registers Lead.
     *
     * @param submission the form submission fields.
     * @return a simple success confirmation record.
     */
    PublicLeadResponse registerPublicLead(PublicLeadSubmission submission);
}
