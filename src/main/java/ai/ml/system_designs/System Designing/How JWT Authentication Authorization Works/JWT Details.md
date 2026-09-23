# JWT (JSON Web Token) - Complete Details

## What is JWT?

![img.png](img.png)

JWT is a way to securely transmit information between parties as a JSON Object.

JWT is a compact, URL-safe token that can carry information between parties.

**URL-safe token** - It uses Base64 URL Encoding.

A JWT is a string consisting of three parts, separated by dots.

1. Header
2. Payload
3. Signature

![img_1.png](img_1.png)

Reference: https://jwt.io/

![img_2.png](img_2.png)

---

## Structure of a JWT

### 1. Header
The Header typically consists of 2 parts: The type of the Token (JWT) and the Signing Algorithm being used such as SHA256, RSA or HMAC.

```json
{
    "alg": "HS256",
    "typ": "JWT"
}
```

### 2. Payload
The Payload contains the claims. Claims are statements about an Entity (typically the User) and additional metadata.

```json
{
    "email": "email@gmail.com",
    "name": "John Doe"
}
```

### 3. Signature
The Signature is used to verify that the Sender of the JWT is who it says it is and to ensure that the message was not changed along the way.

To create the Signature part you have to take the encoded Header, the encoded Payload, a Secret, the Algorithm specified in the Header, and sign that.

```
HMACSHA256(
  secret,
  base64UrlEncode(header) + "." + base64UrlEncode(payload)
)
```

For signature first we will put `base64UrlEncode` on header and then put `.` and then we will put `base64UrlEncode` on payload and then we will take any secret key and the string which we got `[ base64UrlEncode(header) + "." + base64UrlEncode(payload) ]` we will sign with secret key with the help of HMACSHA256.

---

## Basic Auth vs JWT

In Basic Auth for every REQUEST we were sending UserName & Password.

### How Basic Authentication Handles Request through Postman

![img_3.png](img_3.png)

We were using HttpBasic Authentication which mean while fetching record we were passing authentication details.

![img_4.png](img_4.png)

And that username and password was coming to Authorization Header:

```
key   - Authorization
value - Basic XXXXXXXX
```

![img_5.png](img_5.png)

Where `XXXXXXXX` is `Base64Encode(username:password)`

### What is the problem with Basic Auth? Why we are going for JWT?

![img_6.png](img_6.png)

![img_7.png](img_7.png)

So our username and password anyone can see once they decode it.

**Drawbacks of Basic Auth:**
- We are just encoding and sending.
- In every request we are sending username/password.

**JWT Advantages:**
- We are generating a TOKEN.
- TOKEN expiry also we can set.
- In every REQ we are not sending username/password.

---

## Configuration

Add the below 3 dependencies:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

### App Modifications

In Public Controller let's change end-point name `/create-user` to `/signup`.

- `/create-user` endpoint we changed in public controller to `/signup` and added one more endpoint `/login`.

---

## UseCase - Thought Pattern

1. First User will do Sign-up, there username/pwd entry will save to db.
2. While he login with username and password we will give him JWT Token in Response.
3. Now for further REQ he will provide Token not Username and password.
4. So first we need to authenticate the User based on his provided credentials if valid we can give him JWT token with expiration time, like 1 hour expiration time given so user can use jwt token for 1 hour and then again he needs to provide username and password.

- **With the help of `AuthenticationManager` we will Authenticate our User.**
- **Now Basic Auth we are going to Remove now.**

![img_8.png](img_8.png)

Internally our `UserDetailServiceImpl` getting call. It will call `loadUserByUserName()` of `UserDetailServiceImpl`. If user not found it will throw exception `UserNotFoundException` and we have created a Bean of `PasswordEncoder`.

![img_9.png](img_9.png)

![img_10.png](img_10.png)

Internally by using both it will check either user is existing user or not. If user is valid user then with the help of `UserDetailService` of `loadUserByUserName` by using userName it will get the UserDetail. With the help of userDetail we are going to generate a Token and the generated token we are going to return in our login controller.

---

## JWTUtil Story

### How to generate Token

![img_11.png](img_11.png)

We are calling `generateToken()` and passing userName. We are using the HashMap as claims.

- **Claims** we send inside a payload. Suppose, some information (like name, mail) we want to send then you have to put into claims `<HashMap>` and then send it.

![img_12.png](img_12.png)

- **subject** - How we can identify this. In our case we are having User so based on userName, so we have added Subject as userName.
- **header** - In Header as a type we send JWT.
- **issuedAt** - At what time token generated.
- **expiration** - 5 minutes.
- **signWith** - We are signing with the signing KEY, for signature we took the key and Signed In.

By default we are using Algo - **HMACSHA256** Algorithm, which is taking SECRET Key and our data. So it's taking below data:

```
HMACSHA256(
  secret,
  base64UrlEncode(header) + "." + base64UrlEncode(payload)
)
```

