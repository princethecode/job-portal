# Job Portal System Architecture

## Overview
The Job Portal system is a comprehensive employment platform with multiple user types and interfaces:

1. **Android Mobile Application** - Feature-rich client for job seekers and recruiters
2. **Admin Panel** - Web-based interface for system administrators
3. **Backend API** - RESTful services with role-based authentication
4. **Database** - MySQL with comprehensive schema for users, jobs, applications, and recruiters

## System Architecture Diagram

```
+-------------------+        +-------------------+        +-------------------+
|                   |        |                   |        |                   |
|   Android App     |        |   Admin Panel     |        |   Recruiter Web   |
|   (Job Seekers)   |        |   (Admins)        |        |   (Future)        |
|   Java/Retrofit   |        |   Laravel Blade   |        |   Laravel Blade   |
|                   |        |                   |        |                   |
+--------+----------+        +---------+---------+        +---------+---------+
         |                             |                            |
         |                             |                            |
         v                             v                            v
+--------------------------------------------------------------------------------------+
|                                                                                      |
|                            RESTful API Layer (Laravel)                              |
|                                                                                      |
|  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              |
|  │   Auth      │  │    Jobs     │  │  Applications│  │  Recruiters │              |
|  │ Controllers │  │ Controllers │  │  Controllers │  │ Controllers │              |
|  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘              |
|                                                                                      |
|  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              |
|  │    Users    │  │ Candidates  │  │ Interviews  │  │  Analytics  │              |
|  │ Controllers │  │ Controllers │  │ Controllers │  │ Controllers │              |
|  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘              |
|                                                                                      |
+------------------------------------+---------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------------------+
|                                                                                      |
|                               MySQL Database                                        |
|                                                                                      |
|  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    |
|  │  users  │  │  jobs   │  │recruiters│  │interviews│  │contacts │  │categories│    |
|  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────┘    |
|                                                                                      |
|  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    |
|  │applicat-│  │featured_│  │saved_can-│  │experien-│  │notifica-│  │app_vers-│    |
|  │ions     │  │jobs     │  │didates  │  │ces      │  │tions    │  │ions     │    |
|  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────┘    |
|                                                                                      |
+--------------------------------------------------------------------------------------+
```

## Component Details

### 1. Android Mobile Application
- **Technology**: Java with Retrofit 2.9.0 for API calls, Gson 2.8.9 for JSON parsing
- **Architecture**: Fragment-based with MVVM patterns, singleton managers for session and auth
- **User Types**: 
  - **Job Seekers**: Complete user flow with authentication, job search, applications, profile management
  - **Recruiters**: Full recruitment platform with dashboard, job management, candidate tracking

#### **Job Seeker Features**:
- **Authentication**: Login, registration, password recovery, Google Sign-In integration
- **Job Management**: Browse jobs with filters, categories, featured jobs, job sharing
- **Application Process**: Apply for jobs with resume upload, employment details, application tracking
- **Profile Management**: Complete profile with skills, experience, photo upload, contact sync
- **Notifications**: FCM integration for real-time updates

#### **Recruiter Features**:
- **Dashboard**: Statistics display (total jobs, applications, interviews, hiring metrics)
- **Job Management**: Create, edit, delete jobs with status management and application tracking
- **Application Processing**: Review applications, update status, schedule interviews, download resumes
- **Candidate Management**: View candidate profiles, save candidates, manage candidate pipeline
- **Interview Scheduling**: Schedule, manage, and track interviews with calendar integration
- **Analytics**: Comprehensive recruitment analytics and reporting

#### **Technical Implementation**:
- **Network Layer**: Retrofit with custom interceptors for authentication and error handling
- **Authentication**: Separate auth systems for users and recruiters with token-based security
- **Data Management**: SharedPreferences for session management, real-time API synchronization
- **UI/UX**: Material Design components, responsive layouts, loading states, error handling
- **File Management**: Resume and photo upload with progress tracking

