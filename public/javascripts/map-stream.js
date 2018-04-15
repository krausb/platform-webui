///////////////////
// INIT BASE TYPES
///////////////////

var Session = function (d, s, initialLat, initialLon, map) {
    this.currentMap = map;
    this.deviceId = d;
    this.sessionId = s;
    this.polyline = L.polyline([[initialLat, initialLon]], {color: 'red'}).addTo(this.currentMap);
    this.frontMarker = L.marker(L.latLng(initialLat, initialLon), {draggable: false}).addTo(this.currentMap);
}

Session.prototype.addPolylinePoint = function (lat, lon) {
    console.log("Adding point to polyline: " + lat + ", " + lon)
    this.polyline.addLatLng(L.latLng(lat, lon));
};
Session.prototype.setFrontMarkerPos = function (lat, lon) {
    console.log("Setting front marker position: " + lat + ", " + lon)
    this.frontMarker.setLatLng(L.latLng(lat, lon));
};

var Sessions = function() {
    this.activeSessions = [];
    this.currentMap = undefined;
}
Sessions.prototype.sessionKey = function (d, s) {
    return d + "-" + s;
};
Sessions.prototype.currentMap = function() {
    return this.currentMap;
};
Sessions.prototype.setMap = function(map) {
    this.currentMap = map;
};
Sessions.prototype.activeSessions = function(){
    return this.activeSessions;
}

var sessions = new Sessions();

$( document ).ready(function() {

    /////////////////
    // INIT MAP
    /////////////////

    sessions.setMap(new L.Map('map').setView([48.7682249, 11.6190613], 13));

    // add an OpenStreetMap tile layer
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(sessions.currentMap);

    new L.Control.Scale({maxWidth: 800, metric: false}).addTo(sessions.currentMap);
    new L.Control.Scale({maxWidth: 400, metric: false}).addTo(sessions.currentMap);

    /////////////////
    // INIT Websocket
    /////////////////

    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
    } else {
        console.log("WebSocket NOT supported by your Browser!");
        return;
    }

    var url = $('#data-stream-script').attr("data-url");
    var connection = new WebSocket(url);

    connection.onopen = function() {
        $('#wsState').html('-> connected')
    };
    connection.onerror = function(error) {
        $('#wsState').html('-> connection error: ' + error);
        console.log('WebSocket Error ', error);
    };
    connection.onmessage = function(event) {
        var posTel = JSON.parse(event.data)
        var sessionKey = sessions.sessionKey(posTel.deviceId, posTel.sessionId);

        console.log('Incoming point: ' + event.data);

        if (sessions.activeSessions[sessionKey] == undefined) {
            console.log('No session found for key: ' + sessionKey + '. Creating new...')
            sessions.activeSessions[sessionKey] = new Session(posTel.deviceId, posTel.sessionId, posTel.position.latitude, posTel.position.longitude, sessions.currentMap);
        } else {
            sessions.activeSessions[sessionKey].addPolylinePoint(posTel.position.latitude, posTel.position.longitude);
            sessions.activeSessions[sessionKey].setFrontMarkerPos(posTel.position.latitude, posTel.position.longitude);
        }
    };
});