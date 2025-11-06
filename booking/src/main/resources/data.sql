INSERT INTO users (username, password, role) VALUES
                                                 ('admin', '$2a$10$VbHnY3E7M5Kj8M8Q1KJt0uhxXb0zGuqgsk1F6D1aQIZv4Sx2FQk9m', 'ROLE_ADMIN'),
                                                 ('user1', '$2a$10$QpluA8Fh2GcGkgBhAj8C8OZJd4sN4D4SxC0FZdA3w5KqYpUqjWk7m', 'ROLE_USER');

INSERT INTO accommodations (name, location, description, price_per_night, amenities) VALUES
                                                                                         ('台北商旅', '台北', '位於信義區的商務旅館', 2200, 'WiFi, 早餐, 停車場'),
                                                                                         ('高雄港景飯店', '高雄', '擁有海景陽台', 1800, 'WiFi, 健身房, 游泳池');

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    booked_quantity INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- Sample data for room_types
INSERT INTO room_types (id, name, description, price_per_night) VALUES
(1, '標準房', '基本設施的標準房型', 2200),
(2, '豪華房', '提供更高級設施的房型', 4500);

-- Sample data for bookings
INSERT INTO bookings (user_id, room_type_id, check_in, check_out, booked_quantity, total_price, status) VALUES
(1, 1, '2025-11-01', '2025-11-05', 2, 8800.00, 'CONFIRMED'),
(2, 2, '2025-11-10', '2025-11-15', 1, 9000.00, 'PENDING');
