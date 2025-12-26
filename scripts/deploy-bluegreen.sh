#!/bin/bash
set -euo pipefail

# ==============================================
# Blue-Green Deployment Script for MyLog
# ==============================================

readonly LOG_FILE="/var/log/deployment.log"
readonly HEALTH_RETRIES=30
readonly HEALTH_DELAY=5
readonly TRAFFIC_STABILIZATION_TIME=10

# 로깅 설정
setup_logging() {
    sudo touch "$LOG_FILE"
    exec > >(sudo tee -a "$LOG_FILE") 2>&1
    echo "🚀 GCP Blue-Green Deployment started at $(date)"
}

# Health check 함수 (최적화된 버전)
wait_for_health() {
    local port=$1
    local service_name=${2:-"service"}
    
    echo "🔍 Waiting for $service_name health check on port $port..."
    
    # 포트 종류에 따른 헬스체크 URL 결정
    local health_url
    if [[ $port == "8080" ]]; then
        # Blue 환경: 컨테이너 내부 8080 포트
        health_url="http://localhost:8080/actuator/health"
    elif [[ $port == "8081" ]]; then
        # Green 환경: 컨테이너 내부 8081 포트 (환경 변수로 설정 필요)
        health_url="http://localhost:8081/actuator/health" 
    else
        health_url="http://localhost:${port}/actuator/health"
    fi
    
    for ((i=1; i<=HEALTH_RETRIES; i++)); do
        if timeout 10 curl -sf "$health_url" | grep -q "UP"; then
            echo "✅ Health check passed for $service_name on port $port"
            return 0
        fi
        
        echo "⏳ Attempt $i/$HEALTH_RETRIES: $service_name not healthy yet..."
        sleep $HEALTH_DELAY
    done
    
    echo "❌ Health check failed for $service_name after $HEALTH_RETRIES attempts"
    return 1
}

# 컨테이너 정리 함수
cleanup_containers() {
    local container_name=$1
    echo "🧹 Cleaning up $container_name container..."
    
    docker stop "$container_name" 2>/dev/null || true
    docker rm "$container_name" 2>/dev/null || true
    
    echo "✅ $container_name container cleaned up"
}

# Nginx 포트 전환 함수 (HELP.md 설정 구조에 맞춤)
switch_nginx_port() {
    local port=$1
    local environment=$2
    local config_file="/etc/nginx/sites-available/mylog-api.click"
    
    echo "🔄 Switching Nginx to $environment environment (port $port)..."
    
    # HELP.md 구조에 맞춰 proxy_pass 라인만 정확히 변경
    # location / { 블록 내의 proxy_pass 라인을 찾아서 변경
    if sudo sed -i "/location \/ {/,/}/ s|proxy_pass http://localhost:[0-9]*;|proxy_pass http://localhost:$port;|g" "$config_file"; then
        # Nginx 설정 검증
        if sudo nginx -t; then
            # Nginx 리로드
            if sudo systemctl reload nginx; then
                echo "✅ Nginx switched to $environment environment (port $port)"
                
                # 변경사항 확인 로그
                echo "📋 Current proxy_pass configuration:"
                sudo grep -A 10 "location / {" "$config_file" | grep "proxy_pass" || true
                
                return 0
            else
                echo "❌ Failed to reload Nginx"
                return 1
            fi
        else
            echo "❌ Nginx configuration test failed"
            echo "🔍 Configuration syntax check output:"
            sudo nginx -t 2>&1 || true
            return 1
        fi
    else
        echo "❌ Failed to update Nginx configuration"
        echo "🔍 Checking if config file exists:"
        ls -la "$config_file" || true
        return 1
    fi
}

