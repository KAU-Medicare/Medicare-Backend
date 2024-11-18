# python-service/Dockerfile
FROM python:3.9-slim

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    cmake \
    build-essential \
    python3-dev

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . /app

# Python 패키지 설치
RUN pip install --upgrade pip
RUN pip install dlib==19.24.6
RUN pip install -r requirements.txt

# 실행 명령어 설정
CMD ["python", "app.py"]
