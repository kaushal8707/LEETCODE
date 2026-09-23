# How OAuth 2.0 Works

## OAuth2 | Implementation in a Spring Boot Project

Suppose we have some files in our Google Drive and any third-party apps which wants to access it, then how can you give an access? Will you give your username and password? No. Then what you will do — you will do **Sign In with Google** and give the access. So there is no need to give the username and password and your file sent to that third-party app. So this is called **OAuth2**.

**OAUTH2** — OAuth2 allows third-party applications to access certain parts of your account **without giving your credentials**, and whenever you want you can revoke an access.

---

## Example

When we go to any account and click on create an account or we do signup, so one way is you have to fill all the details and do signup, and another way is Sign In with Google/GitHub/Facebook.

![img.png](img.png)

Suppose LeetCode is a website. Suppose your credentials is in Google and I want to login with the help of Google. My account is in Google. Then whose responsibility is to authorize me? **Google**.

So, here LeetCode is a website, a third-party or a **Client** for Google. Who is authorizing it? Google. Now our request — where will it go? Our request will go to the Google server which we call **Authorization Server**.

- I do Sign In with Google, then LeetCode will go to the Google Authorization Server and asking few details about Google account like name/email.
- Google authorization server will give the token when we will allow.
- With the help of that token LeetCode can access our data — he accesses some specific data which we allowed and that token will have limited permission and expiration.

---

## Open Authorization 2

![img_1.png](img_1.png)

OAuth 2 is a **Protocol of Authorization** where some set of Rules and Guidelines to implement OAuth 2. So, OAuth2 is an **open** — anyone can implement, none of the companies own OAuth2. That's what we call it **OPEN**.

---

## Complete Flow — OAUTH2

![img_2.png](img_2.png)
![img_3.png](img_3.png)

Suppose we want to access a LeetCode/Client but you do not want to create an account, you want to create an account with the help of Google or Facebook. That one we are calling an **Authorization Server** which will be responsible for an Authorization.

### Step 1: I went to a Client

### Step 2: Client will redirect to the Authorization Server

When we do Sign In with Google, so it will redirect to the Google account and what all details will be present.

Which means when we do Sign In with Google, it will have:
- **Client Id** — in a request to identify who is coming to access it.
- **Redirect URL** — when authorization server will authorized then where it will go, this also we are sending in a request.
- **Scope** — what all things LeetCode can access. For example in case of LeetCode need only email.
- **Response Type** — which means which type of response type Client needs from authorization server.
- **State** — will have a unique value which Client sends to auth server and when auth server sends back to Client this unique value also goes there, through which it checks response is coming from auth server.

Let's do Sign in with Google.

![img_4.png](img_4.png)

Now LeetCode has sent us to a Google account.

![img_5.png](img_5.png)

And this time the URL which we are having is details with a Client Id, redirect Url, scope, response Type and state.

![img_6.png](img_6.png)

Now our account is ready, so now our authorization server has redirected to the LeetCode so our account has been created.

![img_7.png](img_7.png)

So now LeetCode has assigned some random username and there is no meaning of the password because we have logged in using Google. So how much convenient and secure it is.

![img_8.png](img_8.png)

### Step 3: Grant a Permission

We have to grant a permission and we have done that.

### Step 4: Allowing consent screen and Client will get Authorization Code

When we click on approve or continue on a consent screen where it was asking "are you allowing LeetCode to access Google account", when we grant it authorization server will send a user to a Client where it mentioned in redirect URL parameter. Now LeetCode has received one **authorization Code**.

![img_9.png](img_9.png)

### Step 5: LeetCode will send a POST Request to a Google Token Endpoint

Now LeetCode will send a POST Request to Google, because LeetCode is not having any data in presents — he is having only the authorization Code. Now this POST request will go to the Google's token endpoint where in response we will get **access token**. With the help of that access token LeetCode will be able to access Google account limited data.

![img_10.png](img_10.png)

We went to a LeetCode, we did continue with Google. When we do continue with Google these all data like Client Id, redirect Url, scope, response Type and state — sent to an authorization server. Authorization server has given us a Code (authorization Code) when we granted it. Now till this point LeetCode is having only authorization Code nothing else. Then with the help of that Code, LeetCode has hit to one of the endpoint — he has called one Google's token endpoint so that he can get an access token, with the help of that access token he can get the details.

