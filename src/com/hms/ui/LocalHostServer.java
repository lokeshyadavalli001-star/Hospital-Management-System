package com.hms.ui;

import com.hms.exception.HospitalException;
import com.hms.model.Appointment;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.enums.Gender;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import com.hms.service.AppointmentService;
import com.hms.service.AuditService;
import com.hms.service.BillingService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;
import com.hms.service.ReportService;
import com.hms.service.UserService;
import com.hms.util.DateTimeUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Built-in Core Java HTTP Server hosting a local web dashboard on http://localhost:8080.
 * Operates purely with standard JDK com.sun.net.httpserver.HttpServer with zero external libraries.
 */
public class LocalHostServer {

    private static final int PORT = 8080;
    private final HttpServer server;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final ReportService reportService;
    private final AuditService auditService;

    public LocalHostServer(PatientService patientService,
                           DoctorService doctorService,
                           AppointmentService appointmentService,
                           BillingService billingService,
                           ReportService reportService,
                           AuditService auditService) throws IOException {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
        this.reportService = reportService;
        this.auditService = auditService;

        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.server.setExecutor(Executors.newCachedThreadPool());

        // Register handlers
        this.server.createContext("/", new DashboardHandler());
        this.server.createContext("/api/patient/add", new AddPatientHandler());
        this.server.createContext("/api/appointment/book", new BookAppointmentHandler());
        this.server.createContext("/api/bill/pay", new PayBillHandler());
    }

    public void start() {
        server.start();
        System.out.println("================================================================================");
        System.out.println("       HMS LOCALHOST WEB DASHBOARD IS LIVE AT: http://localhost:" + PORT);
        System.out.println("       Open the above URL in your browser to inspect live hospital data.");
        System.out.println("================================================================================");
    }

    public void stop() {
        server.stop(1);
    }

