# URL Shortener API

A RESTful API service built with Java and Spring Boot that allows users to shorten long URLs, similar to services like
Bitly or TinyURL.

## Features

- Create short URLs from long URLs
- Retrieve original URLs using short codes
- Update existing URLs
- Delete short URLs
- Track access statistics for each short URL
- Validation for URLs and input data

## Technologies

- Java 21
- Spring Boot 3.4.4
- Spring Data JPA
- H2 Database (in-memory)
- Maven
- Lombok

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── urlshortener/
│   │           ├── UrlShortenerApplication.java
│   │           ├── controller/
│   │           │   └── UrlShortenerController.java
│   │           ├── dto/
│   │           │   ├── ShortenUrlRequest.java
│   │           │   ├── ShortenUrlResponse.java
│   │           │   └── UrlStatisticsResponse.java
│   │           ├── entity/
│   │           │   └── ShortenedUrl.java
│   │           ├── exception/
│   │           │   ├── ResourceNotFoundException.java
│   │           │   └── GlobalExceptionHandler.java
│   │           ├── repository/
│   │           │   └── ShortenedUrlRepository.java
│   │           └── service/
│   │               └── impl
│   │                   └── UrlShortenerServiceImpl.java
│   │               └── UrlShortenerService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── urlshortener/
                ├── controller/
                ├── integration/
                ├── service/
                └── repository/
```

## API Endpoints

### Create Short URL

- **POST** `/shorten`
- Request Body:
  ```json
  {
    "url": "https://www.example.com/some/long/url"
  }
  ```
- Response (201 Created):
  ```json
  {
    "id": "1",
    "url": "https://www.example.com/some/long/url",
    "shortCode": "abc123",
    "createdAt": "2021-09-01T12:00:00Z",
    "updatedAt": "2021-09-01T12:00:00Z"
  }
  ```

### Get Original URL

- **GET** `/shorten/{shortCode}`
- Response (200 OK):
  ```json
  {
    "id": "1",
    "url": "https://www.example.com/some/long/url",
    "shortCode": "abc123",
    "createdAt": "2021-09-01T12:00:00Z",
    "updatedAt": "2021-09-01T12:00:00Z"
  }
  ```

### Update URL

- **PUT** `/shorten/{shortCode}`
- Request Body:
  ```json
  {
    "url": "https://www.example.com/some/updated/url"
  }
  ```
- Response (200 OK):
  ```json
  {
    "id": "1",
    "url": "https://www.example.com/some/updated/url",
    "shortCode": "abc123",
    "createdAt": "2021-09-01T12:00:00Z",
    "updatedAt": "2021-09-01T12:30:00Z"
  }
  ```

### Delete URL

- **DELETE** `/shorten/{shortCode}`
- Response (204 No Content)

### Get URL Statistics

- **GET** `/shorten/{shortCode}/stats`
- Response (200 OK):
  ```json
  {
    "id": "1",
    "url": "https://www.example.com/some/long/url",
    "shortCode": "abc123",
    "createdAt": "2021-09-01T12:00:00Z",
    "updatedAt": "2021-09-01T12:00:00Z",
    "accessCount": 10
  }
  ```

## Setup and Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Steps to Run

1. Clone the repository
   ```
   git clone https://github.com/bdkamaci/url-shortening-service.git
   cd url-shortening-service
   ```

2. Build the project
   ```
   mvn clean install
   ```

3. Run the application
   ```
   mvn spring-boot:run
   ```

4. The API will be available at `http://localhost:8080`

## Testing the API with Postman

You can use the following Postman examples to test the API:

### 1. Create a short URL

- **Method**: POST
- **URL**: `http://localhost:8080/shorten`
- **Headers**: Content-Type: application/json
- **Body**:
  ```json
  {
    "url": "https://www.example.com/very/long/url/that/needs/to/be/shortened"
  }
  ```

### 2. Get original URL

- **Method**: GET
- **URL**: `http://localhost:8080/shorten/{shortCode}`

### 3. Update URL

- **Method**: PUT
- **URL**: `http://localhost:8080/shorten/{shortCode}`
- **Headers**: Content-Type: application/json
- **Body**:
  ```json
  {
    "url": "https://www.example.com/updated/url"
  }
  ```

### 4. Get URL statistics

- **Method**: GET
- **URL**: `http://localhost:8080/shorten/{shortCode}/stats`

### 5. Delete URL

- **Method**: DELETE
- **URL**: `http://localhost:8080/shorten/{shortCode}`

## Potential Improvements

- Add user authentication and authorization
- Implement rate limiting to prevent abuse
- Add custom short code feature
- Create a frontend interface for better user experience
- Implement persistent storage using a production database
- Add comprehensive logging
- Implement caching for frequently accessed URLs
- Add expiration date for short URLs

## Acknowledgments

- SpringBoot Documentation
- https://roadmap.sh/projects/url-shortening-service

## License

This project is licensed under the MIT License - see the LICENSE file for details.