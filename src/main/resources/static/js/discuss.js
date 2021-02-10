$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data){
            console.log(data)
            var _data = $.parseJSON(data);
            if(_data.code == 0){
                $(btn).children("i").text(_data.likeCount);
                $(btn).children("b").text(_data.likeStatus==1?'已赞':'赞');
            }else {
                //alert(data.msg);
            }
        }
    );
}

// 置顶
function setTop() {
    $.post(
        "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = "/index";
            } else {
                console.log(data.msg);
                alert(data.msg);
            }
        }
    );
}