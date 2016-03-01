
var baseline_pdf_name = &baseline_pdf_name&;
var test_pdf_name = &test_pdf_name&;
var diff_page_count = &diff_page_count&;
var page_count = &page_count&;
var diff_page_nums = &diff_page_nums&;
var baseline_page_images = &baseline_page_images&;
var test_page_images = &test_page_images&;

function onload()
{
	var baseline_pdf_span = document.getElementById("baseline_pdf_name");
	baseline_pdf_span.textContent = baseline_pdf_name;
	
	var test_pdf_span = document.getElementById("test_pdf_name");
	test_pdf_span.textContent = baseline_pdf_name;
	
	var page_count_span = document.getElementById("page_count");
	page_count_span.textContent = page_count;
	
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
	for (i = 0; i < page_count; i++) {
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
		var imageTag = baseline_page_images[pageNo];
		var c = document.getElementById("baseline_page_canvas");
		var ctx = c.getContext("2d");
		showPageImage(imageTag, ctx);
		
		imageTag = test_page_images[pageNo];
		c = document.getElementById("test_page_canvas");
		ctx = c.getContext("2d");
		showPageImage(imageTag, ctx);
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

