# HMS System Architecture & Design Specification

## 1. Architectural Overview
The **Hospital Management System (HMS)** is architected using a classical **4-tier layered architecture** tailored for high maintainability, strict separation of concerns, and clean dependency inversion. The system operates strictly within Core Java SE without relying on web containers, enterprise application servers, or external database engines.

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

## 2. Layer-by-Layer Responsibilities

### 2.1 Presentation Layer (`com.hms.ui` & `com.hms.Main`)
- **Bootstrap & Routing**: `Main.java` orchestrates dependency instantiation, verifies initial seed data presence, presents authentication prompts, and routes authenticated users to their corresponding role console.
- **Role Menus**: Concrete subclasses of `ConsoleMenu` (`AdminMenu`, `ReceptionistMenu`, `DoctorMenu`, `PatientMenu`) implement granular workflows matching each actor's authority.
- **Terminal Aesthetics & Usability**: `ConsoleUtil` delivers neatly aligned ASCII tables with dynamic column width calculation, text truncation, colored status indicators, and stream-safe input prompts that gracefully tolerate EOF or closed stdin pipes.

### 2.2 Service Layer (`com.hms.service`)
- **Encapsulated Business Logic**: Houses all domain validation, calculations, state transition constraints, and collision detection rules.
- **Cross-Entity Validation**: `AppointmentService` coordinates between `PatientService`, `DoctorService`, and `AppointmentRepository` to ensure:
  1. Patient exists and is in an `ACTIVE` status.
  2. Doctor exists and is `ACTIVE` (not `ON_LEAVE` or `INACTIVE`).
  3. Appointment time strictly falls within the doctor's declared clinic hours (`isAvailableAt`).
  4. The doctor has no concurrent active appointment at the specified date and time.
  5. The patient has no concurrent booking at the same slot.
- **Financial Reconciliation**: `BillingService` computes line-item subtotaling, concession discounts, tax rate multiplications, and enforces non-negative arithmetic and partial-payment balances.
- **Analytical Intelligence**: `ReportService` utilizes the Java 8+ Stream API (`Collectors.groupingBy`, `summingDouble`, `counting`, `filter`) to compute dynamic aggregations over in-memory collections without hardcoded metrics.
- **Auditing**: `AuditService` guarantees an append-only, timestamped transaction trail for critical events (logins, registrations, schedule updates, payments).

### 2.3 Domain Model Layer (`com.hms.model`)
- **Inheritance Hierarchy**:
  - `abstract class User` implements `Identifiable` and `Searchable`. Provides foundational credentials, profile fields, and an abstract method `public abstract String getRoleDescription()`.
  - `Admin`, `Receptionist`, and `Doctor` extend `User`.
  - `Doctor` encapsulates clinical attributes (`specialization`, `department`, `consultationFee`, `DoctorStatus`, `availableFrom`, `availableTo`, `qualification`) while inheriting authentication capabilities.
- **High Cohesion Entities**:
  - `Patient`: Maintained as an independent entity implementing `Identifiable` and `Searchable`. Intentionally uncoupled from `User` to reflect realistic clinical domain boundaries (outpatients do not inherit administrative employee state).
  - `Appointment`: Encapsulates slot timing, reasons, notes, and the `AppointmentStatus` lifecycle (`SCHEDULED` -> `RESCHEDULED` -> `COMPLETED` / `CANCELLED`).
  - `Bill`: Encapsulates financial calculations, discounts, taxes, payments, and `PaymentStatus` (`PENDING`, `PARTIAL`, `PAID`).

### 2.4 Repository & Persistence Layer (`com.hms.repository`)
- **Generic DAO Abstraction**:
  - `Repository<T extends Identifiable>` defines CRUD contracts (`findById`, `findAll`, `save`, `deleteById`, `existsById`, `count`, `search`).
  - `AbstractCsvRepository<T>` implements the **Template Method pattern**, managing file creation, parsing, in-memory caching (`LinkedHashMap<String, T>`), and synchronized disk flushes.
  - Subclasses only need to specify three abstract hooks:
    ```java
    protected abstract String getCsvHeader();
    protected abstract String serialize(T entity);
    protected abstract T deserialize(List<String> tokens) throws Exception;
    ```
- **Resilience**: Malformed CSV lines are intercepted defensively during startup, logged as warnings, and skipped without terminating the application.

---

## 3. Object-Oriented Principles Demonstrated

| Principle | Concrete Implementation in HMS |
| :--- | :--- |
| **Encapsulation** | All entity fields are declared `private`. Mutators enforce business boundaries (e.g., non-negative financial inputs in `Bill`, age bounds in `Patient`). |
| **Abstraction** | Abstract base classes `User`, `AbstractCsvRepository<T>`, and `ConsoleMenu` hide underlying persistence, formatting, and plumbing complexities behind high-level APIs. |
| **Inheritance** | `Admin`, `Receptionist`, and `Doctor` specialize `User`. Concrete repositories specialize `AbstractCsvRepository<T>`. |
| **Polymorphism** | Menus are dispatched dynamically via `UserRole`. Repositories are accessed via generic `Repository<T>` references. Overridden `matches()` provides polymorphic keyword searching. |
| **Generics** | `Repository<T extends Identifiable>` and `AbstractCsvRepository<T>` provide compile-time type safety across heterogeneous entity models. |
| **Interface Segregation** | Fine-grained interfaces `Identifiable` and `Searchable` allow entities to declare exact capabilities without bloated interfaces. |

---

## 4. Security & Cryptographic Architecture
- **Password Salting & Hashing**: Utilizes Java's standard cryptographic engine `java.security.MessageDigest` (SHA-256) combined with `java.security.SecureRandom` 8-byte hexadecimal salts. Plaintext passwords are never persisted.
- **Role-Based Access Control (RBAC)**: Menus are strictly segregated. An authenticated receptionist cannot access user administration or modify system configurations; a doctor cannot access general ledger billing controls.
- **Defensive Input Handling**: All terminal inputs pass through `InputValidator` regex and range filters before touching service logic.
