# GuardianAI — AI-Powered Cybersecurity SOC Analyst

GuardianAI is a defensive cybersecurity platform that analyzes security logs, detects suspicious activity, explains incidents using AI, stores findings in PostgreSQL, and generates incident response reports.

This project is built for educational and defensive portfolio use. It analyzes simulated logs and does not attack, exploit, scan, or interact with real targets.

## Features

- User registration and login with Spring Security
- Log file upload for `.txt` and `.log` files
- Java log parsing into structured `LogEntry` objects
- Brute force login detection
- Possible account compromise detection
- Privileged account targeting detection
- Credential stuffing detection
- Unusual login time detection
- AI-generated or local defensive incident explanations
- Incident severity and confidence scoring
- PostgreSQL incident storage
- Thymeleaf SOC dashboard
- Downloadable text incident reports
- Sample logs for quick demos

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Thymeleaf
- PostgreSQL
- Remote AI integration with local fallback
- Docker and Docker Compose
- Maven

## Architecture

Log Upload -> Log Parser -> Threat Detection Engine -> AI Analysis -> PostgreSQL -> Dashboard -> Report

See [docs/architecture.md](docs/architecture.md) for a Mermaid architecture diagram.

## Project Structure

```text
src/main/java/com/adnan/guardianai
├── controller
├── dto
├── model
├── repository
├── security
└── service
```

## How To Run

1. Install Java 17+ and Maven, or open the project in IntelliJ IDEA with a Java 17+ SDK.
2. Run `GuardianAiApplication` from IntelliJ.
3. Open `http://localhost:8080`.
4. Register, sign in, and upload a file from `sample-logs/`.

The default local setup uses an embedded development database stored in `data/guardian_ai`, so the app can run immediately while you build and demo it.

## PostgreSQL Setup

For the full portfolio database setup, start PostgreSQL and create a database:

```sql
CREATE DATABASE guardian_ai;
```

Then configure credentials with environment variables:

```properties
DB_URL=jdbc:postgresql://localhost:5432/guardian_ai
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_DRIVER=org.postgresql.Driver
```

## Docker

```bash
docker compose up --build
```

The app will run at `http://localhost:8080` and PostgreSQL will run on port `5432`.

## AI Configuration

GuardianAI works without an API key using a local defensive explanation fallback.

To use remote AI analysis:

```bash
AI_PROVIDER=remote
AI_API_KEY=your_api_key
AI_MODEL=your_model
AI_ENDPOINT=https://your-ai-provider.example/v1/chat/completions
```

The AI prompt is defensive-only and asks for SOC response guidance, MITRE ATT&CK-style mapping, severity justification, and prevention steps.

## Sample Detection

Input:

```text
2026-05-23 10:15:22 FAILED_LOGIN user=admin ip=192.168.1.50
2026-05-23 10:15:30 FAILED_LOGIN user=admin ip=192.168.1.50
2026-05-23 10:15:45 FAILED_LOGIN user=admin ip=192.168.1.50
2026-05-23 10:16:01 FAILED_LOGIN user=admin ip=192.168.1.50
2026-05-23 10:16:20 FAILED_LOGIN user=admin ip=192.168.1.50
2026-05-23 10:17:05 SUCCESS_LOGIN user=admin ip=192.168.1.50
```

Expected output:

- Threat Type: Possible Account Compromise
- Severity: Critical
- Confidence: High
- Recommended response: reset credentials, review logs, block suspicious IP where appropriate, enable MFA

## API Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/logs/upload`
- `GET /api/logs`
- `GET /api/logs/{id}`
- `GET /api/incidents`
- `GET /api/incidents/{id}`
- `GET /api/incidents/severity/{severity}`
- `POST /api/ai/analyze`
- `POST /api/ai/analyze/{incidentId}`
- `GET /api/reports/{incidentId}`

## Demo

Use [docs/demo-script.md](docs/demo-script.md) to record a short walkthrough for GitHub or LinkedIn.

## Future Improvements

- React dashboard
- JWT authentication
- PDF incident reports
- Email alerts
- More log formats
- Rule configuration UI
- MITRE ATT&CK technique IDs
- Local LLM integration through Ollama

## Resume Bullet

Designed and developed GuardianAI, a defensive cybersecurity platform that automates log analysis, threat detection, AI-assisted incident explanation, severity scoring, and report generation using Java, Spring Boot, PostgreSQL, and Git.