### getSigningKey()

![img_13.png](img_13.png)

We have created a random Secret Key. It should be more than 32 Bytes. With our key we have Created a new `SecretKey` instance for use with HMAC-SHA algorithms based on the specified key byte array. So we already saw we were giving KEY and Data (header + payload).

![img_14.png](img_14.png)

So, now our Token Has been generated.

Let's create a Bean of `AuthenticationManager` in `SecurityConfig`.

![img_15.png](img_15.png)

---

## Testing - 1st Case: Generate Token

Let's do SignUp and then Sign-In and let's generate a TOKEN.

### Sign Up

`POST - localhost:8080/journal/public/signup`

```json
{
    "userName":"jwtUser",
    "email": "jwtUser@gmail.com",
    "sentimentAnalysis": true,
    "password":"jwt12345"
}
```

### Login for the new signup user

`POST - localhost:8080/journal/public/login`

![img_16.png](img_16.png)

![img_19.png](img_19.png)

![img_20.png](img_20.png)

![img_21.png](img_21.png)

![img_22.png](img_22.png)

**Token created:**
```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqd3RVc2VyIiwiaWF0IjoxNzUyNTc5NjU4LCJleHAiOjE3NTI1Nzk5NTh9.hTneKb10Zc18XhvMHmhp-8iv36q2gHGQ_FcEVrlVYjE
```

### So Token has been created. What shall we do with this token?

Now for New Request Instead of sending username and password we can simply send generated token.

Before that let's do some analysis. Take that token and go to jwt.io Interface.

![img_23.png](img_23.png)

![img_24.png](img_24.png)

Our SECRET KEY for signing with algo - `TaK+HaV^uvCHEFsEVfypW#7g9^k*Z8$V`

**When we put valid SECRET_KEY for Signing IN then only Signature Verified otherwise Signature Failed.**

### Explanation

While putting token in JWT Interface we can see our data like username what we pass in our claims as a subject and then issued time and expiration time and In Header our algo like HMAC SHA256 and at end it's asking to verify a signature with secret key.

Suppose if any User is having this token and suppose if he does any changes then he can't see.

Suppose we have given expiry 5 minutes, but he wants to set for a year so, he can't do.

Because it's already signed in with `header encoded + payload encoded` with our Secret Key.

And it will only verify with a same SECRET key, which mean easily we get to know there is some modification happened with our token.

So, at least here we are having an expiry that is the important point here.

Now instead of Basic Auth we need to select Bearer Token.

![img_25.png](img_25.png)

![img_26.png](img_26.png)

Then automatically in Header Tab Authorization key and value will be `Bearer XXXXXX`:

```
Key   - Authorization
Value - Bearer <TOKEN>
```

---

## Replace Basic Auth with JWT & httpBasic with a Filter

![img_27.png](img_27.png)

Control will come here once user get authenticated.

![img_28.png](img_28.png)

So journal and user need authenticated and other endpoints like public has a permit all.

- **Now `httpBasic` we have to remove, and we need to add a Filter.**

### Important

This Jwt Filter we will run **before** the Spring Security's Basic Authentication. Basic authentication we have removed so now Spring Security Basic Authentication is a Form Login. Before that I will run Jwt Filter and after that REQUEST will go to the Controller.

![img_29.png](img_29.png)

Let's see how we can remove `httpBasic` auth and add our JWT Filter in our `SpringSecurityConfig`.

![img_30.png](img_30.png)

- **We have removed Basic Auth and rest all as it is and before `UsernamePasswordAuthenticationFilter` filter we run our Jwt Filter.**
- **What this (`UsernamePasswordAuthenticationFilter`) will do is as we removed basic auth then by default our form login will run so before Form Login we run Jwt Filter. What this will do is it will run `doFilterInternal()`. And Jwt Filter extends `OncePerRequestFilter`, which mean for each request it will filter once.**

![img_29.png](img_29.png)

### Explanation

Here 3 things coming `httpServletRequest`, `httpServletResponse` and `filterChain`. Filter chain mean as we have added our Jwt filter should run before `UsernamePasswordAuthenticationFilter` (form login) filter. So there are so many filters we can add before or after.

