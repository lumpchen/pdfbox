
var diff_page_count = &diff_page_count&;
var diff_page_nums = &diff_page_nums&;

var base_pdf_json = &base_pdf_json&;
var test_pdf_json = &test_pdf_json&;
var diff_content_json = &diff_content_json&;

var base_pdf_json_obj = JSON.parse(base_pdf_json);
var test_pdf_json_obj = JSON.parse(test_pdf_json);
var diff_content_json_obj = JSON.parse(diff_content_json);

var tree = 
	[
	 	{
	 		text: "Page",
	 		color: "#000000",
            backColor: "#FFFFFF",
            selectable: true,
            state: {
            	checked: true,
            	disabled: false,
            	expanded: true,
            	selected: false
            },
            tags: ['available'],

            nodes: [
                    	{
                    		text: "Text",
                    		nodes: [],
							tags: ['0'],
                    	},
                    	{
                    		text: "Image",
							nodes: [],
							tags: ['0'],
                    	},
						{
                    		text: "Path",
							nodes: [],
							tags: ['0'],
                    	},
						{
                    		text: "Annot",
							nodes: [],
							tags: ['0'],
                    	},
                    ]
	 	}
	 ];

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
		
		cell.onclick = pageSelectHandler(i);
		
		if (diff_page_nums.indexOf(i) >= 0) {
			cell.style.color = "#FF0000";
		} else {
			cell.style.color = "rgb(111, 111, 111)";
		}
	}
	
	$(function() {
		var options = {
				bootstrap2: false, 
				showTags: true,
				levels: 5,
				data: tree};
		$('#treeview').treeview(options);
		
		$('#treeview').on('nodeSelected', function(e, node) {
		});
	}
	);		
}

var page_view_paras = {};

function pageSelectHandler(pageNo) {
	return function() {
		page_view_paras["PageNo"] = pageNo;
		page_view_paras["DiffContent"] = null;
		page_view_paras["BasePageWidth"] = base_pdf_json_obj.pages[pageNo].width;
		page_view_paras["BasePageHeight"] = base_pdf_json_obj.pages[pageNo].height;
		page_view_paras["TestPageWidth"] = test_pdf_json_obj.pages[pageNo].width;
		page_view_paras["TestPageHeight"] = test_pdf_json_obj.pages[pageNo].height;
		
		drawTree(pageNo);
		updatePageView();
	};
}

function updatePageView() {
	pageNo = page_view_paras["PageNo"];
	var imageTag = base_pdf_json_obj.pages[pageNo].imageTag;
	var c = document.getElementById("base_page_canvas");
	c.width = page_view_paras["BasePageWidth"] + 2;
	c.height = page_view_paras["BasePageHeight"] + 2;
	drawPage(imageTag, c);
	
	var imageTag = test_pdf_json_obj.pages[pageNo].imageTag;
	c = document.getElementById("test_page_canvas");
	c.width = page_view_paras["TestPageWidth"] + 2;
	c.height = page_view_paras["TestPageHeight"] + 2;
	drawPage(imageTag, c);
	
	var item = page_view_paras["DiffContent"];
	$("#attribute_table tbody tr").remove();
	if ((typeof(item) !== 'undefined') && (item !== null)) {
		updateAttributeTable(item);
	}
}

function updateAttributeTable(item) {
	$("#attribute_table tbody tr").remove();
	var tableBody = document.getElementById("attribute_table").getElementsByTagName("tbody")[0];
	for (i = 0; i < item.Attributes.length; i++) {
		var attr = item.Attributes[i];
		
		var attrRow = tableBody.insertRow(tableBody.rows.length);
		
		var cell = attrRow.insertCell(0);
		var text = document.createTextNode(attr.Key);
		cell.appendChild(text);
		if (!attr.Equals) {
			cell.style.color = "#FF0000";
		} else {
		}
		
		var cell = attrRow.insertCell(1);
		var text = document.createTextNode(attr.Value[0]);
		cell.appendChild(text);
		if (!attr.Equals) {
			cell.style.color = "#FF0000";
		} else {
		}
		
		var cell = attrRow.insertCell(2);
		var text = document.createTextNode(attr.Value[1]);
		cell.appendChild(text);
		if (!attr.Equals) {
			cell.style.color = "#FF0000";
		} else {
		}
	}
}

