# E-Commerce AI Service

This is the independent AI Microservice for the E-Commerce API platform. It is built using **Python** and **Flask**, and utilizes **Cohere AI** to process and summarize product reviews.

## Purpose
Instead of burdening the main Java (Spring Boot) backend with machine learning tasks and AI integrations, this service is decoupled as a separate microservice. 
When a user views a product, the Java backend calls this service via HTTP to generate a real-time, AI-powered sentiment summary of all the reviews for that specific product.

## Architecture
- **Framework:** Flask (Python)
- **AI Engine:** Cohere API
- **Server:** Gunicorn

## Deployment
This service is designed to be completely stateless and is deployed independently on **Railway**. The Java backend communicates with the Railway production URL, ensuring the two services can scale independently.

## Local Development
To run this service locally on your machine:

1. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```
2. **Environment Variables:**
   You will need to set your `COHERE_API_KEY` in your environment variables.
3. **Run the server:**
   ```bash
   flask run
   ```
   *(Or using Gunicorn for production: `gunicorn main:app`)*

---
*Part of the E-Commerce API Monorepo.*