import os
from flask import Flask, request, jsonify
import cohere

app = Flask(__name__)

COHERE_API_KEY = os.environ.get("COHERE_API_KEY")
if not COHERE_API_KEY:
    raise ValueError("COHERE_API_KEY environment variable is missing!")

co = cohere.ClientV2(COHERE_API_KEY)

@app.route('/analyze', methods=['POST'])
def analyze():
    try:
        data = request.json
        reviews = data.get('reviews', 'No reviews provided.')
        
        response = co.chat(
            model="command-a-03-2025", 
            messages=[{
                "role": "user",
                "content": f"Please summarize these product reviews in 3-4 sentences in English. Focus on quality and customer satisfaction: {reviews}"
            }]
        )
        
        summary_text = response.message.content[0].text
        
        return jsonify({
            "summary": summary_text,
            "status": "success"
        })
    except Exception as e:
        return jsonify({"error": str(e), "status": "error"}), 500

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port)