In this POST Request will be:
- Code
- Client Id
- Client Secret
- Redirect URL

And redirect URL should be same in both the requests, Google will check. Google will verify the Code. Using combination of Client id and Client Secret, it will check is this request actually coming from LeetCode, and redirectURI it will verify with earlier one is same or not. If everything is correct then in response it will send **access token**.

![img_11.png](img_11.png)

Now LeetCode will go to Google again. Now there are so many Google APIs to get user info, so with the help of access token he will get the specific details within the scopes like a username or email or profile picture, and once he got the details he will create an account at their end.

![img_12.png](img_12.png)

Access Token will be having some expiry. If inside a response **Refresh Token** also comes, then LeetCode will take this Refresh Token and go to the Google and he will generate a new Access Token. So again you do not have to logged in — account has been already created and you do not have to log in again. So sometime you saw you did Sign In with Google and for a long term it gets logged in, he is never asking to login again because he is having a **REFRESH TOKEN**.

> \*\*\* We saw in case of LeetCode in response type is: **Code**. So as we saw in our flow, first we get authorization Code then we get access token. So in this type of flow response type will always have `Code`, but there is some other flow as well where response type value can have `token`, where authorization server directly returns access key to the Client — so there will be some security risks and usecases.

![img_13.png](img_13.png)

- We went to LeetCode.
- We said continue with Google.
- LeetCode sent us to authorization server (with Client Id, redirect Url, scope, response Type and state).
- LeetCode will get an authorization Code.
- LeetCode went to Google `/token` endpoint along with auth Code.
- LeetCode will receive an access token/refresh token.
- LeetCode went to Google that endpoint `/profile` where user details presents along with access token.
- LeetCode took that required info which was in scopes.
- And with the help of that he has created a user.

### Why we are having 2 steps?

Because first request initiated through a Browser (because we saw that complete URL), but where it get redirected — it redirected to LeetCode secure server. So that second request go from there which will be more secure because Client Secret also there on their server — the things get exposed probability is less.

---

## Integrate OAuth2 with our Journal App

![img_14.png](img_14.png)

We saw LeetCode is like a Client here — same like our Spring Boot Journal app will also be a Client.

- Here in step 3 we were sending Client Id and in step we were sending Client Secret, so that Google identify the LeetCode Client that this is the LeetCode.
- Which means whenever any application want to use AUTH Server, then he must have to get registered there.

When we were creating an account in LeetCode there coming Sign In with Google/GitHub/Facebook, which means LeetCode must get registered in all these 3 sites so that he can get uniquely identified. So every authorization provider should must know from where request is coming, who is he.

---

## Important Points Before Implementation

- Now we have created a Spring Boot project. Till now we were creating an account with username and password, even login also with username and password.
- But now I want with the help of Google to create an account, so I am a new application, so I have to go to Google to please identify me. So how it will identify — with the help of **Client Id** and **Client Secret**.
- So, what I have to do is I have to go to Google — there may be some website — and our Journal Application need to get registered with them. So they will provide us a Client Id and Client Secret and only that details we need, that's it.

---

## Coding Starts Here — Actual Implementation

If you want to add Sign In with Google or Sign In with GitHub functionality, so the party who is going to authorize your application — you must have to register your app there.

**Authorization Server should know who is using his service.**

