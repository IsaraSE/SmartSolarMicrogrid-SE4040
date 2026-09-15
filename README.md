# ☀️ Smart Solar Microgrid Trading System

<div align="center">
  
  **A Decentralized Energy Trading Platform for a Sustainable Tomorrow**
  
  [![C# ASP.NET](https://img.shields.io/badge/C%23_ASP.NET-512BD4?style=for-the-badge&logo=c-sharp&logoColor=white)](#)
  [![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](#)
  [![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
  [![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)](#)
</div>

---

## 📖 Project Overview

The **Smart Solar Microgrid Trading System (SE4040)** is an enterprise-grade platform designed to revolutionize local energy distribution. By connecting **Prosumers** (energy producers and consumers) with **Microgrid Stations**, the system facilitates secure, transparent, and efficient peer-to-peer energy trading. 

This ecosystem comprises a powerful central **ASP.NET Web API**, a responsive **React Web Application** for administrative and operational oversight, and a dual-purpose **Native Android Mobile Application** for Prosumers and Grid Operators.

---

## ✨ Key Features

### 🏢 Backoffice Administration (Web)
- **Comprehensive Dashboard:** Real-time metrics on energy trades, station statuses, and user activity.
- **Microgrid Management:** Add, configure, and monitor solar grid stations and battery slots.
- **User Governance:** Role-based access control, account approvals, and comprehensive user management.
- **Reservation Oversight:** Enforce strict business rules (e.g., 7-day advance booking, 12-hour cancellation windows).

### 📱 Prosumer Experience (Mobile)
- **Station Discovery:** Browse available microgrid stations and view live battery slot capacity.
- **Energy Booking:** Schedule energy deposits/withdrawals seamlessly.
- **Secure Authentication:** Generate dynamic, encrypted QR codes for approved reservations to authorize physical energy transfers.

### 👷 Grid Operator Tools (Mobile & Web)
- **Live Maps Integration:** Discover and navigate to nearby microgrid stations using Google Maps API.
- **QR Verification:** Scan Prosumer QR codes at the station to validate and complete energy transfers securely against live server data.
- **Operational Dashboard:** Manage daily schedules, oversee pending transfers, and monitor grid health.

---

## 🏗️ Architecture & Technology Stack

The system embraces a **RESTful microservices-inspired architecture**, ensuring clean separation of concerns and robust scalability. All core business logic is centralized in the backend API, strictly preventing direct database access from client applications.

### Technology Stack
- **Backend Services:** C# ASP.NET Web API, hosted on Windows IIS.
- **Database Layer:** MongoDB (Cloud) for centralized state, SQLite for local mobile persistence.
- **Web Frontend:** React.js, Tailwind CSS / Bootstrap 5, Vite.
- **Mobile Frontend:** Pure Native Android (Java/Kotlin).
- **Integrations:** Google Maps API, QR Code Generation/Scanning logic.

---

## ⚙️ System Workflow & Architecture

The **Smart Solar Microgrid Trading System** connects three distinct operational layers through a centralized API, ensuring real-time synchronization and secure energy trading.

### 1. The Prosumer Flow (Mobile App)
- Prosumers register and log into the Native Android app.
- They browse live microgrid stations and view available battery slots.
- They schedule an energy reservation (deposit or withdrawal) up to 7 days in advance.
- Upon approval, a **Secure QR Code** is generated on their mobile device.

### 2. The Backoffice Flow (Web Application)
- System Administrators log into the React Web Dashboard.
- They have a bird's-eye view of all global reservations, stations, and users.
- They manage microgrid infrastructure, adjusting capacities and schedules.
- They oversee role assignments and can reactivate suspended accounts.

### 3. The Grid Operator Flow (Mobile App)
- Grid Operators use Google Maps integration to navigate to physical microgrid stations.
- When a Prosumer arrives, the Grid Operator scans their Secure QR Code.
- The system validates the QR code in real-time against the MongoDB central database.
- The operator completes the energy transfer, instantly updating both the Prosumer's history and the Backoffice Dashboard.

### High-Level Architecture Diagram
```text
  [ Web Application ]                 [ Android Application ]
    (React.js UI)                     (Native Java/Kotlin UI)
          │                                      │
          │             [ REST API ]             │
          └────────> (C# ASP.NET Core) <─────────┘
          ┌──────────────────┴───────────────────┐
          ▼                                      ▼
[ MongoDB Database ]                    [ Android SQLite ]
  (Central Data)                         (Local Caching)
```

---

## 🚀 Setup & Installation

### Prerequisites
- .NET 8.0 SDK or higher
- Node.js (v18+)
- Android Studio
- MongoDB URI (configured via environment variables)

### 1. Backend Setup (API)
```bash
cd backend/SmartSolarMicrogrid.Api
dotnet restore
dotnet run
```
*The API will start at `https://localhost:5001` or `http://localhost:5000`.*

### 2. Web Application Setup
```bash
cd web/SmartSolarMicrogrid.Web
npm install
npm run dev
```
*The React app will start at `http://localhost:5174`.*

### 3. Mobile Application Setup
- Open the `android/SmartSolarMicrogridMobile` directory in **Android Studio**.
- Sync Gradle projects.
- Run on an Emulator or physical Android device.

---

## 🎥 Demonstration

- **Repository Link:** [Insert GitHub Repository Link Here]
- **Demo Video:** [Insert <=5 Minute Demo Video Link Here]

---
<div align="center">
  <i>Developed for SE4040</i>
</div>
