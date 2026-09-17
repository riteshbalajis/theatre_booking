ALTER TABLE bookings
    ADD COLUMN ticket_code VARCHAR(64) NULL,
    ADD COLUMN ticket_status ENUM('VALID', 'USED', 'CANCELLED') NULL,
    ADD CONSTRAINT uq_bookings_ticket_code UNIQUE (ticket_code);
