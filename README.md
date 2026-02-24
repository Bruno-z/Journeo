# Journeo – Product Requirements Document (PRD) & README

## PRD

**Project Name:** Journeo  
**Project Type:** Web App / Mobile App  
**Tech Stack:** Angular (Front-end), Spring Boot (Back-end), PostgreSQL, Optional Docker for deployment

### 1. Context & Objective
Journeo is a travel guide application that allows users to create, browse, and interact with travel guides. The main goal of this project is to provide an intuitive and interactive platform for both travelers and admins to manage travel guides efficiently.

**Objectives:**
- Enable users to view and interact with guides they have created or have been invited to.
- Provide admins with full control over guide and activity management.
- Ensure a smooth, performant, and user-friendly experience.
- Demonstrate professional full stack development skills using Angular and Spring Boot.

### 2. Target Users
1. **Regular Users:**
   - Can view guides they are invited to.
   - Can browse guide details, activities, and schedules.
   - Cannot modify or delete guides.

2. **Admins:**
   - Can create, update, delete, and view all guides.
   - Can manage users and assign permissions.
   - Can add activities to guides and organize their order.

### 3. Features & Requirements

**3.1 User Authentication**
- Users and admins must register and log in with email and password.
- Password encryption and secure authentication (JWT).
- Role-based access control (user/admin).

**3.2 Guide Management**
- Admins can create, edit, delete, and view guides.
- Guides must include: title, description, number of days, mobility options (car, bike, walking, motorcycle), season (summer, spring, autumn, winter), target audience (family, solo, group, friends).
- Users can view guides they are invited to only.

**3.3 Activity Management**
- Guides can contain multiple activities.
- Activity attributes: title, description, category (museum / castle / activity / park / cave), address, phone, opening hours, website, day and order number.
- Admins can add, edit, or remove activities.
- Activities can be grouped by day and ordered.

**3.4 Front-End Requirements**
- Angular-based responsive web app.
- Display list of guides accessible to the logged-in user.
- Detailed view of selected guides and activities.
- Smooth navigation between days and activities.
- Search and filtering of guides by title, season, mobility, or audience.
- Optional offline mode with data sync when connection restores.
- Use animations to enhance UX.

**3.5 Back-End Requirements**
- Spring Boot REST API to handle all operations: user authentication, CRUD for guides and activities, role-based access control, data validation and error handling.
- Database: PostgreSQL with proper relations (Users, Guides, Activities, Permissions).
- Optional: Dockerized deployment for consistency.

**3.6 Performance & Quality**
- Fast response times (both front-end and back-end).
- Proper error handling and notifications.
- Unit tests for API endpoints (JUnit / Mockito).
- Front-end testing using Jasmine/Karma or Cypress.

### 4. Bonus / Optional Features
- Real-time updates (WebSockets) when a guide is updated.
- Calendar view for guide days and activities.
- Export guide as PDF.
- Mobile-friendly interface or PWA capabilities.
- Optional Docker deployment to streamline development and deployment.

### 5. Success Metrics
- Users can view and navigate guides without errors.
- Admins can perform all management actions correctly.
- API responds within < 200ms for common queries.
- Front-end achieves > 80% coverage on unit tests.
- Smooth, responsive, and intuitive UI.

---

## README (for GitHub Repository)

# Journeo
Journeo is a professional travel guide web application allowing users to view, manage, and interact with travel guides. Built with Angular and Spring Boot, it provides role-based access for users and admins.

## Tech Stack
- Front-end: Angular
- Back-end: Spring Boot
- Database: PostgreSQL
- Optional: Docker
- Testing: Jasmine/Karma (frontend), JUnit/Mockito (backend)

## Installation & Setup

**Clone the repository:**
```bash
git clone https://github.com/yourusername/journeo.git
cd journeo
```

**Back-end:**
1. Navigate to backend folder
2. Build and run Spring Boot application
```bash
./mvnw spring-boot:run
```

**Front-end:**
1. Navigate to frontend folder
2. Install dependencies
```bash
npm install
```
3. Start Angular app
```bash
ng serve
```

Open [http://localhost:4200](http://localhost:4200) to access the application.

## Docker Setup (Optional)

To run Journeo using Docker:

1. Build Docker images:
```bash
docker-compose build
```

2. Start containers:
```bash
docker-compose up
```

3. The app will be available at:
- Front-end: http://localhost:4200
- Back-end API: http://localhost:8080

4. Stop containers:
```bash
docker-compose down
```

**Notes:**
- The backend container connects to a PostgreSQL container.
- Volumes are mounted for code changes, so you can develop without rebuilding constantly.

## Features
- User authentication (JWT)
- Role-based access control (user/admin)
- CRUD operations for guides and activities
- Search and filter guides
- Responsive UI with animations
- Optional offline mode with data sync
- Optional Docker deployment for development and testing

## Testing
- Front-end: `ng test`
- Back-end: `./mvnw test`

## Contribution
Feel free to fork the repository and submit pull requests. Ensure all tests pass and follow the coding standards.

## Contact
For questions or support, contact Bruno Zilio.

