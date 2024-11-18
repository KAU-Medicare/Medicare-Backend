FROM python:3.9-slim

WORKDIR /app

# 필요한 시스템 라이브러리 설치
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# shape_predictor_68_face_landmarks.dat 파일이 현재 디렉토리에 있어야 합니다
COPY shape_predictor_68_face_landmarks.dat .

CMD ["python", "FaceMosaic.py"]