# EMS Spring Boot Backend (Angular + MySQL)

## 1. Before running
- Java 17+ and Maven are required.
- Back up your `employee_db` database before starting.
- This project is mapped to the supplied SQL schema: `users`, `employees`, `attendance`, `employee_leave_balance`, `leave_application`, `holidays`, `announcements`.
- Do not import the SQL dump again over an already populated database. Configure your existing database connection instead.

## 2. Database settings
`src/main/resources/application.properties` defaults to:
- URL: `jdbc:mysql://localhost:3307/employee_db`
- user: `root`
- password: blank

Change `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables or edit the properties file to match your local MySQL setup. `spring.jpa.hibernate.ddl-auto=update` is used; back up first and do not use this setting blindly on production.

## 3. Start
Open a terminal in this folder and run:
```bash
mvn spring-boot:run
```
The API listens on `http://localhost:8080` and allows the Angular dev server at `http://localhost:4200`.

## 4. Login account
The existing `users` table from the supplied SQL dump contains an admin user (`admin@company.com`) and an employee user (`mohannataraj144@gmail.com`). Their passwords are BCrypt hashes, so the plaintext password cannot be recovered from the SQL dump. Use the password you originally assigned to those accounts. If the username `admin@ems.local` does not already exist, the app creates it using `EMS_ADMIN_PASSWORD` (default `ChangeMe123!`), even if another admin user exists. Change this default immediately after first login.

Login request: `POST /api/auth/login` with JSON `{ "username": "...", "password": "..." }`.

## 5. Email OTP
Set `MAIL_USERNAME` to the sender Gmail address and `MAIL_APP_PASSWORD` to that Gmail's Google App Password. The OTP is sent to the account email; it expires after 5 minutes. OTP/reset tokens are kept in memory for development, so restarting the server invalidates them.

## 6. Main endpoints
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`, `/api/auth/verify-otp`, `/api/auth/reset-password`
- `GET/POST/PUT/DELETE /api/employees`
- `GET/POST/PUT/DELETE /api/announcements`
- `GET/POST/PUT/DELETE /api/holidays`, `GET /api/holidays/upcoming`
- `GET/POST /api/leaves`, `PUT /api/leaves/{id}/approve?approver=Admin`, `PUT /api/leaves/{id}/reject?approver=Admin`, `GET /api/leaves/upcoming?employeeId=19`
- `GET /api/leave-balance/{employeeId}`
- `POST /api/attendance/check-in/{employeeId}`, `POST /api/attendance/check-out/{employeeId}`, `GET /api/attendance/today/{employeeId}`, `/employee/{employeeId}`, `/summary/{employeeId}`
- `PUT /api/password/change?username=...`

## Important notes
- This is a generated recovery backend based on the Angular source and SQL dump. It has not been run against your local database in this environment. Start it locally and share any build/runtime error for fixes.
- Your SQL dump contains hashed passwords, not plaintext. Do not share real passwords or Gmail App Passwords in chat.
- Before production use, review authorization rules for admin-only actions and move OTP/reset-token storage to a persistent, rate-limited store.
