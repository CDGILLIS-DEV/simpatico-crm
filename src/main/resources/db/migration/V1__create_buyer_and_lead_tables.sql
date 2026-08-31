-- Create buyer table
CREATE TABLE buyer (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    company_name VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address_line1 VARCHAR(150),
    address_line2 VARCHAR(150),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    country VARCHAR(50) DEFAULT 'USA',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create lead table
CREATE TABLE lead (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    buyer_id UUID NOT NULL,
    inventory_category VARCHAR(50) NOT NULL,
    inventory_condition VARCHAR(50) NOT NULL,
    requested_quantity INT,
    budget DECIMAL(12, 2),
    preferred_geographic_area VARCHAR(100),
    purchase_frequency VARCHAR(50),
    additional_requirements TEXT,
    status VARCHAR(50) NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lead_buyer FOREIGN KEY (buyer_id) REFERENCES buyer(id) ON DELETE RESTRICT
);

-- Indexes for frequent searches
CREATE INDEX idx_buyer_company_name ON buyer(company_name);
CREATE INDEX idx_buyer_last_name ON buyer(last_name);
CREATE INDEX idx_lead_buyer_id ON lead(buyer_id);
CREATE INDEX idx_lead_status ON lead(status);
CREATE INDEX idx_lead_created_at ON lead(created_at DESC);
