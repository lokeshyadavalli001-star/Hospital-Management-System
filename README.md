# Hospital Management System (HMS)

A modular, console-based **Hospital Management System** developed in **Core Java** for the VITyarthi course evaluation project. 

It handles everyday hospital operations—such as registering patients, managing doctors, scheduling appointments without time conflicts, generating bills, and viewing real-time hospital reports. It stores all records locally in CSV files and also includes a lightweight local web dashboard (`http://localhost:8080`) built purely using standard Java.

---

##  Project Highlights

- **Pure Java SE**: Built using standard Java (`java.util`, `java.time`, `java.io`). No external frameworks (like Spring Boot) or databases required.
- **Easy to Run**: Can be compiled and run directly from the terminal or using simple scripts (`run.bat` or `run.sh`).
- **Conflict-Free Appointments**: Prevents booking the same doctor or patient at the same date and time.
- **Billing & Payments**: Calculates consultation fee, diagnostic tests, medicines, discounts, and taxes with support for partial payments.
- **Role-Based Access**: 4 distinct roles: **Admin**, **Receptionist**, **Doctor**, and **Patient**.
- **File Persistence**: Automatically saves and reads data from CSV files in the `data/` folder.
- **Dual Mode**: Interactive Command-Line Interface (CLI) + Localhost Web Dashboard (`http://localhost:8080`).

---

##  Quick Start (How to Run)

### Option 1: Using the Launcher Script (Easiest)

#### On Windows:
Double-click or run in PowerShell / Command Prompt:
```cmd
run.bat
```
*(To launch the local web server directly, run `run_localhost.bat`)*

#### On Linux / macOS:
```bash
chmod +x run.sh
./run.sh
```

---

### Option 2: Using Standard Java Commands

#### 1. Compile the Project:
**Windows (PowerShell):**
```powershell
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out }
$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
```

**Linux / Mac / Git Bash:**
```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm sources.txt
```

#### 2. Run the Terminal Application:
```bash
java -cp out com.hms.Main
```

#### 3. Run the Localhost Web Dashboard:
```bash
java -cp out com.hms.ui.LocalHostServer
```
Then open your browser at **http://localhost:8080**.

---

## 🧪 How to Run Tests

The project includes an automated test runner with 42 test cases covering registration, validation, scheduling conflicts, billing calculations, and authentication:

```powershell
# Compile both src and test
$files = Get-ChildItem -Recurse -Filter *.java src,test | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files

# Run the test suite
java -cp out com.hms.TestRunner
```
**Result**: 42 / 42 tests pass (100% success rate).

---

##  Demo Login Credentials

The system comes pre-loaded with sample accounts:

| Role | Username | Password | Who It Is | What They Can Do |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `Admin@123` | Dr. Rajesh Mehta | Full access: manage users, doctors, view reports & audit logs |
| **Receptionist** | `recep` | `Recep@123` | Ananya Sen | Register patients, book/cancel appointments, generate bills |
| **Doctor** | `dr_sharma` | `Doctor@123` | Dr. Priya Sharma | View daily appointments, write consultation notes |
| **Doctor** | `dr_verma` | `Doctor@123` | Dr. Arun Verma | View appointments, update availability |
| **Patient** | `pat_rahul` | `Patient@123` | Rahul Verma | View personal appointments and bills (`PAT-1001`) |

---

##  Main Functional Modules

1. **Patient Management**:
   - Register new patients with validation (name, age 0–120, 10-digit phone, blood group).
   - Automatically generates patient IDs like `PAT-1001`.
   - Search by name, phone number, or ID.
   - Update patient details or deactivate accounts.

2. **Doctor Management**:
   - Manage doctor profiles, specialization, department, and clinic hours.
   - View doctors by department (Cardiology, Neurology, Orthopedics, etc.).
   - Set status: `ACTIVE`, `ON_LEAVE`, or `INACTIVE`.

3. **Appointment Management**:
   - Book appointments with conflict detection.
   - If a doctor is already booked at that time, the system blocks the slot.
   - Reschedule or cancel appointments.
   - Doctors can mark appointments `COMPLETED` and add prescription notes.

4. **Billing & Invoices**:
   - Calculate total bill: Consultation + Tests + Medicines + Room Charges.
   - Apply discounts and tax (GST).
   - Supports partial payments and calculates remaining balance.
   - View and print neat text invoices.

5. **Reports & Analytics**:
   - Daily summary: total patients, active doctors, today's appointments, revenue collected, and pending dues.
   - Option to export report as a CSV file.

6. **Security & Audit Logs**:
   - Passwords stored securely using salted SHA-256 hashes (no plain-text passwords).
   - System transactions recorded in `data/audit.log`.

---

##  Java Concepts Used

- **OOP Concepts**:
  - **Inheritance**: Base class `User` extended by `Admin`, `Receptionist`, and `Doctor`.
  - **Polymorphism**: Menu routing based on user role; generic repository access.
  - **Encapsulation**: Private variables with validation in getters and setters.
  - **Abstraction**: Generic `Repository<T>` interface and abstract classes.
- **Collections Framework**: `ArrayList` for lists, `LinkedHashMap` for fast in-memory cache, and `HashSet` for validation sets.
- **Java Streams**: Used for calculating real-time report summaries (`filter`, `groupingBy`, `summingDouble`).
- **Date/Time API (`java.time`)**: `LocalDate` and `LocalTime` for scheduling and appointment overlap checks.
- **Exception Handling**: Custom exceptions like `AppointmentConflictException`, `PatientNotFoundException`, and `InsufficientPaymentException`.
- **File I/O**: Reading and writing CSV files under `data/` folder with proper quote escaping.

---

##  Project Structure

```
├── README.md               # Quick overview and running guide
├── statement.md            # Problem statement and scope
├── run.bat / run.sh        # One-click terminal run scripts
├── run_localhost.bat / .sh # One-click localhost web server scripts
│
├── src/com/hms/
│   ├── Main.java           # Entry point and login router
│   ├── model/              # User, Admin, Doctor, Patient, Appointment, Bill
│   ├── model/enums/        # UserRole, Gender, AppointmentStatus, PaymentStatus
│   ├── repository/         # Generic CSV persistence and file handling
│   ├── service/            # Business logic (scheduling, billing, validation)
│   ├── exception/          # Custom exceptions
│   ├── util/               # Input validation, ID generator, password hashing
│   └── ui/                 # Menus for Admin, Receptionist, Doctor, Patient, and LocalHostServer
│
├── test/com/hms/           # Automated test suite (TestRunner.java)
├── data/                   # Saved CSV files (patients, doctors, appointments, bills, users)
└── docs/                   # Full academic report, diagrams, and rubric mapping
```

---

##  Localhost Web Dashboard

To see the system on your browser:
1. Run `run_localhost.bat` (or select Option 4 in the terminal menu).
2. Open **http://localhost:8080** in any browser.
3. You will see live statistics, patient list, doctor list, appointments, and billing with simple forms to add new records.

---

##  Academic Note
This project is an educational simulation created for academic evaluation in the **Programming in Java** course at **VITyarthi**. It is not meant for real-world hospital clinical diagnosis.
