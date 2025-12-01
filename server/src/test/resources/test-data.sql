INSERT INTO users (id, name, email) VALUES
(1, 'John Doe', 'john@email.com'),
(2, 'Jane Smith', 'jane@email.com'),
(3, 'Bob Wilson', 'bob@email.com');

INSERT INTO items (id, name, description, available, owner_id, request_id) VALUES
(1, 'Drill', 'Powerful drill', true, 1, null),
(2, 'Hammer', 'Heavy hammer', true, 1, null),
(3, 'Saw', 'Circular saw', false, 2, null),
(4, 'Wrench', 'Adjustable wrench', true, 3, null);

INSERT INTO bookings (id, start_time, end_time, item_id, booker_id, status) VALUES
(1, '2024-01-15 10:00:00', '2024-01-20 10:00:00', 1, 2, 'APPROVED'),
(2, '2024-02-01 10:00:00', '2024-02-05 10:00:00', 1, 3, 'WAITING'),
(3, '2024-01-10 10:00:00', '2024-01-12 10:00:00', 2, 2, 'APPROVED'),
(4, '2024-03-01 10:00:00', '2024-03-05 10:00:00', 4, 1, 'REJECTED');

INSERT INTO requests (id, description, requester_id, created) VALUES
(1, 'Need a power saw', 2, '2024-01-01 10:00:00'),
(2, 'Looking for gardening tools', 3, '2024-01-02 10:00:00');

INSERT INTO comments (id, text, item_id, author_id, created) VALUES
(1, 'Great drill!', 1, 2, '2024-01-25 10:00:00'),
(2, 'Very reliable', 2, 2, '2024-01-13 10:00:00');