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

const app = smarthome({
  debug: true,
});

app.onSync(async (body, headers) => {
  const userid = headers.authorization.substr(7);
  const userdevices = db.collection('users').doc(userid).collection('devices');
  const snapshot = await userdevices.get()
  const devices = []
  snapshot.forEach(doc => {
    const data = doc.data()
    devices.push({
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
    })
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
  const devicestates = db.collection('users').doc(userid).collection('devices').doc(deviceId)
  const doc = await devicestates.get()
  console.log(doc.data().states)
  return doc.data().states
}

app.onQuery(async (body, headers) => {
  const userid = headers.authorization.substr(7);
  const {requestId} = body;
  const payload = {
    devices: {},
  };
  const queryPromises = [];
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
  const userid = headers.authorization.substr(7);
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
        const devicestates = db.collection('users').doc(userid).collection('devices').doc(deviceId)
        payload.commands[0].ids.push(deviceId);
        for (const execution of command.execution) {
          const execCommand = execution.command;
          const {params} = execution;
          switch (execCommand) {
            case 'action.devices.commands.OnOff':
              await devicestates.update({
                'states.on': params.on
              })
              payload.commands[0].states.on = params.on;
              break;
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

exports.smarthome = functions.https.onRequest(app);