FROM python:3.9

WORKDIR /app
COPY . /app

RUN apt-get update && apt-get install -y \
    cmake \
    build-essential \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    curl

RUN pip install --no-cache-dir dlib==19.24.6
RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "app.py"]