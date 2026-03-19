# Authentication and Authorization Policies

## Purpose
This document details the authentication and authorization mechanisms employed in the backend system, covering user management, API security, and network policies.

## Authentication Policy

### Overview
The system uses **HTTP Session-based authentication**. It does not employ JWT. Upon successful login, authentication information is stored in the server session, and the session is maintained via browser cookies (`JSESSIONID`).

### User Model
Currently, there is only a single role: `TEACHER`. All authenticated users have identical permissions without a separate authorization table.

### Authentication Flow
-   **Login:** `POST /auth/login` endpoint is used. It validates credentials against `AuthenticationManager` and `TeacherUserDetailsService`, hashes passwords with BCrypt, and establishes an `HttpSession`.
-   **Session Management:** `JSESSIONID` cookie is used for subsequent requests.
-   **Logout:** `POST /auth/logout` invalidates the session.

### API Endpoints (Authentication Related)
| Method | Path       | Auth Required | Description                                 |
| :----- | :--------- | :------------ | :------------------------------------------ |
| POST   | `/auth/login` | No            | Login and issue session cookie              |
| POST   | `/auth/logout`| No*           | Logout and invalidate session               |
| GET    | `/auth/me`  | No*           | Retrieve information of the currently logged-in user |

*Note: While `/auth/logout` and `/auth/me` are permitted without authentication, they will return `401` if no valid `Authentication` object is present.*

### Password Policy
-   **Hashing:** BCrypt is used for password hashing. Plain text passwords are not stored.
-   **Initial Account:** A default `admin` teacher account is created on server startup, with credentials configurable via `INITIAL_TEACHER_USERNAME` and `INITIAL_TEACHER_PASSWORD` environment variables. The password will be updated if the account already exists.

## Authorization Policy

### Access Control
-   Spring Security configuration (`SecurityConfig.java`) defines access rules:
    -   `/auth/**`: Permitted without authentication (`permitAll`).
    -   `/h2-console/**`: Permitted without authentication (only for local profiles).
    -   All other paths (`/api/**` and others): Require authentication (`anyRequest().authenticated()`).
-   Role-Based Access Control (RBAC) is not implemented; all authenticated users have the same permissions.

## Network Policies

### CORS Policy
-   `allowCredentials: true` is set, enabling cookie-based sessions across cross-origin requests.
-   Allowed Origins:
    -   Local (default): `http://localhost:3000`
    -   Production (`prod` profile): `https://*.vercel.app`
-   Allowed Methods: `GET, POST, PUT, DELETE, OPTIONS`
-   Allowed Headers: `*`

## Security Configuration Summary

| Item             | Configuration                     | Notes                                      |
| :--------------- | :-------------------------------- | :----------------------------------------- |
| CSRF             | Disabled                          | Not required for REST APIs                 |
| Form Login       | Disabled                          | Custom `/auth/login` used                  |
| HTTP Basic Auth  | Disabled                          | Replaced by session-based authentication   |
| Session Storage  | `HttpSessionSecurityContextRepository` | Default Spring Security behavior           |
| H2 Console       | Allowed (local profile only)      | `frameOptions` disabled for H2 Console     |
