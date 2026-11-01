# Scrambled!
Made with love by ryzen7800xt/Caleb!

Scrambled! is a Spring Boot recipe app with recipe search, account-based recipe submissions, accessibility preferences, and a private in-app inbox.

## Run locally

You need Java 17 or newer and Maven installed. From the project folder, run:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The sample recipes load automatically. The default H2 database is in memory and resets when the app restarts.

Create an account on the Recipes page to add recipes. Registration requires a validly formatted email address, a username (3–30 letters, numbers, dots, underscores, or hyphens), and a password (at least 12 characters and at most 72 UTF-8 bytes). Sign in with the same email address and password. Passwords are stored as BCrypt hashes. Email addresses are checked for valid format and uniqueness, but ownership is not verified by email; account updates and recipe confirmations appear in the Profile & inbox page, and the app does not send notification emails.

Accessibility preferences (bold text, larger text, high contrast, and reduced motion) are saved in the current browser.

## Deploy the backend and database

ignore this
