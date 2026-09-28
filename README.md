# Contract Watch — Contract Renewal Reminder Tracker

Spring Boot + MySQL backend with a simple localhost web UI.

## 1. Create the database

Open MySQL and run:

```sql
CREATE DATABASE contract_watch;
```

The application creates the tables automatically using JPA.

The default MySQL settings in `application.properties` are:

- username: `root`
- password: `root`

Change them there if your MySQL installation uses different credentials.

## 2. Run the project on Windows

From the `contract_watch` folder:

```cmd
mvnw.cmd spring-boot:run
```

Or in an IDE, run `ContractWatchApplication.java`.

## 3. Open the application

Browser:

`http://localhost:8080/index.html`

## Main features

1. Add and view vendors.
2. Add contracts with vendor, start date, end date, and renewal notice period.
3. Automatic renewal-review flag on the exact notice-period boundary.
4. Renewal decision logging: `RENEWED` or `TERMINATED`.
5. Renewed contracts receive a new end date.
6. Terminated contracts are removed from active renewal reminders.
7. View contracts expiring in the next 30 days.
8. Add/view document references such as local file paths or document URLs.
9. Validation and clear global error responses.
10. Scheduled renewal check runs every 60 seconds so the rule can be demonstrated locally.

## Important demo case for the automatic flag

Suppose today is `2026-09-28`.

Create an active contract with:

- End date: `2026-10-28`
- Renewal notice period: `30` days

The notice boundary is `2026-09-28`, so the contract is flagged for renewal review.

The service also checks the rule whenever contracts/reminders are requested.
