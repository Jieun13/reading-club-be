#!/bin/bash

# 배포 스크립트
echo "=== Reading Club Backend 배포 시작 ==="

# 환경 변수 설정
export SPRING_PROFILES_ACTIVE=prod

# 기존 프로세스 종료
echo "기존 프로세스 종료 중..."
pkill -f "reading-club" || echo "실행 중인 프로세스가 없습니다."

# 프로젝트 빌드
echo "프로젝트 빌드 중..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "빌드 실패!"
    exit 1
fi

# JAR 파일 실행
echo "애플리케이션 시작 중..."
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Xms512m -Xmx1024m \
    build/libs/reading-club-0.0.1-SNAPSHOT.jar \
    > logs/application.log 2>&1 &

# PID 저장
echo $! > app.pid

echo "배포 완료! PID: $(cat app.pid)"
echo "로그 확인: tail -f logs/application.log"
echo "상태 확인: curl http://localhost:8080/actuator/health"
