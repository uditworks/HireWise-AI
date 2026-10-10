# HireWise AI — Intelligent Recruitment & Talent Matching Platform

HireWise AI is a full-stack recruitment platform engineered to connect job seekers with employers through explainable resume analysis, deterministic talent-job matching, and role-based applicant tracking. Built on a Spring Boot 3.4 backend with Java 21 and a modern React 19 single-page frontend.

---

## 🚀 Key Features

### 🏢 Public Job Discovery
- **Live Job Catalog**: Browse and search published job openings with real-time keyword and employment type filters (`FULL_TIME`, `PART_TIME`, `CONTRACT`, `INTERNSHIP`, `REMOTE`).
- **Comprehensive Job Details**: Inspect salary ranges, location requirements, skill tags, and recruiter contact info.
- **Access Isolation**: Draft and closed postings remain strictly hidden from unauthenticated and public searches.

### 👤 Job Seeker Experience
- **Secure Authentication**: Register as a `JOB_SEEKER` with client and server-validated email/password policies.
- **Resume Vault**: Upload, view, download, replace, and delete PDF resumes up to 10MB.
- **Deterministic Resume Analyzer**: Algorithmic extraction of skills, education, and career experience.
- **Talent-Job Matcher**: Compare your uploaded PDF resumes against published job postings to receive instant compatibility scores, matching skills, and missing skill breakdowns.
- **Application Lifecycle**: Submit applications with custom cover letters, track review progress in real-time, and withdraw pending submissions.

### 👔 Recruiter Portal
- **Job Management Dashboard**: Create, edit, publish, and close job requisitions owned by the authenticated recruiter.
- **Applicant Tracking System (ATS)**: Filter candidates by status (`SUBMITTED`, `UNDER_REVIEW`, `SHORTLISTED`, `REJECTED`, `ACCEPTED`, `WITHDRAWN`).
- **Status Workflow Engine**: Update candidate statuses through backend-enforced transition rules.
- **Secure Resume Downloads**: Stream and download candidate PDF attachments through authenticated endpoints.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 19, Vite 8, React Router v7, Tailwind CSS v3, Axios, Lucide React, Oxlint |
| **Backend** | Java 21, Spring Boot 3.4, Spring Security 6, Spring Data JPA, Hibernate, JJWT (JSON Web Token) |
| **Database** | MySQL 8.0+ (Automatic schema synchronization via Hibernate `update`) |
| **File Processing** | Apache PDFBox (PDF text extraction & metadata parsing) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, Vite Production Bundler |

---

## 📁 Repository Structure

```text
hirewise-backend/
├── hirewise-backend/               # Spring Boot 3.4 Java Backend
│   ├── mvnw / mvnw.cmd             # Maven Wrapper scripts
│   ├── pom.xml                     # Maven dependencies & plugins
│   ├── storage/                    # Default local storage for resumes (git-ignored)
│   └── src/
│       ├── main/
│       │   ├── java/com/hirewise/hirewise_backend/
│       │   │   ├── config/         # SecurityConfig, CorsConfig, PasswordEncoder
│       │   │   ├── controller/     # REST Endpoints (Auth, Jobs, Applications, Resumes, Health)
│       │   │   ├── dto/            # Data Transfer Objects (Requests & Responses)
│       │   │   ├── entity/         # JPA Entities (User, Job, Application, Resume)
│       │   │   ├── exception/      # GlobalExceptionHandler & custom exceptions
│       │   │   ├── repository/     # Spring Data JPA Repositories
│       │   │   ├── security/       # JwtTokenProvider, JwtAuthFilter, UserDetailsService
│       │   │   └── service/        # Business Logic & AI Parsing Services
│       │   └── resources/
│       │       └── application.properties # Spring configuration & environment mappings
│       └── test/                   # 133 Unit & Integration Tests (100% passing)
│
├── hirewise-frontend/              # React 19 + Vite Frontend SPA
│   ├── package.json                # Dependencies, build & lint scripts
│   ├── vite.config.js              # Vite config with /api reverse proxy to :8080
│   ├── tailwind.config.js          # Tailwind CSS theme configuration
│   ├── dist/                       # Production bundle output
│   └── src/
│       ├── api/                    # Axios client & service modules (auth, jobs, applications, resumes)
│       ├── components/             # Reusable UI (Navbar, Footer, Modal, Badge, ErrorBoundary)
│       ├── context/                # AuthContext (JWT state) & ToastContext (notifications)
│       ├── pages/                  # Public, Seeker, and Recruiter views
│       └── utils/                  # Constants, formatting helpers & status mappings
│
└── README.md                       # Complete project documentation
```

---

## 📋 Prerequisites & Software Requirements

- **Java Development Kit (JDK)**: Version 21 (Eclipse Temurin, Oracle, or OpenJDK)
- **Node.js**: Version 18.x or 20.x LTS with `npm` 9+
- **MySQL Server**: Version 8.0 or newer
- **Operating System**: Windows 10/11, macOS, or Linux

---

## ⚙️ Backend Environment Variables & Configuration

The backend dynamically reads credentials and configuration from environment variables with sensible development defaults.

Create or set the following environment variables in your operating system or `.env` file before launching:

