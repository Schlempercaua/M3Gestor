-- Add discount column to quotes table
ALTER TABLE quotes ADD COLUMN IF NOT EXISTS discount DOUBLE PRECISION DEFAULT 0.0;
