-- Create match_record table
CREATE TABLE match_record (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    lead_id UUID NOT NULL,
    inventory_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_lead FOREIGN KEY (lead_id) REFERENCES lead(id) ON DELETE RESTRICT,
    CONSTRAINT fk_match_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE RESTRICT,
    CONSTRAINT uq_match_lead_inventory UNIQUE (lead_id, inventory_id)
);

-- Indexes for match queries
CREATE INDEX idx_match_lead_id ON match_record(lead_id);
CREATE INDEX idx_match_inventory_id ON match_record(inventory_id);
CREATE INDEX idx_match_status ON match_record(status);
