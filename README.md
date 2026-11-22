# 🩸 LifeLink – Local Blood Donor Finder

LifeLink is a Java-based desktop application designed to bridge the gap between blood donors and people in need of blood. It provides a centralized platform where seekers can raise blood requests and donors can find compatible requests, express their willingness to donate, and coordinate directly with seekers.

The application aims to make blood donation coordination more accessible, organized, and efficient, especially during urgent medical situations. It manages the complete request lifecycle, from creating a blood request to donor selection and fulfillment.

## ✨ Features

### 👤 User Registration & Authentication
- Separate registration for Blood Donors and Blood Seekers.
- User profile management with personal and contact information.
- Secure password storage using password hashing (PBKDF2)
- Role-based dashboards and access.

### 🩸 Donor Management
- Donors can register their blood group.
- Donors can manage their availability status.
- Donors can view blood requests compatible with their blood group.
- Donors can express interest in a request or withdraw their interest.

### 🏥 Blood Request Management
- Seekers can create blood requests specifying:
  - Required blood group
  - Hospital name
  - City
  - Number of units required
  - Urgency level
- Requests can be cancelled while open.
- Emergency and urgent requests are prioritized.

### 🔍 Blood Group Compatibility Matching
- The application matches donors with compatible blood requests.
- Donors are shown requests corresponding to their blood group compatibility.
- Compatible requests are organized according to urgency.

### 🤝 Donor–Seeker Coordination
- Seekers can view donors who have expressed interest in their requests.
- Seekers can select a donor for a request.
- Other interested donors are notified through request status updates in the application.
- Donors can access seeker contact information, including phone number and location details, to coordinate donation.

### ✅ Request Lifecycle Management
- Tracks blood requests through their lifecycle.
- Manages donor responses such as interested, withdrawn, selected, and not selected.
- Seekers can mark requests as fulfilled after the donation process is completed.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application logic |
| JavaFX 21 | Desktop graphical user interface |
| MySQL | Database management and persistent storage |
| JDBC | Java–MySQL connectivity |
| Maven | Build and dependency management |
| PBKDF2 | Password Hashing |

## 🗄️ Database

LifeLink uses MySQL to store and manage:

- User profiles and credentials
- Donor blood groups and availability
- Blood requests
- Donor responses and selection statuses

The application uses relational tables to maintain relationships between users, donors, requests, and donor responses.

## 🚀 Installation & Setup

### Prerequisites

- Java 21 or later
- MySQL Server
- Maven
- IntelliJ IDEA (recommended)

### 1. Clone the repository

```bash
git clone https://github.com/vaishnavisingla222/LifeLink.git
```

### 2. Open the project

Open the cloned repository in IntelliJ IDEA and allow Maven to download the required dependencies.

### 3. Configure MySQL

Create the database:

```sql
CREATE DATABASE lifelink;
```

Configure the database connection with your local MySQL credentials. Ensure the required tables are created according to the project's database schema.

### 4. Run the application

From the project root directory:

```bash
mvn clean javafx:run
```

## 🔄 Application Workflow

1. A user registers as either a Donor or a Seeker.
2. The user logs in and accesses their role-specific dashboard.
3. Seekers create blood requests with the required details.
4. Donors view compatible open requests, prioritized by urgency.
5. Donors express interest in requests.
6. Seekers view interested donors and select a donor.
7. Donors and seekers coordinate using the contact information provided.
8. The seeker marks the request as fulfilled once completed.

## 🎯 Project Objective

The primary objective of LifeLink is to simplify the process of finding blood donors by providing an organized platform for donor matching, blood request management, and donor–seeker communication.

## ⚠️ Disclaimer

LifeLink is an academic prototype developed for demonstration purposes. Blood group compatibility matching is preliminary and does not replace medical evaluation, laboratory testing, or cross-matching by qualified healthcare professionals.

## 👩‍💻 Project

Developed as an academic project using Java, JavaFX, MySQL, JDBC, and Maven.

---

⭐ Contributions, suggestions, and feedback are welcome!
