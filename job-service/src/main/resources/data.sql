insert into category (id, name, number_choose) values
    (1, 'JAVA', 3),
    (2, 'NODEJS', 2),
    (3, 'PHP', 4),
    (4, 'ASP.NET', 1),
    (5, 'PYTHON', 2),
    (6, 'C++', 1),
    (7, 'KOTLIN', 3)
on conflict (id) do nothing;

insert into role (id, role_name) values
    (1, 'EMPLOYER'),
    (2, 'USER')
on conflict (id) do nothing;

insert into cv (id, file_name) values
    (1, 'john-cv.pdf'),
    (2, 'mary-cv.pdf'),
    (3, 'susan-cv.pdf'),
    (4, 'fancy-cv.pdf'),
    (5, 'beck-cv.pdf'),
    (6, 'alex-cv.pdf')
on conflict (id) do nothing;

insert into users (id, address, description, email, email_verified, email_verified_at, full_name, image, password, phone_number, status, role_id, cv_id) values
    (1, 'Da Nang', 'Employer account', 'john@gmail.com', true, timestamp '2026-04-01 00:00:00', 'John', 'person-1.jpg', '$2a$12$f0cTRhHKbeSF/pxfEljKOOXyJErdOtmFo/jj3O0N7bl0.dTEKLck.', '0905123456', 1, 1, 1),
    (2, 'Hue', 'Candidate account', 'mary@gmail.com', true, timestamp '2026-04-01 00:00:00', 'Mary', 'person-2.jpg', '$2a$12$f0cTRhHKbeSF/pxfEljKOOXyJErdOtmFo/jj3O0N7bl0.dTEKLck.', '0907123456', 1, 2, 2),
    (3, 'Ha Noi', 'Employer account', 'susan@gmail.com', true, timestamp '2026-04-01 00:00:00', 'Susan', 'person-3.jpg', '$2a$12$f0cTRhHKbeSF/pxfEljKOOXyJErdOtmFo/jj3O0N7bl0.dTEKLck.', '0909123456', 1, 1, 3),
    (4, 'Ho Chi Minh City', 'Employer account', 'fancy@gmail.com', true, timestamp '2026-04-01 00:00:00', 'Fancy', 'person-4.jpg', '$2a$12$f0cTRhHKbeSF/pxfEljKOOXyJErdOtmFo/jj3O0N7bl0.dTEKLck.', '0908123456', 1, 1, 4),
    (5, 'Quy Nhon', 'Employer account', 'beck@gmail.com', true, timestamp '2026-04-01 00:00:00', 'Beck', 'person-5.jpg', '$2a$12$f0cTRhHKbeSF/pxfEljKOOXyJErdOtmFo/jj3O0N7bl0.dTEKLck.', '0909223456', 1, 1, 5),
    (6, 'Da Nang', 'Candidate account', 'alex@gmail.com', true, timestamp '2026-04-01 00:00:00', 'Alex', 'person-6.jpg', '$2a$12$f0cTRhHKbeSF/pxfEljKOOXyJErdOtmFo/jj3O0N7bl0.dTEKLck.', '0909553456', 1, 2, 6)
on conflict (id) do nothing;

insert into company (id, address, description, email, logo, company_name, phone_number, status, user_id) values
    (1, 'Da Nang', 'Enterprise software company', 'fpt@gmail.com', 'logo-fpt.jpg', 'FPT Software', '0123456789', 1, 1),
    (2, 'Hue', 'Outsourcing software company', 'bap@gmail.com', 'logo-bap.png', 'BAP Software', '0123456790', 1, 3),
    (3, 'Ha Noi', 'Technology consulting company', 'dac@gmail.com', 'logo-dac.png', 'DAC Tech', '0123456791', 1, 4),
    (4, 'Ho Chi Minh City', 'Product engineering company', 'company-4.jpg', 'company-4.jpg', 'NEOLAB', '0123456792', 1, 5)
on conflict (id) do nothing;

insert into recruitment (id, address, description, experience, quantity, ranks, salary, title, type, deadline, view, status, created_at, company_id, category_id) values
    (1, 'Da Nang', 'Build Java backend services for enterprise systems.', '1 year', 5, 'Fresher', '8000000', 'Java Developer', 'Fulltime', '2026-08-15', 23, 1, '2026-04-01', 1, 1),
    (2, 'Da Nang', 'Maintain .NET systems and APIs.', '2 years', 2, 'Senior', '25000000', '.NET Developer', 'Fulltime', '2026-08-15', 19, 1, '2026-04-02', 1, 4),
    (3, 'Hue', 'Work on Node.js microservices and internal tooling.', '1 year', 3, 'Middle', '18000000', 'Node.js Engineer', 'Hybrid', '2026-08-20', 14, 1, '2026-04-03', 2, 2),
    (4, 'Ha Noi', 'Develop PHP systems for business operations.', '2 years', 4, 'Middle', '16000000', 'PHP Developer', 'Fulltime', '2026-08-18', 11, 1, '2026-04-04', 3, 3),
    (5, 'Ho Chi Minh City', 'Build data automation with Python.', '1 year', 2, 'Junior', '17000000', 'Python Developer', 'Remote', '2026-08-22', 9, 1, '2026-04-05', 4, 5),
    (6, 'Quy Nhon', 'Maintain C++ desktop integrations.', '3 years', 1, 'Senior', '22000000', 'C++ Engineer', 'Fulltime', '2026-08-25', 7, 1, '2026-04-06', 4, 6)
on conflict (id) do nothing;

insert into follow_company (id, company_id, user_id) values
    (1, 1, 2),
    (2, 1, 6),
    (3, 2, 2),
    (4, 3, 6)
on conflict (id) do nothing;

insert into save_job (id, recruitment_id, user_id) values
    (1, 1, 2),
    (2, 3, 2),
    (3, 5, 6)
on conflict (id) do nothing;

insert into applypost (id, name_cv, text, status, recruitment_id, user_id, created_at) values
    (1, 'mary-cv.pdf', 'I want to apply for this role.', 1, 1, 2, '2026-04-01'),
    (2, 'alex-cv.pdf', 'Please review my profile.', 1, 3, 6, '2026-04-02')
on conflict (id) do nothing;


-- Advance each sequence to at least the seed's max value, but never below the current actual max.
-- This is safe to run on every startup (idempotent).
select setval(pg_get_serial_sequence('category',     'id'), greatest(8,  coalesce((select max(id) from category),     0)));
select setval(pg_get_serial_sequence('role',         'id'), greatest(3,  coalesce((select max(id) from role),         0)));
select setval(pg_get_serial_sequence('cv',           'id'), greatest(7,  coalesce((select max(id) from cv),           0)));
select setval(pg_get_serial_sequence('users',        'id'), greatest(7,  coalesce((select max(id) from users),        0)));
select setval(pg_get_serial_sequence('company',      'id'), greatest(5,  coalesce((select max(id) from company),      0)));
select setval(pg_get_serial_sequence('recruitment',  'id'), greatest(7,  coalesce((select max(id) from recruitment),  0)));
select setval(pg_get_serial_sequence('follow_company','id'),greatest(5,  coalesce((select max(id) from follow_company),0)));
select setval(pg_get_serial_sequence('save_job',     'id'), greatest(4,  coalesce((select max(id) from save_job),     0)));
select setval(pg_get_serial_sequence('applypost',    'id'), greatest(3,  coalesce((select max(id) from applypost),    0)));
select setval(pg_get_serial_sequence('reset_token',  'id'), greatest(1,  coalesce((select max(id) from reset_token),  0)));