Go to - `XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_15.png](img_15.png)

Here we are going to register our app so that in future we can use Sign In with Google.

Go and create a project — `journalApp`.

![img_16.png](img_16.png)

Go to **API and Services** -> **OAuth Consent Screen**.

![img_17.png](img_17.png)

![img_18.png](img_18.png)

**External user**

![img_19.png](img_19.png)

Here Client means our app.

![img_20.png](img_20.png)

**Consent Screen** — when you are on LeetCode and clicked on Sign In with Google, then there must write like these all details you are going to share with, which will be visible to users.

- Go to **audience tab** -> **publish app** -> **confirm**.
- Go to **data access tab** -> **add or remove scopes** -> tick first 3 and then **update** and then **save it**.

![img_21.png](img_21.png)

- Go to **verification centre**.
- Go to **Clients** -> **Create a Client**.

![img_22.png](img_22.png)

![img_23.png](img_23.png)

---

## 10 Steps While We Do Login with Google

1. **Step 1** — While we click on Login with Google.
2. **Step 2** — We get redirected to the Google login page with parameters (Client_id, scopes, etc.).
3. **Step 3** — If not logged in then we have to login to Google account.
4. **Step 4** — One consent screen will come and ask will you be approve or allow to share.
5. **Step 5** — One authorization Code (Auth Code) Google will give to our app.
6. **Step 6** — Our front-end will receive Auth Code.
7. **Step 7** — Now front-end will send this Code (auth-Code) to backend. Code is there with LeetCode server like Secret and all, so that is the backend not our browser.
8. **Step 8** — Now backend will send this Code and Secret to Google.
9. **Step 9** — Backend will receive an access token.
10. **Step 10** — Backend will validate that token and grant access to the user.

> \*\* Now we do not have a frontend and we are also not interested to create it now, then how will we do that?
> \*\* We will do with the help of `XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`.
> \*\* This **OAuth Playground** work like our front-end.

![img_24.png](img_24.png)

> \*\* First we need to select a scope, means what all we want to share.

So input your own scope — `XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx`

![img_25.png](img_25.png)

As soon as when I click on **Authorize API** then flow will be same. It will ask you with which Google account you do want to Sign In.

- When I click on Authorize API, then with this scope (`https://www.googleapis.com/auth/userinfo.email`) we are going to Google means to the Google Authorization Server, means Playground is going to Google Authorization Server.
- Playground will say this is our scope and please give us an Authorization Code.

Before that we will go to the **Settings** and tick **Use your own OAuth Credentials**.

![img_26.png](img_26.png)

So we can copy ClientId and Client Secret.

![img_27.png](img_27.png)

![img_28.png](img_28.png)

When we click on **Authorize API** then same request will be going how it was going from LeetCode, so when we were clicking on login with Google then what all details were going like ClientId, scope, redirectURL etc...

When we clicked on Authorize API it is showing **Access Blocked!!**

![img_29.png](img_29.png)

This is happening because in our Client we have not given Re-Direct URI, because our Playground is treating like an application so it should be get registered.

![img_30.png](img_30.png)
![img_31.png](img_31.png)

Now clicked on **Authorized API**.

![img_32.png](img_32.png)

![img_33.png](img_33.png)
![img_34.png](img_34.png)

When we clicked on **Continue**...

![img_35.png](img_35.png)

Now we got the **Authorization Code**.

![img_36.png](img_36.png)
![img_37.png](img_37.png)

Now there will be 2 things — from here itself you can exchange Code with Token, or take this access token and refresh token both at backend, that will be more secure. What we discussed earlier — that frontend which is currently playground app will give only authorization Code to the backend and Backend will do the entire work automatically.

### So, There are 2 ways

- **Way -1**: From here itself you got the access or refresh token and send this token to the Backend, which will be less secure — because here at your front end token came which is local, there token occurred which is less secure which we saw earlier.
- **Way -2**: You received the Authorization Code and send this Code to your Backend and Backend will exchange automatically.

We are going to Apply more secure way. For simplicity, we can use this generated token and send this to the backend and our backend will do everything that is happening, but we are going to apply a more secured way. So, from FrontEnd we will get only Authorization Code and all communication to and fro will happen in backend.

---

## OAuth Implementation & Coding

### Create a Controller & callback URL

We are going to create a controller and we will define an endpoint where we will be passing this authorization-Code that will be get exchanged with an access-token.

![img_38.png](img_38.png)

Basically what will happen is, since now we are not having any frontend, so manually we will copy authorization Code and from Postman we will hit that endpoint which we are going to create. But in future when we will create a front end, then we do not have to do anything — till authorization Code generation front end will do what we have done till now, and this authorization Code front end will take and again he will hit this endpoint controller which we are creating now.

How it was happening in LeetCode — same it is happening here. LeetCode was going to Google and Google will be redirected to an endpoint where we were sending Authorization Code. So, same it is happening here — to us authorization Code will come and then we will redirect to our controller endpoint which I am going to create now.

So, we are telling to Google once User get logged in then send that authorization Code at this endpoint, because of this we call this as a **callback URL** because Google calls again this URL with `authorization_Code`.

First of all we have to exchange authorization Code with token. So we saw earlier while clicking on Exchange Auth Code for Token then what Playground do is he sent one API, along with API he send auth Code, auth Client, secret etc. So through Code we can send. Which endpoint Playground was using — which we will also use in our Code — we can find out in our documentation.

