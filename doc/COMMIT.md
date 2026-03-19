# Commit Convention Guidelines

## Purpose
This document defines the project's commit message convention, based on Conventional Commits, to ensure consistency and clarity in the commit history.

## Format
A commit message consists of a header, an optional body, and an optional footer.

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Header
-   **`<type>`**: The type of change. Must be one of the following:
    -   `feat`: New feature
    -   `fix`: Bug fix
    -   `docs`: Documentation changes
    -   `style`: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc.)
    -   `refactor`: A code change that neither fixes a bug nor adds a feature
    -   `test`: Adding missing tests or correcting existing tests
    -   `chore`: Changes to the build system, tools, or dependencies, or other maintenance tasks.
-   **`<scope>`** (Optional): A noun describing the section of the codebase this commit affects (e.g., `auth`, `frontend`, `backend`, `infra`).
-   **`<subject>`**: A concise description of the change.
    -   Must be in English.
    -   Must use the imperative mood (e.g., `add`, `fix`, `update`).
    -   Must not end with a period.
    -   Should be 50 characters or less.

### Body (Optional)
-   Provide more context about the change if necessary.
-   Separate it from the header by a blank line.
-   Wrap at or below 72 characters.

### Footer (Optional)
-   Used for referencing issues or other metadata.
-   Can include `Co-authored-by:` tags. However, DO NOT include `Co-Authored-By: Claude`.

## Rules Summary
-   Commit messages must be in English.
-   Use the imperative mood for the subject line.
-   Do not end the subject line with a period.
-   Keep the subject line to 50 characters or less.
-   Provide a body and/or footer when necessary, separated by a blank line.
-   **DO NOT** add `Co-Authored-By: Claude` to commit messages.

## Examples

```
feat(auth): add JWT login endpoint

This commit introduces the JWT-based authentication endpoint, allowing users to log in and receive a token.

fix(frontend): resolve null pointer on page load

The null pointer exception occurred when the user's profile data was not immediately available. This fix adds a check for the user object before accessing its properties.

docs: update README with setup instructions

Added detailed steps for setting up the project locally in the README.md file.

chore: upgrade Spring Boot to 3.4

Updated the Spring Boot version to 3.4 to leverage the latest features and security patches.
```
