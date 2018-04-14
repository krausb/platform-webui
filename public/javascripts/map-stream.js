$( document ).ready(function() {

    /////////////////
    // INIT MAP
    /////////////////
    var map = new L.Map('map').setView([48.7682249, 11.6190613], 13);

    // add an OpenStreetMap tile layer
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    new L.Control.Scale({maxWidth: 800, metric: false}).addTo(map);
    new L.Control.Scale({maxWidth: 400, metric: false}).addTo(map);

    var polyline = L.polyline([], {color: 'red'}).addTo(map);

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
        console.log('Incoming point: ' + event.data)
        polyline.addLatLng(L.latLng(posTel.position.latitude, posTel.position.longitude))
    };
});