/**
 * Privacy-Conscious Analytics & Conversion Tracking Helper
 * Configurable for Google Analytics 4 (GA4) and Google Ads Conversion Tracking.
 * 
 * IMPORTANT: Ensures zero PII (name, email, phone, message) is sent to analytics providers.
 */

(function() {
  // Configurable tracking IDs (replace with actual IDs in production or set via window globals)
  const gaId = window.GA_MEASUREMENT_ID || null;
  const googleAdsId = window.GOOGLE_ADS_CONVERSION_ID || null;

  // Initialize dataLayer and gtag if tracking ID is present
  if (gaId || googleAdsId) {
    window.dataLayer = window.dataLayer || [];
    function gtag(){ dataLayer.push(arguments); }
    window.gtag = gtag;
    gtag('js', new Date());

    if (gaId) {
      // Dynamically load GA4 script tag
      const script = document.createElement('script');
      script.async = true;
      script.src = `https://www.googletagmanager.com/gtag/js?id=${gaId}`;
      document.head.appendChild(script);

      gtag('config', gaId, {
        anonymize_ip: true,
        send_page_view: true
      });
    }

    if (googleAdsId) {
      gtag('config', googleAdsId);
    }
  }
})();

/**
 * Triggers privacy-safe lead conversion tracking event.
 * MUST only be invoked AFTER successful lead form submission API response (HTTP 200/201).
 */
function trackLeadConversion() {
  if (typeof window.gtag === 'function') {
    // 1. Google Analytics 4 Conversion Event (No PII passed)
    window.gtag('event', 'generate_lead', {
      event_category: 'Lead Capture',
      event_label: 'Wholesale Lead Submission',
      value: 1.0
    });

    // 2. Google Ads Conversion Event Hook (If configured)
    if (window.GOOGLE_ADS_CONVERSION_ID && window.GOOGLE_ADS_CONVERSION_LABEL) {
      window.gtag('event', 'conversion', {
        'send_to': `${window.GOOGLE_ADS_CONVERSION_ID}/${window.GOOGLE_ADS_CONVERSION_LABEL}`,
        'value': 1.0,
        'currency': 'USD'
      });
    }
  }
}

// Export for global browser usage
window.trackLeadConversion = trackLeadConversion;
