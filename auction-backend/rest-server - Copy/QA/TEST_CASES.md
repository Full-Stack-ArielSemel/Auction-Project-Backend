# Auction Management Platform - Test Cases

## 1. Purpose

This document defines functional test cases for the main workflows and business rules of the Auction Management Platform.

The test cases include positive and negative scenarios covering authentication, authorization, products, bidding, auction operations, administration, validation, and real-time updates.

## 2. Test Case Fields

Each test case contains:

- Test Case ID
- Module
- Scenario
- Preconditions
- Test Steps
- Test Data
- Expected Result
- Priority
- Type

---

## TC-001 - Login With Valid Credentials

**Module:** Authentication  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- A valid user exists in the system.

**Test Steps:**

1. Send a login request.
2. Provide a valid username.
3. Provide the correct password.
4. Submit the request.

**Test Data:**

- Valid username
- Valid password

**Expected Result:**

- Login succeeds.
- The response indicates a successful operation.
- Authentication information is returned for the user.

---

## TC-002 - Login With Invalid Password

**Module:** Authentication  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- A valid user exists in the system.

**Test Steps:**

1. Send a login request.
2. Provide a valid username.
3. Provide an incorrect password.
4. Submit the request.

**Test Data:**

- Valid username
- Invalid password

**Expected Result:**

- Login is rejected.
- The response indicates failure.
- An appropriate error is returned.

---

## TC-003 - Register Valid User

**Module:** Registration  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- The username and email used for the test are not already registered.

**Test Steps:**

1. Send a registration request.
2. Provide valid user information.
3. Provide matching password values.
4. Submit the request.

**Test Data:**

- Valid full name
- Valid email
- Unique username
- Valid password
- Matching repeated password

**Expected Result:**

- Registration succeeds.
- A new user is created.

---

## TC-004 - Register Existing Username

**Module:** Registration  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- A user with the selected username already exists.

**Test Steps:**

1. Send a registration request.
2. Provide an existing username.
3. Provide the remaining required information.
4. Submit the request.

**Test Data:**

- Existing username
- Valid registration data

**Expected Result:**

- Registration is rejected.
- The existing user is not duplicated.
- An appropriate error is returned.

---

## TC-005 - Register With Invalid Input

**Module:** Registration  
**Priority:** Medium  
**Type:** Negative

**Preconditions:**

- None.

**Test Steps:**

1. Send a registration request.
2. Provide invalid registration input.
3. Submit the request.

**Test Data:**

Examples:

- Invalid username
- Invalid email
- Invalid password
- Non-matching passwords

**Expected Result:**

- Registration is rejected.
- Validation failure is returned.

---

## TC-006 - Access Protected Operation With Invalid Token

**Module:** Authentication / Authorization  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- A protected operation is available.

**Test Steps:**

1. Send a request to a protected operation.
2. Provide an invalid authentication token.
3. Submit the request.

**Test Data:**

- Invalid token
- User ID

**Expected Result:**

- Access is rejected.
- The requested operation is not performed.
- An authentication or permission error is returned.

---

## TC-007 - Retrieve Products Available for Sale

**Module:** Products  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- User is authenticated.
- Products available for sale exist.

**Test Steps:**

1. Authenticate as a valid user.
2. Request products available for sale.

**Test Data:**

- Valid token
- Valid user ID

**Expected Result:**

- Request succeeds.
- Available products are returned.

---

## TC-008 - Retrieve Existing Product Details

**Module:** Products  
**Priority:** Medium  
**Type:** Positive

**Preconditions:**

- User is authenticated.
- The requested product exists.

**Test Steps:**

1. Authenticate as a valid user.
2. Request details for an existing product.

**Test Data:**

- Valid token
- Valid user ID
- Existing product ID

**Expected Result:**

- Request succeeds.
- Product information is returned.

---

## TC-009 - Retrieve Non-Existing Product

**Module:** Products  
**Priority:** Medium  
**Type:** Negative

**Preconditions:**

- User is authenticated.

**Test Steps:**

1. Authenticate as a valid user.
2. Request details using a product ID that does not exist.

**Test Data:**

- Valid token
- Valid user ID
- Non-existing product ID

**Expected Result:**

- Request is rejected or returns a failure response.
- An appropriate product-not-found error is returned.

---

## TC-010 - Create Product With Valid Input

**Module:** Products  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- User is authenticated and allowed to create a product.

**Test Steps:**

1. Authenticate as a valid user.
2. Submit valid product information.
3. Create the product.

**Test Data:**

- Product name
- Product description
- Image URL
- Valid starting price

**Expected Result:**

- Product creation succeeds.
- The new product is available in the system.

---

## TC-011 - Place Valid Bid

**Module:** Bidding  
**Priority:** Critical  
**Type:** Positive

**Preconditions:**

- User is authenticated.
- Product exists.
- Auction is open.
- Product belongs to another user.
- User has sufficient credit.
- Bid satisfies the required price rules.

**Test Steps:**

1. Authenticate as a valid user.
2. Select an active auction.
3. Submit a valid bid.

**Test Data:**

- Valid token
- Valid user ID
- Valid product ID
- Valid bid amount

**Expected Result:**

- Bid is accepted.
- Auction state is updated accordingly.

---

## TC-012 - Place Bid Below Required Amount

**Module:** Bidding  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- User is authenticated.
- Auction is open.

**Test Steps:**