Now from REQUEST we took the Authorization Header and take the jwt token. From Jwt we extract the userName like our username is as a subject in Jwt Claim. From userName we are fetching UserDetails from db and then validating with jwt. So we are validating token with userName fetch from db and the userName extracted from jwt if both the userName is equal and token is not expired so we validated the token. So, once token validated then in SpringContext which we are using in our code like service and controller to fetch the userName after authentication so in our `SecurityContextHolder` we are setting our auth context.

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
String userName = authentication.getName();
```

So, here control will come only when user get successfully Authenticated.

```java
chain.doFilter(request, response);
```

In a chain we have sent our request and response for next filters. And in response also we have added a header. We can send header in response also. Now what will happen is let's see in `journalController`...

![img_31.png](img_31.png)

Internally what was the request and response in response chain here also will come. So what response we are sending it converts in HttpServlet Request and Response so the header what we set there that also will come in final response.

---

## Debugging & Testing

Let's hit `/getAllJournalEntries` endpoint and in auth instead of basic auth pass Bearer Token and first generate a token and give expiry for 30 minutes.

![img_32.png](img_32.png)

### 1. First do signup

```json
{
    "userName":"kaupersi",
    "email": "kaupersi@gmail.com",
    "sentimentAnalysis": true,
    "password":"kaupersi12345"
}
```

![img_33.png](img_33.png)

![img_34.png](img_34.png)

### 2. Let's do login and generate a token for 30 minutes

```json
{
    "userName":"kaupersi",
    "password":"kaupersi12345"
}
```

![img_35.png](img_35.png)

![img_36.png](img_36.png)

Now we got the token and just copy that token.

![img_37.png](img_37.png)

### 3. Now we are going to use this token as a Bearer Token and hit `getAllJournalEntriesOfUser()` method endpoint of `JournalEntryController`

![img_38.png](img_38.png)  -- `/journal` - contextPath and another for endpoint of api

![img_39.png](img_39.png)

-- In Authorization Header we got the Bearer Token

![img_40.png](img_40.png)

-- Now we can see in Claims we have subject as an userName, issuedAt and expirationTime

![img_41.png](img_41.png)

-- userDetails loaded from db

![img_42.png](img_42.png)

-- An Authentication Object - `UsernamePasswordAuthenticationToken`

> An `org.springframework.security.core.Authentication` implementation that is designed for simple presentation of a username and password.
> The principal and credentials should be set with an Object that provides the respective property via its `Object.toString()` method.
> The simplest such Object to use is String.

![img_43.png](img_43.png)

-- Now we are setting our auth in `SecurityContextHolder`

![img_44.png](img_44.png)

-- After jwt authentication and our filter it will hit our endpoint

![img_45.png](img_45.png)

-- After hitting api we can see we got an authentication object from `SecurityContextHolder`.

![img_46.png](img_46.png)

-- By username we fetch the data from database

![img_47.png](img_47.png)

-- As of now no entries of this user so data not found

![img_48.png](img_48.png)

![img_49.png](img_49.png)

-- Let's try to add and then try

![img_50.png](img_50.png)

![img_51.png](img_51.png)

![img_52.png](img_52.png)

**jwt expired**

![img_53.png](img_53.png)

So again generate a new token from `/login` endpoint and then use it to create journal entry.

![img_54.png](img_54.png)

-- Let's try to add and then try

![img_55.png](img_55.png)

![img_56.png](img_56.png)

![img_57.png](img_57.png)

![img_58.png](img_58.png)

![img_59.png](img_59.png)

-- Earlier we were getting 404 bcz token expired now we are getting journalEntries Response

![img_60.png](img_60.png)

---

## Enable Basic Auth and Disable JWT

**If you do want to Enable Basic Auth and Disable JWT:**

![img_61.png](img_61.png)

**If user is already signed up now while hitting endpoint in authorization select Basic Auth and pass username and pwd:**

![img_62.png](img_62.png)

![img_63.png](img_63.png)

**We got the response the reason is while we hit endpoint our request will first go to JwTFilter:**

![img_64.png](img_64.png)

So as we are not passing bearer token so no username will found and no jwt token validate.

**So, Always Remember if your jwt expire need to re-generate and then use it.**

![img_65.png](img_65.png)

Now I enabled JWT Token Authorization.

---

## Response Header Demonstration

One thing I want to show you:

```
/**
    just to show once we add response in Header in Filter
    this header will add in httpServletResponse and 
    show in response postman header
**/
```

![img_66.png](img_66.png)

![img_67.png](img_67.png)

![img_68.png](img_68.png)

### Summary of Flow

- **signup**
- **login to generate jwt token**
- **use token as Bearer Token in Authorization**
- **access your any rest-endpoint**

---

## Few Important Points for JWT Authentication/Authorization

SignUp we saw, we saw log in also, log-out you will do from client side, client side mean from the app. So what happen app actually after login it saves token in their local storage from there you can delete. Obviously while you will try these endpoints since token will not be there so, you can't access it.

Since JWT is **Stateless** that's why we are thinking of client side. Because in server side we are not storing jwt.

There are no storage in server side to store token it just checks and verify best way is client side.

### 1st Way - Log out

You can remove jwt token from client-side. Suppose you are login from journal app from front-end then while you are logging out then you can delete that token from local-storage. At your front end token is there then he can access it all endpoints which mean he logged in, if you remove from local storage then no one can access it.

### 2nd Way - Save in Redis & Blacklist

You can save the token in redis for blacklisting tokens.

So before checking expiry we can check: **is this token is blacklisted?**

