$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//标题和内容
	let title = $("#recipient-name").val();
	let content = $("#message-text").val();

	$.post(
		"/discuss/add",
		{"title":title,"context":content},
		function (data){
			data = $.parseJSON(data);
			//在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			//两秒后隐藏提示框
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//
				if(data.code == 0){
					window.location.reload();
				}

			}, 2000);
		}
	)



}