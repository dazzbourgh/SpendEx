# Plaid implementation

## Task

Implement actual authorization with Chase bank via Plaid. We need to:

1. Send a request to link token endpoint with our app client id and secret (can be found at `/Users/leonidborisevich/.spndx/app-data-sandbox.json`). Redirect URL can also be found in that file under `redirect_url` field for later steps
2. Starts a process with default system browser opening `https://cdn.plaid.com/link/v2/stable/link.html?token=<link_token>` URL, where `link_token` is taken from step 1
3. Spin up a temporary minimal HTTP server locally to accept redirect and wait for user to authorize the app. The port must be the same as in `redirect_url` from step 1
4. Extract access token for this item from the redirect sent to our temporary server
5. Shut down the temporary server and save the token in `~/.spndx`

Use our DAO interfaces to persist data to disk (add more if needed), use our service interfaces to encapsulate Plaid specific logic.

Make a plan first and write it to file. Think very hard.
