# Setup

## `auth0-config.json`

You will need to create a file called `auth0-config.json` in this directory.
It is used to setup the APIs for Auth0. Make sure it fits the format below.

```
{
    "clientId": "<clientid>",
    "domain": "<domain>.auth0.com"
}
```

## HomeGraph APIs
### Request Sync
The Request Sync feature allows a cloud integration to send a request to the Home Graph
to send a new SYNC request.

1. Navigate to the
[Google Cloud Console API Manager](https://console.developers.google.com/apis)
for your project id.
1. Enable the [HomeGraph API](https://console.cloud.google.com/apis/api/homegraph.googleapis.com/overview). This will be used to request a new sync and to report the state back to the HomeGraph.
1. Click Credentials
1. Click 'Create credentials'
1. Click 'API key'
1. Copy the API key shown and insert it in a file called `api-key.json`.

```
{
    key: '<api key>'
}
```

To use it, add a new device while the sample is active.

#### Report State
The Report State feature allows a cloud integration to proactively provide the
current state of devices to the Home Graph without a `QUERY` request. This is
done securely through [JWT (JSON web tokens)](https://jwt.io/).

1. Navigate to the [Google Cloud Console API & Services page](https://console.cloud.google.com/apis/credentials)
1. Select **Create Credentials** and create a **Service account key**
1. Create the account and download a JSON file.
   Save this as `service-key.json`.

The sample already includes support for report state. To use it, create a device
in the web frontend. Then click on the arrow icon in the top-right corner. It will
start reporting state when the state changes locally.