-- =====================================================================
-- Phase 1: Members + Membership Plans + Memberships
-- Har gym DB (dbgym, dbgym_noida ...) pe ek baar manually chalao:
--   mysql -uroot -p dbgym < db/phase1_members_plans.sql
-- hbm2ddl=none hai, isliye tables yahi file banati hai. Dobara chalane pe kuch nahi tootega (IF NOT EXISTS).
-- gym_code = login wale gym ka code (LAJPAT, NOIDA) - ek DB me kai gym ho sakte hain, isolation isi se.
-- =====================================================================

CREATE TABLE IF NOT EXISTS members (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  gym_code        VARCHAR(50)  NOT NULL,
  member_code     VARCHAR(30)  NOT NULL,
  full_name       VARCHAR(150) NOT NULL,
  phone           VARCHAR(15)  NOT NULL,
  email           VARCHAR(100) DEFAULT NULL,
  gender          VARCHAR(10)  DEFAULT NULL,
  dob             DATE         DEFAULT NULL,
  address         TEXT,
  emergency_name  VARCHAR(100) DEFAULT NULL,
  emergency_phone VARCHAR(15)  DEFAULT NULL,
  joining_date    DATE         NOT NULL,
  notes           TEXT,
  isdelete        TINYINT(1)   NOT NULL DEFAULT 0,
  created_by      VARCHAR(50)  DEFAULT NULL,
  created_on      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  updated_by      VARCHAR(50)  DEFAULT NULL,
  updated_on      DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_members_gym_code_member_code (gym_code, member_code),
  KEY idx_members_gym_phone (gym_code, phone),
  KEY idx_members_gym_name (gym_code, full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS membership_plans (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  gym_code       VARCHAR(50)   NOT NULL,
  plan_name      VARCHAR(100)  NOT NULL,
  duration_type  VARCHAR(20)   NOT NULL,
  duration_days  INT           NOT NULL,
  price          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  description    VARCHAR(500)  DEFAULT NULL,
  is_active      TINYINT(1)    NOT NULL DEFAULT 1,
  isdelete       TINYINT(1)    NOT NULL DEFAULT 0,
  created_by     VARCHAR(50)   DEFAULT NULL,
  created_on     DATETIME      DEFAULT CURRENT_TIMESTAMP,
  updated_by     VARCHAR(50)   DEFAULT NULL,
  updated_on     DATETIME      DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_plans_gym (gym_code, isdelete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS memberships (
  id               BIGINT        NOT NULL AUTO_INCREMENT,
  gym_code         VARCHAR(50)   NOT NULL,
  member_id        BIGINT        NOT NULL,
  plan_id          BIGINT        NOT NULL,
  plan_name        VARCHAR(100)  NOT NULL,
  start_date       DATE          NOT NULL,
  end_date         DATE          NOT NULL,
  price            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  discount         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  final_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status           VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  payment_status   VARCHAR(20)   NOT NULL DEFAULT 'UNPAID',
  renewed_from_id  BIGINT        DEFAULT NULL,
  created_by       VARCHAR(50)   DEFAULT NULL,
  created_on       DATETIME      DEFAULT CURRENT_TIMESTAMP,
  updated_by       VARCHAR(50)   DEFAULT NULL,
  updated_on       DATETIME      DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_memberships_gym_member_end (gym_code, member_id, end_date),
  KEY idx_memberships_gym_created (gym_code, created_on),
  CONSTRAINT fk_memberships_member FOREIGN KEY (member_id) REFERENCES members (id),
  CONSTRAINT fk_memberships_plan FOREIGN KEY (plan_id) REFERENCES membership_plans (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- OPTIONAL: Local sample data (sirf LAJPAT, sirf local testing ke liye).
-- Chahiye to neeche ke comments hata ke chalao.
-- =====================================================================
-- INSERT INTO membership_plans (gym_code, plan_name, duration_type, duration_days, price, description, created_by) VALUES
--   ('LAJPAT', 'Monthly Basic',  'MONTHLY',   30,  1500.00, 'Gym floor access', 'seed'),
--   ('LAJPAT', 'Quarterly Pro',  'QUARTERLY', 90,  4000.00, 'Gym floor + cardio', 'seed'),
--   ('LAJPAT', 'Yearly Elite',   'YEARLY',    365, 14000.00, 'All access', 'seed');
-- INSERT INTO members (gym_code, member_code, full_name, phone, email, gender, joining_date, created_by) VALUES
--   ('LAJPAT', 'LAJ-0001', 'Rahul Verma',   '9811000001', 'rahul@example.com', 'MALE',   CURDATE(), 'seed'),
--   ('LAJPAT', 'LAJ-0002', 'Priya Singh',   '9811000002', NULL,                'FEMALE', CURDATE(), 'seed'),
--   ('LAJPAT', 'LAJ-0003', 'Aman Gupta',    '9811000003', NULL,                'MALE',   CURDATE(), 'seed'),
--   ('LAJPAT', 'LAJ-0004', 'Neha Kapoor',   '9811000004', NULL,                'FEMALE', CURDATE(), 'seed'),
--   ('LAJPAT', 'LAJ-0005', 'Vikram Malhotra','9811000005', NULL,               'MALE',   CURDATE(), 'seed');