### 2. Admin Panel
- **Technology**: PHP Laravel framework with Blade templates, Bootstrap CSS
- **Features**:
  - **Admin Authentication**: Secure login system with role-based access
  - **Dashboard**: Comprehensive statistics and analytics with visual charts
  - **User Management**: CRUD operations for users, recruiters, and admins
  - **Job Management**: Full job lifecycle management with categories and featured jobs
  - **Application Processing**: Application review, status updates, bulk operations
  - **Contact Management**: Contact import/export with labeling system
  - **Notification System**: Email notifications and Firebase push notifications
  - **Version Control**: App version management with update notifications
  - **Export Functionality**: Excel exports for applications, contacts, and reports

### 3. Backend API
- **Technology**: PHP Laravel framework with Sanctum authentication
- **Architecture**: RESTful API with role-based middleware and comprehensive error handling
- **Features**:
  - **Multi-role Authentication**: Separate authentication flows for users, recruiters, and admins
  - **API Security**: Token-based authentication, input validation, rate limiting
  - **Data Processing**: Complex business logic for job matching, application processing
  - **File Management**: Secure file upload and download for resumes and photos
  - **Real-time Features**: Firebase integration for push notifications
  - **Analytics**: Advanced reporting and analytics endpoints

#### **API Architecture**:
- **Middleware**: Custom authentication middleware for different user roles
- **Controllers**: Organized by functionality (Auth, Jobs, Applications, Recruiters, etc.)
- **Models**: Comprehensive Eloquent models with relationships and business logic
- **Policies**: Authorization policies for resource access control
- **Services**: External service integration (Firebase, Email, File storage)

### 4. Database
- **Technology**: MySQL with comprehensive relational schema
- **Features**:
  - **Data Integrity**: Foreign key constraints and data validation
  - **Performance**: Optimized indexes and query performance
  - **Scalability**: Designed for horizontal scaling and large datasets
  - **Backup**: Automated backup and recovery strategies

## Database Schema

### Core Tables

#### Users Table
```sql
users
- id (PK)
- name
- email (unique)
- mobile
- password
- email_verified_at
- is_admin (boolean)
- is_active (boolean)
- profile_photo
- resume_path
- skills (text)
- experience (text)
- location
- job_title
- about_me (text)
- contact (text)
- last_contact_sync
- fcm_token
- google_id (for Google auth)
- provider (google/email)
- current_company
- department
- current_salary
- expected_salary
- joining_period
- remember_token
- created_at
- updated_at
```

#### Recruiters Table
```sql
recruiters
- id (PK)
- name
- email (unique)
- password
- mobile
- company_name
- company_logo
- company_website
- company_description (text)
- company_size
- industry
- location
- designation
- is_active (boolean)
- is_verified (boolean)
- fcm_token
- email_verified_at
- remember_token
- created_at
- updated_at
```

#### Jobs Table
```sql
jobs
- id (PK)
- title
- description (text)
- company
- company_name (from recruiter)
- location
- salary (decimal)
- job_type (Full-time, Part-time, Contract)
- category_id (FK to categories)
- category (legacy field)
- posting_date
- expiry_date
- is_active (boolean)
- recruiter_id (FK to recruiters)
- image (job image)
- share_count (integer)
- created_at
- updated_at
```

#### Applications Table
```sql
applications
- id (PK)
- user_id (FK to users)
- job_id (FK to jobs)
- recruiter_id (FK to recruiters)
- resume_path
- status (Applied, Under Review, Shortlisted, Rejected, Hired)
- applied_date
- posting_date
- notes (text)
- current_company
- department
- current_salary
- expected_salary
- joining_period
- skills (text)
- experience (text)
- created_at
- updated_at
```

#### Interviews Table
```sql
interviews
- id (PK)
- application_id (FK to applications)
- recruiter_id (FK to recruiters)
- user_id (FK to users)
- job_id (FK to jobs)
- interview_date
- interview_time
- interview_type (online/offline)
- location
- meeting_link
- notes (text)
- status (scheduled, completed, cancelled, rescheduled)
- created_at
- updated_at
```