# Blue-Green 전환 함수
promote_green_to_blue() {
    echo "🔄 Starting Blue-Green deployment switch..."
    
    # 1단계: Nginx를 Green으로 전환 (무중단)
    switch_nginx_port 8081 "Green"
    echo "🌐 Traffic now routing to Green environment"
    
    # 2단계: 트래픽 안정화 대기
    echo "⏳ Waiting for traffic stabilization..."
    sleep $TRAFFIC_STABILIZATION_TIME
    
    # 3단계: 기존 Blue 컨테이너 정리
    echo "🔵 Stopping old Blue container..."
    cleanup_containers "mylog-blue"
    
    # 4단계: 새로운 Blue 컨테이너 시작
    echo "🔵 Starting new Blue container..."
    # mylog-blue 서비스 시작 (container_name: mylog-blue, 포트: 8080)
    if ! docker compose up mylog-blue -d; then
        echo "❌ Failed to start new Blue container"
        return 1
    fi
    
    # 5단계: 새 Blue 환경 헬스체크
    echo "🔍 Health checking new Blue environment..."
    if wait_for_health 8080 "Blue"; then
        echo "✅ New Blue environment is healthy"
        
        # 6단계: Nginx를 새로운 Blue로 전환
        switch_nginx_port 8080 "Blue"
        echo "🌐 Traffic now routing to new Blue environment"
        
        # 7단계: Green 컨테이너 정리
        echo "🟢 Cleaning up Green container..."
        cleanup_containers "mylog-green"
        
        echo "🎉 Blue-Green deployment completed successfully!"
        return 0
    else
        echo "❌ New Blue environment health check failed!"
        echo "🚨 Rolling back to Green environment..."
        switch_nginx_port 8081 "Green"
        return 1
    fi
}

# Docker 초기화
initialize_docker() {
    echo "🐳 Initializing Docker services..."
    
    sudo systemctl start docker
    sudo systemctl enable docker
    
    # Docker Hub 인증
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
    echo "✅ Logged into Docker Hub"
    
    # 최신 이미지 다운로드
    docker compose pull
    echo "✅ Pulled latest Docker images from Docker Hub"
}

# 메인 배포 실행
main() {
    setup_logging
    
    initialize_docker
    
    # 🔍 Step 1: 현재 Blue 환경 상태 확인
    echo "🔍 Checking current Blue environment status..."
    
    if wait_for_health 8080 "Current Blue" &>/dev/null; then
        echo "✅ Blue environment is running - proceeding with Blue-Green deployment"
        
        # 🟢 Step 2: Green 환경 생성 (새 버전)
        echo "🟢 Creating Green environment with new version..."
        # mylog-green 서비스 시작 (container_name: mylog-green, 포트: 8081)
        if ! docker compose up mylog-green -d; then
            echo "❌ Failed to start Green environment"
            exit 1
        fi
        echo "✅ Green environment (mylog-green container) started on port 8081"
        
        # 🔍 Step 3: Green 환경 헬스 체크
        echo "🔍 Checking health of Green environment..."
        if wait_for_health 8081 "Green"; then
            echo "✅ Green environment is healthy"
            
            # 🔄 Step 4: Blue-Green 스위치 실행
            if promote_green_to_blue; then
                echo "🔄 Blue-Green switch completed successfully"
                echo "✅ Application is now running the new version on port 8080"
            else
                echo "❌ Blue-Green switch failed, cleaning up..."
                cleanup_containers "mylog-green"
                exit 1
            fi
        else
            echo "❌ Green environment health check failed"
            echo "🚨 Rolling back - cleaning up failed Green environment"
            cleanup_containers "mylog-green"
            echo "❌ Deployment failed. Blue environment (previous version) remains active."
            exit 1
        fi
        
        # 🔍 Step 5: 최종 헬스 체크
        echo "🔍 Final health check on Blue environment (port 8080)..."
        if ! wait_for_health 8080 "Final Blue"; then
            echo "❌ Final health check failed on Blue environment"
            echo "🚨 Critical: Both Blue and Green environments have issues"
            exit 1
        fi
        
    else
        # 🔵 Blue 환경이 없는 경우 - 초기 배포
        echo "📋 No Blue environment detected - performing initial deployment"
        echo "🔵 Starting Blue environment..."
        
        # mylog-blue 서비스와 redis 시작 (container_name: mylog-blue, 포트: 8080)
        if ! docker compose up mylog-blue redis -d; then
            echo "❌ Failed to start initial Blue environment"
            exit 1
        fi
        
        echo "✅ Blue environment (mylog-blue container) started on port 8080"
        
        # 초기 Blue 환경 헬스 체크
        echo "🔍 Checking health of initial Blue environment..."
        if wait_for_health 8080 "Initial Blue"; then
            echo "✅ Initial Blue environment is healthy"
            echo "🎉 Initial deployment completed successfully!"
        else
            echo "❌ Initial Blue environment health check failed"
            cleanup_containers "mylog-blue"
            exit 1
        fi
    fi
    
    # 정리 작업
    docker image prune -f
    
    # .env만 제거 (compose.yaml은 유지)
    rm -f .env
    
    echo "✅ GCP Blue-Green Deployment completed successfully at $(date)"
    echo "📋 compose.yaml file preserved for future deployments"
}

# 스크립트 실행
main "$@"
