# Job Portal System Architecture

## Overview
The Job Portal system consists of three main components:
1. **Android Mobile Application** - Client-facing interface for job seekers
2. **Admin Panel** - Web-based interface for administrators
3. **Backend API** - RESTful services connecting the mobile app and admin panel to the database

## System Architecture Diagram

```
+-------------------+        +-------------------+
|                   |        |                   |
|   Android App     |        |   Admin Panel     |
|   (Java)          |        |   (PHP Laravel)   |
|                   |        |                   |
+--------+----------+        +---------+---------+
         |                             |
         |                             |
         v                             v
+--------------------------------------------------+
|                                                  |
|              RESTful API Layer                   |
|              (PHP Laravel)                       |
|                                                  |
+----------------------+---------------------------+
                       |
                       |
                       v
+--------------------------------------------------+
|                                                  |
|                MySQL Database                    |
|                                                  |
+--------------------------------------------------+
```

## Component Details

### 1. Android Mobile Application
- **Technology**: Java with Retrofit for API calls
- **Features**:
  - User authentication (login, registration, password recovery)
  - Job browsing with filters
  - Job application submission
  - Profile management
  - Application status tracking
- **Communication**: Connects to backend via RESTful API calls

### 2. Admin Panel
- **Technology**: PHP Laravel framework with Blade templates
- **Features**:
  - Admin authentication
  - Dashboard with statistics
  - Job management (CRUD operations)
  - User management
  - Application processing
- **Communication**: Direct database access and API endpoints

### 3. Backend API
- **Technology**: PHP Laravel framework
- **Features**:
  - RESTful API endpoints
  - Authentication with token-based security
  - Data validation
  - Business logic implementation
  - Database operations
- **Communication**: Interfaces between clients and database

### 4. Database
- **Technology**: MySQL
- **Features**:
  - Relational data storage
  - Data integrity through constraints
  - Efficient querying

## Database Schema

### Users Table
```
users
- id (PK)
- name
- email
- mobile
- password
- resume_path
- skills
- experience
- is_active
- created_at
- updated_at
```

### Jobs Table
```
jobs
- id (PK)
- title
- description
- company
- location
- salary
- job_type (Full-time, Part-time, Contract)
- category
- posting_date
- expiry_date
- is_active
- created_at
- updated_at
```

### Applications Table
```
applications
- id (PK)
- user_id (FK)
- job_id (FK)
- resume_path
- status (Applied, Under Review, Shortlisted, Rejected)
- applied_date
- created_at
- updated_at
```

### Admins Table
```
admins
- id (PK)
- username
- email
- password
- created_at
- updated_at
```

### Notifications Table
```
notifications
- id (PK)
- user_id (FK)
- message
- is_read
- created_at
- updated_at
```

## API Endpoints

### Authentication
- `POST /api/login` - User login
- `POST /api/register` - User registration
- `POST /api/forgot-password` - Password recovery
- `POST /api/admin/login` - Admin login

### Jobs
- `GET /api/jobs` - List all jobs with filters
- `GET /api/jobs/{id}` - Get job details
- `POST /api/jobs` - Create new job (admin)
- `PUT /api/jobs/{id}` - Update job (admin)
- `DELETE /api/jobs/{id}` - Delete job (admin)

### Applications
- `POST /api/jobs/{id}/apply` - Apply for a job
- `GET /api/applications` - Get user's applications
- `GET /api/admin/applications` - Get all applications (admin)
- `PUT /api/admin/applications/{id}` - Update application status (admin)

### Users
- `GET /api/profile` - Get user profile
- `PUT /api/profile` - Update user profile
- `GET /api/admin/users` - List all users (admin)
- `PUT /api/admin/users/{id}` - Update user status (admin)
- `DELETE /api/admin/users/{id}` - Delete user (admin)

## Security Considerations
- API authentication using JWT tokens
- Password hashing
- Input validation
- CSRF protection
- XSS prevention
- Rate limiting
- Secure file uploads

## Data Flow

### Job Application Process
1. User logs into Android app
2. User browses and filters available jobs
3. User selects a job and views details
4. User applies for the job with resume
5. Application is stored in database
6. Admin reviews application via admin panel
7. Admin updates application status
8. User receives notification of status change

## Scalability Considerations
- Database indexing for performance
- Caching strategies
- Pagination for large data sets
- Optimized queries
- Potential for horizontal scaling
