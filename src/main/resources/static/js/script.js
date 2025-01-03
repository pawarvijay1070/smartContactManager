

const toggleSidebar = () => {
	if($(".sidebar").is(":visible"))
	{
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","3%");
	}
	else
	{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
}