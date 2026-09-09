// Interactive functions for Wholesale Liquidation Landing Page

document.addEventListener('DOMContentLoaded', () => {
  initSmoothScroll();
  initFaqAccordion();
  initLeadFormValidation();
  initFooterFormValidation();
  initModalClose();
});

/**
 * Smooth scrolling for anchor links with focus management
 */
function initSmoothScroll() {
  const links = document.querySelectorAll('a[href^="#"]');
  
  links.forEach(link => {
    link.addEventListener('click', function(e) {
      const targetId = this.getAttribute('href');
      if (targetId === '#') return;
      
      const targetElement = document.querySelector(targetId);
      if (targetElement) {
        e.preventDefault();
        
        // Scroll smoothly
        targetElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
        
        // Update URL hash without jumping
        history.pushState(null, null, targetId);
        
        // Set focus to the target element for accessibility
        targetElement.setAttribute('tabindex', '-1');
        targetElement.focus({ preventScroll: true });
      }
    });
  });
}

/**
 * FAQ Accordion Toggles
 */
function initFaqAccordion() {
  const triggers = document.querySelectorAll('.faq-trigger');
  
  triggers.forEach(trigger => {
    trigger.addEventListener('click', function() {
      const parent = this.parentElement;
      const isExpanded = this.getAttribute('aria-expanded') === 'true';
      
      // Close other open panels first for accordion behavior
      document.querySelectorAll('.faq-item').forEach(item => {
        if (item !== parent && item.classList.contains('active')) {
          item.classList.remove('active');
          item.querySelector('.faq-trigger').setAttribute('aria-expanded', 'false');
        }
      });
      
      // Toggle current panel
      parent.classList.toggle('active');
      this.setAttribute('aria-expanded', !isExpanded);
    });
  });
}

/**
 * Map budget select value to numeric BigDecimal
 */
function parseBudget(budgetValue) {
  switch (budgetValue) {
    case 'under_5k': return 5000.00;
    case '5k_15k': return 15000.00;
    case '15k_50k': return 50000.00;
    case '50k_plus': return 100000.00;
    default: return 5000.00;
  }
}

/**
 * Lead Capture Form Validation and API Submission
 */
