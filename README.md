# 📚 Readingwithme Backend

독서 기록용 페이지를 위한 Spring Boot 기반 백엔드 API 서버입니다.

## 🚀 주요 기능

### 📖 독서 관리
- **현재 읽고 있는 책**: 진행률 추적, 독서 일지 작성
- **읽다 만 책**: 하차 이유와 메모 기록
- **위시리스트**: 읽고 싶은 책 관리
- **다 읽은 책**: 완독한 책의 별점과 한줄평 작성
- 사용자 별 통계 제공 (전체/월별)

### 👥 독서 모임
- **독서 그룹**: 모임 생성 및 관리
- **그룹 멤버**: 초대, 권한 관리
- **모임 일정**: 모임 일정 관리

### 📝 게시글
- **독후감**: 책에 대해 독후감 작성
- **추천/비추천**: 책에 대한 추천 및 비추천 게시글 작성
- **문장 수집**: 책에서 마음에 드는 문장 저장
- 공개 범위 설정 가능 : 공개/비공개

### 🔐 인증 및 보안
- **카카오 로그인**: OAuth 2.0 기반 소셜 로그인
- **JWT 토큰**: 액세스 토큰 및 리프레시 토큰 관리

## 🛠 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Security**
- **Spring Data JPA**
- **Gradle**

### Database
- **MySQL** (프로덕션)

### External APIs
- **알라딘 도서 API**: 도서 검색 및 정보 조회

### Infrastructure
- **AWS EC2**: 서버 호스팅
- **GitHub Actions**: CI/CD 자동 배포
- **systemd**: 서비스 관리

## 📁 프로젝트 구조

```
src/main/java/com/readingclub/
├── config/          # 설정 클래스들
├── controller/      # REST API 컨트롤러
├── dto/            # 데이터 전송 객체
├── entity/         # JPA 엔티티
├── repository/     # 데이터 접근 계층
├── service/        # 비즈니스 로직
├── security/       # 보안 관련 클래스
└── util/           # 유틸리티 클래스
```

### 프로덕션 배포

GitHub main 브랜치에 푸시하면 자동으로 EC2에 배포됩니다.

## 📚 API 문서

### 주요 엔드포인트

#### 인증
- `POST /api/auth/kakao` - 카카오 로그인
- `POST /api/auth/refresh` - 토큰 갱신

#### 독서 관리
- `GET /api/currently-reading` - 현재 읽고 있는 책 목록
- `POST /api/currently-reading` - 새 책 추가
- `GET /api/dropped-books` - 읽다 만 책 목록
- `GET /api/wishlists` - 위시리스트

#### 독서 모임
- `GET /api/reading-groups` - 독서 그룹 목록
- `POST /api/reading-groups` - 새 그룹 생성
- `GET /api/group-meetings` - 모임 일정

#### 커뮤니티
- `GET /api/book-reviews` - 독서 리뷰
- `GET /api/posts` - 게시글
- `GET /api/comments` - 댓글

## 🔧 개발 가이드

### 브랜치 전략
- `main`: 프로덕션 배포용
- `dev`: 개발용

### 배포 프로세스
1. `dev` 브랜치에서 개발
2. GitHub에서 `dev` → `main` PR 생성
3. 코드 리뷰 후 merge
4. GitHub Actions를 통한 자동 배포

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.