We have created a HashMap. We will send Code, ClientId, Client Secret, redirectUri — which URI we sent during authorization Code, which we already registered, same you have to send for security purpose because Google will verify that either it is coming from same Client.

We have given `grant_type` `authorization_Code` which means you are telling you have an Authorization Code and you want to exchange it from tokens. Even you can use `refresh_token` also — your token get expired and you do want to refresh your token.

![img_39.png](img_39.png)

This ClientId and Client Secret you can do hardCoded also, but we are going to configure in our `.yml`.

> \*\* Now we have to hit this URL - `"XXXXXXXXXXX"`, so we will use `RestTemplate`. We will send headers as well and in header we will tell to Google which type of Request we are going to send. In header we set content type `APPLICATION_FORM_URLENCODED`. So how we fill some application form, in same way we are sending request and as it is mentioned in documentation like this.

> \*\* Then as a response we will get `tokenResponse` and from token response we will retrieve the TOKEN and with the help of this token we have to hit the Google's information API, so we are sending access token and with the help of this we will get the user information.

![img_40.png](img_40.png)

Again with the help of `RestTemplate` we will hit this API - `"XXXXXXXXXXXXXXXXXXXXXXXXXX"` and we will get and store in `userInfoResponse` object. If status Code is OK then take email from body.

![img_41.png](img_41.png)

> \*\* From token we took an email of a user and we will check in our database with that email id user exists or not. If exists then it is ok. If not exists then we will create a user.

So let's create an object for `UserDetailsServiceImpl` and load user by user name and we will pass email instead of username.

![img_42.png](img_42.png)

![img_43.png](img_43.png)

Password random generated because we are Sign In with Google.

![img_44.png](img_44.png)

![img_45.png](img_45.png)

![img_46.png](img_46.png)

> \*\* That's it we have saved the user.
> \*\* We already save the user, and let's get userdetails again so that we can save into a context. Let me explain you once again.
> \*\* In the Spring Context, we want to set Authentication for that we need an object of `UsernamePasswordAuthenticationToken`.

![img_47.png](img_47.png)

Now this `/auth/google/callback` we need to allow in our Spring Security config.

We have setted ClientId and Client Secret in Environment Variables.

---

## Testing

![img_49.png](img_49.png)
![img_48.png](img_48.png)

Environment Variable - `XXXXXXXXXXXXXXXXXXXXXXX`

![img_50.png](img_50.png)

Our app is started, now go to Postman -

`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_51.png](img_51.png)

![img_52.png](img_52.png)
![img_53.png](img_53.png)
![img_54.png](img_54.png)

Restarted app in debug mode.

![img_55.png](img_55.png)

```
org.springframework.web.Client.HttpClientErrorException$BadRequest: 400 Bad Request: "{
  "error": "invalid_grant",
  "error_description": "Bad Request"
}"
```

Re-generate authorization Code:

`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_56.png](img_56.png)

![img_57.png](img_57.png)

![img_58.png](img_58.png)

![img_59.png](img_59.png)

```
ClientId: XXXXXXXXXXXXXXX

Client Secret: XXXXXXXXXXXX

authorizationCode: XXXXXXXXXXXXXXXXXXXXX
```

![img_60.png](img_60.png)

We have received the **Token Response**.

![img_61.png](img_61.png)

![img_63.png](img_63.png)

![img_62.png](img_62.png)

Then we are passing the `id_token` and preparing the endpoint to get the user info.

`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_64.png](img_64.png)

We got the user **InformationResponse** details.

![img_65.png](img_65.png)

From `userInfoResponse` we are retrieving an email, and we are checking is this present in database or not, so we are getting `UserNotFound` Exception.

Below line throws an error — **UserNotFound in database**:

```java
UserDetails userDetails = userDetailsService.loadUserByUsername(email);
```

```
org.springframework.security.core.userdetails.UsernameNotFoundException: User not found with username - oauth1291@gmail.com
```

![img_66.png](img_66.png)

The reason is because we have created a new gmail id which is not registered with our MongoDB.

So, let's do some Code change:

![img_67.png](img_67.png)

What we did is: If a User is not found into a database then create a User.

![img_68.png](img_68.png)

Now we are getting exception 400-Bad Request, let's re-generate the authorization Code and use in our Postman Request.

![img_69.png](img_69.png)

![img_70.png](img_70.png)

`auth Code - XXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_71.png](img_71.png)

Now when user not found, going in catch block we created a user and then saving into db and then loading user details from db, and below is the user details, and then we are setting in our Spring Context.