### Supporting Tables

#### Experiences Table
```sql
experiences
- id (PK)
- user_id (FK to users)
- company_name
- position
- start_date
- end_date
- is_current (boolean)
- description (text)
- created_at
- updated_at
```

#### Categories Table
```sql
categories
- id (PK)
- name
- description (text)
- is_active (boolean)
- created_at
- updated_at
```

#### Featured Jobs Table
```sql
featured_jobs
- id (PK)
- job_id (FK to jobs)
- start_date
- end_date
- is_active (boolean)
- priority (integer)
- created_at
- updated_at
```

#### Saved Candidates Table
```sql
saved_candidates
- id (PK)
- recruiter_id (FK to recruiters)
- user_id (FK to users)
- notes (text)
- created_at
- updated_at
```

#### Contacts Table
```sql
contacts
- id (PK)
- user_id (FK to users)
- name
- phone
- email
- created_at
- updated_at
```

#### Notifications Table
```sql
notifications
- id (PK)
- user_id (FK to users)
- title
- message (text)
- type
- is_read (boolean)
- data (json)
- created_at
- updated_at
```

#### App Versions Table
```sql
app_versions
- id (PK)
- version_name
- version_code (integer)
- description (text)
- download_url
- is_force_update (boolean)
- is_active (boolean)
- release_date
- created_at
- updated_at
```

#### Authentication Tables
```sql
personal_access_tokens (Laravel Sanctum)
- id (PK)
- tokenable_type
- tokenable_id
- name
- token (hashed)
- abilities (text)
- last_used_at
- expires_at
- created_at
- updated_at

password_reset_tokens
- email (PK)
- token
- created_at

failed_jobs
- id (PK)
- uuid
- connection (text)
- queue (text)
- payload (longtext)
- exception (longtext)
- failed_at
```

## API Endpoints

### Public Endpoints

#### App Version Management
- `GET /api/app-versions/latest` - Get latest app version
- `POST /api/app-versions/check-update` - Check for app updates

#### User Authentication
- `POST /api/login` - User login with email/password
- `POST /api/register` - User registration
- `POST /api/forgot-password` - Password recovery
- `POST /api/reset-password` - Reset password with token
- `POST /api/login/google` - Google Sign-In
- `POST /api/register/google` - Google registration

#### Recruiter Authentication
- `POST /api/recruiter/login` - Recruiter login
- `POST /api/recruiter/register` - Recruiter registration

#### Public Job Listings
- `GET /api/jobs` - List all active jobs with filters
- `GET /api/jobs/{id}` - Get specific job details
- `POST /api/jobs/{id}/share` - Increment job share count
- `GET /api/featured-jobs` - Get featured jobs
- `GET /api/featured-jobs/{id}` - Get specific featured job

### Protected User Endpoints (auth:sanctum)

#### User Profile Management
- `GET /api/user` - Get current user profile
- `POST /api/profile` - Update user profile
- `POST /api/profile/photo` - Upload profile photo
- `POST /api/profile/resume` - Upload resume
- `POST /api/change-password` - Change password
- `POST /api/user/update-profile` - Update profile with employment details

#### Job Applications
- `GET /api/applications` - Get user's applications
- `GET /api/applications/{id}` - Get specific application
- `POST /api/jobs/{id}/apply` - Apply for a job
- `POST /api/jobs/{id}/apply-with-details` - Apply with employment details
- `GET /api/user/applied-jobs` - Get user's applied jobs

#### Experience Management
- `GET /api/experiences` - Get user experiences
- `POST /api/experiences` - Create new experience
- `GET /api/experiences/{id}` - Get specific experience
- `PUT /api/experiences/{id}` - Update experience
- `DELETE /api/experiences/{id}` - Delete experience

#### Contacts & Notifications
- `POST /api/contacts/upload` - Upload contacts
- `POST /api/users/update-contact` - Update contact information
- `GET /api/notifications` - Get user notifications
- `POST /api/notifications/{id}/read` - Mark notification as read
- `POST /api/users/register-fcm-token` - Register FCM token

