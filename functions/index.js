// functions/index.js  (CommonJS + API v2)
const admin = require('firebase-admin');
admin.initializeApp();

const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { setGlobalOptions } = require('firebase-functions/v2');

setGlobalOptions({ region: 'us-central1', memory: '128MiB' });

exports.addDriverRole = onDocumentCreated('drivers/{uid}', async (event) => {
  const uid = event.params.uid;
  await admin.auth().setCustomUserClaims(uid, { role: 'driver' });
  console.log(`Driver claim set for ${uid}`);
});