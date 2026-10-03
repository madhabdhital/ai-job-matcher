# AI Job Matcher – Requested Changes

## Implemented

1. Public homepage
   - `/` and `/dashboard` show jobs without login.
   - `View Details` is protected and sends unauthenticated visitors to login.
   - After login, the user is returned to the requested job details page.

2. Save Application
   - On Job Details, `Track Application` is renamed to `Save Application`.
   - Backend behavior is intentionally unchanged, as requested.

3. Resume section
   - Uploaded resume bytes are now stored.
   - The Resume page shows the user's own uploaded resume.
   - PDFs get an in-page preview; DOCX files show extracted text.
   - Existing resume metadata/text remains usable; older records without stored file bytes need one re-upload for file preview.

4. Resume-based job matching
   - Resume upload detects a primary domain and extracts common skills.
   - Logged-in users see `/api/v1/jobs/recommended`, filtered by that domain.
   - Public visitors still see all jobs.
   - Supported example domains include Software Development, Data Science & AI, Data & Analytics, Cloud & DevOps, Cybersecurity, UI/UX & Design, Marketing & Sales, Finance & Accounting, and Human Resources.

5. AI analysis
   - Switched the implementation from Groq to Gemini.
   - Uses structured JSON output instead of asking the model to imitate a JSON format.
   - This is the main fix for inconsistent Analyze Match response structures.
   - Configure `GEMINI_API_KEY` locally.

6. Free job source
   - Added a Himalayas sync endpoint: `POST /api/v1/jobs/sync/free`.
   - It imports a page of remote jobs and a separate internship search.
   - Himalayas is a public no-auth API; attribution is required.

## Local configuration

Do not commit real credentials. Set:

- `DB_PASSWORD`
- `JWT_SECRET`
- `GEMINI_API_KEY`

The repository's secret/config files now contain placeholders.

## Important

The uploaded project contained credentials in source/config files. Rotate the exposed database/API credentials before using the project again, especially if the project was ever pushed to GitHub or shared with anyone.