### Protected Recruiter Endpoints (recruiter.sanctum)

#### Recruiter Profile
- `GET /api/recruiter/profile` - Get recruiter profile
- `POST /api/recruiter/profile` - Update recruiter profile
- `POST /api/recruiter/logout` - Logout recruiter

#### Dashboard & Analytics
- `GET /api/recruiter/dashboard` - Get dashboard statistics
- `GET /api/recruiter/analytics` - Get detailed analytics

#### Job Management
- `GET /api/recruiter/jobs` - Get recruiter's jobs
- `POST /api/recruiter/jobs` - Create new job
- `GET /api/recruiter/jobs/{id}` - Get specific job
- `PUT /api/recruiter/jobs/{id}` - Update job
- `DELETE /api/recruiter/jobs/{id}` - Delete job
- `PATCH /api/recruiter/jobs/{id}/toggle-status` - Toggle job status

#### Application Management
- `GET /api/recruiter/applications` - Get applications for recruiter's jobs
- `GET /api/recruiter/applications/{id}` - Get specific application
- `PATCH /api/recruiter/applications/{id}/status` - Update application status
- `POST /api/recruiter/applications/{id}/schedule-interview` - Schedule interview
- `GET /api/recruiter/applications/{id}/download-resume` - Download resume

#### Candidate Management
- `GET /api/recruiter/candidates` - Get candidates who applied
- `GET /api/recruiter/candidates/{id}` - Get specific candidate
- `PATCH /api/recruiter/candidates/{id}/toggle-save` - Save/unsave candidate
- `GET /api/recruiter/candidates/{id}/download-resume` - Download candidate resume

#### Interview Management
- `GET /api/recruiter/interviews` - Get scheduled interviews
- `GET /api/recruiter/interviews/{id}` - Get specific interview
- `PUT /api/recruiter/interviews/{id}` - Update interview details
- `PATCH /api/recruiter/interviews/{id}/status` - Update interview status
- `POST /api/recruiter/interviews/{id}/cancel` - Cancel interview

### Protected Admin Endpoints (auth:sanctum + admin)

#### App Version Management
- `GET /api/app-versions` - List all app versions
- `POST /api/app-versions` - Create new version
- `GET /api/app-versions/{id}` - Get specific version
- `PUT /api/app-versions/{id}` - Update version
- `DELETE /api/app-versions/{id}` - Delete version
- `POST /api/app-versions/{id}/toggle-status` - Toggle version status

#### Featured Jobs Management
- `POST /api/featured-jobs` - Create featured job
- `PUT /api/featured-jobs/{id}` - Update featured job
- `DELETE /api/featured-jobs/{id}` - Remove featured job

### File Access Endpoints
- `GET /api/profile_photos/{filename}` - Access profile photos
- `GET /api/resumes/{filename}` - Download resume files

## Security Considerations

### Authentication & Authorization
- **Multi-role Authentication**: Separate Laravel Sanctum tokens for users, recruiters, and admins
- **Custom Middleware**: Role-specific middleware (`recruiter.sanctum`) for access control
- **Token-based Security**: Bearer token authentication with automatic token refresh
- **Password Security**: Bcrypt hashing with strong password policies
- **Session Management**: Secure token storage with automatic cleanup

### Data Protection
- **Input Validation**: Comprehensive validation rules for all API endpoints
- **SQL Injection Prevention**: Eloquent ORM with parameterized queries
- **XSS Prevention**: Output sanitization and CSP headers
- **CSRF Protection**: Laravel CSRF tokens for web forms
- **File Upload Security**: Validated file types, size limits, and secure storage

### API Security
- **Rate Limiting**: API throttling to prevent abuse
- **CORS Configuration**: Proper cross-origin resource sharing setup
- **HTTPS Enforcement**: SSL/TLS encryption for all API communications
- **Error Handling**: Secure error responses without sensitive information exposure
- **Audit Logging**: Comprehensive logging for security monitoring