    public static void main(String[] args) throws Exception {
        String dataDir = "data";
        AuditService auditService = new AuditService(dataDir + File.separator + "audit.log");
        UserRepository userRepo = new UserRepository(dataDir + File.separator + "users.csv");
        PatientRepository patientRepo = new PatientRepository(dataDir + File.separator + "patients.csv");
        DoctorRepository docRepo = new DoctorRepository(dataDir + File.separator + "doctors.csv");
        AppointmentRepository apptRepo = new AppointmentRepository(dataDir + File.separator + "appointments.csv");
        BillRepository billRepo = new BillRepository(dataDir + File.separator + "bills.csv");

        UserService userService = new UserService(userRepo, auditService);
        PatientService patientService = new PatientService(patientRepo, auditService);
        DoctorService doctorService = new DoctorService(docRepo, userRepo, auditService);
        AppointmentService appointmentService = new AppointmentService(apptRepo, patientService, doctorService, auditService);
        BillingService billingService = new BillingService(billRepo, patientService, auditService);
        ReportService reportService = new ReportService(patientService, doctorService, appointmentService, billingService);

        LocalHostServer localhost = new LocalHostServer(patientService, doctorService, appointmentService, billingService, reportService, auditService);
        localhost.start();

        System.out.println("Press Ctrl+C in terminal to stop server.");
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ReportService.DailySummary summary = reportService.generateDailySummary(LocalDate.now());
            List<Patient> patients = patientService.getAllPatients();
            List<Doctor> doctors = doctorService.getAllDoctors();
            List<Appointment> appts = appointmentService.getAllAppointments();
            List<Bill> bills = billingService.getAllBills();
            List<String> logs = auditService.getRecentLogs(15);

            String html = renderHtml(summary, patients, doctors, appts, bills, logs);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class AddPatientHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String name = params.getOrDefault("name", "").trim();
                    int age = Integer.parseInt(params.getOrDefault("age", "30").trim());
                    Gender gender = Gender.fromString(params.getOrDefault("gender", "OTHER"));
                    String phone = params.getOrDefault("phone", "").trim();
                    String email = params.getOrDefault("email", "").trim();
                    String address = params.getOrDefault("address", "").trim();
                    String blood = params.getOrDefault("bloodGroup", "O+").trim();
                    String emergency = params.getOrDefault("emergencyContact", "").trim();

                    patientService.registerPatient(name, age, gender, phone, email, address, blood, emergency, "LOCALHOST_WEB");
                } catch (Exception e) {
                    System.err.println("[WEB ERROR] Patient registration failed: " + e.getMessage());
                }
            }
            redirect(exchange, "/");
        }
    }

    private class BookAppointmentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String patId = params.getOrDefault("patientId", "").trim();
                    String docId = params.getOrDefault("doctorId", "").trim();
                    LocalDate date = LocalDate.parse(params.getOrDefault("date", LocalDate.now().toString()).trim());
                    LocalTime time = LocalTime.parse(params.getOrDefault("time", "10:00").trim());
                    String reason = params.getOrDefault("reason", "Outpatient Consultation").trim();

                    appointmentService.bookAppointment(patId, docId, date, time, reason, "LOCALHOST_WEB");
                } catch (Exception e) {
                    System.err.println("[WEB ERROR] Appointment booking failed: " + e.getMessage());
                }
            }
            redirect(exchange, "/");
        }
    }

    private class PayBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    String billId = params.getOrDefault("billId", "").trim();
                    double amount = Double.parseDouble(params.getOrDefault("amount", "0").trim());
                    billingService.recordPayment(billId, amount, "LOCALHOST_WEB");
                } catch (Exception e) {
                    System.err.println("[WEB ERROR] Payment failed: " + e.getMessage());
                }
            }
            redirect(exchange, "/");
        }
    }

    private static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String[] pairs = sb.toString().split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }

    private static void redirect(HttpExchange exchange, String target) throws IOException {
        exchange.getResponseHeaders().set("Location", target);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private String renderHtml(ReportService.DailySummary summary,
                              List<Patient> patients,
                              List<Doctor> doctors,
                              List<Appointment> appts,
                              List<Bill> bills,
                              List<String> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>City Hospital Management System</title>");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        sb.append("<style>");
        sb.append(":root { --primary: #0284c7; --primary-dark: #0369a1; --bg: #f8fafc; --card: #ffffff; --text: #1e293b; --muted: #64748b; --border: #e2e8f0; --success: #16a34a; --warning: #d97706; --danger: #dc2626; }");
        sb.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif; }");
        sb.append("body { background: var(--bg); color: var(--text); padding-bottom: 40px; }");
        sb.append(".navbar { background: #0f172a; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        sb.append(".navbar h1 { font-size: 1.25rem; display: flex; align-items: center; gap: 0.5rem; }");
        sb.append(".badge { background: #38bdf8; color: #0f172a; font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 9999px; font-weight: bold; }");
        sb.append(".container { max-width: 1200px; margin: 1.5rem auto; padding: 0 1rem; }");
        sb.append(".grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }");
        sb.append(".card { background: var(--card); padding: 1.25rem; border-radius: 10px; border: 1px solid var(--border); box-shadow: 0 1px 3px rgba(0,0,0,0.05); }");
        sb.append(".stat-label { font-size: 0.85rem; color: var(--muted); text-transform: uppercase; font-weight: 600; }");
        sb.append(".stat-val { font-size: 1.75rem; font-weight: bold; margin-top: 0.25rem; color: var(--primary-dark); }");
        sb.append(".nav-tabs { display: flex; gap: 0.5rem; border-bottom: 2px solid var(--border); margin-bottom: 1.5rem; }");
        sb.append(".tab-btn { padding: 0.6rem 1.2rem; background: none; border: none; border-bottom: 3px solid transparent; font-weight: 600; color: var(--muted); cursor: pointer; }");
        sb.append(".tab-btn.active { color: var(--primary); border-bottom-color: var(--primary); }");
        sb.append(".tab-content { display: none; }");
        sb.append(".tab-content.active { display: block; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 0.75rem; background: var(--card); border-radius: 8px; overflow: hidden; }");
        sb.append("th, td { padding: 0.75rem 1rem; text-align: left; border-bottom: 1px solid var(--border); font-size: 0.9rem; }");
        sb.append("th { background: #f1f5f9; font-weight: 600; color: #475569; }");
        sb.append(".tag { display: inline-block; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.75rem; font-weight: 600; }");
        sb.append(".tag-active, .tag-paid, .tag-completed { background: #dcfce7; color: #166534; }");
        sb.append(".tag-scheduled, .tag-partial { background: #fef3c7; color: #92400e; }");
        sb.append(".tag-cancelled, .tag-pending { background: #fee2e2; color: #991b1b; }");
        sb.append("form { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.75rem; background: #f8fafc; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border: 1px solid var(--border); }");
        sb.append("input, select { padding: 0.5rem; border: 1px solid var(--border); border-radius: 6px; font-size: 0.9rem; }");
        sb.append("button.btn { background: var(--primary); color: white; border: none; padding: 0.6rem 1rem; border-radius: 6px; font-weight: 600; cursor: pointer; }");
        sb.append("button.btn:hover { background: var(--primary-dark); }");
        sb.append(".logs { font-family: monospace; font-size: 0.85rem; background: #0f172a; color: #38bdf8; padding: 1rem; border-radius: 8px; max-height: 250px; overflow-y: auto; }");
        sb.append("</style></head><body>");

        // Header
        sb.append("<div class=\"navbar\"><h1>City General Hospital & Research Center</h1><span class=\"badge\">Pure Core Java SE Localhost</span></div>");
        sb.append("<div class=\"container\">");

        // KPI Summary Cards
        sb.append("<div class=\"grid\">");
        sb.append("<div class=\"card\"><div class=\"stat-label\">Total Patients</div><div class=\"stat-val\">").append(summary.totalPatients).append("</div></div>");
        sb.append("<div class=\"card\"><div class=\"stat-label\">Active Doctors</div><div class=\"stat-val\">").append(summary.activeDoctors).append("</div></div>");
        sb.append("<div class=\"card\"><div class=\"stat-label\">Today's Appointments</div><div class=\"stat-val\">").append(summary.todaysAppointments).append("</div></div>");
        sb.append("<div class=\"card\"><div class=\"stat-label\">Total Revenue Billed</div><div class=\"stat-val\">INR ").append(String.format("%.2f", summary.totalRevenueBilled)).append("</div></div>");
        sb.append("<div class=\"card\"><div class=\"stat-label\">Revenue Collected</div><div class=\"stat-val\" style=\"color:var(--success);\">INR ").append(String.format("%.2f", summary.totalRevenueCollected)).append("</div></div>");
        sb.append("<div class=\"card\"><div class=\"stat-label\">Outstanding Balance</div><div class=\"stat-val\" style=\"color:var(--danger);\">INR ").append(String.format("%.2f", summary.totalRevenuePending)).append("</div></div>");
        sb.append("</div>");

        // Tabs
        sb.append("<div class=\"nav-tabs\">");
        sb.append("<button class=\"tab-btn active\" onclick=\"showTab('patients')\">Patients (").append(patients.size()).append(")</button>");
        sb.append("<button class=\"tab-btn\" onclick=\"showTab('doctors')\">Doctors (").append(doctors.size()).append(")</button>");
        sb.append("<button class=\"tab-btn\" onclick=\"showTab('appointments')\">Appointments (").append(appts.size()).append(")</button>");
        sb.append("<button class=\"tab-btn\" onclick=\"showTab('billing')\">Invoices & Billing (").append(bills.size()).append(")</button>");
        sb.append("<button class=\"tab-btn\" onclick=\"showTab('audit')\">Audit Trail</button>");
        sb.append("</div>");

        // Patients Tab
        sb.append("<div id=\"tab-patients\" class=\"tab-content active\">");
        sb.append("<div class=\"card\"><h3>Register New Outpatient</h3><form method=\"POST\" action=\"/api/patient/add\">");
        sb.append("<input name=\"name\" placeholder=\"Full Name\" required>");
        sb.append("<input name=\"age\" type=\"number\" placeholder=\"Age\" min=\"0\" max=\"120\" required>");
        sb.append("<select name=\"gender\"><option value=\"MALE\">Male</option><option value=\"FEMALE\">Female</option><option value=\"OTHER\">Other</option></select>");
        sb.append("<input name=\"phone\" placeholder=\"Phone (10 digits)\" required>");
        sb.append("<input name=\"bloodGroup\" placeholder=\"Blood Group (e.g. O+, B-)\">");
        sb.append("<button type=\"submit\" class=\"btn\">+ Register Patient</button></form>");
        sb.append("<table><thead><tr><th>ID</th><th>Name</th><th>Age</th><th>Gender</th><th>Phone</th><th>Blood</th><th>Status</th></tr></thead><tbody>");
        for (Patient p : patients) {
            sb.append("<tr><td><strong>").append(p.getId()).append("</strong></td><td>").append(p.getName()).append("</td><td>")
                    .append(p.getAge()).append("</td><td>").append(p.getGender()).append("</td><td>").append(p.getPhone())
                    .append("</td><td>").append(p.getBloodGroup()).append("</td><td><span class=\"tag tag-active\">")
                    .append(p.isActive() ? "Active" : "Inactive").append("</span></td></tr>");
        }
        sb.append("</tbody></table></div></div>");

        // Doctors Tab
        sb.append("<div id=\"tab-doctors\" class=\"tab-content\">");
        sb.append("<div class=\"card\"><h3>Physician & Consultant Directory</h3>");
        sb.append("<table><thead><tr><th>ID</th><th>Doctor Name</th><th>Department</th><th>Specialization</th><th>Fee</th><th>Hours</th><th>Status</th></tr></thead><tbody>");
        for (Doctor d : doctors) {
            sb.append("<tr><td><strong>").append(d.getId()).append("</strong></td><td>Dr. ").append(d.getFullName()).append("</td><td>")
                    .append(d.getDepartment()).append("</td><td>").append(d.getSpecialization()).append("</td><td>INR ")
                    .append(String.format("%.2f", d.getConsultationFee())).append("</td><td>")
                    .append(DateTimeUtil.formatTime(d.getAvailableFrom())).append(" - ").append(DateTimeUtil.formatTime(d.getAvailableTo()))
                    .append("</td><td><span class=\"tag tag-active\">").append(d.getStatus()).append("</span></td></tr>");
        }
        sb.append("</tbody></table></div></div>");

        // Appointments Tab
        sb.append("<div id=\"tab-appointments\" class=\"tab-content\">");
        sb.append("<div class=\"card\"><h3>Book New Outpatient Visit</h3><form method=\"POST\" action=\"/api/appointment/book\">");
        sb.append("<input name=\"patientId\" placeholder=\"Patient ID (e.g. PAT-1001)\" required>");
        sb.append("<input name=\"doctorId\" placeholder=\"Doctor ID (e.g. DOC-2001)\" required>");
        sb.append("<input name=\"date\" type=\"date\" value=\"").append(LocalDate.now()).append("\" required>");
        sb.append("<input name=\"time\" type=\"time\" value=\"10:00\" required>");
        sb.append("<input name=\"reason\" placeholder=\"Reason for Visit\">");
        sb.append("<button type=\"submit\" class=\"btn\">+ Book Appointment</button></form>");
        sb.append("<table><thead><tr><th>Appt ID</th><th>Patient</th><th>Doctor</th><th>Date</th><th>Time</th><th>Status</th><th>Reason</th></tr></thead><tbody>");
        for (Appointment a : appts) {
            String tagClass = "tag-" + a.getStatus().name().toLowerCase();
            sb.append("<tr><td><strong>").append(a.getId()).append("</strong></td><td>").append(a.getPatientId()).append("</td><td>")
                    .append(a.getDoctorId()).append("</td><td>").append(DateTimeUtil.formatDate(a.getAppointmentDate())).append("</td><td>")
                    .append(DateTimeUtil.formatTime(a.getAppointmentTime())).append("</td><td><span class=\"tag ").append(tagClass).append("\">")
                    .append(a.getStatus()).append("</span></td><td>").append(a.getReason()).append("</td></tr>");
        }
        sb.append("</tbody></table></div></div>");

        // Billing Tab
        sb.append("<div id=\"tab-billing\" class=\"tab-content\">");
        sb.append("<div class=\"card\"><h3>Quick Payment Intake</h3><form method=\"POST\" action=\"/api/bill/pay\">");
        sb.append("<input name=\"billId\" placeholder=\"Invoice ID (e.g. BIL-4001)\" required>");
        sb.append("<input name=\"amount\" type=\"number\" step=\"0.01\" placeholder=\"Payment Amount (INR)\" required>");
        sb.append("<button type=\"submit\" class=\"btn\">+ Record Payment</button></form>");
        sb.append("<table><thead><tr><th>Invoice ID</th><th>Patient</th><th>Appt ID</th><th>Total (INR)</th><th>Paid (INR)</th><th>Balance (INR)</th><th>Status</th><th>Date</th></tr></thead><tbody>");
        for (Bill b : bills) {
            String tagClass = "tag-" + b.getPaymentStatus().name().toLowerCase();
            sb.append("<tr><td><strong>").append(b.getId()).append("</strong></td><td>").append(b.getPatientId()).append("</td><td>")
                    .append(b.getAppointmentId()).append("</td><td>INR ").append(String.format("%.2f", b.getFinalAmount())).append("</td><td>INR ")
                    .append(String.format("%.2f", b.getPaidAmount())).append("</td><td><strong>INR ")
                    .append(String.format("%.2f", b.getBalanceAmount())).append("</strong></td><td><span class=\"tag ").append(tagClass).append("\">")
                    .append(b.getPaymentStatus()).append("</span></td><td>").append(DateTimeUtil.formatDate(b.getBillingDate())).append("</td></tr>");
        }
        sb.append("</tbody></table></div></div>");

        // Audit Logs Tab
        sb.append("<div id=\"tab-audit\" class=\"tab-content\">");
        sb.append("<div class=\"card\"><h3>Real-Time Security & Transaction Audit Trail</h3>");
        sb.append("<div class=\"logs\">");
        for (String log : logs) {
            sb.append(log).append("<br>");
        }
        sb.append("</div></div></div>");

        // Client JS for tab switching
        sb.append("<script>");
        sb.append("function showTab(id) {");
        sb.append("document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));");
        sb.append("document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));");
        sb.append("document.getElementById('tab-' + id).classList.add('active');");
        sb.append("event.target.classList.add('active');");
        sb.append("}");
        sb.append("</script>");

        sb.append("</div></body></html>");
        return sb.toString();
    }
}
