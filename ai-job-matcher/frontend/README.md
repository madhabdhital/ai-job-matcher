# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.


# AI Job Matcher

A full-stack web app that matches jobs and internships to your resume. Upload a resume, get jobs filtered to your detected domain, and run an AI analysis of how well you fit a specific job, including matched skills, missing skills and tailored resume suggestions.

**Stack:** React 19 + Vite (frontend) · Spring Boot 3.2 + Spring Security + JWT (backend) · MySQL · Apache Tika (resume parsing) · Groq LLM API (analysis)

## Features

- **Public job feed**: browse all jobs without logging in, with search by title or company, job-type filter and pagination.
- **Registration and login** with JWT authentication and BCrypt-hashed passwords.
- **Resume upload** (PDF or DOCX): text is extracted, a primary domain and common skills are detected, and the file can be previewed again later.
- **Personalised feed**: logged-in users see jobs matching the domain detected from their resume.
- **AI match analysis**: match score, matched and missing skills, and tailored suggestions for a chosen job.
- **Application tracker**: save jobs and update their status (Applied, Interviewing, Selected, Rejected).
- **Automatic job import** from the free Himalayas API (attribution required).

## Screenshots

| Jobs feed | Job details |
| --- | --- |
| ![Jobs feed](docs/screenshots/jobs.png) | ![Job details](docs/screenshots/job-details.png) |

| Resume upload | AI analysis |
| --- | --- |
| ![Resume](docs/screenshots/resume.png) | ![Analysis](docs/screenshots/analysis.png) |

| Applications | Login |
| --- | --- |
| ![Applications](docs/screenshots/applications.png) | ![Login](docs/screenshots/login.png) |

## Project structure

```
.
├── ai-job-matcher/            # Spring Boot backend (Maven)
│   ├── src/main/java/com/jobmatcher/
│   │   ├── controller/        # REST controllers
│   │   ├── service/           # business logic, LLM + job sync
│   │   ├── security/          # JWT filter, rate limiting
│   │   └── ...
│   └── src/main/resources/    # application*.properties
└── frontend/                  # React + Vite frontend
    └── src/
        ├── pages/             # Dashboard, Login, Resume, ...
        ├── components/
        └── services/          # API calls
```

## Prerequisites

- **Java 17**
- **Node.js** (a recent version; 20.19+ is recommended for Vite 8) and npm
- **MySQL 8** running locally on port 3306
- A free **Groq API key** for the AI analysis (https://console.groq.com)

## Setup

### 1. Clone

```bash
git clone <your-repo-url>
cd <your-repo-folder>
```

### 2. Configure the backend

The database is created automatically (`createDatabaseIfNotExist=true`), but the MySQL user must exist.

Edit `ai-job-matcher/src/main/resources/application-local.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Edit `ai-job-matcher/src/main/resources/application-secret.properties`:

```properties
app.jwt.secret=A_LONG_RANDOM_STRING_AT_LEAST_32_CHARACTERS
app.jwt.expiration-ms=1800000

groq.api.key=YOUR_GROQ_API_KEY
groq.model=openai/gpt-oss-20b

# Optional legacy job source; leave blank if unused
indianapi.jobs.url=
indianapi.jobs.api-key=
```

> **Never commit real credentials.** `application-secret.properties` should stay out of Git. If any key or password was ever pushed or shared, rotate it.

### 3. Run the backend

```bash
cd ai-job-matcher
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**. On startup the app imports jobs from Himalayas, so the feed fills in after a moment.

### 4. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. Vite proxies `/api` requests to the backend on port 8080, so no extra configuration is needed.

## Using the app

1. Open the home page to browse jobs without an account.
2. Click **Register** and create an account. Passwords need 8+ characters with an uppercase letter, a lowercase letter, a number and a special character (`@$!%*?&`).
3. Go to **My Resume** and upload a PDF or DOCX. Your domain is detected automatically.
4. Return to **Jobs** to see matches for your domain, open a job, and click **Analyze Match** for the AI report.
5. Click **Save Application** to track a job, then update its status on the **Applications** page.

## API overview

Base path: `/api/v1`. Everything except auth and the public job feed requires `Authorization: Bearer <token>`.

| Method | Endpoint | Auth | Purpose |
| --- | --- | --- | --- |
| POST | `/auth/register` | No | Create an account |
| POST | `/auth/login` | No | Log in, returns a JWT |
| GET | `/jobs?q=&type=&domain=&page=&size=` | No | Search and paginate jobs |
| GET | `/jobs/recommended` | Yes | Jobs matching your resume domain |
| GET | `/jobs/{id}` | Yes | Job details |
| POST | `/resumes/upload` | Yes | Upload a resume |
| GET | `/resumes/current` | Yes | Your current resume |
| GET | `/resumes/download` | Yes | Download your resume |
| POST | `/job-analysis/{jobId}` | Yes | Run AI match analysis |
| GET | `/job-analysis/{jobId}` | Yes | Fetch a saved analysis |
| POST | `/applications` | Yes | Save a job to track |
| GET | `/applications` | Yes | List your applications |
| PUT | `/applications/{id}/status` | Yes | Update application status |
| DELETE | `/applications/{id}` | Yes | Remove an application |

Job types: `INTERNSHIP`, `FULL_TIME`, `CONTRACT`. Page size is capped at 50.

## Running the tests

```bash
cd ai-job-matcher
./mvnw test
```

The tests cover registration (hashing, email normalisation, duplicate emails), login (valid and invalid credentials), request validation rules, and job search (filtering, pagination limits, recommendations). They use mocks, so no database is needed.

## Troubleshooting

- **Blank or unstyled page**: make sure you ran `npm install` and are opening port 5173, not 8080.
- **`Access denied` or connection errors to MySQL**: check the username and password in `application-local.properties`, and that MySQL is running.
- **"AI analysis is not configured"**: `groq.api.key` is empty in `application-secret.properties`.
- **"Please upload your resume first"** on the personalised feed: upload a resume on the My Resume page.

## Credits

Remote job data from [Himalayas](https://himalayas.app) (public API, attribution required).