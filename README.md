# Hospital Management System (HMS) — Core Java

[![Java](https://img.shields.io/badge/Java-17%2B%20%7C%2021%20%7C%2026-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-Layered%204--Tier-blue?style=for-the-badge)](docs/architecture.md)
[![Build](https://img.shields.io/badge/Build-Pure%20JDK%20CLI%20(Zero%20Dependencies)-brightgreen?style=for-the-badge)](run.bat)
[![Tests](https://img.shields.io/badge/Tests-42%2F42%20Passing%20(100%25)-success?style=for-the-badge)](docs/testing.md)
[![Academic](https://img.shields.io/badge/Evaluation-VITyarthi%20Project-orange?style=for-the-badge)](docs/EVALUATION_MAPPING.md)

An original, modular, terminal-based **Hospital Management System (HMS)** built from first principles in **Core Java**. 

Engineered specifically for the **VITyarthi "Build Your Own Project" evaluation criteria** in *Programming in Java*, this system showcases clean 4-tier layered architecture, object-oriented domain modeling, collision-free appointment scheduling, itemized medical billing, salted cryptographic password hashing, robust CSV persistence, and an in-tree automated test runner—with **zero external frameworks** required.

---

## Table of Contents
1. [Overview & Problem Statement](#overview--problem-statement)
2. [Objectives](#objectives)
3. [Key Features & Functional Modules](#key-features--functional-modules)
4. [Non-Functional Requirements](#non-functional-requirements)
5. [Java Concepts Demonstrated](#java-concepts-demonstrated)
6. [System Architecture](#system-architecture)
7. [Project Directory Structure](#project-directory-structure)
8. [Prerequisites & System Requirements](#prerequisites--system-requirements)
9. [How to Compile](#how-to-compile)
10. [How to Run the Application](#how-to-run-the-application)
11. [How to Run Automated Tests](#how-to-run-automated-tests)
12. [Demo Credentials for Evaluation](#demo-credentials-for-evaluation)
13. [Sample Evaluation Workflow](#sample-evaluation-workflow)
14. [Data Storage & CSV Persistence](#data-storage--csv-persistence)
15. [Error Handling & Resilience](#error-handling--resilience)
16. [Screenshots (Placeholders)](#screenshots-placeholders)
17. [Academic Disclaimer](#academic-disclaimer)

---

## Overview & Problem Statement

Outpatient medical centers often struggle with manual, disjointed processes:
- Overlapping specialist bookings due to absent double-booking prevention.
- Erroneous fee calculations across consultation, laboratory diagnostics, pharmacy, and room allocations.
- Inability to track partial payments and outstanding patient balances.
- Lack of immediate visibility into operational and departmental metrics.

**HMS** delivers a lightweight, reliable, self-contained administrative software console that runs seamlessly from the terminal without bloated frameworks or external database server setups.

---

## Objectives

- **100% Core Java SE**: No Spring Boot, Hibernate, JavaFX, or external Maven dependencies.
- **Strict Layered Separation**: Presentation $\rightarrow$ Service $\rightarrow$ Domain Model $\rightarrow$ Repository $\rightarrow$ CSV Storage.
- **Deterministic Scheduling**: Collision detection algorithm preventing doctor and patient double-booking.
- **Itemized Ledger**: Precise financial subtotaling, concession discounts, GST tax calculations, and partial-payment balances.
- **Security Simulation**: Role-Based Access Control (RBAC) with salted SHA-256 cryptographic password hashing.
- **Instant Reproducibility**: Cloned repository compiles and runs via standard JDK CLI tools on Windows, Linux, and macOS.

---

## Key Features & Functional Modules

### Module 1: Patient Management
- Register outpatient demographics (Name, Age, Gender, Phone, Email, Address, Blood Group, Emergency Contact).
- Sequential unique ID generation: `PAT-1001`, `PAT-1002`, ...
- Search patients by ID, Name, Phone Number, or Blood Group.
- Update demographic details and toggle active/inactive status (soft-deactivation preserving audit logs).
- Strict validation for phone syntax, age ranges, and approved blood groups.

### Module 2: Doctor Management
- Manage physician profiles with specialization, department, consultation fee, and clinic working hours.
- Dynamic operational status: `ACTIVE`, `ON_LEAVE`, `INACTIVE`.
- Synchronized staff login credentials created upon physician onboarding.
- Departmental filtering and clinic hours availability checks (`isAvailableAt`).

### Module 3: Appointment Scheduling
- Book outpatient visits with real-time double-booking prevention:
  - Doctor slot conflict check (prevents multiple patients booking same doctor at same time).
  - Patient slot conflict check (prevents patient having two overlapping consults).
  - Doctor working hours check (rejects bookings outside physician's active hours).
- Reschedule appointments with automatic collision verification.
- Cancel appointments with mandatory cancellation reason (cannot cancel completed visits).
- Complete consultations with clinical observations and prescription notes.

### Module 4: Billing & Financial Ledger
- Multi-component invoicing: Consultation Fee + Diagnostic Labs + Pharmacy + Room Charges.
- Concession discounts and percentage-based taxes (GST).
- Partial cash payment tracking and automatic balance updates.
- Status transitions: `PENDING` $\rightarrow$ `PARTIAL` $\rightarrow$ `PAID`.
- Render and display comprehensive formatted ASCII invoice receipts.

### Module 5: User Access Control & Security
- Four distinct system roles: **ADMIN**, **RECEPTIONIST**, **DOCTOR**, **PATIENT**.
- Salted SHA-256 cryptographic password hashing via `java.security.MessageDigest` and `SecureRandom`.
- Interactive login session and self-service password modification.
- Administrator controls to activate/deactivate user accounts.

### Module 6: Executive Reports & Analytics
- Daily Hospital Executive Summary:
  - Total and active patients.
  - Active on-duty doctors and distribution across departments.
  - Today's appointment breakdown (Completed, Scheduled, Cancelled).
  - Real-time financial revenue metrics (Billed, Collected, Outstanding Due).
- Single-click CSV export of executive reports.

### Module 7: Security Audit Logging
- Append-only transactional audit trail (`data/audit.log`) tracking system events, timestamps, categories, and acting users.

---

## Non-Functional Requirements

- **Performance**: In-memory `LinkedHashMap` indexing ensures $O(1)$ ID lookups; Stream aggregations execute within milliseconds.
- **Security**: Salted SHA-256 password storage; Role-Based Access Control (RBAC) isolating menu views.
- **Usability**: Dynamic-width ASCII tables, clear user banners, input validation loops, and stream-safe prompts.
- **Reliability**: Defensive CSV tokenizer handles commas within quotes; corrupted records are skipped without crashing.
- **Maintainability**: High cohesion and loose coupling through generic repositories and decoupled domain entities.
- **Resource Efficiency**: Operates within lightweight JVM allocations (< 64MB memory).

---

## Java Concepts Demonstrated

| Core Java Concept | Concrete Implementation in HMS |
| :--- | :--- |
| **Inheritance & Abstraction** | `abstract class User` extended by `Admin`, `Receptionist`, `Doctor`. Abstract methods define role authority. |
| **Polymorphism** | Dynamic menu dispatching by `UserRole`; generic DAO interactions via `Repository<T>`. |
| **Encapsulation** | Private fields, immutable identifiers, defensive copying, and validated mutators. |
| **Generics** | `Repository<T extends Identifiable>` and `AbstractCsvRepository<T>` providing compile-time type safety. |
| **Collections Framework** | `ArrayList` (ordered lists/pagination), `LinkedHashMap` (insertion-ordered cache), `HashSet` (validation sets). |
| **Java Streams API** | Stream filters, `groupingBy`, `counting`, `summingDouble`, and `map` for real-time reporting. |
| **Date/Time API (`java.time`)** | `LocalDate`, `LocalTime`, `LocalDateTime`, `DateTimeFormatter`, and working hours interval checks. |
| **Custom Exceptions** | Hierarchical checked exception tree rooted at `HospitalException`. |
| **File I/O & Serialization** | `BufferedReader`, `BufferedWriter`, RFC-4180 style CSV quote parsing, and append-only audit logging. |
| **Cryptographic APIs** | `java.security.MessageDigest` (SHA-256) and `java.security.SecureRandom` for password salting. |

---

## System Architecture

```
+-------------------------------------------------------------+
|                      Presentation Layer                     |
|  (Main Bootstrap, ConsoleMenu, AdminMenu, ReceptionistMenu, |
|            DoctorMenu, PatientMenu, ConsoleUtil)            |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                        Service Layer                        |
|   (UserService, PatientService, DoctorService,              |
|    AppointmentService, BillingService, ReportService,       |
|    AuditService - Business Rules, Validation, Logic)        |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                      Repository Layer                       |
|   (Repository<T>, AbstractCsvRepository<T>,                 |
|    UserRepository, PatientRepository, DoctorRepository,     |
|    AppointmentRepository, BillRepository - DAO & Caching)   |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                     File Persistence Layer                  |
|    (CSV Flat Files: data/*.csv, Transactional: audit.log)   |
+-------------------------------------------------------------+
```

---

## Project Directory Structure

```
├── .gitignore                          # Version control exclusions
├── README.md                           # Comprehensive technical manual & instructions
├── statement.md                        # Academic problem statement & scope
├── run.bat                             # One-click Windows launch script
├── run.sh                              # One-click Unix/Linux/macOS launch script
├── data/                               # CSV flat-file persistence
│   ├── patients.csv
│   ├── doctors.csv
│   ├── appointments.csv
│   ├── bills.csv
│   ├── users.csv
│   └── audit.log
├── docs/                               # Academic design documentation
│   ├── architecture.md                 # Detailed architectural specifications
│   ├── requirements.md                 # Functional & Non-Functional requirements
│   ├── testing.md                      # Test suite documentation & logs
│   ├── EVALUATION_MAPPING.md           # VITyarthi rubric mapping & evidence
│   ├── PROJECT_REPORT.md               # 15-section formal academic project report
│   └── diagrams/                       # Source-controlled Mermaid UML diagrams
│       ├── system_architecture.mermaid
│       ├── use_case_diagram.mermaid
│       ├── class_diagram.mermaid
│       ├── sequence_diagram.mermaid
│       ├── workflow_diagram.mermaid
│       └── storage_er_diagram.mermaid
├── src/                                # Core Java source code
│   └── com/
│       └── hms/
│           ├── Main.java               # Application bootstrap & session router
│           ├── model/                  # Domain entities & inheritance tree
│           ├── repository/             # Generic persistence & DAO implementations
│           ├── service/                # Business services & validation logic
│           ├── exception/              # Custom checked exception hierarchy
│           ├── util/                   # Input validation, ID generator, formatting
│           └── ui/                     # Interactive role console menus
└── test/                               # Automated regression test suite
    └── com/
        └── hms/
            ├── TestRunner.java         # Zero-dependency CLI test runner
            ├── PatientServiceTest.java
            ├── DoctorServiceTest.java
            ├── AppointmentServiceTest.java
            ├── BillingServiceTest.java
            └── AuthenticationTest.java
```

---

## Prerequisites & System Requirements

- **Java Development Kit**: JDK 17, 21, or higher.
- **Git**: Git 2.x or higher for version control.
- **Operating System**: Cross-platform (Windows 10/11, Ubuntu/Debian/Fedora Linux, macOS).
- **RAM**: Minimum 512 MB available.

---

## How to Compile

### Windows (PowerShell):
```powershell
# Create output directory and compile all Java source files
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out }
$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
```

### Windows (Command Prompt):
```cmd
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
```

### Linux / macOS (Bash):
```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm sources.txt
```

---

## How to Run the Application

### Option A: Using Pre-Configured Scripts (Recommended)
- **Windows**: Double-click or execute `.\run.bat` in Command Prompt/PowerShell.
- **Linux / macOS**:
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

### Option B: Direct Terminal Command
```bash
java -Dfile.encoding=UTF-8 -cp out com.hms.Main
```

---

## How to Run Automated Tests

The project includes an in-tree test runner that validates all core services against an isolated sandbox directory (`data/test_sandbox/`):

### Windows (PowerShell):
```powershell
$files = Get-ChildItem -Recurse -Filter *.java src,test | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
java -cp out com.hms.TestRunner
```

### Linux / macOS (Bash):
```bash
find src test -name "*.java" > test_sources.txt
javac -encoding UTF-8 -d out @test_sources.txt
rm test_sources.txt
java -cp out com.hms.TestRunner
```

**Expected Result**: All 42 unit and integration test assertions pass with **100.0% Success**.

---

## Demo Credentials for Evaluation

The system comes pre-seeded with realistic records and credentials:

| Role | Username | Password | User Identity | Simulated Responsibilities |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `Admin@123` | Dr. Rajesh Mehta | Full oversight, accounts, master directories, reports |
| **RECEPTIONIST** | `recep` | `Recep@123` | Ananya Sen | Patient registration, scheduling, billing intake |
| **DOCTOR** | `dr_sharma` | `Doctor@123` | Dr. Priya Sharma | Consultant Cardiologist (OPD 09:00–15:00) |
| **DOCTOR** | `dr_verma` | `Doctor@123` | Dr. Arun Verma | Senior Neurologist (OPD 10:00–16:00) |
| **PATIENT** | `pat_rahul` | `Patient@123` | Rahul Verma | Outpatient self-service portal (`PAT-1001`) |

*Note: All passwords are automatically stored using salted SHA-256 cryptographic hashes in `data/users.csv`.*

---

## Sample Evaluation Workflow

1. **Launch App**: Execute `.\run.bat` or `java -cp out com.hms.Main`.
2. **Login as Administrator**:
   - Sign in with `admin` / `Admin@123`.
   - Select option **6 (Hospital Reports & Analytics)** to view real-time daily metrics.
   - Select option **7 (Security Audit Logs)** to view timestamped activity logs.
   - Log out.
3. **Login as Receptionist**:
   - Sign in with `recep` / `Recep@123`.
   - Select option **1 (Register New Patient)** $\rightarrow$ Enter demographics (e.g., Vikram Roy, Age: 38, Phone: 9876543230).
   - Select option **5 (Book Appointment)** $\rightarrow$ Book visit for newly created patient with doctor `DOC-2001` at 10:00.
   - **Test Conflict Detection**: Attempt to book another patient with doctor `DOC-2001` at the exact same slot. Observe the immediate `[WARNING] Doctor is already booked` conflict block.
   - Select option **9 (Generate Invoice / Bill)** $\rightarrow$ Create bill with consultation fee, lab fee, and discount.
   - Select option **10 (Record Bill Payment)** $\rightarrow$ Record a partial cash payment.
   - Select option **11 (View Invoice Receipt)** $\rightarrow$ Display formatted ASCII invoice with calculated balance.
   - Log out.
4. **Login as Doctor**:
   - Sign in with `dr_sharma` / `Doctor@123`.
   - Select option **1 (View Today's Appointments)** $\rightarrow$ View scheduled consultation.
   - Select option **3 (Complete Consultation & Record Notes)** $\rightarrow$ Enter clinical observations and mark visit completed.
   - Log out.
5. **Login as Patient**:
   - Sign in with `pat_rahul` / `Patient@123`.
   - View demographic profile, appointment history, and billing statements.

---

## Data Storage & CSV Persistence

All entities are persisted in human-readable CSV flat files under `data/`:
- `data/patients.csv`: Demographic details, blood groups, and emergency contacts.
- `data/doctors.csv`: Specialist credentials, clinic hours, fees, and operational status.
- `data/appointments.csv`: Outpatient slot bookings, status lifecycles, and prescription notes.
- `data/bills.csv`: Line-item financials, discounts, taxes, and payment balances.
- `data/users.csv`: System handles, roles, salts, and SHA-256 credential hashes.
- `data/audit.log`: Transactional audit log tracking system events with timestamps.

The repository layer features defensive reading (skips corrupted rows with non-fatal warnings) and RFC-4180 compliant quotation escaping.

---

## Error Handling & Resilience

HMS avoids generic uncaught exceptions (`catch (Exception e) { e.printStackTrace(); }`). The system employs an explicit custom checked exception tree:
- `HospitalException` (Base application checked exception)
  - `PatientNotFoundException`
  - `DoctorNotFoundException`
  - `AppointmentConflictException`
  - `InvalidInputException`
  - `AuthenticationException`
  - `InsufficientPaymentException`

The terminal UI encapsulates inputs with boundary validation loops and handles closed input streams (EOF) gracefully without crashing.

---

## Screenshots (Placeholders)

> Capture terminal screenshots during your evaluation walkthrough and place them here:

- **Screenshot 1: Main Banner & Authentication Prompt**  
  `[Insert screenshot: Application Splash Screen]`
- **Screenshot 2: Executive Hospital Analytics Dashboard**  
  `[Insert screenshot: Hospital Daily Summary Report]`
- **Screenshot 3: Patient Registration & RegEx Validation**  
  `[Insert screenshot: Patient Registration Screen]`
- **Screenshot 4: Appointment Scheduling & Conflict Block**  
  `[Insert screenshot: Double-Booking Conflict Prevention Alert]`
- **Screenshot 5: Formatted ASCII Medical Invoice Receipt**  
  `[Insert screenshot: Printable Medical Invoice Receipt]`
- **Screenshot 6: Automated Test Suite Run (42/42 Tests Passing)**  
  `[Insert screenshot: Terminal TestRunner Execution Results]`

---

## Academic Disclaimer

This project is an **educational simulation** developed for the **VITyarthi "Build Your Own Project" evaluation** in *Programming in Java*. It is designed to demonstrate Core Java design patterns and language concepts. It is not intended for clinical decision-making or production healthcare deployment.
