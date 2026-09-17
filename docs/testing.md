# HMS Verification & Testing Strategy

## 1. Testing Philosophy & Framework Design
To adhere strictly to the VITyarthi course guidelines prohibiting unnecessary external framework dependencies, HMS incorporates an in-tree, zero-dependency automated test runner: [`com.hms.TestRunner`](file:///test/com/hms/TestRunner.java).

The test suite runs against an isolated sandbox directory (`data/test_sandbox/`), preventing test assertions from altering live sample application files.

---

## 2. Test Execution Commands

### Windows (PowerShell):
```powershell
# 1. Compile source and test classes
$files = Get-ChildItem -Recurse -Filter *.java src,test | ForEach-Object { $_.FullName }
javac -d out $files

# 2. Run automated test suite
java -cp out com.hms.TestRunner
```

### Windows (Command Prompt):
```cmd
dir /s /b src\*.java test\*.java > test_sources.txt
javac -d out @test_sources.txt
del test_sources.txt
java -cp out com.hms.TestRunner
```

### Linux / macOS (Bash):
```bash
find src test -name "*.java" > test_sources.txt
javac -d out @test_sources.txt
rm test_sources.txt
java -cp out com.hms.TestRunner
```

---

## 3. Test Suites & Verification Matrix

| Test Suite | Test Case Identifier | Objective & Verification Condition | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| **AuthenticationTest** | `TC-AUTH-01` | User registration with salt & SHA-256 hash | Non-null USR ID, hash != plaintext | **PASS** |
| | `TC-AUTH-02` | Successful authentication with valid credentials | Authenticated user returned, role matched | **PASS** |
| | `TC-AUTH-03` | Rejection of incorrect password | Throws `AuthenticationException` | **PASS** |
| | `TC-AUTH-04` | Rejection of non-existent username | Throws `AuthenticationException` | **PASS** |
| **PatientServiceTest** | `TC-PAT-01` | Register valid patient | Non-null PAT ID, demographics preserved | **PASS** |
| | `TC-PAT-02` | Reject invalid short phone number (< 10 digits) | Throws `InvalidInputException` | **PASS** |
| | `TC-PAT-03` | Reject negative age value (< 0) | Throws `InvalidInputException` | **PASS** |
| | `TC-PAT-04` | Search and locate patient by phone number | Returns matching patient entity | **PASS** |
| | `TC-PAT-05` | Case-insensitive substring search by name | Returns match list | **PASS** |
| **DoctorServiceTest** | `TC-DOC-01` | Add valid doctor with specialization and fee | Non-null DOC ID, synchronized login account | **PASS** |
| | `TC-DOC-02` | Filter doctors by department | Returns doctors assigned to department | **PASS** |
| | `TC-DOC-03` | Toggle availability status (`ACTIVE` -> `ON_LEAVE`) | Status updated in repository and cache | **PASS** |
| | `TC-DOC-04` | Doctor working hours availability check (valid slot) | `isAvailableAt(10:30)` returns `true` | **PASS** |
| | `TC-DOC-05` | Doctor working hours availability check (after hours) | `isAvailableAt(18:00)` returns `false` | **PASS** |
| **AppointmentServiceTest** | `TC-APT-01` | Book valid outpatient appointment | Status `SCHEDULED`, assigned APT ID | **PASS** |
| | `TC-APT-02` | **Slot Conflict Check**: Double-book same doctor at same slot | Throws `AppointmentConflictException` | **PASS** |
| | `TC-APT-03` | Reschedule appointment to alternative slot | Status transitions to `RESCHEDULED` | **PASS** |
| | `TC-APT-04` | Complete appointment with prescription notes | Status `COMPLETED`, notes persisted | **PASS** |
| | `TC-APT-05` | Prevent cancellation of already completed visit | Throws `InvalidInputException` | **PASS** |
| **BillingServiceTest** | `TC-BIL-01` | Invoicing arithmetic (Subtotal, Discount, GST Tax, Net) | Net final amount accurate within delta 0.01 | **PASS** |
| | `TC-BIL-02` | Initial bill state validation | Balance equals net amount, status `PENDING` | **PASS** |
| | `TC-BIL-03` | Partial payment recording | Balance reduced, status `PARTIAL` | **PASS** |
| | `TC-BIL-04` | **Overpayment Protection**: Pay amount > due balance | Throws `InsufficientPaymentException` | **PASS** |
| | `TC-BIL-05` | Full invoice balance settlement | Balance 0.00, status `PAID` | **PASS** |
| | `TC-BIL-06` | Prevent further payment on fully settled invoice | Throws `InvalidInputException` | **PASS** |

---

## 4. Test Execution Output Log Evidence
```
================================================================================
                  HMS AUTOMATED CLI TEST SUITE EXECUTION                        
================================================================================

[SUITE] Running Authentication & Security Tests...
  [PASS] User created with unique USR ID
  [PASS] Username matches
  [PASS] Password stored as hash, not plain text
  [PASS] Successful login with correct credentials
  [PASS] User role correctly identified as ADMIN
  [PASS] Authentication correctly rejected invalid password
  [PASS] Authentication correctly rejected nonexistent user

[SUITE] Running Patient Service Tests...
  [PASS] Patient registered with PAT- prefix ID
  [PASS] Patient name preserved
  [PASS] Patient age matches
  [PASS] Validation correctly rejected invalid phone number
  [PASS] Validation correctly rejected negative age
  [PASS] Patient located by phone number
  [PASS] Correct patient ID matched by phone
  [PASS] Search query matched registered patient name

[SUITE] Running Doctor Service Tests...
  [PASS] Doctor created with DOC- prefix ID
  [PASS] Department correctly set
  [PASS] Located doctor via department filter
  [PASS] Doctor status updated to ON_LEAVE
  [PASS] Doctor available within working hours (10:30)
  [PASS] Doctor unavailable outside working hours (18:00)

[SUITE] Running Appointment Service Tests...
  [PASS] Appointment booked successfully
  [PASS] Initial status is SCHEDULED
  [PASS] Doctor slot conflict correctly detected and prevented
  [PASS] Status updated to RESCHEDULED
  [PASS] Appointment time updated
  [PASS] Status updated to COMPLETED
  [PASS] Consultation notes preserved
  [PASS] Correctly prevented cancellation of already completed appointment

[SUITE] Running Billing & Accounting Tests...
  [PASS] Bill generated with BIL- prefix ID
  [PASS] Gross subtotal computed accurately
  [PASS] Tax calculated at 5% on discounted amount
  [PASS] Net final amount accurately calculated
  [PASS] Initial balance equals net payable
  [PASS] Initial status is PENDING
  [PASS] Paid amount recorded as 500.00
  [PASS] Remaining balance is 445.00
  [PASS] Status updated to PARTIAL
  [PASS] Overpayment correctly blocked with InsufficientPaymentException
  [PASS] Remaining balance is 0.00
  [PASS] Status updated to PAID
  [PASS] Correctly rejected subsequent payment on settled invoice

================================================================================
  TEST EXECUTION SUMMARY: Total: 42 | Passed: 42 | Failed: 0 (100.0% Success)
================================================================================

ALL TEST CASES PASSED SUCCESSFULLY!
```
