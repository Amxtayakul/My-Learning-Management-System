# LMS Dashboard

A desktop Learning Management System built in **Java (Swing)** with a **MySQL** backend. After logging in, users land on one of two dashboards depending on their role — students browse their enrolled courses and lessons, instructors manage their courses, lessons, and rosters.

## Features

**Login**
- One login screen for both roles (`LoginForm`)
- Credentials are checked against the `LOGIN` table; the matched row's `role` decides which dashboard loads next

**Student Dashboard**
- Shows the student's profile (ID, name, program, year, section, contact)
- Lists enrolled courses, each grouped with that course's lessons
- Live search box to filter lessons by title
- Shows a message instead of a course list if the student isn't enrolled in anything yet

**Instructor Dashboard**
- Shows the instructor's profile (ID, name, contact)
- Lists every course the instructor teaches, each with:
  - **View Students** — a dialog listing everyone enrolled (name, program, year, section, contact, enrollment type/term)
  - **Add Lesson** — a form dialog to create a new lesson under that course
  - **Edit / Delete** on each existing lesson, both with a confirmation prompt
- Full lesson CRUD (create, edit, delete) is wired to the `LESSON` table

**Shared**
- Logout button with an "are you sure?" confirmation that returns to the login screen

## Project structure

```
├── LoginForm.java             Entry point — login UI + authentication
├── Mainframe.java             Post-login window; routes to the right dashboard, handles logout
├── StudentDashboard.java      Student view (JPanel)
├── InstructorDashboard.java   Instructor view (JPanel)
├── User.java                  Simple data holder for the logged-in user
├── lib/
│   └── mysql-connector-j-9.7.0.jar   MySQL JDBC driver
└── settings.json              VS Code Java project config (source/output paths, referenced libs)
```

`Mainframe` reads `user.role` after login and adds either a `StudentDashboard` or an `InstructorDashboard` panel to itself — the two dashboards are independent views and don't share code.

## Database

Inferred from the queries in the code, the app expects a MySQL database with (at least) these tables:

| Table | Key columns |
|---|---|
| `LOGIN` | `username`, `pass`, `role`, `student_ID`, `instructor_ID` |
| `STUDENT` | `student_ID`, `student_name`, `program_ID` → `PROGRAM`, `student_Year`, `student_Section`, `student_Contact` |
| `INSTRUCTOR` | `instructor_ID`, `instructor_Name`, `instructor_Contact` |
| `PROGRAM` | `Program_ID`, `Program_Name` |
| `COURSE` | `course_ID`, `course_name`, `course_Units`, `instructor_ID` → `INSTRUCTOR` |
| `LESSON` | `lesson_ID`, `course_ID` → `COURSE`, `lesson_title`, `lesson_Content` |
| `ENROLLMENT` | `student_ID` → `STUDENT`, `course_ID` → `COURSE`, `enrollment_Status`, `enrollment_Type`, `enrollment_Term` |

## Setup

1. **Create the database** using the tables above (with the matching foreign keys) and load in your sample data.
2. **Set your DB credentials.** `LoginForm.java`, `Mainframe.java`, `StudentDashboard.java`, and `InstructorDashboard.java` each carry their own copy of:
   ```java
   final String DB_URL = "jdbc:mysql://localhost:3306/*ENTER DBNAME*";
   final String DB_USER = "*ENTER USERNAME*";
   final String DB_PASS = "*ENTER PASSWORD*";
   ```
   Replace the placeholders in each file with your actual database name, username, and password.
3. **Add the JDBC driver.** Put `mysql-connector-j-9.7.0.jar` in a `lib/` folder at the project root — `settings.json` already points VS Code's Java extension at `lib/**/*.jar`.
4. **Run it.** Compile and run `LoginForm.main()`.

## Team

_Add your team members and their roles here._

## Possible improvements

- DB credentials are duplicated across four files — pulling them into one shared connection helper would mean changing them in one place instead of four.
- Passwords are currently compared as plain text against `LOGIN`. Fine for a class demo, but worth a mention in your report as something you'd harden (e.g. hashing) for real-world use.
