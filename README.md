# USW Circle Link (동아리 관리 플랫폼)

## 📌 프로젝트 개요
**USW Circle Link**는 수원대학교 동아리 관리와 소통을 지원하는 플랫폼입니다.  
동아리 모집 공고, 회원 관리, 공지사항 전달을 웹과 모바일 환경에서 통합 제공하여, 회장단과 학생 모두 효율적으로 동아리를 관리하고 참여할 수 있도록 설계되었습니다.  

현재 서비스는 **갤럭시 스토어와 애플 앱스토어에 배포되어 있으며**, 실제 학생들이 사용 중입니다.  
백엔드 서버는 AWS 기반으로 **상시 운영**되고 있습니다.

---

## 🛠 Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot
- **Database**: MySQL, Redis
- **Storage**: AWS S3
- **Security**: Spring Security, JWT, Validation
- **Infra**: AWS EC2, RDS, NGINX
- **Build Tool**: Gradle
- **Others**: JPA, REST API
- **Frontend**: Vue.js (Web), Flutter + Firebase (Mobile)

---

## ⚙️ 아키텍처 & 주요 기능
- **Controller**: REST API 엔드포인트 제공
- **Service**: 비즈니스 로직 처리
- **Repository**: JPA 기반 데이터베이스 접근
- **Domain/Entity**: DB 매핑 객체
- **DTO**: 요청/응답 데이터 모델
- **Infra**: AWS EC2 서버 운영, RDS(MySQL) 연결, S3 파일 업로드 관리

### 주요 기능
**관리자(Admin)**  
- 동아리 카테고리 관리  
- 공지사항 등록 및 관리  
- 관리자 계정 인증/인가 (JWT + Spring Security)  

**동아리 회장(Club Leader)**  
- 동아리 등록/수정/삭제  
- 동아리원 관리  
- 동아리 소개 자료 등록  

**사용자(User)**  
- 동아리 목록 및 상세 조회  
- 동아리 지원 및 지원 현황 확인  
- 공지사항 열람  
- 동아리 사진 자료 확인 (S3 연동)  

---

## 👤 My Role (방혁)
본 프로젝트에서 저는 **백엔드 핵심 로직, 보안, 데이터베이스, 인프라 전반**을 담당했습니다.

- **인증·보안**
  - **JWT 기반 인증·인가 로직 구현** (Spring Security)  
  - Validation 처리 및 사용자 입력 검증  

- **데이터베이스**
  - **MySQL DB 설계 및 최적화**  
  - Redis 캐싱 적용으로 조회 성능 개선  

- **파일 관리**
  - AWS **S3 파일 업로드/다운로드 로직** 구현  

- **인프라**
  - AWS EC2 서버 연결 및 **서비스 배포/운영 관리**  
  - AWS RDS(MySQL) 연결 및 데이터베이스 운영  
  - NGINX 리버스 프록시 및 서버 환경 구성  

- **비즈니스 로직**
  - Admin, Application, Validation 핵심 서비스 로직 개발  
