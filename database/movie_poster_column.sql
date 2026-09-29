-- Add poster_filename column to movies table
ALTER TABLE movies
    ADD COLUMN poster_filename VARCHAR(255) NULL;
