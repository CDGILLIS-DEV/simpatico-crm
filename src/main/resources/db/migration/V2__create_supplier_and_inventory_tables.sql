-- Create supplier table
CREATE TABLE supplier (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50) DEFAULT 'USA',
    website VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create inventory table
CREATE TABLE inventory (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    supplier_id UUID NOT NULL,
    title VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    condition VARCHAR(50) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    unit_type VARCHAR(20) NOT NULL,
    asking_price DECIMAL(12, 2) NOT NULL,
    location VARCHAR(100),
    availability_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE RESTRICT
);

-- Indexes for frequent searches
CREATE INDEX idx_supplier_company_name ON supplier(company_name);
CREATE INDEX idx_supplier_status ON supplier(status);
CREATE INDEX idx_inventory_supplier_id ON inventory(supplier_id);
CREATE INDEX idx_inventory_category ON inventory(category);
CREATE INDEX idx_inventory_condition ON inventory(condition);
CREATE INDEX idx_inventory_asking_price ON inventory(asking_price);
CREATE INDEX idx_inventory_location ON inventory(location);
CREATE INDEX idx_inventory_status ON inventory(availability_status);
CREATE INDEX idx_inventory_created_at ON inventory(created_at DESC);
