# Hospital Management System (HMS)

A modular, console-based **Hospital Management System** developed using **Core Java** as part of the VITyarthi Programming in Java course evaluation.

The system is designed to manage common hospital activities such as patient registration, doctor management, appointment scheduling, billing, payments, and basic hospital reports. Data is stored locally using CSV files, so the project does not require an external database.

The project also includes a lightweight web dashboard that runs locally at `http://localhost:8080`. The web interface is implemented using standard Java libraries without external frameworks.

---

## Project Overview

The Hospital Management System provides separate functionality for four types of users:

* **Admin**
* **Receptionist**
* **Doctor**
* **Patient**

Each role has access to features relevant to its responsibilities. The system validates user input, prevents appointment conflicts, maintains billing records, and stores important system activities in local files.

The project was developed using Java SE features such as object-oriented programming, collections, streams, exception handling, file I/O, and the Java Date/Time API.

---

## Key Features

* Developed using standard **Java SE**
* No external frameworks or database required
* Console-based interactive application
* Local web dashboard using Java's built-in HTTP server functionality
* Role-based access control
* Patient registration and management
* Doctor profile and availability management
* Appointment scheduling with conflict detection
* Appointment rescheduling and cancellation
* Consultation notes and prescription information
* Bill generation and payment tracking
* Support for partial payments
* Daily hospital reports
* CSV-based data persistence
* Audit logging
* Password hashing using salted SHA-256
* Automated test suite with 42 test cases

---

## Requirements

Before running the project, make sure the following are installed:

* Java Development Kit (JDK) 8 or later
* Command Prompt, PowerShell, Terminal, or Git Bash
* A web browser for accessing the local dashboard

You can verify the Java installation using:

```bash
java -version
javac -version
```

---

## How to Run

### Option 1: Using the Launcher Script

The project contains scripts to simplify compilation and execution.

#### Windows

Run the following from Command Prompt or PowerShell:

```cmd
run.bat
```

To start only the local web dashboard:

```cmd
run_localhost.bat
```

#### Linux / macOS

Make the script executable:

```bash
chmod +x run.sh
```

Then run:

```bash
./run.sh
```

For the local web dashboard:

```bash
chmod +x run_localhost.sh
./run_localhost.sh
```

---

## Option 2: Compile and Run Manually

### Step 1: Compile the Source Code

#### Windows PowerShell

```powershell
if (!(Test-Path out)) {
    New-Item -ItemType Directory -Path out
}

$files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName }

javac -encoding UTF-8 -d out $files
```

#### Linux / macOS / Git Bash

```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm sources.txt
```

### Step 2: Start the Console Application

```bash
java -cp out com.hms.Main
```

The application will display the login and navigation menus in the terminal.

### Step 3: Start the Local Web Dashboard

```bash
java -cp out com.hms.ui.LocalHostServer
```

After the server starts, open the following address in a browser:

```text
http://localhost:8080
```

---

## Running the Tests

The project includes an automated test runner containing **42 test cases**.

The tests cover areas such as:

* Patient registration
* Input validation
* Doctor management
* Appointment scheduling
* Appointment conflict detection
* Billing calculations
* Payment validation
* Authentication
* Data handling

### Compile Source and Test Files

#### Windows PowerShell

```powershell
$files = Get-ChildItem -Recurse -Filter *.java src,test | ForEach-Object { $_.FullName }

javac -encoding UTF-8 -d out $files
```

### Run the Test Suite

```bash
java -cp out com.hms.TestRunner
```

Expected result:

```text
42 / 42 tests passed
```

---

## Demo Login Credentials

The project includes sample accounts for testing the different roles.

| Role         | Username    | Password      | Sample User      | Main Access                                    |
| ------------ | ----------- | ------------- | ---------------- | ---------------------------------------------- |
| Admin        | `admin`     | `Admin@123`   | Dr. Rajesh Mehta | User, doctor, reports and audit management     |
| Receptionist | `recep`     | `Recep@123`   | Ananya Sen       | Patient registration, appointments and billing |
| Doctor       | `dr_sharma` | `Doctor@123`  | Dr. Priya Sharma | Appointments and consultation notes            |
| Doctor       | `dr_verma`  | `Doctor@123`  | Dr. Arun Verma   | Appointments and availability                  |
| Patient      | `pat_rahul` | `Patient@123` | Rahul Verma      | Personal appointments and bills                |

