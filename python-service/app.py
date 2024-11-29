from flask import Flask, request, jsonify
from flask_cors import CORS
import cv2, numpy as np, os, dlib, base64
from openai import OpenAI
from dotenv import load_dotenv
import json
from datetime import datetime
from itertools import combinations
import os
import httpx

app = Flask(__name__)
CORS(app)

# OpenAI 설정
load_dotenv()
client = OpenAI(
    api_key=os.getenv('OPENAI_API_KEY'),
    http_client=httpx.Client()
)
app.json.ensure_ascii = False

# 모자이크 설정
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
profile_face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_profileface.xml')
eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
eyeglasses_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye_tree_eyeglasses.xml')
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")

# 챗봇 설정
conversation_history = []
system_message = (
    "당신은 의약품 및 영양제에 대한 전문 지식을 가진 AI 건강 보조 도우미입니다. "
    "사용자가 제공한 정보를 바탕으로 알레르기 및 부작용의 가능성을 평가하고, "
    "이를 이해하기 쉽게 설명해야 합니다. "
    "모든 응답은 최신 의학 및 약리학 정보를 기반으로 하며, "
    "사용자에게 신뢰할 수 있는 정보를 제공해야 합니다. "
    "각 성분의 일반적인 부작용 및 알레르기 반응에 대한 정보를 명확히 언급하세요. "
    "개인적인 의견이나 주관적인 해석을 피하고, 과학적 근거에 기반한 정보를 제공해야 합니다. "
    "사용자의 복용 이력과 날짜에 따라 발생할 수 있는 부작용을 예측할 때, "
    "객관적인 데이터와 사례를 활용하세요. "
    "사용자가 질문한 의약품이나 영양제의 성분, 복용 방법, 상호작용 등에 대한 정보를 "
    "제공하며, 사용자의 건강 상태에 맞는 적절한 조언을 할 수 있도록 합니다. "
    "사용자가 제공한 정보에 대해 충분히 질문하여, 보다 정확한 정보를 제공할 수 있도록 하세요."
)

# 챗봇 함수
def get_chatbot_response(user_message):
    conversation_history.append({"role": "user", "content": user_message})
    conversation_history.append({"role": "system", "content": system_message})
    completion = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=conversation_history
    )
    bot_response = completion.choices[0].message.content
    conversation_history.append({"role": "assistant", "content": bot_response})
    return bot_response

# 모자이크 관련 함수들
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


# 알레르기 추론
def parse_date(date_str):
    # 문자열 형식의 날짜를 datetime 객체로 변환
    return datetime.strptime(date_str, "%Y.%m.%d")

def analyze_allergy(allergy_info, medication_history):
    # 복용 이력을 날짜 순으로 정렬
    medication_history.sort(key=lambda x: parse_date(x['date']))
    new_medications = set() # 알레르기 발생시 전에 먹지 않은 약물
    new_combinations = set() # 알레르기 발생시 전에 없던 조합
    previous_medications = set() # 이전에 복용한 약물

    for entry in medication_history:
        current_meds = set(entry['medications']) # 현재 복용 중인 약물 집합 생성
        if entry['allergy']:
            # 케이스 1: 새로운 약물
            new_meds = current_meds - previous_medications # 이전에 복용하지 않은 약물 찾기
            if new_meds:
                return generate_gpt_response(1, allergy_info, list(new_meds), medication_history)
            
            # 케이스 2: 새로운 조합
            for combo in [frozenset(combo) for combo in combinations(current_meds, 2)]: 
                if combo not in new_combinations: # 새로운 조합이 이전에 없던 경우
                    return generate_gpt_response(2, allergy_info, list(combo), medication_history)
            
            # 케이스 3: 이전에 문제없던 약물
            if current_meds.issubset(previous_medications): # 현재 복용 중인 약물이 모두 이전에 복용한 경우
                return generate_gpt_response(3, allergy_info, list(current_meds), medication_history)
            
            # 케이스 4: 기타 경우
            return generate_gpt_response(4, allergy_info, list(current_meds), medication_history)
        
        previous_medications.update(current_meds) 
        new_combinations.update([frozenset(combo) for combo in combinations(current_meds, 2)])

    # 알레르기가 발생하지 않은 경우
    return generate_gpt_response(4, allergy_info, [], medication_history)

