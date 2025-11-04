INSERT INTO users (username, password, role) VALUES
                                                 ('admin', '$2a$10$VbHnY3E7M5Kj8M8Q1KJt0uhxXb0zGuqgsk1F6D1aQIZv4Sx2FQk9m', 'ROLE_ADMIN'),
                                                 ('user1', '$2a$10$QpluA8Fh2GcGkgBhAj8C8OZJd4sN4D4SxC0FZdA3w5KqYpUqjWk7m', 'ROLE_USER');

INSERT INTO accommodations (name, location, description, price_per_night, amenities) VALUES
                                                                                         ('台北商旅', '台北', '位於信義區的商務旅館', 2200, 'WiFi, 早餐, 停車場'),
                                                                                         ('高雄港景飯店', '高雄', '擁有海景陽台', 1800, 'WiFi, 健身房, 游泳池');
