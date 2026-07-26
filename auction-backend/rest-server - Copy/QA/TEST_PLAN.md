# Auction Management Platform - Test Plan

## 1. Introduction

This document defines the testing approach for the Auction Management Platform backend.

The application is a full-stack auction system that allows users to register, authenticate, create products, participate in auctions, place bids, manage products, and receive real-time updates.

The purpose of testing is to verify the correctness and reliability of the backend functionality, with emphasis on authentication, authorization, input validation, REST API behavior, auction business rules, error handling, and administrative operations.

## 2. Test Objectives

The main testing objectives are:

- Verify that core application functionality behaves according to expected business rules.
- Validate successful and unsuccessful authentication scenarios.
- Verify authorization and permission restrictions.
- Validate user and product input.
- Verify product creation and retrieval functionality.
- Validate bidding rules and auction operations.
- Verify administrative functionality and access restrictions.
- Validate REST API behavior and responses.
- Verify error handling for invalid requests and business conditions.
- Validate selected real-time update behavior.
- Provide automated regression coverage for important backend functionality.

## 3. Scope

### In Scope

The following areas are included in the current testing scope:

- User registration
- User login
- Authentication
- Authorization and permissions
- User statistics
- Product creation
- Product retrieval
- Product details
- User product management
- Bidding
- Auction closing
- Credit validation
- Administrative operations
- Input validation
- Error handling
- REST API endpoints
- Spring MVC behavior
- Utility logic
- Real-time update behavior

### Out of Scope

The following areas are outside the current testing scope:

- Browser-based UI automation
- Cross-browser compatibility testing
- Mobile application testing
- Performance and load testing
- Security penetration testing
- Production environment testing
- Full database integration testing

These areas may be included in future testing phases.

## 4. Test Approach

The project uses multiple testing levels and techniques.

### 4.1 Unit Testing

JUnit 5 is used for automated unit testing of backend functionality.

Mockito is used to mock dependencies and isolate components during testing.

Unit tests focus on:

- Controller logic
- Authentication behavior
- Validation logic
- Permission checks
- Auction business rules
- Administrative operations
- Utility methods
- Real-time update behavior

### 4.2 Spring MVC Testing

Spring MockMvc is used to test selected controller endpoints through the Spring MVC layer without requiring an external web server.

MockMvc tests focus on:

- Endpoint mappings
- HTTP methods
- Request parameters
- Response handling
- JSON responses
- Authentication failures
- Authorization failures
- Business error scenarios

MockMvc coverage includes:

- LoginController
- FeaturesController
- ManageController

### 4.3 REST API Testing

Postman is used to test REST API workflows.

The Postman collection covers:

- Registration
- Login
- User statistics
- Product operations
- Bidding
- Auction operations
- Administrative functionality

Postman tests include both positive and negative scenarios and use assertions to validate selected response behavior.

Collection location:

`postman/Auction-API.postman_collection.json`

## 5. Test Techniques

The testing approach includes:

### Positive Testing

Valid inputs and valid application flows are tested to verify that expected operations succeed.

Examples:

- Valid registration
- Valid login
- Product retrieval
- Product creation
- Valid bid
- Valid auction operation
- Authorized administrative operation

### Negative Testing

Invalid inputs and invalid application states are tested to verify that the system rejects operations correctly.

Examples:

- Invalid login credentials
- Invalid authentication token
- Invalid registration data
- Insufficient credit
- Invalid bid amount
- Bidding on an owned product
- Bidding on a closed auction
- Unauthorized administrative operation
- Missing or invalid resources

### Boundary and Validation Testing

Validation behavior is tested for selected user input and business values where applicable.

Examples include:

- Invalid authentication data
- Invalid registration input
- Invalid bid values
- Missing identifiers
- Invalid product data

## 6. Test Environment

The backend is implemented using:

- Java
- Spring Boot
- Maven
- MySQL

Testing tools:

- JUnit 5
- Mockito
- Spring MockMvc
- Postman

Local REST API testing assumes the backend is available at:

`http://localhost:8080`

## 7. Test Data

Testing uses controlled data representing different application states.

Examples include:

- Valid user credentials
- Invalid credentials
- Regular users
- Administrative users
- Valid and invalid authentication tokens
- Existing and non-existing product IDs
- Open and closed auctions
- Users with sufficient credit
- Users with insufficient credit
- Valid and invalid bid amounts

Postman collection variables are used for reusable test values, including:

- `baseUrl`
- `username`
- `password`
- `token`
- `userId`
- `productId`
- `bidOffer`
- `adminToken`
- `adminUserId`
- `targetUserId`

No production credentials or sensitive authentication data should be stored in the repository.

## 8. Entry Criteria

Testing can begin when:

- The backend source code is available.
- Required test dependencies are configured.
- The application can be built.
- Required test data is available.
- For Postman testing, the backend and required database services are available.
- Required collection variables are configured.

## 9. Exit Criteria

The testing phase can be considered complete when:

- Planned critical scenarios have been executed.
- Automated tests can be executed successfully.
- Core authentication and authorization scenarios have been verified.
- Core auction business rules have been verified.
- Critical REST API workflows have been tested.
- Critical defects discovered during testing have been reviewed and documented.

## 10. Test Deliverables

The current testing deliverables include:

- JUnit automated tests
- Mockito-based isolated tests
- Spring MockMvc tests
- Postman API collection
- Test Plan
- Test Cases

Automated Java tests are located under:

`src/test/java`

API tests are located under:

`postman/`

QA documentation is located under:

`qa/`
