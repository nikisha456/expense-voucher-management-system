-- Demo password for every account: Password@123
-- BCrypt hash generated with cost 10.

INSERT INTO users (id, name, email, password, role, employee_code, department, signature_path, active, created_at, updated_at)
VALUES
    (1, 'Priya Sharma', 'director@abc.com', '$2b$10$ymNVbcMqvNxFGjTWekNUEORCdZvZq1aPGb7bJ0hPXU/imaGA.7286', 'DIRECTOR', NULL, 'Administration', 'seed-director.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Rahul Mehta', 'accounts@abc.com', '$2b$10$ymNVbcMqvNxFGjTWekNUEORCdZvZq1aPGb7bJ0hPXU/imaGA.7286', 'ACCOUNTS', NULL, 'Finance', 'seed-accounts.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Ananya Iyer', 'ananya.iyer@abc.com', '$2b$10$ymNVbcMqvNxFGjTWekNUEORCdZvZq1aPGb7bJ0hPXU/imaGA.7286', 'EMPLOYEE', 'EMP-1001', 'Engineering', 'seed-ananya.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Karan Patel', 'karan.patel@abc.com', '$2b$10$ymNVbcMqvNxFGjTWekNUEORCdZvZq1aPGb7bJ0hPXU/imaGA.7286', 'EMPLOYEE', 'EMP-1002', 'Sales', 'seed-karan.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Meera Nair', 'meera.nair@abc.com', '$2b$10$ymNVbcMqvNxFGjTWekNUEORCdZvZq1aPGb7bJ0hPXU/imaGA.7286', 'EMPLOYEE', 'EMP-1003', 'Marketing', 'seed-meera.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO vouchers (
    id, voucher_number, voucher_date, expense_date, department_name, expense_title, expense_category,
    expense_description, amount, employee_name, employee_code, employee_signature_path, status,
    director_signature_path, approval_date, rejection_reason, created_by, created_at, updated_at
) VALUES
    (1, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000001', CURRENT_DATE - 250, CURRENT_DATE - 250, 'Engineering',
        'Bangalore client kickoff', 'TRAVEL',
        'Return airfare and local cab for the Bangalore account kickoff.',
        34000.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 248, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '250 days', CURRENT_TIMESTAMP - INTERVAL '248 days'),
    (2, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000002', CURRENT_DATE - 220, CURRENT_DATE - 220, 'Sales',
        'Regional team lunch', 'FOOD_MEALS',
        'Working lunch with the west region sales pod after the quarterly review.',
        5600.50, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 218, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '220 days', CURRENT_TIMESTAMP - INTERVAL '218 days'),
    (3, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000003', CURRENT_DATE - 190, CURRENT_DATE - 190, 'Marketing',
        'Product launch dinner', 'CLIENT_ENTERTAINMENT',
        'Dinner with the retail partner group during the product launch week.',
        12800.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 189, NULL, 5,
        CURRENT_TIMESTAMP - INTERVAL '190 days', CURRENT_TIMESTAMP - INTERVAL '189 days'),
    (4, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000004', CURRENT_DATE - 160, CURRENT_DATE - 160, 'IT Support',
        'Design and analytics subscriptions', 'SOFTWARE_SUBSCRIPTIONS',
        'Annual seats for the design and product analytics tools used by the squad.',
        8999.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 158, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '160 days', CURRENT_TIMESTAMP - INTERVAL '158 days'),
    (5, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000005', CURRENT_DATE - 130, CURRENT_DATE - 130, 'Finance',
        'Field travel fuel', 'FUEL_TRANSPORT',
        'Fuel for customer site visits recorded in the finance travel log.',
        4100.75, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 129, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '130 days', CURRENT_TIMESTAMP - INTERVAL '129 days'),
    (6, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000006', CURRENT_DATE - 100, CURRENT_DATE - 100, 'HR',
        'Hyderabad offsite stay', 'ACCOMMODATION',
        'Two nights of hotel stay for the people-operations offsite.',
        27500.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 98, NULL, 5,
        CURRENT_TIMESTAMP - INTERVAL '100 days', CURRENT_TIMESTAMP - INTERVAL '98 days'),
    (7, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000007', CURRENT_DATE - 70, CURRENT_DATE - 70, 'Administration',
        'Spring Boot training workshop', 'TRAINING',
        'External workshop fee for the platform engineering training day.',
        18000.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 69, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '70 days', CURRENT_TIMESTAMP - INTERVAL '69 days'),
    (8, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000008', CURRENT_DATE - 40, CURRENT_DATE - 40, 'Operations',
        'Mumbai customer travel', 'TRAVEL',
        'Flights and airport transfer for the Mumbai operations review.',
        45250.00, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 38, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '40 days', CURRENT_TIMESTAMP - INTERVAL '38 days'),
    (9, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000009', CURRENT_DATE - 20, CURRENT_DATE - 20, 'Legal',
        'Contract workshop stationery', 'OFFICE_SUPPLIES',
        'Printed contracts, folders, and stationery for the legal workshop.',
        2150.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'APPROVED', 'seed-director.png', CURRENT_DATE - 19, NULL, 5,
        CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '19 days'),
    (10, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000010', CURRENT_DATE - 3, CURRENT_DATE - 3, 'Engineering',
        'Vendor working lunch', 'FOOD_MEALS',
        'Working lunch with the infrastructure vendor during the renewal discussion.',
        980.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'APPROVED', 'seed-director.png', CURRENT_DATE, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
    (11, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000011', CURRENT_DATE - 6, CURRENT_DATE - 6, 'Marketing',
        'Campaign booth supplies', 'MISCELLANEOUS',
        'Last-minute booth supplies for the campus hiring campaign.',
        1500.00, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'APPROVED', 'seed-director.png', CURRENT_DATE, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP),
    (12, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000012', CURRENT_DATE - 2, CURRENT_DATE - 2, 'Engineering',
        'Q4 offsite accommodation', 'ACCOMMODATION',
        'Hotel block for the engineering offsite. Invoice attached in the description.',
        18600.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'PENDING_APPROVAL', NULL, NULL, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '1 hour'),
    (13, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000013', CURRENT_DATE - 1, CURRENT_DATE - 1, 'Sales',
        'North region client dinner', 'CLIENT_ENTERTAINMENT',
        'Client dinner after the renewal workshop with the north region account.',
        9400.00, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'PENDING_APPROVAL', NULL, NULL, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '2 hours'),
    (14, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000014', CURRENT_DATE - 8, CURRENT_DATE - 8, 'Marketing',
        'Campaign design tool plan', 'SOFTWARE_SUBSCRIPTIONS',
        'Additional design-tool seat for the campaign freelancer.',
        4999.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'PENDING_APPROVAL', NULL, NULL, NULL, 5,
        CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '8 days' + INTERVAL '3 hours'),
    (15, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000015', CURRENT_DATE - 12, CURRENT_DATE - 12, 'Engineering',
        'Internal platform workshop', 'TRAINING',
        'Registration fee for the internal platform engineering workshop.',
        15000.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'PENDING_APPROVAL', NULL, NULL, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP - INTERVAL '12 days' + INTERVAL '1 hour'),
    (16, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000016', CURRENT_DATE - 4, CURRENT_DATE - 4, 'Sales',
        'Delhi customer visit', 'TRAVEL',
        'Airfare for the Delhi enterprise customer onsite.',
        22000.00, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'PENDING_APPROVAL', NULL, NULL, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days' + INTERVAL '90 minutes'),
    (17, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000017', CURRENT_DATE - 15, CURRENT_DATE - 15, 'Marketing',
        'Field team mobile data', 'COMMUNICATION',
        'Mobile data reimbursement for the field marketing week.',
        3200.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'PENDING_APPROVAL', NULL, NULL, NULL, 5,
        CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days' + INTERVAL '2 hours'),
    (18, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000018', CURRENT_DATE - 30, CURRENT_DATE - 30, 'Marketing',
        'Conference travel claim', 'TRAVEL',
        'Travel claim for the industry conference in Chennai.',
        15600.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'REJECTED', NULL, NULL,
        'Missing boarding pass and hotel invoice. Please attach the supporting documents and resubmit.',
        5, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '28 days'),
    (19, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000019', CURRENT_DATE - 60, CURRENT_DATE - 60, 'Engineering',
        'Client entertainment overage', 'CLIENT_ENTERTAINMENT',
        'Evening hosting for a visiting client engineering team.',
        22000.00, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'REJECTED', NULL, NULL,
        'Amount exceeds the client entertainment policy limit for a single meeting.',
        3, CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP - INTERVAL '58 days'),
    (20, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000020', CURRENT_DATE - 5, CURRENT_DATE - 5, 'Sales',
        'Duplicate IDE subscription', 'SOFTWARE_SUBSCRIPTIONS',
        'Personal IDE subscription purchased before checking the central license pool.',
        9999.00, 'Karan Patel', 'EMP-1002', 'seed-karan.png', 'REJECTED', NULL, NULL,
        'This subscription is already covered under the central IT license.',
        4, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP),
    (21, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000021', CURRENT_DATE - 110, CURRENT_DATE - 110, 'Marketing',
        'Weekend fuel claim', 'FUEL_TRANSPORT',
        'Fuel for weekend travel between home and the campaign venue.',
        1800.00, 'Meera Nair', 'EMP-1003', 'seed-meera.png', 'REJECTED', NULL, NULL,
        'Personal commute is not reimbursable under the company fuel policy.',
        5, CURRENT_TIMESTAMP - INTERVAL '110 days', CURRENT_TIMESTAMP - INTERVAL '108 days'),
    (22, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000022', CURRENT_DATE, CURRENT_DATE - 1, 'Engineering',
        'Upcoming Pune client trip', 'TRAVEL',
        'Draft for the Pune client visit next week. Tickets are not booked yet.',
        12500.00, 'Ananya Iyer', 'EMP-1001', NULL, 'DRAFT', NULL, NULL, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
    (23, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000023', CURRENT_DATE, CURRENT_DATE - 3, 'Engineering',
        'Squad coffee and snacks', 'FOOD_MEALS',
        'Coffee and snacks for the release-week squad room.',
        1850.50, 'Ananya Iyer', 'EMP-1001', 'seed-ananya.png', 'DRAFT', NULL, NULL, NULL, 3,
        CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP),
    (24, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000024', CURRENT_DATE, CURRENT_DATE - 6, 'Sales',
        'Dealer visit fuel', 'FUEL_TRANSPORT',
        'Draft fuel claim for the dealer visits. Odometer photo still pending.',
        2400.00, 'Karan Patel', 'EMP-1002', NULL, 'DRAFT', NULL, NULL, NULL, 4,
        CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP),
    (25, 'EV-' || to_char(CURRENT_DATE, 'YYYY') || '-000025', CURRENT_DATE, CURRENT_DATE - 2, 'Marketing',
        'Campaign printouts', 'OFFICE_SUPPLIES',
        'Draft for campaign printouts. Vendor invoice has not arrived.',
        760.00, 'Meera Nair', 'EMP-1003', NULL, 'DRAFT', NULL, NULL, NULL, 5,
        CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP);

DO $$
DECLARE
    r RECORD;
    decided_at TIMESTAMPTZ;
BEGIN
    FOR r IN SELECT * FROM vouchers ORDER BY id LOOP
        INSERT INTO voucher_status_history (voucher_id, from_status, to_status, changed_by, remarks, changed_at)
        VALUES (r.id, NULL, 'DRAFT', r.created_by, 'Voucher created as draft', r.created_at);

        IF r.status <> 'DRAFT' THEN
            INSERT INTO voucher_status_history (voucher_id, from_status, to_status, changed_by, remarks, changed_at)
            VALUES
                (r.id, 'DRAFT', 'SUBMITTED', r.created_by, 'Submitted for approval', r.created_at + INTERVAL '1 hour'),
                (r.id, 'SUBMITTED', 'PENDING_APPROVAL', r.created_by, 'Awaiting director approval', r.created_at + INTERVAL '1 hour 1 second');
        END IF;

        IF r.status = 'APPROVED' THEN
            decided_at := CASE
                WHEN r.approval_date = CURRENT_DATE THEN CURRENT_TIMESTAMP
                ELSE (r.approval_date::timestamp + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'
            END;
            INSERT INTO voucher_status_history (voucher_id, from_status, to_status, changed_by, remarks, changed_at)
            VALUES (r.id, 'PENDING_APPROVAL', 'APPROVED', 1, 'Approved for reimbursement', decided_at);
        ELSIF r.status = 'REJECTED' THEN
            INSERT INTO voucher_status_history (voucher_id, from_status, to_status, changed_by, remarks, changed_at)
            VALUES (r.id, 'PENDING_APPROVAL', 'REJECTED', 1, r.rejection_reason, r.updated_at);
        END IF;
    END LOOP;
END $$;

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('vouchers', 'id'), (SELECT MAX(id) FROM vouchers));
SELECT setval('voucher_number_seq', (SELECT MAX(id) FROM vouchers));
SELECT setval(pg_get_serial_sequence('voucher_status_history', 'id'), (SELECT MAX(id) FROM voucher_status_history));
