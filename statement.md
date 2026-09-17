# Project Statement: Hospital Management System (HMS)

## Course & Project Context
- **Course Domain**: Programming in Java
- **Curriculum Model**: VITyarthi "Build Your Own Project" Evaluation Framework
- **Project Title**: Hospital Management System (HMS) - Outpatient & Administrative Operations Console
- **Execution Mode**: Terminal / Command-Line Interface (CLI)
- **Target Platform**: Java Standard Edition (JDK 17+)

---

## 1. Problem Statement
Modern healthcare facilities and outpatient clinics struggle with disjointed administrative processes when managing patient intakes, physician clinic hours, appointment slot allocations, and billing reconciliation. In many mid-sized community healthcare centers, off-the-shelf commercial enterprise hospital software is cost-prohibitive, highly bloated, and dependent on complex web servers or proprietary cloud databases.

Manual and paper-based record-keeping introduces frequent scheduling collisions, lost patient files, billing discrepancies, double-booking of specialists, and lack of visibility into daily operational metrics. 

There is an acute need for a lightweight, robust, self-contained administrative software system built with disciplined software engineering practices that can run reliably in constrained environments without heavy third-party framework overhead.

---

## 2. Project Scope

### In-Scope:
- **Administrative Demographics**: Registration, indexing, and updating of outpatient demographics, blood group records, and emergency contacts.
- **Medical Staff Management**: Physician profile directory, specialization mapping, department grouping, consultation tariff tracking, and operational availability status.
- **Outpatient Scheduling**: Slot reservation, automated conflict detection (preventing doctor double-booking and concurrent patient bookings), rescheduling, and cancellation management.
- **Itemized Billing & Ledger**: Multi-component invoice generation (consultation, diagnostics, medications, facility charges), tiered discounting, tax computation, and support for partial cash payments.
- **Access Control & Identity**: Role-Based Access Control (RBAC) supporting four distinct personas (Admin, Receptionist, Doctor, Patient) with salted cryptographic password hashing simulation.
- **Operational Intelligence**: Daily aggregated summaries, doctor workload tracking, departmental breakdowns, financial ledger auditing, and CSV export.
- **Audit Logging**: Append-only transaction logging capturing administrative actions with timestamps and actor identities.

### Out-of-Scope:
- In-depth clinical decision support, medical diagnosis algorithms, drug interaction contraindication calculators, or live ICU telemetry monitoring.
- Real-world electronic health record (EHR) compliance certifications (e.g., HIPAA/HL7/FHIR compliance).
- Graphical User Interface (GUI) or browser-based rendering (strictly terminal-driven per evaluation specifications).

---

## 3. Target Users & Stakeholders

| User Role | Primary Responsibilities & Interactions |
| :--- | :--- |
| **System Administrator** | Manages system user credentials, audits security logs, supervises master physician and department directories, and reviews executive financial summaries. |
| **Receptionist / Front Desk** | Handles patient demographic registration, outpatient slot booking, schedule inquiries, bill generation, and payment collections. |
| **Consultant Physician / Doctor** | Reviews assigned daily appointment queues, records clinical examination notes, and manages personal on-duty/on-leave availability. |
| **Patient (Self-Service)** | Reviews registered demographic profiles, checks scheduled visits and visit history, and inspects outstanding billing statements. |

---

## 4. High-Level Features & Capabilities

1. **Patient Registry & Demographics**: Complete CRUD operations with validation for age boundaries, phone number syntax, and blood group categorization.
2. **Physician Directory & Availability Control**: Specialty filtering, working-hours constraints, and dynamic status toggles (`ACTIVE`, `ON_LEAVE`, `INACTIVE`).
3. **Collision-Free Appointment Engine**: Real-time interval validation preventing duplicate bookings for both doctors and patients at identical time slots.
4. **Itemized Accounting & Invoicing**: Automated subtotaling, concession deductions, GST tax calculations, payment status lifecycles (`PENDING`, `PARTIAL`, `PAID`), and ASCII invoice receipts.
5. **Role-Based Menus**: Dynamic terminal console dispatching strictly authenticated functionality tailored to individual user permissions.
6. **Persistence & Data Safety**: CSV-based data access object (DAO) layer featuring automatic file creation, quote escaping, and defensive recovery from malformed records.
7. **Analytical Reporting**: Stream-based real-time computation of revenue metrics, departmental allocations, and daily throughput with CSV export.

---

## 5. Expected Outcome
The project delivers a fully standalone, production-grade Core Java terminal application requiring no external database or containerized environment. Evaluators can clone the repository, compile using standard `javac`, run automated regression tests with zero dependencies, and interactively explore realistic healthcare workflows.
