# AI Job Matcher

A full-stack web app that helps students find jobs and internships in their own field. Upload your resume, see openings matched to your domain, and get an AI analysis of how well your resume fits a job, with matched skills, missing skills and a short cover letter.

**Live demo:** https://YOUR-APP.vercel.app

> The backend runs on a free hosting plan and sleeps when idle, so the first load after a quiet period can take about a minute.

## Features

- Public homepage that lists jobs and internships, with search, job-type filter and pagination
- Registration and login with JWT authentication
- Job details are available only to logged-in users
- Resume upload (PDF or DOCX) with automatic domain detection, so the dashboard shows jobs for your field
- View your uploaded resume on the My Resume page
- AI "Analyze Match": match percentage, matched skills, missing skills and a tailored cover letter, returned in a fixed structure
- Save jobs to your applications list and track their status
- Jobs imported automatically from a free job feed on startup and once a day
- Login and sign-up rate limiting, and job descriptions cleaned before display

## Tech stack

| Part | Technology |
|------|------------|
| Frontend | React, Vite, React Router |
| Backend | Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA |
| Database | MySQL |
| AI | Groq API (LLM with structured JSON output and automatic retries) |
| Job data | Himalayas free job feed |
| Hosting | Vercel (frontend), Render (backend), Aiven (database) |

## Project structure

```
ai-job-matcher/
├── ai-job-matcher/   # Spring Boot backend
└── frontend/         # React frontend
```

## Run it locally

You need Java 17, Node.js 18+ and a MySQL database.

**1. Backend** (from `ai-job-matcher/ai-job-matcher`). Set these environment variables, then start the app:

```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/jobmatcher
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
APP_JWT_SECRET=a_random_string_of_at_least_32_characters
GROQ_API_KEY=your_groq_key
```

```
./mvnw spring-boot:run
```

The backend runs on http://localhost:8080.

**2. Frontend** (from `ai-job-matcher/frontend`):

```
npm install
npm run dev
```

Open http://localhost:5173. Requests to `/api` are forwarded to the backend by the Vite dev server.

## Deployment

The frontend is deployed on Vercel, which forwards `/api` requests to the backend on Render. The database is a free managed MySQL service on Aiven. All secrets are set as environment variables on the hosting platforms and are never committed to the repository.

## Credits

Job listings are provided by [Himalayas](https://himalayas.app).
