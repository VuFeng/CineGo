# Docker Setup Guide

Hướng dẫn setup và chạy CineGo API với Docker.

## Yêu cầu

- Docker Desktop (Windows/Mac) hoặc Docker Engine (Linux)
- Docker Compose v2.0+

## Quick Start

### 1. Clone và setup environment

```bash
# Copy file .env.example thành .env
cp .env.example .env

# Chỉnh sửa .env nếu cần (hoặc dùng giá trị mặc định)
```

### 2. Build và chạy

```bash
# Build và start tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f server

# Stop services
docker-compose down

# Stop và xóa volumes (xóa database data)
docker-compose down -v
```

## Services

### Spring Boot API
- **Port**: 8080 (có thể thay đổi trong .env)
- **Base URL**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/health
- **Database**: Kết nối với Supabase (external)

**Lưu ý**: Project sử dụng Supabase làm database, không chạy PostgreSQL local trong Docker.

## Environment Variables

Tạo file `.env` trong root directory:

```env
# Supabase Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres?prepareThreshold=0
SPRING_DATASOURCE_USERNAME=postgres.your-project-id
SPRING_DATASOURCE_PASSWORD=your-supabase-password

# Server
SERVER_PORT=8080

# JWT
JWT_SECRET=your-strong-secret-key-min-256-bits

# Flyway
FLYWAY_ENABLED=false

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

**Lưu ý**: 
- Lấy connection string từ Supabase Dashboard > Settings > Database
- Sử dụng connection pooler URL nếu có (port 6543) hoặc direct connection (port 5432)
- Thêm `?prepareThreshold=0` vào URL để tương thích với Supabase pooler

## Development Mode

Để chạy với hot-reload (development):

```bash
# Sử dụng docker-compose.dev.yml
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

**Lưu ý**: Development mode mount source code vào container để hot-reload.

## Production Mode

### Build image:

```bash
cd server
docker build -t cinego-server:latest .
```

### Run với docker-compose:

```bash
docker-compose up -d
```

### Hoặc run standalone:

```bash
# Start API với Supabase connection
docker run -d \
  --name cinego-server \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres?prepareThreshold=0 \
  -e SPRING_DATASOURCE_USERNAME=postgres.your-project-id \
  -e SPRING_DATASOURCE_PASSWORD=your-password \
  -e JWT_SECRET=your-secret-key \
  -e SPRING_PROFILES_ACTIVE=docker \
  -p 8080:8080 \
  cinego-server:latest
```

## Useful Commands

### Xem logs:
```bash
# Tất cả services
docker-compose logs -f

# Chỉ server
docker-compose logs -f server

# Chỉ database
docker-compose logs -f postgres
```

### Restart service:
```bash
docker-compose restart server
```

### Rebuild và restart:
```bash
docker-compose up -d --build server
```

### Vào container:
```bash
# Server container
docker exec -it cinego-server sh

# Database container
docker exec -it cinego-postgres psql -U cinego_user -d cinego_db
```

### Xem status:
```bash
docker-compose ps
```

### Stop tất cả:
```bash
docker-compose down
```

### Xóa tất cả (bao gồm volumes):
```bash
docker-compose down -v
```

## Troubleshooting

### Port đã được sử dụng:
```bash
# Kiểm tra port đang được sử dụng
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Mac/Linux

# Thay đổi port trong .env
SERVER_PORT=8081
```

### Database connection error:
- Kiểm tra Supabase connection string trong .env
- Đảm bảo Supabase project đang active
- Kiểm tra network connectivity từ container đến Supabase
- Xem logs: `docker-compose logs server`

### Build error:
```bash
# Clean build
docker-compose build --no-cache server
```

### Permission denied (Linux):
```bash
# Fix permissions
sudo chown -R $USER:$USER .
```

## Health Checks

### API Health:
```bash
curl http://localhost:8080/api/health
```

### Database Health:
```bash
# Test connection từ container
docker-compose exec server sh -c "echo 'SELECT 1;' | psql \$SPRING_DATASOURCE_URL"
```

## Backup Database

Vì sử dụng Supabase, backup/restore nên thực hiện qua:
- Supabase Dashboard > Database > Backups
- Hoặc sử dụng Supabase CLI

## Production Recommendations

1. **Security**:
   - Thay đổi tất cả default passwords
   - Sử dụng strong JWT secret (min 256 bits)
   - Enable SSL/TLS cho database connections
   - Sử dụng secrets management (Docker Secrets, AWS Secrets Manager)

2. **Performance**:
   - Tune PostgreSQL settings
   - Configure connection pooling
   - Enable Flyway migrations
   - Use production-ready JVM options

3. **Monitoring**:
   - Add monitoring (Prometheus, Grafana)
   - Configure log aggregation
   - Set up alerts

4. **High Availability**:
   - Use Docker Swarm hoặc Kubernetes
   - Configure database replication
   - Set up load balancing
