$( document ).ready(function() {
    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
    } else {
        console.log("WebSocket NOT supported by your Browser!");
        return;
    }

    var send = function() {
        var text = $message.val();
        $message.val("");
        connection.send(text);
        console.log("Sent " + text + " to WebSocket...")
    };

    var $messages = $("#messages"), $send = $("#send"), $message = $("#message");

    var url = $('#data-stream-script').attr("data-url");
    var connection = new WebSocket(url);

    $send.prop("disabled", true);

    connection.onopen = function() {
        $send.prop("disabled", false);
        $messages
            .prepend($("<li class='bg-info' style='font-size: 1.5em'>Connected</li>"));
        $send.on('click', send);

    };
    connection.onerror = function(error) {
        console.log('WebSocket Error ', error);
    };
    connection.onmessage = function(event) {
        $messages.append($("<li style='font-size: 1.5em'>" + event.data + "</li>"))
        $("html,body").animate({ scrollTop: $(document).height() }, "slow");
    };

    console.log( "chat app is running!" );
});