![img_72.png](img_72.png)

Now when user not found going in catch block we created a user and then saving into db and then loading user details from db and below is the user details and then we are setting in our Spring Context.

We can see **200: OK** came to our Postman.

![img_73.png](img_73.png)

---

## Important Points

What shall we do with this OK Response? Because in our app there are no sessions (JWT), we have kept everything Stateless.

> \*\* Actually at this place we should have to generate a **JWT Token** and we should use in our further REQUESTS.

![img_74.png](img_74.png)

> \*\* We will call `jwtUtils` to generate Token using email and we will give map as a response back as a token and jwtToken.

![img_75.png](img_75.png)

Let's restart and test.

So let's regenerate the Authorization Code first.

![img_76.png](img_76.png)

`auth Code - XXXXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_77.png](img_77.png)

We will use in Postman..

![img_78.png](img_78.png)

![img_79.png](img_79.png)

![img_80.png](img_80.png)

![img_81.png](img_81.png)

We can see we will give a Code here — basically Frontend will give an authorization Code and Front End will get a JWT Token and then the Front End will save that generated token for use it further. So this is our secure way.

In our DB we can see our new OAuth user entry.

![img_82.png](img_82.png)

---

## Final Journal APP Testing

Now no need to do anything, just add one new Entry. Let's use this token in our app to create an entry.

Generate JWT Token using Authorization Code:

![img_81.png](img_81.png)

In Postman **Auth** tab select **Bearer Token** and give your generated Token and use your API.

![img_83.png](img_83.png)

![img_84.png](img_84.png)

![img_85.png](img_85.png)

![img_87.png](img_87.png)

![img_88.png](img_88.png)

![img_89.png](img_89.png)

![img_90.png](img_90.png)

In db we can see one password generated, that is the random generated password from our Code:

`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

![img_91.png](img_91.png)

Because when user not found, we are creating a new user with `uname as email` and `email as email` and `pwd random generated`.

![img_92.png](img_92.png)

---

## Entire Work Flow — Let's See a Complete Flow Once Again

- This will be the **FrontEnd** for us — Playground — `XXXXXXXXXXXXXXXXXXXXXXXXXXXXX`
- This went to a Google Auth Server and brought back **Authorization Code**.
- We have given to our **Spring Boot Application**.
- Given us a **JWT Token** corresponding to that User.
- How earlier we were creating JWT Token using username and password.
- Earlier we were retrieving JWT Token from username and password — same here.
- Same here after completing the OAUTH flow we are retrieving JWT Token.

So in future if we will create any frontend, then what we will do is we will Google API endpoint below one:

`XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX`

So frontend will hit auth API, in which we will put `redirect_URI = /auth/google/callback` controller which we have created now.

We are sending this so that Google can understand the authorization Code will get generated where he has to send.

Which means Google will call this callback URL with authorization Code — in other words Google will call our backend.

And then in backend our complete Code under our controller will got run.

Finally, he will give **JWT Token** to our Front End. That's it.

![img_95.png](img_95.png)

![img_93.png](img_93.png)

![img_94.png](img_94.png)

![img_96.png](img_96.png)

---

## Let's Understand One More Thing — Should Be Clear

In Playground happening something different — Playground is front-end and Playground was receiving authorization Code, which means authorization Code was going to front end and then we were taking authorization Code, and that authorization Code we were using through the Postman.

**But**

In Real World FrontEnd will hit the `/auth` API which now Playground was doing it, and then Google will call this `/auth/google/callback` URL — that's why the name is **callback URL** — Google will call this with authorization Code then control will come to our controller with authorization Code, all Code will run under our controller and then return JWT to our frontend.

So, you can think how backend is going to send to the frontend, because this call is initiated by a Google, so we can put redirect URL of our frontend.

In the future, we can add one more **Authentication Provider** like how `/auth/google` we can create `/auth/github` or `/auth/facebook`, but the **core concept will remain the same**.

![img_97.png](img_97.png)

So here we got JWT and we can use in a further Request, and obviously after an hour it gets expired so again we have to login. So now in our Spring Boot app there is 2 ways to login — using **username/password** and using **OAUTH2**. In both the ways we received JWT and for next further Request we were sending generated JWT.

Earlier **OAUTH 1** was there which was more complex to implement because there each and every Request will need get Signed with Secret which was complex.

