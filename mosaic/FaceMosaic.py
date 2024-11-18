import cv2
import numpy as np
import os
import dlib
from flask import Flask, request, jsonify
import base64

app = Flask(__name__)

# Haar Cascade 분류기 로드
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
profile_face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_profileface.xml')
eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
eyeglasses_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye_tree_eyeglasses.xml')

# dlib의 얼굴 탐지기와 랜드마크 예측기 로드
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor("Medicare-Backend/mosaic/shape_predictor_68_face_landmarks.dat")

def safe_mosaic(image, rect):
    """ 안전한 모자이크 처리 함수
    - 입력된 영역이 유효하지 않은 경우에도 오류 없이 처리
    """
    x, y, w, h = rect
    if w < 1 or h < 1:  # 너비나 높이가 1 미만인 경우 처리하지 않음
        return
    roi = image[y:y+h, x:x+w]
    new_w, new_h = max(1, w // 10), max(1, h // 10)  # 최소 크기를 1로 설정
    roi = cv2.resize(roi, (new_w, new_h))
    roi = cv2.resize(roi, (w, h), interpolation=cv2.INTER_NEAREST)
    image[y:y+h, x:x+w] = roi

def mosaic(image, rect):
    """ 모자이크 처리 함수
    - 지정된 영역을 모자이크 처리
    - 오류 발생(모자이크 크기가 작은 경우) 시 safe_mosaic 함수 호출
    """
    try:
        x, y, w, h = rect
        roi = image[y:y+h, x:x+w]
        roi = cv2.resize(roi, (w // 10, h // 10))
        roi = cv2.resize(roi, (w, h), interpolation=cv2.INTER_NEAREST)
        image[y:y+h, x:x+w] = roi
    except cv2.error:
        print("mosaic function failed. Using safe version.")
        safe_mosaic(image, rect)

def detect_tattoo(image):
    """ 문신 감지 함수
    - HSV와 YCrCb 색상 공간을 사용하여 피부 영역 탐지
    - 어두운 영역을 잠재적 문신으로 간주
    - 윤곽선 탐지 및 필터링을 통해 문신 영역 결정
    """
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    ycrcb = cv2.cvtColor(image, cv2.COLOR_BGR2YCrCb)

    # HSV 색상 범위로 피부 탐지
    lower_skin = np.array([0, 20, 70], dtype=np.uint8)
    upper_skin = np.array([20, 255, 255], dtype=np.uint8)
    skin_mask_hsv = cv2.inRange(hsv, lower_skin, upper_skin)

    # YCrCb 색상 공간에서 피부 탐지
    lower_skin_ycrcb = np.array([0, 135, 85], dtype=np.uint8)
    upper_skin_ycrcb = np.array([255, 180, 135], dtype=np.uint8)
    skin_mask_ycrcb = cv2.inRange(ycrcb, lower_skin_ycrcb, upper_skin_ycrcb)

    # 피부 마스크 결합
    skin_mask = cv2.bitwise_and(skin_mask_hsv, skin_mask_ycrcb)

    # 어두운 영역 탐지 (잠재적 타투)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    _, dark_mask = cv2.threshold(gray, 80, 255, cv2.THRESH_BINARY_INV)

    # 노이즈 제거 및 영역 확장
    kernel = np.ones((5,5), np.uint8)
    dark_mask = cv2.morphologyEx(dark_mask, cv2.MORPH_CLOSE, kernel)
    dark_mask = cv2.dilate(dark_mask, kernel, iterations=2)

    # 타투 마스크 생성
    tattoo_mask = cv2.bitwise_and(dark_mask, cv2.bitwise_not(skin_mask))

    # 윤곽선 탐지 및 필터링
    contours, _ = cv2.findContours(tattoo_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    tattoo_regions = []
    for contour in contours:
        area = cv2.contourArea(contour)
        if area > 200:  # 최소 영역 크기 필터링
            x, y, w, h = cv2.boundingRect(contour)
            aspect_ratio = float(w)/h
            if 0.2 < aspect_ratio < 5:
                '''
                하한값을 낮추면 길쭉한 타투 더 잘 감지, 상한값을 높이면 큰 크기의 타투를 더 잘 감지
                이 범위가 커질수록 타투를 더 감지 잘하는 대신 타투가 아닌곳을 타투로 생각할 확률이 노팡짐
                '''
                tattoo_regions.append((x, y, w, h))
    return tattoo_regions

def get_unique_filename(output_path):
    """ 고유한 파일명 생성 함수
    - 동일한 파일명이 존재할 경우 번호를 붙여 고유한 파일명 생성
    """
    base, ext = os.path.splitext(output_path)
    counter = 1
    while os.path.exists(output_path):
        output_path = f"{base}({counter}){ext}"
        counter += 1
    return output_path

def detect_and_mosaic_facial_features(image):
    """ 얼굴 특징 감지 및 모자이크 처리 함수
    - dlib을 사용하여 얼굴 랜드마크 감지
    - 눈, 코, 입 영역을 모자이크 처리
    """
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    faces = detector(gray)
    for face in faces:
        landmarks = predictor(gray, face)
        # 눈 모자이크
        left_eye = landmarks.parts()[36:42]
        right_eye = landmarks.parts()[42:48]
        left_eye_rect = cv2.boundingRect(np.array([(p.x, p.y) for p in left_eye]))
        right_eye_rect = cv2.boundingRect(np.array([(p.x, p.y) for p in right_eye]))
        mosaic(image, left_eye_rect)
        mosaic(image, right_eye_rect)
        # 코 모자이크
        nose = landmarks.parts()[27:36]
        nose_rect = cv2.boundingRect(np.array([(p.x, p.y) for p in nose]))
        mosaic(image, nose_rect)
        # 입 모자이크
        mouth = landmarks.parts()[48:68]
        mouth_rect = cv2.boundingRect(np.array([(p.x, p.y) for p in mouth]))
        mosaic(image, mouth_rect)
    return image

def process_image(input_image_path, output_image_path):
    """ 이미지 처리 메인 함수
    - 얼굴, 눈, 안경 감지 및 모자이크 처리
    - 문신 감지 및 모자이크 처리
    """
    img = cv2.imread(input_image_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Haar Cascade를 사용한 얼굴 탐지
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5)
    profile_faces = profile_face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5)
    
    has_face_or_features = False

    # 정면 얼굴에서 눈 탐지
    for (x, y, w, h) in faces:
        roi_gray = gray[y:y+h, x:x+w]
        roi_color = img[y:y+h, x:x+w]
        # 얼굴의 상단 2/3 영역만 탐지하도록 제한
        upper_roi_gray = roi_gray[0:h*2//3, :]
        upper_roi_color = roi_color[0:h*2//3, :]
        # 눈 탐지 및 안경 쓴 눈 탐지
        eyes = eye_cascade.detectMultiScale(upper_roi_gray)
        eyeglasses = eyeglasses_cascade.detectMultiScale(upper_roi_gray)
        for (ex, ey, ew, eh) in eyes:
            # 눈 영역 크기 조정 및 모자이크 처리
            new_ew, new_eh = int(ew * 0.7), int(eh * 0.5)  # 감지영역 축소해서 최대한 눈만 모자이크 할 수 있게끔함
            ex += (ew - new_ew) // 2
            ey += (eh - new_eh) // 2
            mosaic(upper_roi_color, (ex, ey, new_ew, new_eh))
            has_face_or_features = True
        for (ex, ey, ew, eh) in eyeglasses:
            # 안경 영역 크기 조정 및 모자이크 처리
            new_ew, new_eh = int(ew * 0.7), int(eh * 0.5)
            ex += (ew - new_ew) // 2
            ey += (eh - new_eh) // 2
            mosaic(upper_roi_color, (ex, ey, new_ew, new_eh))
            has_face_or_features = True

    # 측면 얼굴에서 눈 탐지
    for (x, y, w, h) in profile_faces:
        roi_gray = gray[y:y+h, x:x+w]
        roi_color = img[y:y+h, x:x+w]
        upper_roi_gray = roi_gray[0:h*2//3, :]
        upper_roi_color = roi_color[0:h*2//3, :]
        eyes = eye_cascade.detectMultiScale(upper_roi_gray)
        eyeglasses = eyeglasses_cascade.detectMultiScale(upper_roi_gray)
        for (ex, ey, ew, eh) in eyes:
            new_ew, new_eh = int(ew * 0.7), int(eh * 0.5)
            ex += (ew - new_ew) // 2
            ey += (eh - new_eh) // 2
            mosaic(upper_roi_color, (ex, ey, new_ew, new_eh))
            has_face_or_features = True
        for (ex, ey, ew, eh) in eyeglasses:
            new_ew, new_eh = int(ew * 0.7), int(eh * 0.5)
            ex += (ew - new_ew) // 2
            ey += (eh - new_eh) // 2
            mosaic(upper_roi_color, (ex, ey, new_ew, new_eh))
            has_face_or_features = True

    # dlib을 사용한 얼굴 특징 모자이크
    img = detect_and_mosaic_facial_features(img)

    # 문신 감지 및 모자이크 (얼굴이나 특징이 감지되지 않은 경우에만)
    if not has_face_or_features:
        tattoo_regions = detect_tattoo(img)
        for region in tattoo_regions:
            mosaic(img, region)

    # 고유한 파일명 생성 및 결과 이미지 저장
    unique_output_path = get_unique_filename(output_image_path)
    success = cv2.imwrite(unique_output_path, img)
    if not success:
        print("Error: Failed to save the image.")
    return unique_output_path

@app.route('/mosaic', methods=['POST'])  # '/mosaic' 경로로 POST 요청을 처리하는 라우트 정의
def mosaic_image():  # 이미지 모자이크 처리 함수 정의
    if 'file' not in request.files:  # 요청에 파일이 포함되어 있는지 확인
        return jsonify({'error': 'No file part'}), 400  # 파일이 없으면 에러 응답 반환
    file = request.files['file']  # 요청에서 파일 객체 추출
    if file.filename == '':  # 파일 이름이 비어있는지 확인
        return jsonify({'error': 'No selected file'}), 400  # 파일 이름이 비어있으면 에러 응답 반환
    if file:  # 파일이 존재하면 처리 시작
        input_path = 'Medicare-Backend/AllergyImages/ex6.jpg'  # 임시 입력 파일 경로 설정
        output_path = 'Medicare-Backend/AllergyImages/out.jpg'  # 임시 출력 파일 경로 설정
        file.save(input_path)  # 업로드된 파일을 임시 입력 경로에 저장
        processed_path = process_image(input_path, output_path)  # 이미지 처리 함수 호출
        
        with open(processed_path, "rb") as image_file:  # 처리된 이미지 파일 열기
            encoded_string = base64.b64encode(image_file.read()).decode('utf-8')  # 이미지를 base64로 인코딩
        
        os.remove(input_path)  # 임시 입력 파일 삭제
        os.remove(processed_path)  # 처리된 임시 출력 파일 삭제
        
        return jsonify({'processed_image': encoded_string})  # 인코딩된 이미지 데이터를 JSON 형식으로 반환

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)