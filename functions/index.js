/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const serviceAccount = require("./service-account.json");

// Initialize Firebase Admin with service account
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// HTTP triggered function
exports.sendNotification = functions.https.onRequest(async (req, res) => {
    try {
        const token = req.body.token; // FCM token from Android app
        const title = req.body.title || "Hello Rituraj!";
        const body = req.body.body || "This is a test message.";

        if (!token) {
            res.status(400).send("FCM token is required");
            return;
        }

        const message = {
            notification: { title, body },
            token: token
        };

        const response = await admin.messaging().send(message);
        console.log("Successfully sent message:", response);
        res.status(200).send("Notification sent: " + response);

    } catch (error) {
        console.error("Error sending message:", error);
        res.status(500).send(error.toString());
    }
});
