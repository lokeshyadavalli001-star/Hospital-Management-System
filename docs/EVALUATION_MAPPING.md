# VITyarthi Evaluation Rubric Mapping & Evidence

This document provides direct mapping between the **VITyarthi "Build Your Own Project" Evaluation Rubric** dimensions and concrete implementation evidence present in this repository.

---

## Rubric Dimension 1: Problem Understanding & Requirements (Weightage: 10%)

| Evaluation Criteria | Concrete Implementation Evidence in Codebase | Source Reference |
| :--- | :--- | :--- |
| **Realistic Domain Problem** | Solves outpatient clinical administration, eliminating double-booking, tracking patient demographics, and handling multi-item billing reconciliation. | [`statement.md`](file:///statement.md#L12-L26) |
| **Clear Scope & Boundaries** | Explicitly defines in-scope administration features and out-of-scope clinical/EHR diagnostic claims. | [`statement.md`](file:///statement.md#L28-L50) |
| **Comprehensive Functional Requirements** | 6 Major Functional Modules documented with granular functional requirements (FR-1.1 to FR-6.3). | [`docs/requirements.md`](file:///docs/requirements.md#L3-L75) |
| **Explicit Non-Functional Requirements** | 7 NFRs defined with verifiable metrics (Performance, Security, Usability, Reliability, Maintainability, Error Handling, Resource Efficiency). | [`docs/requirements.md`](file:///docs/requirements.md#L77-L95) |

---

## Rubric Dimension 2: Design & Documentation (Weightage: 20%)

| Evaluation Criteria | Concrete Implementation Evidence in Codebase | Source Reference |
| :--- | :--- | :--- |
| **System Architecture Diagram** | Layered 4-tier diagram mapping Presentation $\rightarrow$ Service $\rightarrow$ Domain Model $\rightarrow$ Repository $\rightarrow$ Storage. | [`docs/diagrams/system_architecture.mermaid`](file:///docs/diagrams/system_architecture.mermaid) |
| **Use Case Diagram** | Complete actor-to-feature mapping for Admin, Receptionist, Doctor, and Patient personas. | [`docs/diagrams/use_case_diagram.mermaid`](file:///docs/diagrams/use_case_diagram.mermaid) |
| **Class Diagram** | UML class diagram capturing inheritance, interfaces, associations, and repository generics. | [`docs/diagrams/class_diagram.mermaid`](file:///docs/diagrams/class_diagram.mermaid) |
| **Sequence Diagram** | Step-by-step transaction flow of appointment booking, conflict verification, and bill generation. | [`docs/diagrams/sequence_diagram.mermaid`](file:///docs/diagrams/sequence_diagram.mermaid) |
| **Process Flow / Workflow Diagram** | End-to-end outpatient journey from patient arrival to invoicing and analytics update. | [`docs/diagrams/workflow_diagram.mermaid`](file:///docs/diagrams/workflow_diagram.mermaid) |
| **Storage / ER Design Diagram** | Relational CSV storage schemas with foreign key references and data attributes. | [`docs/diagrams/storage_er_diagram.mermaid`](file:///docs/diagrams/storage_er_diagram.mermaid) |
| **Design Decisions & Rationale** | Documented architectural tradeoffs (layered design, CSV vs SQLite, decoupled Patient model). | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md#L8-L10) |

---

## Rubric Dimension 3: Implementation Quality (Weightage: 25%)

| Evaluation Criteria | Concrete Implementation Evidence in Codebase | Source Reference |
| :--- | :--- | :--- |
| **OOP Inheritance & Abstraction** | Abstract class `User` extended by `Admin`, `Receptionist`, and `Doctor`. Abstract methods define role capabilities. | [`User.java`](file:///src/com/hms/model/User.java), [`Doctor.java`](file:///src/com/hms/model/Doctor.java) |
| **OOP Polymorphism & Encapsulation** | Strict private fields, validated accessors. Polymorphic menu dispatching and repository access. | [`ConsoleMenu.java`](file:///src/com/hms/ui/ConsoleMenu.java), [`Bill.java`](file:///src/com/hms/model/Bill.java) |
| **Generics & Interfaces** | `Repository<T extends Identifiable>` contract and generic `AbstractCsvRepository<T>` implementation. | [`Repository.java`](file:///src/com/hms/repository/Repository.java), [`AbstractCsvRepository.java`](file:///src/com/hms/repository/AbstractCsvRepository.java) |
| **Collections Framework** | `ArrayList` for ordered lists, `LinkedHashMap` for $O(1)$ cached storage, `HashSet` for validation sets. | [`AbstractCsvRepository.java`](file:///src/com/hms/repository/AbstractCsvRepository.java), [`InputValidator.java`](file:///src/com/hms/util/InputValidator.java) |
| **Java Streams API** | Real-time analytics computing grouping, counting, and revenue sums via `Stream` pipelines. | [`ReportService.java`](file:///src/com/hms/service/ReportService.java) |
| **Modern Date/Time API** | `LocalDate`, `LocalTime`, `LocalDateTime` formatting, parsing, and working-hours range comparisons. | [`DateTimeUtil.java`](file:///src/com/hms/util/DateTimeUtil.java), [`AppointmentService.java`](file:///src/com/hms/service/AppointmentService.java) |
| **Exception Architecture** | Custom checked exceptions (`AppointmentConflictException`, `PatientNotFoundException`, `InsufficientPaymentException`). | [`com.hms.exception`](file:///src/com/hms/exception/HospitalException.java) |
| **File-Based Persistence** | Robust CSV parsing handling quoted commas, directory auto-creation, and non-fatal malformed row skips. | [`FileUtil.java`](file:///src/com/hms/util/FileUtil.java), [`AbstractCsvRepository.java`](file:///src/com/hms/repository/AbstractCsvRepository.java) |
| **Input Validation** | RegEx validation for phones, emails, blood groups, and monetary boundary conditions. | [`InputValidator.java`](file:///src/com/hms/util/InputValidator.java) |

---

## Rubric Dimension 4: Innovation, Depth & Complexity (Weightage: 15%)

| Feature Area | Technical Depth / Innovation Demonstrated | Implementation Reference |
| :--- | :--- | :--- |
| **Slot Collision Engine** | Real-time cross-entity verification preventing doctor double-booking, patient slot clashes, and after-hours bookings. | [`AppointmentService.java`](file:///src/com/hms/service/AppointmentService.java#L52-L92) |
| **Cryptographic Security** | Password salting and hashing simulation using standard `java.security.MessageDigest` SHA-256 and `SecureRandom`. | [`PasswordUtil.java`](file:///src/com/hms/util/PasswordUtil.java) |
| **Itemized Invoicing Engine** | Multi-tier accounting calculating gross subtotal, concession discount, GST tax, and partial payment balance. | [`Bill.java`](file:///src/com/hms/model/Bill.java), [`BillingService.java`](file:///src/com/hms/service/BillingService.java) |
| **Printable ASCII Invoice** | Neatly aligned terminal receipts complete with clinic headers, patient metadata, item breakdown, and due amounts. | [`BillingService.java`](file:///src/com/hms/service/BillingService.java#L125-L165) |
| **Security Audit Trail** | Append-only transaction logging recording security and administrative actions with timestamps and actors. | [`AuditService.java`](file:///src/com/hms/service/AuditService.java) |
| **Analytical Reporting & Export** | Executive dashboard calculating live hospital stats and exporting formatted CSV summary reports. | [`ReportService.java`](file:///src/com/hms/service/ReportService.java) |

---

## Rubric Dimension 5: GitHub Repository & Version Control (Weightage: 10%)

| Evaluation Criteria | Concrete Implementation Evidence in Codebase | Source Reference |
| :--- | :--- | :--- |
| **Clean Repository Structure** | Clear top-level organization (`src/`, `test/`, `data/`, `docs/`, `run.bat`, `run.sh`). | [Project Root Directory](file:///) |
| **Professional README** | Complete overview, features, prerequisites, build/run commands, credentials, and concept breakdown. | [`README.md`](file:///README.md) |
| **Academic Statement File** | Dedicated `statement.md` detailing problem statement, scope, target users, and expected outcomes. | [`statement.md`](file:///statement.md) |
| **Git Configuration** | Exhaustive `.gitignore` filtering out `.class`, `out/`, IDE configs, and sandbox test files. | [`.gitignore`](file:///.gitignore) |
| **Platform-Agnostic Launchers** | Ready-to-run scripts for Windows (`run.bat`) and Linux/macOS (`run.sh`). | [`run.bat`](file:///run.bat), [`run.sh`](file:///run.sh) |

---

## Rubric Dimension 6: Project Report (Weightage: 20%)

| Report Section Requirement | Corresponding Section in Report | Source Reference |
| :--- | :--- | :--- |
| 1. Cover Page & Academic Metadata | Section 1 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 2. Introduction | Section 2 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 3. Problem Statement | Section 3 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 4. Objectives | Section 4 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 5. Functional & Non-Functional Requirements | Section 5 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 6. System Architecture | Section 6 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 7. Design Diagrams (6 UML/Mermaid Diagrams) | Section 7 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 8. Design Decisions & Rationale | Section 8 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 9. Implementation Details | Section 9 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 10. Screenshots / Terminal Results Placeholders | Section 10 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 11. Testing Approach & Results (42/42 Tests) | Section 11 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 12. Challenges Faced & Mitigations | Section 12 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 13. Learnings & Key Takeaways | Section 13 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 14. Future Enhancements | Section 14 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
| 15. References | Section 15 | [`docs/PROJECT_REPORT.md`](file:///docs/PROJECT_REPORT.md) |
