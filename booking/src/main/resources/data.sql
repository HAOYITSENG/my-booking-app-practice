-- users
insert into users (id, username, password) values (1, 'user', 'password');
insert into users (id, username, password) values (2, 'admin', 'admin');

-- accommodations
insert into accommodations (id, name, location, description, price_per_night) values
                                                                                  (1, 'Taipei 101 View', 'Taipei', 'City view near 101', 3200),
                                                                                  (2, 'Kaohsiung Harbor Inn', 'Kaohsiung', 'Near harbor', 2400),
                                                                                  (3, 'Tainan Old House', 'Tainan', 'Historic district', 1800);
