# Pizza Pay Backend Application

This codebase contains an example of a backend used for a Nexi SoftPOS application.

The application can be started following the `mvn spring-boot:run` command after updating the `./src/main/resources/application.yaml` file.

The main capabilities of this backend are listed below.

## Login

The Pizza Pay app requires a user to be used.
The user is managed by the application and can require a new session using the `POST /pizzapay/api/v1/user/login` API.

Users will be authenticated without interaction with Nexi's backend. The application can choose any flow to accomplish this. In the general case is expected that there is already a login system for the customer app.

## Expose configuration for the Android App

Once the user is authenticated the android app must obtain some parameter to bootstrap the SDK.

Using the `POST /pizzapay/api/v1/android-app-config/obtain` API the android app can obtain:

- Client ID
- Point of sale code
- Terminal ID for SoftPOS

The client id is provided in the onboarding process by Nexi. In our example we provide the Client ID via API to be able to change the value only in one place (the backend).

Point of sale and Terminal ID are provided by Nexi in the web portal for the merchant and must be associated with the user and the device.

Each pair of Point of sale code and Terminal ID can be used only by one user at one time. Changing the pair will trigger a new pairing in the SoftPOS app itself.

In our example we will fetch the first idle pair from the configuration file and assign those to the user. _We are assuming that one user will use only one device at each point in time_. Working with multiple device at once may require additional work to pair the Terminal ID to the proper device.

This process is done without any calls to the Nexi's backend.
The application can chose how to pair the Terminal ID.
Our example is: a basket of Terminal IDs randomly assigned to a user.

## PAR request

The last function exposed by the backend is the PAR request.

The SDK requires request_uri to perform the payment process, this value is obtained using the `POST /pizzapay/api/v1/par/execute` API.

The backend will connect to Nexi using mTLS, will fetch JWKs used for JWE encryption and then send the PAR API call to Nexi in order to obtain the request_uri.

This is the only point where the application will call the Nexi system.

This interaction will require the use of a bearer token obtained via OAuth2 using client id/secret provided by Nexi in the onboarding process.

Warning: The OAuth2 has an expiration, the system must cache the token until expiration.

The PAR API call to Nexi MUST be executed via backend to keep the private key and the client secret not exposed. Exposing this security quantities will compromise the system security.
