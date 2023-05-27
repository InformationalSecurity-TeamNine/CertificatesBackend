insert into Users(email, password, name, surname, telephone_number, role, is_email_confirmed, last_time_password_changed)
values ('tim9certificates@gmail.com', '$2a$12$NZdYWr0/HiLjVwQttZXHteArZshhCeG4qCtcwsjw1DCAIICRF89IG', 'Marko', 'Markovic', '0642421412', 'ADMIN', true,  '2023-05-13 12:55:58.588198');

insert into Users(email, password, name, surname, telephone_number, role, is_email_confirmed, last_time_password_changed)
values ('nebojsa@gmail.com', '$2a$12$HXFqjPtx.FE7OP530tCzQOMibvJx8RfFzzUJKoCB4wo7ugTTSReS6', 'Nebojsa', 'Vuga', '069512512', 'BASIC_USER', true, '2023-03-23 12:55:58.588198');

insert into Users(email, password, name, surname, telephone_number, role, is_email_confirmed, last_time_password_changed)
values ('bogdan@gmail.com', '$2a$12$2AC05eNYundzzLYOhY14w.cZE/UBIIr4xZZ3N42.Gxd8DTlcggliW', 'Bogdan', 'Janosevic', '+381659715120', 'BASIC_USER', true, '2023-05-13 12:55:58.588198');

insert into Users(email, password, name, surname, telephone_number, role, is_email_confirmed, last_time_password_changed)
values ('dusan@gmail.com', '$2a$12$pOWqvnMcnitY5KihwitVd.SKXLTqQq7uybnKYv3161mYmKqKCglOS', 'Dusan', 'Bibin', '06421412', 'BASIC_USER', true, '2023-03-23 12:55:58.588198');

insert into Users(email, password, name, surname, telephone_number, role, is_email_confirmed, last_time_password_changed)
values ('mirko@gmail.com', '$2a$12$VfsULMyyWkkn64G70ERZ3.lrXnKTfrft6VQwYJ1PZk59DkJp0yuWq', 'Mirko', 'Babic', '06444421', 'BASIC_USER', false,'2023-03-23 12:55:58.588198');

insert into certificate_request(id, certificate_type, reason, status, time, issuer_id, parent_certificate_id)
values (1, 'ROOT', null, 'ACCEPTED', '2027-12-27 12:29:00.00000', 1, null);

insert into certificate(id, public_key, serial_number, signature_algorithm, status, type, valid_from, valid_to, issuing_certificate_id, user_id)
values (1, '24106', '48f1a809-a808-4c19-87e0-9c5203013342', 'SHA256WithRSAEncryption', 'VALID', 'ROOT',  '2023-05-26 12:29:00.00000', '2027-12-27 12:29:00.00000', null, 1);