The sample patient account is associated with:

```text
Patient ID: PAT-1001
```

These credentials are intended only for demonstrating the project.

---

# Functional Modules

## 1. Patient Management

The patient management module handles the registration and maintenance of patient records.

Main functions include:

* Register new patients
* Validate patient information
* Automatically generate patient IDs
* Search patients
* Update patient details
* Deactivate patient accounts

The system validates information such as:

* Patient name
* Age between 0 and 120
* 10-digit phone number
* Blood group
* Gender

Patient IDs are generated in a format similar to:

```text
PAT-1001
```

Patients can be searched using their name, phone number, or patient ID.

---

## 2. Doctor Management

The doctor management module maintains information about doctors working in the hospital.

It includes:

* Doctor registration
* Specialization
* Department
* Clinic hours
* Availability
* Doctor status

Supported doctor statuses include:

```text
ACTIVE
ON_LEAVE
INACTIVE
```

Doctors can also be filtered based on their department, such as:

* Cardiology
* Neurology
* Orthopedics
* Other hospital departments

---

## 3. Appointment Management

The appointment module is responsible for scheduling and maintaining appointments between patients and doctors.

The system supports:

* Creating appointments
* Rescheduling appointments
* Cancelling appointments
* Viewing appointments
* Completing appointments
* Adding consultation notes

### Appointment Conflict Detection

The system checks for scheduling conflicts before creating an appointment.

For example, if a doctor already has an appointment at a particular date and time, another appointment cannot be created for the same doctor at that time.

The system also prevents a patient from being assigned to conflicting appointments at the same time.

This helps maintain consistent appointment schedules.

---

## 4. Billing and Payments

The billing module calculates and maintains patient invoices.

A bill can contain:

* Consultation charges
* Diagnostic test charges
* Medicine charges
* Room charges
* Discounts
* GST/tax

The general bill calculation follows the applicable charges and adjustments before determining the final amount.

The system also supports partial payments.

For example:

```text
Total Bill       : ₹5,000
Amount Paid      : ₹3,000
Remaining Amount : ₹2,000
```

Payment status is maintained based on the amount paid and the remaining balance.

The application can also generate text-based invoices for viewing or printing.

---

## 5. Reports and Analytics

The reporting module provides a summary of hospital activity.

The daily report can include:

* Total patients
* Active doctors
* Today's appointments
* Revenue collected
* Pending payments

Reports can also be exported as CSV files for further use.

Java Streams are used in parts of the reporting logic to process and summarize records.

---

## 6. Authentication and Audit Logging

The application provides role-based authentication.

Users are authenticated before accessing their respective menus and features.

Passwords are not stored directly as plain text. The application uses salted SHA-256 hashing for password storage.

Important system activities are recorded in:

```text
data/audit.log
```

The audit log provides a basic record of actions performed within the system.

---

# Java Concepts Used

The project demonstrates several important concepts from Core Java.

## Object-Oriented Programming

### Inheritance

A common `User` class is extended by role-specific classes such as:

```text
Admin
Receptionist
Doctor
```

This allows common user properties and behavior to be reused.

### Polymorphism

Role-based menu routing and generic repository operations demonstrate polymorphic behavior.

### Encapsulation

Model classes use private fields with controlled access through getters and setters. Validation is applied where appropriate.

### Abstraction

The project uses interfaces and abstract classes to separate common behavior from implementation details.

For example:

```text
Repository<T>
```

provides a generic structure for data access.

---

## Collections Framework

Different Java collection classes are used according to the requirements of each module.

Examples include:

```text
ArrayList
LinkedHashMap
HashSet
```

These are used for maintaining lists, caching records, and performing validation or membership checks.

---

## Java Streams

Java Streams are used for processing collections and generating reports.

Operations include:

```text
filter()
groupingBy()
summingDouble()
```

These operations help calculate statistics from the stored hospital records.

---

## Java Date and Time API

The `java.time` package is used for appointment scheduling.

Important classes include:

```text
LocalDate
LocalTime
```

These classes are used to represent appointment dates and times and to perform scheduling and conflict checks.

---

## Exception Handling

The project uses exception handling to manage invalid operations and unexpected conditions.

Custom exceptions include:

```text
AppointmentConflictException
PatientNotFoundException
InsufficientPaymentException
```

Using custom exceptions makes it easier to identify and handle specific application errors.

---

