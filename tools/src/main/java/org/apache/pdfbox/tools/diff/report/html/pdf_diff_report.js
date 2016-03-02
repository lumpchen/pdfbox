
var diff_page_count = &diff_page_count&;
var diff_page_nums = &diff_page_nums&;

var base_pdf_json = &base_pdf_json&;
var test_pdf_json = &test_pdf_json&;

var base_pdf_json_obj = JSON.parse(base_pdf_json);
var test_pdf_json_obj = JSON.parse(test_pdf_json);

function onload()
{
	var base_pdf_span = document.getElementById("base_pdf_name");
	base_pdf_span.textContent = base_pdf_json_obj.title;
	
	var test_pdf_span = document.getElementById("test_pdf_name");
	test_pdf_span.textContent = test_pdf_json_obj.title;
	
	var page_count_span = document.getElementById("page_count");
	page_count_span.textContent = base_pdf_json_obj.pageCount;
	
	var sum = "";
	if (diff_page_count == 0) {
		sum = "These two PDFs are the same!";
	} else if (diff_page_count == 1) {
		sum = "Found <span style=\"color:red\">" + diff_page_count + "</span> different page!";
	} else {
		sum = "Found <span style=\"color:red\">" + diff_page_count + "</span> different pages!";
	}
	var diff_summary_span = document.getElementById("diff_summary");
	diff_summary_span.innerHTML = sum;
	
	var tableBody = document.getElementById("page_list_table").getElementsByTagName("tbody")[0];
	for (i = 0; i < base_pdf_json_obj.pageCount; i++) {
		var pageRow = tableBody.insertRow(tableBody.rows.length);
		var cell = pageRow.insertCell(0);
		var text = document.createTextNode("Page " + (i + 1));
		cell.appendChild(text);
		
		cell.onclick = pageSelectHandler(i, cell);
		
		if (diff_page_nums.indexOf(i) >= 0) {
			cell.style.color = "#FF0000";
		}
	}
}

function pageSelectHandler(pageNo, cell) {
	return function() {
		var imageTag = base_pdf_json_obj.pages[pageNo].imageTag;
		var c = document.getElementById("base_page_canvas");
		c.width = base_pdf_json_obj.pages[pageNo].width + 2;
		c.height = base_pdf_json_obj.pages[pageNo].height + 2;
		var ctx = c.getContext("2d");
		showPageImage(imageTag, ctx);
		ctx.save();
		ctx.beginPath();
		ctx.lineWidth="3";
		ctx.strokeStyle="red";
		ctx.rect(1, 1, c.width, c.height);
		ctx.stroke();
		ctx.restore();
		
		var imageTag = test_pdf_json_obj.pages[pageNo].imageTag;
		c = document.getElementById("test_page_canvas");
		c.width = test_pdf_json_obj.pages[pageNo].width + 2;
		c.height = test_pdf_json_obj.pages[pageNo].height + 2;
		ctx = c.getContext("2d");
		showPageImage(imageTag, ctx);
		ctx.save();
		ctx.beginPath();
		ctx.lineWidth="3";
		ctx.strokeStyle="red";
		ctx.rect(1, 1, c.width, c.height);
		ctx.stroke();
		ctx.restore();
	};
}

function showPageImage(imageTag, ctx) {
	ctx.save();
	var img = new Image();
	img.onload = function() {
		ctx.drawImage(img, 1, 1);	
	}
	img.src = "images/" + imageTag;
	ctx.restore();
}