| Variable Name | Description | Default Value | Example for Production |
|---|---|---|---|
| `DB_USERNAME` | MySQL database username | `root` | `hirewise_user` |
| `DB_PASSWORD` | MySQL user password | *(empty)* | `StrongDbPass#2026!` |
| `JWT_SECRET` | 256-bit secret key for signing tokens | *(auto-generated)* | `4a8f9c0e...` (>=32 chars) |
| `JWT_EXPIRATION_MS`| Token lifetime in milliseconds | `86400000` (24 hours) | `86400000` |
| `RESUME_STORAGE_DIR`| Local directory for storing resumes | `./storage/resumes` | `/var/hirewise/resumes` |
| `AI_PROVIDER` | Resume analysis mode | `local` | `local` / `gemini` |
| `AI_API_KEY` | External LLM API key (optional) | *(empty)* | `AIzaSy...` |

### Setting Environment Variables on Windows (PowerShell)
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="YourVerySecure256BitSecretKeyMustBe32CharsOrLonger!"
```

---

## 🚀 Running the Application Locally

Follow these instructions to start both backend and frontend servers in separate terminals.

### Terminal 1: Start Spring Boot Backend
```powershell
# Navigate to backend directory
cd hirewise-backend

# Start the Spring Boot application using Maven Wrapper
.\mvnw.cmd spring-boot:run
```
*The backend starts at `http://localhost:8080`. Verify connectivity by visiting `http://localhost:8080/api/health` in your browser.*

### Terminal 2: Start React Frontend
```powershell
# Navigate to frontend directory
cd hirewise-frontend

# Install dependencies (first time only)
npm install

# Start the Vite development server
npm run dev
```
*The frontend starts at `http://localhost:5173`. Open this URL in your web browser.*

---

## 🌐 Local Access URLs

| Application / Service | URL | Purpose |
|---|---|---|
| **Frontend Web App** | `http://localhost:5173` | React User Interface |
| **Backend API Health** | `http://localhost:8080/api/health` | Health Check Endpoint |
| **Backend REST API** | `http://localhost:8080/api/...` | Protected & Public REST Endpoints |

---

## 👥 User Roles & Authentication Architecture

### Authentication Mechanism
1. **Stateless JWT Authorization**: Upon login (`POST /api/auth/login`), the backend returns a signed JWT token containing user identity and role authority.
2. **Persistent Session**: The frontend stores the token in `localStorage` and automatically attaches `Authorization: Bearer <token>` to all subsequent Axios requests.
3. **Automatic Expiration & Stale Token Handling**: If the token expires or returns `401 Unauthorized`, the Axios interceptor clears authentication and redirects the user to `/login`.
4. **Role Guards**: Frontend `ProtectedRoute` elements prevent unauthorized navigation across portal boundaries, while backend controller annotations (`@PreAuthorize`) strictly enforce security on the API level.

### User Roles
- **`JOB_SEEKER`**: Default role for public registrations. Can upload resumes, analyze fit against jobs, and manage job applications.
- **`RECRUITER`**: Administrative role. Can create and manage job postings, review applicants, download resumes, and update applicant statuses.

---

## 📄 PDF Resumes & AI Analysis Limitations

- **File Type**: Strictly `.pdf` format. Non-PDF files are rejected with a `400 Bad Request`.
- **File Size Limit**: Up to 10 Megabytes (enforced by Spring Servlet configuration).
- **Text Extraction**: Handled via Apache PDFBox. Resumes composed purely of flattened images/scans without selectable text will produce an empty extracted text notification.
- **Deterministic AI Scoring**: The local analyzer uses keyword extraction, educational degree recognition, and competency matching to compute explainable compatibility scores. Scores serve as advisory guidance for candidates and recruiters.

---

## 🧪 Testing & Automated Verification

### Running Backend Unit & Integration Tests
```powershell
cd hirewise-backend
.\mvnw.cmd test
```
*Executes all 133 test cases spanning authentication, authorization, controllers, repositories, services, and file storage.*

### Running Frontend Checks & Build
```powershell
cd hirewise-frontend

# Run fast code quality checks
npm run lint

# Build production bundle
npm run build
```

---

## 🔧 Troubleshooting Common Setup Issues

| Symptom | Cause | Solution |
|---|---|---|
| `Port 8080 is already in use` | Another process is occupying port 8080 | Terminate the occupying PID using `netstat -ano \| findstr :8080` and `taskkill /F /PID <PID>` |
| `Access denied for user 'root'@'localhost'` | MySQL password mismatch | Ensure `$env:DB_PASSWORD` matches your local MySQL server password |
| `Network Error` when calling `/api/...` | Backend is not running or proxy misconfigured | Verify the backend is listening on `http://localhost:8080/api/health` |
| `File upload fails (413 Payload Too Large)` | PDF exceeds 10MB | Compress PDF file or increase `spring.servlet.multipart.max-file-size` |
| `Invalid JWT signature` | Secret changed between server restarts | Log out and log back in to obtain a newly signed token |

---

## 🔒 Security & Production Deployment Checklist

1. **Secrets Management**: Never commit real database passwords, production JWT secrets, or cloud API keys to source control. Use environment variables or a vault manager.
2. **JWT Secret Strength**: Ensure production JWT secrets are at least 256 bits (32+ alphanumeric characters) to prevent brute-force attacks against HMAC-SHA256 signatures.
3. **Password Security**: All user passwords are encrypted using BCrypt with secure salt generation prior to persistence.
4. **Storage Isolation**: Configure `RESUME_STORAGE_DIR` to an isolated, non-executable filesystem directory with strict OS-level read/write permissions.
5. **CORS & CSRF**: In production, configure explicit origin whitelists in `CorsConfig.java` to restrict API access to trusted frontend domain names.
6. **HTTPS / TLS**: Always terminate SSL/TLS at your reverse proxy (Nginx / Cloudflare) to ensure tokens and resumes are encrypted in transit.