## File I/O

The application stores data locally in CSV files.

The file-handling system manages:

* Reading records
* Writing records
* Updating records
* CSV formatting
* Quote escaping
* Local data persistence

All persistent data is stored inside:

```text
data/
```

---

# Project Structure

```text
HospitalManagementSystem/
│
├── README.md
├── statement.md
│
├── run.bat
├── run.sh
├── run_localhost.bat
├── run_localhost.sh
│
├── src/
│   └── com/
│       └── hms/
│           ├── Main.java
│           │
│           ├── model/
│           │   ├── User.java
│           │   ├── Admin.java
│           │   ├── Receptionist.java
│           │   ├── Doctor.java
│           │   ├── Patient.java
│           │   ├── Appointment.java
│           │   └── Bill.java
│           │
│           ├── model/enums/
│           │   ├── UserRole.java
│           │   ├── Gender.java
│           │   ├── AppointmentStatus.java
│           │   └── PaymentStatus.java
│           │
│           ├── repository/
│           │   └── CSV persistence classes
│           │
│           ├── service/
│           │   ├── Scheduling
│           │   ├── Billing
│           │   └── Validation
│           │
│           ├── exception/
│           │   └── Custom exceptions
│           │
│           ├── util/
│           │   ├── Input validation
│           │   ├── ID generation
│           │   └── Password hashing
│           │
│           └── ui/
│               ├── Admin menu
│               ├── Receptionist menu
│               ├── Doctor menu
│               ├── Patient menu
│               └── LocalHostServer.java
│
├── test/
│   └── com/
│       └── hms/
│           └── TestRunner.java
│
├── data/
│   ├── patients.csv
│   ├── doctors.csv
│   ├── appointments.csv
│   ├── bills.csv
│   ├── users.csv
│   └── audit.log
│
└── docs/
    ├── Academic report
    ├── Diagrams
    └── Rubric mapping
```

---

# Local Web Dashboard

The project also includes a simple browser-based interface for viewing and interacting with hospital records.

To start the dashboard, run:

```bash
java -cp out com.hms.ui.LocalHostServer
```

Then open:

```text
http://localhost:8080
```

The dashboard provides access to information such as:

* Hospital statistics
* Patient records
* Doctor records
* Appointments
* Billing information
* Basic forms for adding records

The web dashboard is intended as a lightweight demonstration interface. It uses standard Java functionality instead of frameworks such as Spring Boot.

---

# Data Storage

The system does not use MySQL, PostgreSQL, MongoDB, or another external database.

Instead, records are stored locally as CSV files inside the `data/` directory.

Example:

```text
data/
├── patients.csv
├── doctors.csv
├── appointments.csv
├── bills.csv
├── users.csv
└── audit.log
```

This approach keeps the project simple to set up and suitable for a Core Java academic environment.

---

# Testing

The project contains an automated test suite with 42 test cases.

The tests are intended to verify important application behavior, including:

```text
Patient Registration
Input Validation
Doctor Management
Appointment Scheduling
Appointment Conflicts
Billing Calculations
Payment Validation
Authentication
```

Current test result:

```text
42 / 42 tests passed
100% success rate
```

---

# Limitations

This project is designed for academic demonstration and is not intended for deployment in an actual hospital environment.

Some limitations include:

* CSV files are used instead of a production database.
* The web interface is intentionally lightweight.
* The authentication system is designed for demonstration purposes.
* The application does not integrate with external healthcare systems.
* It does not provide real medical diagnosis or clinical decision-making.
* It does not include real-world payment gateway integration.
* Concurrent multi-user access is limited compared with production hospital systems.

---

# Academic Purpose

The Hospital Management System was developed as an educational project for the **Programming in Java** course under **VITyarthi**.

The main purpose of the project is to demonstrate practical use of Core Java concepts in a complete application rather than implementing an actual clinical hospital information system.

The project combines object-oriented programming, collections, file handling, exception handling, authentication, scheduling logic, billing calculations, and basic reporting into a single application.

---

# Conclusion

The Hospital Management System provides a practical example of how Core Java can be used to build a modular application for managing hospital-related operations.

By combining a console interface, local file persistence, role-based access, appointment management, billing, reporting, and a lightweight web dashboard, the project demonstrates how different Java concepts can work together in an end-to-end application.

The project is intended primarily for academic evaluation, learning, and demonstration of Java programming concepts.
