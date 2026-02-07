# TrustCare Backend

Spring Boot application with OTP-based authentication system.

## Features

- **Two-Step Authentication**: Credentials → OTP via Email → JWT Token
- **Email Verification**: OTP sent via Gmail SMTP
- **JWT Authentication**: Secure token-based authentication
- **PostgreSQL Database**: Robust data persistence
- **Docker Compose**: Easy deployment and orchestration

## Prerequisites

- Docker and Docker Compose
- Gmail account with 2-Step Verification enabled
- Gmail App Password

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd TrustCare_Backend
```

### 2. Configure Environment Variables

Create a `.env` file in the project root directory:

```bash
cp .env.example .env
```

Edit `.env` and add your Gmail credentials:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

### 3. Generate Gmail App Password

1. Go to your [Google Account](https://myaccount.google.com/)
2. Navigate to **Security** → **2-Step Verification**
3. Scroll to bottom and click **App passwords**
4. Select **Mail** and enter name "TrustCare Backend"
5. Click **Generate**
6. Copy the 16-character password (remove spaces)
7. Paste it into your `.env` file as `MAIL_PASSWORD`

### 4. Start the Application

```bash
docker-compose up -d --build
```

The application will be available at `http://localhost:8080`

## API Endpoints

### Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123",
  "roleName": "PATIENT"
}
```

#### Login (Step 1 - Request OTP)
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePassword123"
}
```

**Response:**
```json
{
  "message": "OTP sent successfully",
  "username": "john_doe",
  "otpSent": true,
  "maskedEmail": "jo**@example.com"
}
```

#### Verify OTP (Step 2 - Get JWT Token)
```http
POST /auth/verify-otp
Content-Type: application/json

{
  "username": "john_doe",
  "otp": "123456"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```

#### Resend OTP
```http
POST /auth/resend-otp
Content-Type: application/json

{
  "username": "john_doe"
}
```

## Environment Variables

### Application Properties

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://postgres:5432/trust_care_db` |
| `DATABASE_USERNAME` | Database username | `trust_care_user` |
| `DATABASE_PASSWORD` | Database password | `trust_care_password` |
| `JWT_SECRET` | JWT token signing secret | Auto-generated |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `MAIL_USERNAME` | Gmail email address | **Required** |
| `MAIL_PASSWORD` | Gmail App Password | **Required** |

### Docker Compose Environment

The `docker-compose.yml` file loads environment variables from `.env` file automatically.

## OTP Configuration

- **OTP Length**: 6 digits
- **OTP Expiry**: 5 minutes
- **Storage**: In-memory cache (single-use)
- **Delivery**: Email via Gmail SMTP

## Security Features

- **Password Encryption**: BCrypt hashing
- **JWT Tokens**: HS256 algorithm, 24-hour expiration
- **OTP One-Time Use**: Automatic deletion after verification
- **Email Masking**: Partial email display for security

## Development

### Build the Application

```bash
./mvnw clean package
```

### Run Tests

```bash
./mvnw test
```

### Stop the Application

```bash
docker-compose down
```

### View Logs

```bash
docker-compose logs -f backend
```

## Troubleshooting

### Email Not Sending

1. Verify Gmail App Password is correct (16 characters, no spaces)
2. Ensure 2-Step Verification is enabled on your Google Account
3. Check if your Gmail account has "Less secure app access" disabled (App Passwords are required)
4. Check logs: `docker-compose logs backend`

### OTP Expired

- OTPs expire after 5 minutes
- Use the `/auth/resend-otp` endpoint to request a new OTP

### Authentication Failed

- Ensure you verify the OTP within 5 minutes
- Each OTP can only be used once
- Use the correct username (not email) for OTP verification

## License

All rights reserved.
