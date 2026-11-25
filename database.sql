-- LifeLink - Local Blood Donor Finder
-- MySQL database setup script
-- Run this file in MySQL Workbench or with: mysql -u YOUR_USERNAME -p < lifelink_database.sql

CREATE DATABASE IF NOT EXISTS lifelink
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE lifelink;

-- 1. Common account details for both donors and seekers
CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('DONOR', 'SEEKER') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    phone VARCHAR(10) NOT NULL,
    city VARCHAR(100) NOT NULL,
    area VARCHAR(150) NOT NULL,
    pincode VARCHAR(6) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

-- 2. Donor-specific details
CREATE TABLE IF NOT EXISTS donors (
    donor_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    available TINYINT(1) NOT NULL DEFAULT 1,
    last_donation_date DATE DEFAULT NULL,
    PRIMARY KEY (donor_id),
    UNIQUE KEY uq_donors_user_id (user_id),
    CONSTRAINT fk_donors_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Blood requests created by seekers
CREATE TABLE IF NOT EXISTS blood_requests (
    request_id INT NOT NULL AUTO_INCREMENT,
    seeker_id INT NOT NULL,
    blood_group ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    hospital_name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    units_needed INT NOT NULL,
    urgency ENUM('NORMAL', 'URGENT', 'EMERGENCY') NOT NULL DEFAULT 'NORMAL',
    status ENUM('OPEN', 'CANCELLED', 'FULFILLED') NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    selected_donor_id INT DEFAULT NULL,
    PRIMARY KEY (request_id),
    KEY idx_requests_seeker (seeker_id),
    KEY idx_requests_status_blood_group (status, blood_group),
    CONSTRAINT fk_requests_seeker
        FOREIGN KEY (seeker_id) REFERENCES users(user_id),
    CONSTRAINT fk_requests_selected_donor
        FOREIGN KEY (selected_donor_id) REFERENCES donors(donor_id)
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Donor responses to blood requests
CREATE TABLE IF NOT EXISTS donor_responses (
    response_id INT NOT NULL AUTO_INCREMENT,
    request_id INT NOT NULL,
    donor_id INT NOT NULL,
    response_status ENUM('INTERESTED', 'WITHDRAWN', 'SELECTED', 'NOT_SELECTED')
        NOT NULL DEFAULT 'INTERESTED',
    responded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (response_id),
    UNIQUE KEY uq_request_donor (request_id, donor_id),
    KEY idx_responses_donor (donor_id),
    CONSTRAINT fk_responses_request
        FOREIGN KEY (request_id) REFERENCES blood_requests(request_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_responses_donor
        FOREIGN KEY (donor_id) REFERENCES donors(donor_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Note: This script creates the database structure only.
-- User accounts and password hashes are created by the LifeLink application.
