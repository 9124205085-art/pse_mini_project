# Passport Automation System

A Java Swing desktop system for a college mini project. An applicant files a passport application, then the file moves through **Admin → Regional Officer → Police**. When all three desks mark it verified, the passport is ready for issue.

This is a working rebuild of the original `LoginDemo`, `CreateAccount`, `a`, `b`, and `c` programs. The old JDBC-ODBC Access driver is gone (it does not run on modern Java). The project now uses a local SQLite file, so you do not need Microsoft Access or an ODBC DSN.

## What you need

- Windows
- **JDK 17 or later** (Java, not only a browser)
- Internet once, so `compile.bat` can download the SQLite driver

If Java is not installed, run:

```bat
winget install --id EclipseAdoptium.Temurin.17.JDK -e --accept-package-agreements
```

Then close and reopen the terminal.

## How to run

Double-click `run.bat`, or from this folder:

```bat
run.bat
```

That compiles the source and opens the login window.

## Demo logins

| Username  | Password      | Opens                    |
|-----------|---------------|--------------------------|
| applicant | applicant123  | Application form         |
| admin     | admin123      | Admin verification desk  |
| regional  | regional123   | Regional officer desk    |
| police    | police123     | Police verification desk |

## Sample applications already in the database

| Proof     | Name          | Where it is waiting      |
|-----------|---------------|--------------------------|
| PROOF1001 | Rahul Sharma  | Admin desk               |
| PROOF1002 | Ananya Iyer   | Regional desk            |
| PROOF1003 | Vikram Patel  | Police desk              |

### Suggested demo

1. Log in as **applicant** and save a new form (use a new proof number).
2. Log in as **admin**, search or load all, select the row, click **Verified**.
3. Log in as **regional**, enter the same name and proof, click **Find** then **Verified**.
4. Log in as **police**, enter name and both addresses, click **Find** then **Verified**.
5. Log in as **applicant** again, type the proof number, click **Check Status**. It should say ready for issue.

## Screens

- `LoginDemo` — username and password; role decides the next screen
- `CreateAccount` — save, reset, and check status
- `AdminVerification` — search, load all, verify, delete, open regional desk
- `RegionalVerification` — find by name + proof, verify, open police desk
- `PoliceVerification` — find by name + addresses, verify

## Database

SQLite file: `data/passport.db`

Tables:

- `login` — users and roles
- `userdetails` — applications and the three verification flags
- `RegionalDatabase` — files released by Admin
- `PoliceDb` — files released by the Regional officer

Delete `data\passport.db` if you want the sample data recreated the next time you start the app.

## Project files

```
compile.bat
run.bat
src/passport/Main.java
src/passport/Database.java
src/passport/LoginDemo.java
src/passport/CreateAccount.java
src/passport/AdminVerification.java
src/passport/RegionalVerification.java
src/passport/PoliceVerification.java
src/passport/UITheme.java
```