def generate_gpt_response(case, allergy_info, suspected_allergens, medication_history):
    if case == 1:
        user_message = f"""
        발생한 알레르기: {allergy_info}
        처음 복용한 약물: {', '.join(suspected_allergens)}
        최근 5일간 복용 정보:
        {json.dumps(medication_history, ensure_ascii=False, indent=2)}

        위 정보를 바탕으로 새로 복용한 약물이 알레르기의 원인일 가능성과 그 이유를 설명해주세요.
        """
    elif case == 2:
        user_message = f"""
        발생한 알레르기: {allergy_info}
        새로운 약물 조합: {' 및 '.join(suspected_allergens)}
        최근 5일간 복용 정보:
        {json.dumps(medication_history, ensure_ascii=False, indent=2)}

        위 정보를 바탕으로 새로운 약물 조합이 알레르기의 원인일 가능성과 그 이유를 설명해주세요.
        """
    elif case == 3:
        user_message = f"""
        발생한 알레르기: {allergy_info}
        이전에 안전하게 복용했던 약물: {', '.join(suspected_allergens)}
        최근 5일간 복용 정보:
        {json.dumps(medication_history, ensure_ascii=False, indent=2)}

        위 정보를 바탕으로 이전에 안전했던 약물이 알레르기를 유발한 이유와 다른 가능성에 대해 설명해주세요.
        """
    else:
        user_message = f"""
        발생한 알레르기: {allergy_info}
        최근 5일간 복용 정보:
        {json.dumps(medication_history, ensure_ascii=False, indent=2)}

        위 정보를 바탕으로 알레르기의 가능한 원인과 이유를 설명해주세요.
        """

    completion = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": (
                "당신은 의약품 및 영양제에 대한 전문 지식을 가진 AI 건강 보조 도우미입니다. "
                "제공된 정보를 바탕으로 알레르기 원인을 추론하고, 이를 JSON 형식으로 출력해야 합니다. "
                "출력 형식은 다음과 같아야 합니다: "
                "{"
                "  \"result\": \"의심되는 약물 또는 영양제 목록\", "
                "  \"cause\": {"
                "    \"reason1\": [\"이유1\", \"관련성\"], "
                "    \"reason2\": [\"이유2\", \"관련성\"], "
                "    \"reason3\": [\"이유3\", \"관련성\"]"
                "  }"
                "}"
                "이유는 최대 3가지까지만 간단 명료하게 작성하세요."
                "이 때 해당 의약품 및 영양제 혹은 의약품 및 영양제 조합이 발생한 알레르기의 원인이 될 수 있는지도 이유에 포함되어야합니다. 이 때 연관성이 적은 부작용이라면 그것을 언급하고 관련된 답변을 해줘야합니다."
                "이유를 명시할 때 마지막에 관련성에 대해서 (관련성 높음), (관련성 보통), (관련성 낮음)을 문구로 표시해줘."
            )},
            {"role": "user", "content": user_message}
        ],
        response_format={"type": "json_object"}
    )

    return completion.choices[0].message.content

# 라우트 설정
@app.route('/')
def health_check():
    return "OK", 200

@app.route('/chat', methods=['POST'])
def chat():
    user_message = request.json['message']
    response = get_chatbot_response(user_message)
    return jsonify({'response': response})

@app.route('/mosaic', methods=['POST'])
def mosaic_image():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400
    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    if file:
        input_path = 'AllergyImages/ex6.jpg'
        output_path = 'AllergyImages/out.jpg'
        file.save(input_path)
        processed_path = process_image(input_path, output_path)

        with open(processed_path, "rb") as image_file:
            encoded_string = base64.b64encode(image_file.read()).decode('utf-8')

        os.remove(input_path)
        os.remove(processed_path)

        return jsonify({'processed_image': encoded_string})

@app.route('/analyze_allergy', methods=['POST'])
def analyze_allergy_route():
    data = request.json
    allergy_info = data['allergy_info']
    medication_history = data['medication_history']
    result = analyze_allergy(allergy_info, medication_history)
    return jsonify(json.loads(result))

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)