const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.pushNotification = functions.database.ref('/notifications/{pushId}').onWrite( (change, context) => {

    console.log('Push notification event triggered');

    //  Grab the current value of what was written to the Realtime Database.
    var valueObject = change.after.val();
    console.log('event value is '+valueObject);

    const payload = {
        data: {
            req_id: valueObject.req_id,
            req_name: valueObject.req_name,
            req_num: valueObject.req_num,
            btype: valueObject.btype,
            pints: valueObject.pints,
            req_msg: valueObject.req_msg,
            req_address: valueObject.req_address
        },
    };

    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };

    return admin.messaging().sendToTopic("notifications"+"_"+valueObject.country_code, payload, options);
});