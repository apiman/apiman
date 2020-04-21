# header-allow-deny-policy

A policy that permits or denies requests matching certain HTTP headers.

The policy allows the user to control which incoming requests are permitted to be passed on to the back-end service. Permission is granted by adding entries for a header.

## How it works

On receiving a request, the policy examines the HTTP headers, then applies the configured rules using a regular expression against the names and values. If these are permitted, the request is passed to the back-end API unmodified. If not, an HTTP 403 response is returned and the call to the back-end service is not made.

## Author

Pete Cornish <outofcoffee@gmail.com>