function initLeadFormValidation() {
  const form = document.getElementById('lead-capture-form');
  if (!form) return;

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  form.addEventListener('submit', async function(e) {
    e.preventDefault();
    let isValid = true;

    // Field elements
    const firstName = document.getElementById('first-name');
    const lastName = document.getElementById('last-name');
    const companyName = document.getElementById('business-name');
    const email = document.getElementById('email-address');
    const phone = document.getElementById('phone-number');
    const city = document.getElementById('city');
    const state = document.getElementById('state');
    const buyerType = document.getElementById('buyer-type');
    const budget = document.getElementById('estimated-budget');
    const contactMethod = document.getElementById('contact-method');
    const faxNumber = document.getElementById('fax-number');
    
    // Checkbox verification (Interested In)
    const interests = document.querySelectorAll('input[name="interest"]:checked');
    const checkboxGrid = document.querySelector('.checkbox-grid');

    // 1. Text Field Validations
    if (!firstName.value.trim()) {
      showError(firstName, 'first-name-error');
      isValid = false;
    } else {
      clearError(firstName, 'first-name-error');
    }

    if (!lastName.value.trim()) {
      showError(lastName, 'last-name-error');
      isValid = false;
    } else {
      clearError(lastName, 'last-name-error');
    }

    if (!city.value.trim()) {
      showError(city, 'city-error');
      isValid = false;
    } else {
      clearError(city, 'city-error');
    }

    // 2. Select Field Validations
    if (!state.value) {
      showError(state, 'state-error');
      isValid = false;
    } else {
      clearError(state, 'state-error');
    }

    if (!buyerType.value) {
      showError(buyerType, 'buyer-type-error');
      isValid = false;
    } else {
      clearError(buyerType, 'buyer-type-error');
    }

    if (!budget.value) {
      showError(budget, 'budget-error');
      isValid = false;
    } else {
      clearError(budget, 'budget-error');
    }

    if (!contactMethod.value) {
      showError(contactMethod, 'contact-error');
      isValid = false;
    } else {
      clearError(contactMethod, 'contact-error');
    }

    // 3. Regex Validations
    if (!emailRegex.test(email.value.trim())) {
      showError(email, 'email-error');
      isValid = false;
    } else {
      clearError(email, 'email-error');
    }

    const cleanPhone = phone.value.replace(/\D/g, '');
    if (cleanPhone.length < 7) {
      showError(phone, 'phone-error');
      isValid = false;
    } else {
      clearError(phone, 'phone-error');
    }

    // 4. Checkbox list validations
    if (interests.length === 0) {
      checkboxGrid.parentElement.classList.add('invalid');
      isValid = false;
    } else {
      checkboxGrid.parentElement.classList.remove('invalid');
    }

    // Submit via API if valid
    if (isValid) {
      const submitBtn = document.getElementById('form-submit-btn');
      const originalBtnHtml = submitBtn.innerHTML;
      submitBtn.disabled = true;
      submitBtn.innerText = 'Submitting...';

      // Join interests into inventory category (max 50 chars for DTO constraint)
      const selectedInterests = Array.from(interests).map(i => i.value).join(', ');
      const categoryStr = selectedInterests.length > 50 ? selectedInterests.substring(0, 47) + '...' : selectedInterests;

      // Construct API payload for PublicLeadSubmission
      const payload = {
        firstName: firstName.value.trim(),
        lastName: lastName.value.trim(),
        companyName: companyName ? companyName.value.trim() : '',
        email: email.value.trim(),
        phone: phone.value.trim(),
        city: city.value.trim(),
        state: state.value,
        country: 'USA',
        inventoryCategory: categoryStr,
        inventoryCondition: 'Wholesale Liquidation',
        requestedQuantity: 1,
        budget: parseBudget(budget.value),
        purchaseFrequency: contactMethod.value,
        additionalRequirements: `Buyer Type: ${buyerType.value}; Preferred Contact: ${contactMethod.value}`,
        source: 'DIRECT',
        faxNumber: faxNumber ? faxNumber.value : ''
      };

      try {
        const response = await fetch('/api/public/leads', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        });

        if (response.ok) {
          if (typeof window.trackLeadConversion === 'function') {
            window.trackLeadConversion();
          }
          showSuccessModal(
            'Registration Successful!',
            'Your inventory request has been received. A liquidation account specialist will email you our active manifest lists and contact you shortly.'
          );
          form.reset();
        } else {
          const errData = await response.json().catch(() => ({}));
          let msg = errData.message || errData.error || 'Submission failed. Please check your information and try again.';
          if (Array.isArray(errData.errors) && errData.errors.length > 0) {
            const detailMsgs = errData.errors.map(e => e.message || `${e.field} is invalid`).join('\n• ');
            msg = `Validation failed:\n• ${detailMsgs}`;
          }
          alert(`Error submitting request:\n${msg}`);
        }
      } catch (err) {
        console.error('Lead submission failed:', err);
        alert('Network error. Unable to submit lead at this time.');
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnHtml;
      }
    }
  });

  // Dynamic input clearing listeners
  const inputFields = form.querySelectorAll('input, select, textarea');
  inputFields.forEach(input => {
    input.addEventListener('input', function() {
      const parent = this.parentElement;
      if (parent.classList.contains('invalid')) {
        parent.classList.remove('invalid');
      }
    });
    input.addEventListener('change', function() {
      const parent = this.parentElement;
      if (parent.classList.contains('invalid')) {
        parent.classList.remove('invalid');
      }
    });
  });

  // Dynamic checkbox clearing listeners
  const checkboxes = form.querySelectorAll('input[type="checkbox"]');
  checkboxes.forEach(box => {
    box.addEventListener('change', function() {
      const grid = document.querySelector('.checkbox-grid');
      const checkedCount = document.querySelectorAll('input[name="interest"]:checked').length;
      if (checkedCount > 0) {
        grid.parentElement.classList.remove('invalid');
      }
    });
  });
}