1. Select an active auction.
2. Submit a bid that does not satisfy the required price rule.

**Test Data:**

- Valid user
- Valid product
- Bid amount below required value

**Expected Result:**

- Bid is rejected.
- Auction state is not updated with the invalid bid.

---

## TC-013 - Place Bid With Insufficient Credit

**Module:** Bidding  
**Priority:** Critical  
**Type:** Negative

**Preconditions:**

- User is authenticated.
- Auction is open.
- User credit is lower than the required bid amount.

**Test Steps:**

1. Authenticate as the user.
2. Select an active auction.
3. Submit a bid exceeding available credit.

**Test Data:**

- User with insufficient credit
- Valid product ID
- Bid exceeding available credit

**Expected Result:**

- Bid is rejected.
- User credit is not incorrectly modified.
- An appropriate failure response is returned.

---

## TC-014 - Bid On Own Product

**Module:** Bidding  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- User is authenticated.
- User owns the selected product.
- Auction is open.

**Test Steps:**

1. Authenticate as the product owner.
2. Attempt to place a bid on the owned product.

**Test Data:**

- Owner user ID
- Owner token
- Owned product ID
- Valid bid amount

**Expected Result:**

- Bid is rejected.
- Product owner cannot bid on their own product.

---

## TC-015 - Bid On Closed Auction

**Module:** Bidding  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- User is authenticated.
- Selected auction is already closed.

**Test Steps:**

1. Select the closed auction.
2. Attempt to place a bid.

**Test Data:**

- Valid user
- Closed product ID
- Bid amount

**Expected Result:**

- Bid is rejected.
- Closed auction state remains unchanged.

---

## TC-016 - Close Eligible Auction

**Module:** Auction Management  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- User is authenticated.
- User owns the product.
- Auction meets the required conditions for closing.

**Test Steps:**

1. Authenticate as the product owner.
2. Select an eligible auction.
3. Request auction closure.

**Test Data:**

- Owner token
- Owner user ID
- Eligible product ID

**Expected Result:**

- Auction closes successfully.
- Product state reflects the closed auction.

---

## TC-017 - Close Auction Without Required Conditions

**Module:** Auction Management  
**Priority:** High  
**Type:** Negative

**Preconditions:**

- User owns the product.
- Auction does not meet the required closing conditions.

**Test Steps:**

1. Authenticate as the product owner.
2. Attempt to close the auction.

**Test Data:**

- Valid owner
- Product that does not meet closing conditions

**Expected Result:**

- Closing operation is rejected.
- Auction remains open.

---

## TC-018 - Close Auction Owned By Another User

**Module:** Auction Management  
**Priority:** Critical  
**Type:** Negative

**Preconditions:**

- User is authenticated.
- Product belongs to another user.

**Test Steps:**

1. Authenticate as a user who does not own the product.
2. Attempt to close the auction.

**Test Data:**

- Valid non-owner user
- Product owned by another user

**Expected Result:**

- Operation is rejected.
- Auction remains unchanged.
- Appropriate permission failure is returned.

---

## TC-019 - Admin Retrieves User Details

**Module:** Administration  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- Administrative user is authenticated.
- Target user exists.

**Test Steps:**

1. Authenticate with administrative permissions.
2. Request details for an existing user.

**Test Data:**

- Admin authentication data
- Existing target user ID

**Expected Result:**

- Request succeeds.
- Target user information is returned.

---

## TC-020 - Non-Admin Performs Administrative Operation

**Module:** Administration  
**Priority:** Critical  
**Type:** Negative

**Preconditions:**

- Regular user is authenticated.

**Test Steps:**

1. Authenticate as a non-admin user.
2. Attempt an administrative operation.

**Test Data:**

- Regular user token
- Valid target user

**Expected Result:**

- Operation is rejected.
- No administrative change is performed.

---

## TC-021 - Admin Adds Credit To User

**Module:** Administration  
**Priority:** High  
**Type:** Positive

**Preconditions:**

- Administrative user is authenticated.
- Target user exists.

**Test Steps:**

1. Authenticate as an administrator.
2. Select an existing user.
3. Add a valid credit amount.

**Test Data:**

- Admin authentication data
- Target user ID
- Valid credit amount

**Expected Result:**

- Operation succeeds.
- Target user's credit is updated correctly.

---

## TC-022 - Create Real-Time Update Connection

**Module:** Live Updates  
**Priority:** Medium  
**Type:** Positive

**Preconditions:**

- Valid user data is available.

**Test Steps:**

1. Request a real-time update connection for a valid user.
2. Verify that the connection is created.

**Test Data:**

- Valid user information

**Expected Result:**

- SSE connection is created successfully.
- The application can use the connection for real-time updates.

---

## TC-023 - Invalid Real-Time Update Connection

**Module:** Live Updates  
**Priority:** Medium  
**Type:** Negative

**Preconditions:**

- None.

**Test Steps:**

1. Attempt to create a real-time update connection using invalid user data.

**Test Data:**

- Invalid user information

**Expected Result:**

- Invalid connection request is handled correctly.
- No invalid active connection is retained.

---

## Test Case Summary

The test cases cover the following areas:

- Authentication
- Registration
- Authorization
- Input validation
- Product operations
- Bidding
- Auction management
- Credit management
- Administrative permissions
- Error handling
- Real-time updates

The scenarios are designed to cover both successful workflows and important failure conditions, with priority given to authentication, permissions, bidding rules, and auction business logic.