### Mobile App Security
- **Token Storage**: Secure SharedPreferences for token management
- **Certificate Pinning**: SSL certificate validation (recommended)
- **Obfuscation**: Code obfuscation for production builds
- **Root Detection**: Security checks for compromised devices (optional)

## Data Flow

### Job Seeker Application Process
1. **User Registration/Login**:
   - User registers via Android app or Google Sign-In
   - Server validates credentials and returns JWT token
   - Token stored securely in SharedPreferences
   - User profile created in database

2. **Job Discovery**:
   - User browses jobs with filtering (location, category, job type)
   - App fetches jobs from `/api/jobs` endpoint
   - Featured jobs displayed prominently
   - Job details shown with company information

3. **Job Application**:
   - User selects job and views detailed requirements
   - Application form pre-filled with profile data
   - Employment details and resume uploaded
   - Application submitted to `/api/jobs/{id}/apply-with-details`
   - Application record created with status "Applied"

4. **Application Tracking**:
   - User receives FCM notification for status updates
   - Application status tracked through `/api/applications`
   - Email notifications sent for major status changes

### Recruiter Workflow
1. **Recruiter Onboarding**:
   - Recruiter registers with company details
   - Account verification process (manual or automated)
   - Profile setup with company information

2. **Job Posting**:
   - Recruiter creates job posting via mobile app
   - Job details include requirements, salary, location
   - Job published and visible to job seekers
   - Analytics tracking for job performance

3. **Application Management**:
   - Recruiter receives notifications for new applications
   - Applications reviewed through mobile dashboard
   - Status updates (Under Review, Shortlisted, Rejected)
   - Bulk operations for efficient processing

4. **Interview Scheduling**:
   - Recruiter schedules interviews from application review
   - Interview details (date, time, type, location) specified
   - Automated email invitations sent to candidates
   - Calendar integration for interview management

5. **Candidate Pipeline**:
   - Recruiter tracks candidates through hiring pipeline
   - Candidate profiles with application history
   - Save candidates for future opportunities
   - Analytics on hiring metrics and performance

### Admin Operations
1. **System Monitoring**:
   - Admin monitors system health via web dashboard
   - User activity tracking and analytics
   - Performance metrics and system statistics

2. **Content Management**:
   - Job moderation and approval workflows
   - User account management and support
   - Featured job promotion and management

3. **Version Control**:
   - App version management and update notifications
   - Force update mechanisms for critical updates
   - Rollback capabilities for problematic releases

### Real-time Communications
1. **Push Notifications**:
   - Firebase Cloud Messaging (FCM) integration
   - Real-time notifications for status updates
   - Targeted notifications based on user preferences
   - Notification history and read status tracking

2. **Email Communications**:
   - Laravel Mail system for transactional emails
   - Interview invitation templates
   - Application status update notifications
   - Welcome emails and password reset communications

## Scalability Considerations

### Database Optimization
- **Indexing Strategy**: Optimized indexes on frequently queried columns (job categories, locations, status fields)
- **Query Optimization**: Efficient Eloquent queries with eager loading to prevent N+1 problems
- **Pagination**: Implemented pagination for all list endpoints to handle large datasets
- **Database Partitioning**: Table partitioning for large tables (applications, notifications)
- **Read Replicas**: Database read replicas for improved performance (future implementation)

### API Performance
- **Caching Strategy**: Laravel cache for frequently accessed data (job categories, featured jobs)
- **Response Optimization**: Optimized JSON responses with selective field loading
- **Background Jobs**: Laravel queues for time-intensive operations (email sending, file processing)
- **Rate Limiting**: API throttling to ensure fair usage and prevent abuse
- **CDN Integration**: Content Delivery Network for file storage and static assets

### Mobile App Optimization
- **Data Caching**: Local SQLite caching for offline functionality
- **Image Optimization**: Compressed images with lazy loading
- **Background Sync**: Background synchronization for improved user experience
- **Progressive Loading**: Incremental data loading to reduce initial load times

