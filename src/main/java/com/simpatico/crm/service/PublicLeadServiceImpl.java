package com.simpatico.crm.service;

import com.simpatico.crm.dto.PublicLeadResponse;
import com.simpatico.crm.dto.PublicLeadSubmission;
import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import com.simpatico.crm.exception.SpamDetectedException;
import com.simpatico.crm.repository.BuyerRepository;
import com.simpatico.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for managing public landing page form submissions.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PublicLeadServiceImpl implements PublicLeadService {

    private final BuyerRepository buyerRepository;
    private final LeadRepository leadRepository;

    @Override
    public PublicLeadResponse registerPublicLead(PublicLeadSubmission submission) {
        // Honeypot spam checking
        if (submission.getFaxNumber() != null && !submission.getFaxNumber().isBlank()) {
            throw new SpamDetectedException("Spam submission detected");
        }

        // Check if buyer already exists by email
        Optional<Buyer> existingBuyer = buyerRepository.findByEmail(submission.getEmail());
        Buyer buyer;

        if (existingBuyer.isPresent()) {
            buyer = existingBuyer.get();
        } else {
            // Create a new buyer profile
            buyer = Buyer.builder()
                    .firstName(submission.getFirstName())
                    .lastName(submission.getLastName())
                    .companyName(submission.getCompanyName())
                    .email(submission.getEmail())
                    .phone(submission.getPhone())
                    .city(submission.getCity())
                    .state(submission.getState())
                    .country(submission.getCountry() != null && !submission.getCountry().isBlank() ? submission.getCountry() : "USA")
                    .active(true)
                    .build();
            buyer = buyerRepository.save(buyer);
        }

        // Create the new lead requirements record
        Lead lead = Lead.builder()
                .buyer(buyer)
                .inventoryCategory(submission.getInventoryCategory())
                .inventoryCondition(submission.getInventoryCondition())
                .requestedQuantity(submission.getRequestedQuantity())
                .budget(submission.getBudget())
                .preferredGeographicArea(submission.getState()) // Defaults preference to state submitted
                .purchaseFrequency(submission.getPurchaseFrequency())
                .additionalRequirements(submission.getAdditionalRequirements())
                .status(LeadStatus.NEW)
                .source(submission.getSource() != null ? submission.getSource() : LeadSource.DIRECT)
                .build();

        Lead savedLead = leadRepository.save(lead);

        return new PublicLeadResponse(true, "Your request has been received.", savedLead.getId());
    }
}
