from flask import Flask, request, jsonify
from flask_cors import CORS
import os
from openai import OpenAI
from dotenv import load_dotenv

app = Flask(__name__)
CORS(app)

load_dotenv()  # .env 파일에서 환경 변수 로드
client = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))

# JSON 인코더 설정 변경
app.json.ensure_ascii = False

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

@app.route('/chat', methods=['POST'])
def chat():
    user_message = request.json['message']
    response = get_chatbot_response(user_message)
    return jsonify({'response': response})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)