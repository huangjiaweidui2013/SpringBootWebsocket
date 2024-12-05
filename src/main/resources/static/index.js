var brokerUrl = $('#brokerUrl').val();

var ws;

function init() {
    brokerUrl = $('#brokerUrl').val();
    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
        ws = new WebSocket(brokerUrl + "?uid=" + $('#token').val() + "&docId="+ $('#docId').val() +"&source=1");
    }
    else {
        alert('当前浏览器 Not support websocket')
    }

    //连接发生错误的回调方法
        ws.onerror = function () {
            showMessageContent("WebSocket连接发生错误");
        };

        //连接成功建立的回调方法
        ws.onopen = function(event) {
            console.log("ws调用连接成功回调方法")
        }
        //接收到消息的回调方法
        ws.onmessage = function(message) {
            console.log("接收消息：" + message.data);
            if (typeof(message.data) == 'string') {
                showMessageContent(message.data);
            }else{
                //接收到了一个二进制消息
                showMessageContent(message.data);
            }
        }
        //ws连接断开的回调方法
        ws.onclose = function(e) {
            console.log("ws连接断开")
            showMessageContent("websocket 连接已断开");
        }

        updateHtmlStatus(true);

}

function updateHtmlStatus(connected){
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);

    if(connected){
        $("#conversation").show();
    }else{
        $("#conversation").hide();
    }
    $("#message").html("");

}

//连接websocket
function connect() {
    init();
}

//断开websocket连接
function disconnect() {
    ws.close();
    updateHtmlStatus(false);
}

//发送消息
function send() {

    var message = $("#content").val();
    var msg = {
        "from": $('#token').val(),
        "docId": $('#docId').val(),
        "data": message,
        "topicChannel": "/queue",
        "to": $("#to").val()
    };
    var messageJson = JSON.stringify(msg);

    //发送消息
    ws.send(messageJson);

    showMessageContent(message);
}

//发送二进制消息
function send2() {

    var message = $("#content2").val();
    // 创建ArrayBuffer
      const buffer = new ArrayBuffer(4);
      // 获取ArrayBuffer的视图
      const view = new DataView(buffer);
      // 设置数据
      view.setInt32(0, 42);

      // 发送二进制数据
      ws.send(buffer);

    showMessageContent(buffer);
}

function showMessageContent(message){
    $("#message").append("<tr><td>" + message + "</td></tr>")
}


$(function(){
    $("form").on('submit',(e) => e.preventDefault())

    $("#connect").click(function(){
        connect();
    });

    $("#disconnect").click(function(){
        disconnect();
    });

    $("#send").click(function(){
        send();
    });

    $("#send2").click(function(){
        send2();
    });

})