### Infrastructure Scaling
- **Horizontal Scaling**: Load balancer support for multiple application servers
- **Microservices Ready**: Modular architecture enables future microservices migration
- **Container Support**: Docker containerization for easy deployment and scaling
- **Cloud Integration**: AWS/GCP ready for cloud deployment

## Deployment Architecture

### Development Environment
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Local Dev     │    │   Staging       │    │   Production    │
│                 │    │                 │    │                 │
│ - Laravel Valet │    │ - Docker Compose│   │ - AWS/GCP      │
│ - MySQL Local   │    │ - MySQL Container│   │ - RDS MySQL    │
│ - Redis Local   │    │ - Redis Container│   │ - ElastiCache  │
│ - File Storage  │    │ - Local Storage  │   │ - S3/GCS       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Production Deployment
```
                    ┌─────────────────┐
                    │   Load Balancer │
                    │   (Nginx/ALB)   │
                    └─────────┬───────┘
                              │
                ┌─────────────┼─────────────┐
                │             │             │
    ┌───────────▼───┐  ┌──────▼──────┐  ┌──▼──────────┐
    │  App Server 1 │  │ App Server 2│  │File Storage │
    │   (Laravel)   │  │  (Laravel)  │  │   (S3/GCS)  │
    └───────────────┘  └─────────────┘  └─────────────┘
                │             │
                └─────────────┼─────────────┘
                              │
                    ┌─────────▼───────┐
                    │   Database      │
                    │   (RDS MySQL)   │
                    └─────────────────┘
```

### Technology Stack Summary

#### Backend (Laravel API)
- **Framework**: Laravel 9+ with PHP 8.0+
- **Database**: MySQL 8.0+ with InnoDB engine
- **Authentication**: Laravel Sanctum with custom middleware
- **Caching**: Redis for session and application caching
- **Queue System**: Laravel queues with Redis driver
- **File Storage**: Local storage (dev) / S3 (production)
- **Email**: Laravel Mail with SMTP/SES integration
- **Push Notifications**: Firebase Cloud Messaging

#### Frontend (Android App)
- **Language**: Java (Android SDK 21+)
- **HTTP Client**: Retrofit 2.9.0 with OkHttp interceptors
- **JSON Parsing**: Gson 2.8.9
- **Image Loading**: Glide (recommended)
- **Local Storage**: SharedPreferences + SQLite
- **Authentication**: JWT token with automatic refresh
- **Push Notifications**: Firebase FCM SDK

#### Admin Panel
- **Framework**: Laravel Blade templates
- **CSS Framework**: Bootstrap 5
- **JavaScript**: Vanilla JS + Chart.js for analytics
- **File Uploads**: Laravel file handling
- **Export Features**: Laravel Excel package

### Monitoring & Analytics
- **Application Monitoring**: Laravel Telescope for debugging
- **Error Tracking**: Sentry integration (recommended)
- **Performance Monitoring**: New Relic / Datadog (recommended)
- **Analytics**: Custom analytics dashboard + Google Analytics
- **Logging**: Laravel Log with structured logging
- **Health Checks**: Automated health monitoring endpoints

### Security Implementation
- **SSL/TLS**: HTTPS enforcement across all environments
- **Firewall**: Web Application Firewall (WAF) protection
- **DDoS Protection**: CloudFlare or AWS Shield
- **Backup Strategy**: Automated daily database backups
- **Disaster Recovery**: Multi-region backup and recovery plan
- **Compliance**: GDPR compliance for user data protection

### Future Enhancements
- **GraphQL API**: Consider GraphQL for more efficient data fetching
- **Real-time Features**: WebSocket integration for live updates
- **Machine Learning**: Job recommendation engine
- **Mobile PWA**: Progressive Web App for recruiters
- **Video Interviews**: Integration with video calling platforms
- **Advanced Analytics**: AI-powered hiring analytics
- **Multi-language Support**: Internationalization (i18n)
- **Dark Mode**: UI theme options for mobile app
