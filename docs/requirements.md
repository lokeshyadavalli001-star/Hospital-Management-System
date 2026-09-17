# HMS Functional & Non-Functional Requirements Specification

## 1. Functional Requirements (FR)

### Module 1: Patient Management
- **FR-1.1 (Registration)**: The system shall register new patients capturing full name, age (0–130), gender (`MALE`, `FEMALE`, `OTHER`), phone number (10–13 digits), email, address, blood group, and emergency contact.
- **FR-1.2 (Unique Identification)**: The system shall automatically generate sequential unique IDs in the format `PAT-xxxx` (e.g., `PAT-1001`).
- **FR-1.3 (Lookup & Search)**: The system shall enable searching patient records by full name, partial name, unique ID, phone number, and blood group.
- **FR-1.4 (Updates)**: Authorized staff shall update demographic details while maintaining historical registration timestamps.
- **FR-1.5 (Lifecycle Status)**: The system shall support soft-deactivation (`active = false`) to preserve clinical audit trails rather than performing destructive physical deletions.
- **FR-1.6 (Validation)**: The system shall reject invalid phone numbers, out-of-range ages, malformed emails, or unapproved blood groups.

### Module 2: Doctor Management
- **FR-2.1 (Profile Creation)**: System shall record physician profiles with full name, specialization, department, consultation fee, operational status, working hours, and qualification.
- **FR-2.2 (Credential Provisioning)**: Adding a doctor shall automatically synchronize user login credentials with salted SHA-256 password hashing.
- **FR-2.3 (Availability State)**: Doctor status shall support `ACTIVE`, `ON_LEAVE`, and `INACTIVE`.
- **FR-2.4 (Department Filtering)**: System shall allow filtering and grouping doctors by department and specialization.
- **FR-2.5 (Working Hours Enforcement)**: The system shall record daily consultation start and end times (e.g., 09:00 to 17:00) and reject bookings outside this window.

### Module 3: Appointment Scheduling
- **FR-3.1 (Slot Reservation)**: Front-desk staff shall book outpatient appointments linking an active patient to an active doctor for a specified future date and time.
- **FR-3.2 (Conflict Prevention)**: 
  - The system shall reject any booking if the doctor already holds an active (`SCHEDULED` or `RESCHEDULED`) appointment on the same date and time.
  - The system shall reject any booking if the patient already has another active appointment at that identical slot.
- **FR-3.3 (Rescheduling)**: The system shall permit shifting an appointment's date and time, updating status to `RESCHEDULED`, subject to the same collision rules.
- **FR-3.4 (Cancellation)**: Patients or staff may cancel scheduled appointments with a mandatory reason recorded. Completed appointments cannot be cancelled.
- **FR-3.5 (Completion)**: Doctors shall mark visited appointments as `COMPLETED` and attach clinical consultation notes and prescription instructions.

### Module 4: Billing & Financial Ledger
- **FR-4.1 (Itemized Invoicing)**: The system shall generate structured bills comprising consultation fee, laboratory/diagnostic charges, pharmacy medication charges, room/bed charges, concession discounts, and tax rates.
- **FR-4.2 (Arithmetic Verification)**:
  $$\text{Subtotal} = \text{Consultation} + \text{Services} + \text{Medicines} + \text{Room}$$
  $$\text{Taxable Base} = \max(0, \text{Subtotal} - \text{Discount})$$
  $$\text{Tax Amount} = \text{Taxable Base} \times \left(\frac{\text{Tax Rate}}{100}\right)$$
  $$\text{Net Final} = \text{Taxable Base} + \text{Tax Amount}$$
  $$\text{Balance Due} = \text{Net Final} - \text{Paid Amount}$$
- **FR-4.3 (Payment Recording)**: Support full and partial cash collections. Payments cannot be negative or exceed outstanding balance.
- **FR-4.4 (Lifecycle Reconciliation)**: Automatically transition status between `PENDING`, `PARTIAL`, and `PAID`.
- **FR-4.5 (Invoice Receipt Printing)**: Format and render comprehensive ASCII invoice receipts containing itemized costs, patient and doctor details, and payment balances.

### Module 5: Staff & User Access Control
- **FR-5.1 (Role-Based Access Control)**: Enforce distinct privileges for `ADMIN`, `RECEPTIONIST`, `DOCTOR`, and `PATIENT`.
- **FR-5.2 (Authentication)**: Require valid username and password credentials. Protect passwords using salted SHA-256 hashing.
- **FR-5.3 (Account Management)**: Administrators can create staff accounts and activate/deactivate user logins.
- **FR-5.4 (Self-Service Password Change)**: Authenticated users can modify their passwords after proving knowledge of their current password.

### Module 6: Executive Reports & Analytics
- **FR-6.1 (Daily Hospital Summary)**: Real-time aggregation of total registered/active patients, total/active doctors, departmental breakdown, appointment volume by status, and total revenue billed, collected, and pending.
- **FR-6.2 (Doctor Workload Analytics)**: Appointment distribution metrics across consulting doctors.
- **FR-6.3 (CSV Export)**: Capability to write executive summary metrics to an external CSV file.

### Module 7: Security Audit Logging
- **FR-7.1 (Transaction Logging)**: Maintain an append-only log file (`data/audit.log`) capturing timestamps, category, action, acting user, and summary details.

---

## 2. Non-Functional Requirements (NFR)

| ID | Dimension | Specification & Metric |
| :--- | :--- | :--- |
| **NFR-01** | **Performance** | In-memory `LinkedHashMap` caching provides $O(1)$ read complexity. Search operations over 1,000 records complete under 50ms in terminal. |
| **NFR-02** | **Security** | Passwords salted with 8-byte secure random values and hashed with SHA-256. No plaintext passwords stored. RBAC enforces menu boundary separation. |
| **NFR-03** | **Usability** | Clear interactive prompts with range bounds, formatted ASCII tables, clear error banners, and pagination helpers. Terminal recovers from EOF stream closure. |
| **NFR-04** | **Reliability** | Atomic file writes and defensive CSV parsing. Corrupted rows in storage files are skipped with non-fatal warnings rather than crashing the system. |
| **NFR-05** | **Maintainability** | Clean 4-tier layered architecture adhering to SOLID principles. Decoupled services, generic repository base, and comprehensive JavaDoc comments. |
| **NFR-06** | **Error Handling** | Comprehensive custom checked exception hierarchy (`HospitalException` base) preventing raw runtime stack traces from leaking to the end-user. |
| **NFR-07** | **Resource Efficiency** | Zero external heavy frameworks (no Spring Boot, Hibernate, or SQL servers). Runs smoothly within standard JVM heap allocations (< 64MB). |
