/**
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

const functions = require('firebase-functions');
const {smarthome} = require('actions-on-google');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();
const settings = {timestampsInSnapshots: true};
db.settings(settings);

const app = smarthome({
  debug: true,
  key: require('./api-key.json').key,
  jwt: require('./service-key.json'),
});

const {AuthenticationClient} = require('auth0');
const auth0 = new AuthenticationClient(require('./auth0-config.json'));

const getUser = async (headers) => {
  // Authorization: "Bearer 123ABC"
  const accessToken = headers.authorization.substr(7);
  const {email} = await auth0.getProfile(accessToken)
  // {
  //   sub: 'google-oauth2|1234567890'
  //   email: 'example@email.com'
  // }
  // Look up this email in our database
  console.log(accessToken, email);
  const emailDoc = await db.collection('emails').doc(email).get();
  const emailData = emailDoc.data();
  return emailData.firebaseId
}

app.onSync(async (body, headers) => {
  const userid = await getUser(headers)
  // Check the userid already exists
  const userDoc = await db.collection('users').doc(userid).get();
  if (!userDoc.exists) {
    console.error(`User ${userid} has not created an account, so there are no devices`)
    return {};
  }

  // Enable smart home for this user
  await db.collection('users').doc(userid).update({
    smartHome: true
  })

  const userdevices = db.collection('users').doc(userid).collection('gizmos');
  const snapshot = await userdevices.get()
  const devices = []
  snapshot.forEach(doc => {
    const data = doc.data()
    const device = {
      id: doc.id,
      type: data.type,
      traits: data.traits,
      name: {
        defaultNames: data.defaultNames,
        name: data.name,
        nicknames: data.nicknames,
      },
      deviceInfo: {
        manufacturer: data.manufacturer,
        model: data.model,
        hwVersion: data.hwVersion,
        swVersion: data.swVersion,
      },
      willReportState: false,
      attributes: {}
    }
    if (data.traits.indexOf('action.devices.traits.Toggles') > -1) {
      // Attributes for toggles
      device.attributes.availableToggles = [];
      for (const toggle of data.toggles) {
        device.attributes.availableToggles.push({
          name: toggle.toggleId,
          name_values: [{
            name_synonym: [toggle.displayName],
            lang: "en"
          }],
        })
      }
    }
    devices.push(device)
  })

  const payload = {
    requestId: body.requestId,
    payload: {
      agentUserId: userid,
      devices: devices,
    },
  };

  return payload
});

const queryDevice = async (userid, deviceId) => {
  const devicestates = db.collection('users').doc(userid).collection('gizmos').doc(deviceId)
  const doc = await devicestates.get()
  return doc.data().states
}

app.onQuery(async (body, headers) => {
  const userid = await getUser(headers)
  const {requestId} = body;
  const payload = {
    devices: {},
  };
  for (const input of body.inputs) {
    for (const device of input.payload.devices) {
      const deviceId = device.id;
      const data = await queryDevice(userid, deviceId)
      payload.devices[deviceId] = data
    }
  }
  // Wait for all promises to resolve
  return {
    requestId: requestId,
    payload: payload,
  }
});

app.onExecute(async (body, headers) => {
  const userid = await getUser(headers)
  const {requestId} = body;
  const payload = {
    commands: [{
      ids: [],
      status: 'SUCCESS',
      states: {
        online: true,
      },
    }],
  };
  for (const input of body.inputs) {
    for (const command of input.payload.commands) {
      for (const device of command.devices) {
        const deviceId = device.id;
        const deviceStates = db.collection('users').doc(userid).collection('gizmos').doc(deviceId)
        const deviceDocument = await deviceStates.get();
        const deviceData = deviceDocument.data();
        payload.commands[0].ids.push(deviceId);
        for (const execution of command.execution) {
          const execCommand = execution.command;
          const {params} = execution;
          switch (execCommand) {
            case 'action.devices.commands.OnOff': {
              await deviceStates.update({
                'states.on': params.on
              })
              payload.commands[0].states.on = params.on;
              break;
            }
            case 'action.devices.commands.SetToggles': {
              // Each value will be a boolean to be on or off
              for (const toggle of Object.keys(params.updateToggleSettings)) {
                const toggleValue = params.updateToggleSettings[toggle];
                await deviceStates.update({
                  'states.currentToggleSettings': {
                    [toggle]: toggleValue
                  }
                })
                // Identify the toggle index and update the field
                const toggleArray = deviceData.toggles;
                for (let i = 0; i < toggleArray.length; i++) {
                  if (toggleArray[i].toggleId === toggle) {
                    deviceData.toggles[i].on = toggleValue;
                    await deviceStates.update(deviceData);
                  }
                }
              }
              break;
            }
          }
        }
      }
    }
  }
  return {
    requestId: requestId,
    payload: payload,
  };
});

app.onDisconnect(async (body, headers) => {
  const userid = await getUser(headers)
  // User is disconnecting from smart home
  // Disable the flag
  await db.collection('users').doc(userid).update({
    smartHome: false
  })
})

exports.smarthome = functions.https.onRequest(app);

const isSmartHomeEnabled = async (userId) => {
  // Check that smart home is enabled
  const userDocument = await db.collection('users').doc(userId).get();
  const {smartHome}  = userDocument.data();
  if (!smartHome) {
    console.warning(`User ${userId} is not connected to smart home`)
  }
  return userDoc.smartHome
}

exports.onDeviceCreate = functions.firestore
    .document('users/{userId}/gizmos/{deviceId}')
    .onCreate(async (snap, context) => {
      const performAction = await isSmartHomeEnabled(context.params.userId)
      if (!performAction) return {}

      // User has added a new device
      // Resync the device list
      console.info(`New device ${context.params.deviceId} created for ${userId}`)
      return app.requestSync(userId)
    });

exports.onDeviceDelete = functions.firestore
    .document('users/{userId}/gizmos/{deviceId}')
    .onDelete(async (snap, context) => {
      const performAction = await isSmartHomeEnabled(context.params.userId)
      if (!performAction) return {}

      // User has removed a device
      // Resync the device list
      console.info(`New device ${context.params.deviceId} deleted for ${context.params.userId}`)
      return app.requestSync(context.params.userId)
    });

exports.onDeviceStateUpdate = functions.firestore
    .document('users/{userId}/gizmos/{deviceId}')
    .onUpdate(async (change, context) => {
      const performAction = await isSmartHomeEnabled(context.params.userId)
      if (!performAction) return {}

      // Report the state for this device
      const userId = context.params.userId
      const deviceId = context.params.deviceId
      const deviceStates = await queryDevice(userId, deviceId)
      console.info(`State changed for device ${context.params.deviceId}`)
      return app.reportState({
        requestId: '123ABC', // Can be any identifier
        agentUserId: userId,
        payload: {
          devices: {
            states: {
              [deviceId]: deviceStates
            }
          }
        }
      })
    });