function findDiffReport(pageNo) {
	for (i = 0; i < diff_content_json_obj.length; i++) {
		var num = diff_content_json_obj[i].PageNo;
		tree[0].nodes[0].nodes.length = 0;
		tree[0].nodes[0].tags = [0]; //Text
		tree[0].nodes[1].tags = [0]; //Image
		if (pageNo == num) {
			var result = diff_content_json_obj[i].Result;
			
			// update Text node
			tree[0].nodes[0].tags = [result.Text.length];
			for (var j = 0; j < result.Text.length; j++) {
				var item = result.Text[j];
				var text = findShowText(item.Attributes);
				
				var treeNodes = tree[0].nodes[0].nodes;
				var newItem = {"text" : text, "item" : item};
				treeNodes.push(newItem);
			}
			
			// update Image node
			tree[0].nodes[1].tags = [result.Image.length];
			for (var j = 0; j < result.Image.length; j++) {
				var item = result.Image[j];
				var text = "image-" + j;
				
				var imageNodes = tree[0].nodes[1].nodes;
				var newItem = {"text" : text, "item" : item};
				imageNodes.push(newItem);
			}
		}
	}
}

function findShowText(attributes) {
	var text;
	for (i = 0; i < attributes.length; i++) {
		if (attributes[i].Key == "Text") {
			text = attributes[i].Value[0];
			if (attributes[i].Value[0] == "") {
				text = attributes[i].Value[1];
			}
		}
	}
	return text;
}

function drawTree(pageNo) {
		tree[0].tags = ["Page " + (pageNo + 1)];
		findDiffReport(pageNo);
		$(function() {
			var options = {
					bootstrap2: false, 
					showTags: true,
					levels: 5,
					data: tree};
			$('#treeview').treeview(options);
			
			$('#treeview').on('nodeSelected', function(e, node) {
				nodeText = node['text'];
				var item = node['item'];
				
				//drawDiffContentOutline(item.Outline);
				page_view_paras["DiffContent"] = item;
				updatePageView();
			});
		}
		);
}

function drawPage(imageTag, canvas) {
	ctx = canvas.getContext("2d");
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	drawPageImage(imageTag, ctx);
	ctx.save();
	ctx.beginPath();
	ctx.lineWidth="3";
	ctx.strokeStyle="red";
	ctx.rect(1, 1, canvas.width, canvas.height);
	ctx.stroke();
	ctx.restore();
}

function drawPageImage(imageTag, ctx) {
	ctx.save();
	var img = new Image();
	img.onload = function() {
		ctx.drawImage(img, 1, 1);
		
		var item = page_view_paras["DiffContent"];
		if ((typeof(item) !== 'undefined') && (item !== null)) {
			drawDiffContentOutline(item.Outline);
		}
	}
	img.src = "images/" + imageTag;
	ctx.restore();
}

function drawDiffContentOutline(outlineArr) { // arr[base, test]
	var baseRect = outlineArr[0];
	var testRect = outlineArr[1];
	var strokeColor = "red";
	if (baseRect.length == 0) {
		baseRect = testRect;
		strokeColor = "green";
	}
	if (baseRect.length > 0) {
		var baseCanvas = document.getElementById("base_page_canvas");
		var baseCtx = baseCanvas.getContext("2d");
		drawContentOutline(baseRect, baseCtx, page_view_paras["TestPageWidth"], page_view_paras["BasePageHeight"], strokeColor);
	}

	strokeColor = "red";
	if (testRect.length == 0) {
		testRect = baseRect;
		strokeColor = "green";
	}
	if (testRect.length > 0) {
		var testCanvas = document.getElementById("test_page_canvas");
		var testCtx = testCanvas.getContext("2d");
		drawContentOutline(testRect, testCtx, page_view_paras["TestPageWidth"], page_view_paras["TestPageHeight"], strokeColor);
	}
}

function drawContentOutline(outline, ctx, canvasWidth, canvasHeight, color) {
	if (outline.length == 0) {
		return;
	}
	var x = toPixel(outline[0]) ;
	var y = canvasHeight - toPixel(outline[1]);
	var w = toPixel(outline[2]);
	var h = toPixel(outline[3]);
	ctx.save();
	ctx.beginPath();
	ctx.lineWidth = "5";
	ctx.strokeStyle = color;
	// ctx.rect(x, y - h, w, h);
	canvas_arrow(ctx, x - 40, y - 50, x, y - 10);
	ctx.stroke();
	
	ctx.lineWidth = "1";
	ctx.moveTo(0, y);
	ctx.lineTo(canvasWidth, y);
	ctx.font = "26pt Calibri";
	ctx.fillStyle = 'red';
	ctx.fillText("x:" + x + " y:" + y, 0, y);
	ctx.stroke();
	
	ctx.restore();
}

function canvas_arrow(context, fromx, fromy, tox, toy) {
    var headlen = 20;   // length of head in pixels
    var angle = Math.atan2(toy-fromy,tox-fromx);
    context.moveTo(fromx, fromy);
    context.lineTo(tox, toy);
    context.lineTo(tox-headlen*Math.cos(angle-Math.PI/6),toy-headlen*Math.sin(angle-Math.PI/6));
    context.moveTo(tox, toy);
    context.lineTo(tox-headlen*Math.cos(angle+Math.PI/6),toy-headlen*Math.sin(angle+Math.PI/6));
}

function toPixel(pt) {
	return parseInt((pt / 72.0) * 96);
}