# Invoice & Expense Management System

A full-stack financial management platform built with Spring Boot and React.
Similar in domain to QuickBooks — handles invoice lifecycle, expense tracking,
and P&L reporting.

## 🚀 Live Demo
- **Frontend:** https://invoice-frontend-n9v6.onrender.com
- **Backend API:** https://invoice-management-5c5t.onrender.com
- **Swagger Docs:** https://invoice-management-5c5t.onrender.com/swagger-ui/index.html

## ✨ Features
- JWT authentication (register/login)
- Client management (create, edit, delete)
- Invoice lifecycle (Draft → Sent → Paid/Overdue)
- Expense tracking by category
- Real-time P&L dashboard with monthly revenue chart
- Automated overdue invoice email reminders
- Status transition validation
- Global exception handling

## 🛠 Tech Stack
### Backend
- Java 21, Spring Boot 3.2
- Spring Security + JWT
- JPA/Hibernate + MySQL
- JavaMailSender
- Docker + docker-compose
- Swagger/OpenAPI

### Frontend
- React 18, Ant Design 5
- Axios, React Router
- Recharts

## 🏗 Architecture
React (Render) → Spring Boot (Render) → MySQL (Railway)
