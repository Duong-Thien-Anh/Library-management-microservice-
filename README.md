

# Library Management Microservices

Hệ thống quản lý thư viện được xây dựng trên kiến trúc Microservices sử dụng **Spring Boot**, **Spring Cloud Eureka**, và **Docker**.

##  Kiến trúc hệ thống

Hệ thống bao gồm các dịch vụ chính:

* **Discovery Server**: Quản lý việc đăng ký và tìm kiếm dịch vụ (Eureka Server).
* **Auth Service**: Quản lý xác thực và phân quyền sử dụng JWT.
* **Book Service**: Quản lý danh mục sách.
* **Borrow Service**: Quản lý hoạt động mượn/trả sách.

---

##  Cấu hình môi trường (Environment Variables)

Trước khi chạy hệ thống,  cần cấu hình các thông số sau trong file `application.yml` hoặc biến môi trường:

### 1. Database Configuration

Mỗi service (`Auth`, `Book`, `Borrow`) đều cần kết nối Database:

* `SPRING_DATASOURCE_URL`: `jdbc:mysql://<db_host>:<port>/<db_name>`
* `SPRING_DATASOURCE_USERNAME`: `<username_cua_babi>`
* `SPRING_DATASOURCE_PASSWORD`: `<password_cua_babi>`

### 2. Security Configuration (JWT)

Tại **Auth ,Book và Borrow Service **, cần thêm Secret Key để mã hóa Token:

* `JWT_SECRET`: `Gõ_Dãy_Ký_Tự_Bất_Kỳ_Thật_Dài_Vào_Đây_Babi_Nhé`
* `JWT_EXPIRATION`: `86400000` (Ví dụ: 24 giờ tính bằng ms)

---

## Hướng dẫn khởi chạy

### Bước 1: Khởi động Discovery Server

Phải chạy **Discovery Server** đầu tiên để các service khác có chỗ "báo danh".

1. Truy cập: `cd discovery-server`
2. Chạy: `mvn spring-boot:run`
3. Kiểm tra tại: `http://localhost:8761`

### Bước 2: Khởi động các Service còn lại

Lặp lại các bước sau cho `auth-service`, `book-service`, và `borrow-service`:

1. `cd <folder-service>`
2. `mvn spring-boot:run`

---

##  Hướng dẫn Build với Docker

 có thể đóng gói tất cả bằng Docker để triển khai nhanh chóng.

### 1. Build File Jar

Ở thư mục gốc của từng service, chạy:

```bash
mvn clean package -DskipTests

```

### 2. Build Docker Image

Chạy lệnh sau cho từng service:

```bash
docker build -t <ten_service>:v1 .

```

### 3. Chạy bằng Docker Compose (Khuyên dùng)

Nếu babi có file `docker-compose.yml` ở thư mục gốc:

```bash
docker-compose up -d

```

---

##  Lưu ý cho người dùng

* Đảm bảo cổng **8761** đã được mở cho Eureka.
* Các service cần vài giây để xuất hiện trên bảng điều khiển của Eureka Server sau khi khởi động.
* Nhớ tạo Database tương ứng trong MySQL/PostgreSQL trước khi chạy service.

---