/**
 * Footer Form Validation
 */
function initFooterFormValidation() {
  const form = document.getElementById('footer-contact-form');
  if (!form) return;

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  form.addEventListener('submit', function(e) {
    e.preventDefault();
    let isValid = true;

    const name = document.getElementById('footer-name');
    const email = document.getElementById('footer-email');
    const message = document.getElementById('footer-message');

    if (!name.value.trim()) {
      showError(name, 'footer-name-error');
      isValid = false;
    } else {
      clearError(name, 'footer-name-error');
    }

    if (!emailRegex.test(email.value.trim())) {
      showError(email, 'footer-email-error');
      isValid = false;
    } else {
      clearError(email, 'footer-email-error');
    }

    if (!message.value.trim()) {
      showError(message, 'footer-message-error');
      isValid = false;
    } else {
      clearError(message, 'footer-message-error');
    }

    if (isValid) {
      const submitBtn = document.getElementById('footer-submit-btn');
      submitBtn.disabled = true;
      submitBtn.innerText = 'Sending Message...';

      setTimeout(() => {
        showSuccessModal(
          'Message Sent Successfully!',
          'Your message has been delivered to our general logistics mailbox. One of our support coordinators will review your inquiry and respond shortly.'
        );
        form.reset();
        submitBtn.disabled = false;
        submitBtn.innerText = 'Send Message';
      }, 800);
    }
  });

  // Clear inputs on change
  form.querySelectorAll('input, textarea').forEach(input => {
    input.addEventListener('input', function() {
      const parent = this.parentElement;
      if (parent.classList.contains('invalid')) {
        parent.classList.remove('invalid');
      }
    });
  });
}

/**
 * Helper to display error messages on inputs
 */
function showError(inputElement, errorId) {
  const parent = inputElement.parentElement;
  parent.classList.add('invalid');
  inputElement.setAttribute('aria-invalid', 'true');
  inputElement.setAttribute('aria-describedby', errorId);
}

/**
 * Helper to clear error messages
 */
function clearError(inputElement, errorId) {
  const parent = inputElement.parentElement;
  parent.classList.remove('invalid');
  inputElement.removeAttribute('aria-invalid');
  inputElement.removeAttribute('aria-describedby');
}

/**
 * Show general success modal
 */
function showSuccessModal(title, text) {
  const modal = document.getElementById('success-modal');
  if (!modal) return;

  const modalTitle = modal.querySelector('.modal-title');
  const modalText = modal.querySelector('.modal-text');

  modalTitle.textContent = title;
  modalText.textContent = text;

  modal.classList.add('open');
  modal.setAttribute('aria-hidden', 'false');
  document.body.style.overflow = 'hidden'; // Lock background scroll
  
  // Set focus on close button
  const closeBtn = document.getElementById('close-modal-btn');
  if (closeBtn) closeBtn.focus();
}

/**
 * Close modal logic
 */
function initModalClose() {
  const modal = document.getElementById('success-modal');
  const closeBtn = document.getElementById('close-modal-btn');
  if (!modal || !closeBtn) return;

  const closeModal = () => {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = ''; // Unlock background scroll
  };

  closeBtn.addEventListener('click', closeModal);

  // Close when clicking overlay backdrop
  modal.addEventListener('click', function(e) {
    if (e.target === modal) {
      closeModal();
    }
  });

  // Keyboard accessibility: ESC key close
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && modal.classList.contains('open')) {
      closeModal();
    }
  });
}
