ALTER TABLE bookings
    ADD CONSTRAINT uq_bookings_booking_reference UNIQUE (booking_reference);
