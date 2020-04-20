# url-whitelist-policy

A policy that only permits requests matching a whitelist.

The policy allows the user to control which incoming requests are permitted to be passed on to the back-end service. Permission is granted by adding whitelist entries for a URL. Individual HTTP methods can also be allowed or denied per whitelist entry.

## How it works

On receiving a request, the policy normalises the incoming URL, then applies the configured rules using a regular expression against the normalised URL. If both URL and HTTP method are permitted, the request is passed to the back-end API unmodified. If not, an HTTP 403 response is returned and the call to the back-end service is not made.

## Author

Pete Cornish <outofcoffee@gmail.com>
