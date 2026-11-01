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

Netlify hosts the static frontend, not this Spring Boot server or its database. Keep the API on a Java-capable host and use a persistent PostgreSQL database for production.

1. Deploy this repository's Spring Boot app to a Java 17+ service on your preferred host. Set its start command to `java -jar target/recipe-app-0.0.1-SNAPSHOT.jar` and make sure Maven builds that jar.
2. Provision PostgreSQL with that host. Set `SPRING_DATASOURCE_URL` to its JDBC URL (for example, `jdbc:postgresql://HOST:5432/DATABASE?sslmode=require`), plus `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.
3. Set `SPRING_JPA_HIBERNATE_DDL_AUTO=update` and `SCRAMBLED_COOKIE_SECURE=true` on the HTTPS backend. Never use the default in-memory H2 database for a public deployment.
4. Publish this workspace to a Git provider, then in Netlify choose **Add new site → Import an existing project**, connect that repository, and deploy. The included [netlify.toml](./netlify.toml) publishes `src/` and routes `/api/*` through a serverless proxy.
5. In Netlify site environment variables, set `SCRAMBLED_API_BASE_URL` to the backend's HTTPS origin only (for example, `https://your-backend.example.com`, with no path or trailing API route), then trigger a new deploy so the function picks it up.
6. Confirm the deployed site's `/api/recipes` endpoint loads and test registering, signing in, adding a recipe, signing out, and opening the inbox.

The Netlify proxy keeps API requests and the HttpOnly session cookie on the site's origin. This workspace is not currently initialized as a Git repository and has no Netlify account connection, so you must publish it to GitHub/GitLab and connect it in your own Netlify account. Set host-specific secrets in your backend/Netlify account only; do not put database credentials in frontend files. Build output and `.env` files are excluded by [.gitignore](./.gitignore).