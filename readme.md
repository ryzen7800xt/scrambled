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

## Deploy the complete app on Render

ignore this

Render can serve both the website and API from the same Spring Boot service; Netlify is optional. Connect this GitHub repository to a Render Web Service using the Docker runtime and the repository root as the service root. The [Dockerfile](./Dockerfile) packages the static pages alongside the Spring Boot application.

For persistent production accounts, recipes, and inbox messages, attach a PostgreSQL database in the same Render region and set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from its connection details. Also set `SPRING_JPA_HIBERNATE_DDL_AUTO=update` and `SCRAMBLED_COOKIE_SECURE=true` in the Render service environment. After pushing changes to the connected branch, Render will redeploy the service.

Open the Render service URL (for example, `https://scrambled-26h6.onrender.com/`) for the home page. The API remains available at `/api/recipes`.
