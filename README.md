# 동구라미 (Donggurami) — 동아리 관리 플랫폼

## 📌 프로젝트 개요
동구라미(Donggurami)는 수원대학교의 동아리 운영·관리 통합 플랫폼입니다.  
동아리 모집, 회원 관리, 공지사항 전달을 웹 · 모바일 환경에서 서비스하여  
동아리 회장단과 학생 모두가 효율적으로 소통하고 운영할 수 있도록 설계된 서비스입니다.  

현재 서비스는 갤럭시 스토어 · 앱스토어에 배포되어 실제 학생들이 사용 중이며,  
백엔드 서버는 AWS 기반 인프라로 운영되고 있습니다.  

---

## 🛠 Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security + JWT
- JPA
- MySQL (AWS RDS)
- Redis
- AWS S3
- Gradle

### Frontend
- Vue.js (Web)
- Flutter + Firebase (Mobile)

### Infrastructure
- AWS EC2
- AWS RDS
- AWS S3
- NGINX (Reverse Proxy)

---

## ⚙️ 주요 기능

### 🔐 관리자(Admin)
- 동아리 카테고리 생성/관리  
- 공지사항 CRUD  
- 동아리 회장 계정 생성/삭제  
- 동아리 등록/삭제  

### 🏅 동아리 회장(Club Leader)
- 동아리 정보 등록/수정/삭제  
- 동아리원 관리  
- 동아리 소개 자료 업로드  

### 👤 사용자(User)
- 동아리 목록 및 상세 조회  
- 동아리 지원 및 지원 현황 확인  
- 공지사항 열람  
- 동아리 사진 자료 열람 (S3 연동)  

---

## 👤 My Role — *Backend Developer (방혁)*

본 프로젝트에서 백엔드 기능, 보안, 데이터베이스, 인프라 전반을 직접 개발했습니다.  

### 🔐 인증·보안
- Spring Security + JWT 기반 인증·인가 로직 전면 구현  
- Access/Refresh Token 발급 및 재발급 처리  
- 사용자 입력 Validation 및 Global ExceptionHandler 구성  

### 💾 데이터베이스
- Redis를 활용한 Refresh Token 저장/조회 구조 구현  
- MySQL 스키마 설계 및 쿼리 최적화  
- JPA 기반 도메인 서비스 개발  

### 🗂 파일 관리 (AWS S3)
- AWS S3 파일 업로드/다운로드 기능 구현  
- 공지사항 이미지 자료 관리 API 개발  

### ☁️ 인프라 (AWS)
- AWS EC2 서버 환경 구성 및 서비스 배포   
- RDS(MySQL) 세팅 및 운영  
- NGINX Reverse Proxy 설정  
- HTTPS 적용 및 운영 환경 튜닝  

### 🧩 비즈니스 로직 개발
- Admin 서비스  
- Notice(공지) 서비스  
- Application(지원) 서비스  
- Security / Validation / GlobalResponse Layer  
- S3File 서비스 로직 구축  

---

## ✔️ Summary
동구라미 프로젝트는 단순 CRUD를 넘어 실제 학생들이 사용하는 실서비스이며,  
백엔드 개발자로서 인증/보안, Redis 기반 토큰 저장, AWS 인프라 구성,  
그리고 동아리 관련 비즈니스 로직 전반을 구현하며 서비스 전체의 핵심 구조를 책임졌습니다.
