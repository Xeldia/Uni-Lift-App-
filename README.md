# Android API Integration Project

## 📦 Submission Requirements
- **Source Code:** https://github.com/Xeldia/Uni-Lift-App-.git

## 🌐 API Documentation

### Authentication Routes
- **POST `/register`** - Registers a new user. Expects `email`, `password`, `name`.
- **POST `/login`** - Authenticates a user. Expects `email`, `password`. Returns Bearer Token.

### Protected Routes (Requires Bearer Token)
- **GET `/dashboard`** - Retrieves user dashboard data.
- **GET `/profile`** - Retrieves user profile information.
- **PUT `/profile/update`** - Updates user profile details.
- **PUT `/profile/password`** - Changes user password. Expects `oldPassword`, `newPassword`.

## 📸 Screenshots

* **Login:** ![Login Screen](docs/login.png)
* **Register:** ![Register Screen](docs/register.png)
* **Dashboard:** ![Dashboard Screen](docs/dashboard.png)
* **Profile:** ![Profile Screen](docs/profile.png)
* **Settings:** ![Settings Screen](docs/settings.png)
* **Update Profile:** ![Update Profile Screen](docs/update_profile.png)
* **Change Password:** ![Change Password Screen](docs/change_password.png)
