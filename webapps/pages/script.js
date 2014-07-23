$(document).ready(function(){
	// # This funciton below is only to test the exist of jQuery.
	$("h1").click(function(){
		$(this).css("color", "red");
	});

	// # When the 'login button clicked', script below is called.
	$("#button").click(function(){
	    // $.get("127.0.0.1:8080/login/?name=ghouan&&ip=127.0.0.1", function(data, status){ 
	    $.get("http://localhost:8080/WEB-INF/login.xml", function(data, status){
	    	console.log(status);
	    	// alert("Data: " + data + "\nStatus: " + status);
	    	if ( status === "success" ) {
	    		// 1. parse XML.
	    		// 2. Manipulate the UI. Hide/Delete the login form and load the bidding UI.
	    		update(data);
	    	} else {
	    		// Login error.
	    		
	    	}
	    });
  	});

	// # 
	$("#submit_auction").click(function() {
		submitAuction();
	});

});

// Helper function for formatting strings
String.prototype.format = String.prototype.f = function() {
    var s = this,
        i = arguments.length;

    while (i--) {
        s = s.replace(new RegExp('\\{' + i + '\\}', 'gm'), arguments[i]);
    }
    return s;
};

function loginError() {
	$(".login_error").show();
	// other ....
}

function update(data) {
	$("#login").hide();
	console.log("update()");
	var $context = $(data).find("auction_context");
	var $roundNumber = $context.children("round").attr("value");
	var $isFinal = $context.children("round").attr("final");

	console.log("Round {0}  Final? {1}".f($roundNumber, $isFinal));

	$("#round_infomation").text(function() {
		return "Round {0}  Final? {1}".f($roundNumber, $isFinal);
	});
	
	$("bid_table").find("tbody").find("tr").remove();

	$context.find("item").each(function(i) {
		console.log(this);
		var $itemId = $(this).attr("id");
		var $itemName = $(this).attr("name");
		var $price = $(this).attr("price");
		var $owner = $(this).attr("owner");

		console.log($itemId, $itemName, $price, $owner);

		var $_id = $("<th class=\"invisible\"></th>").text($itemId);
		var $_name = $("<th></th>").text($itemName);


		var $_price = $("<th></th>").text($price);
		var $_yprice = $("<th></th>").append($("<input type=\"text\" id=\"price{0}\"></input>".f($itemId)));
		var $item = $("<tr></tr>").append($_id, $_name, $_price, $_yprice);

		$("#bid_table").find("tbody").append($item);
	});	

	// Leave for timer
	// ...

	$("#bid").show();
}

// <bid>
// 	<item_list>
// 		<item0 id="0" name="dogfood" price="120" owner=""/>
// 		<item1 id="1" name="kennel" price="290" owner=""/>
// 		<item2 id="2" name="dota" price="0" owner=""/>
// 		<!-- 价格为0表示没有对本品进行报价，或者放弃报价-->
// 	</item_list> 
// </bid>

//

function collectData() {
	var $xmlData = $("<div></div>");
	var $bid = $("<bid></bid>");
	var $itemList = $("<item_list></item_list>");
	
	$("#bid_table").find("tbody").find("tr").each(function(i) {
		
		var $id = $(this).children("th:eq(0)").text();
		var $name = $(this).children("th:eq(1)").text();
		var $price = $("#price{0}".f($id)).val();
		var $owner = "";
		console.log($id, $name, $price, $owner);

		var $temp = $("<item id={0} name={1} price={2} owner={3}></item>".f($id, $name, $price, $owner));
		$itemList.append( $temp );
		
	});
	$bid.append($itemList);
	$xmlData.append($bid);

	console.log($xmlData.text());
	return $xmlData.text();
}


function updateError() {

}

function submitAuction() {

	console.log("submitAuction");
	// collect data
	var $xmlData = collectData();
	

	// other ....

	$.ajax({
		url: 'test.jsp', 
		processData: false,
		async: false,
		contentType: "text/xml",
		cache: false,
        dataType: "xml",
		type: "POST",  // type should be POST
		data: $xmlData, // send the string directly
		success: function(response) {
			update(response);
		},
		error: function(response) {
			// ...
			updateError(response);
		},
		complete: function(response) {
			// ...
		}
	});


}






