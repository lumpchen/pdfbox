var PDF_DIFF = PDF_DIFF || {};

PDF_DIFF.diff_report_view = function(report_data) {
	var dpi = 96;
	var Rendering_Resolution = report_data.Rendering_Resolution;
	var Base_Stroke_Color = report_data.Base_Stroke_Color;
	var Test_Stroke_Color = report_data.Test_Stroke_Color;
	var Test_Fill_Color = report_data.Test_Fill_Color;
	var Base_Fill_Color = report_data.Base_Fill_Color;

	var diff_page_count = report_data.diff_page_count;
	var diff_page_nums = report_data.diff_page_nums;
	var base_pdf_json_obj = report_data.base_pdf_json_obj ;
	var test_pdf_json_obj = report_data.test_pdf_json_obj;
	var diff_content_json_obj = report_data.diff_content_json_obj;

	var page_view_paras = {};
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

	this.onload = function() {
		var base_pdf_span = document.getElementById("base_pdf_name");
		base_pdf_span.textContent = base_pdf_json_obj.title;

		var test_pdf_span = document.getElementById("test_pdf_name");
		test_pdf_span.textContent = test_pdf_json_obj.title;

		var max_page_count = base_pdf_json_obj.pageCount > test_pdf_json_obj.pageCount ? 
			base_pdf_json_obj.pageCount : test_pdf_json_obj.pageCount;
		var page_count_span = document.getElementById("page_count");
		page_count_span.textContent = max_page_count;

		var sum = "";
		if (diff_page_count == 0) {
			sum = "These two PDFs are the same!";
		} else if (diff_page_count == 1) {
			sum = "Found <span style=\"color:red; font-size: 1.5em\">" + diff_page_count + "</span> different page!";
		} else {
			sum = "Found <span style=\"color:red; font-size: 1.5em\">" + diff_page_count + "</span> different pages!";
		}
		var diff_summary_span = document.getElementById("diff_summary");
		diff_summary_span.innerHTML = sum;

		var tableBody = document.getElementById("page_list_table").getElementsByTagName("tbody")[0];
		var diffColor = "#FF0000", sameColor = "rgb(111, 111, 111)";
		for (var i = 0; i < max_page_count; i++) {
			
			var pageRow = tableBody.insertRow(tableBody.rows.length);
			var cell = pageRow.insertCell(0);
			if (i >= base_pdf_json_obj.pageCount) {
				var text = document.createTextNode("NA");
			} else {
				var text = document.createTextNode("Page " + (i + 1));	
			}
			
			cell.appendChild(text);

			if (diff_page_nums.indexOf(i) >= 0) {
				cell.style.color = diffColor;
			} else {
				cell.style.color = sameColor;
			}

			var cell = pageRow.insertCell(1);
			if (i >= test_pdf_json_obj.pageCount) {
				var text = document.createTextNode("NA");
			} else {
				var text = document.createTextNode("Page " + (i + 1));	
			}
			cell.appendChild(text);

			if (diff_page_nums.indexOf(i) >= 0) {
				cell.style.color = diffColor;
			} else {
				cell.style.color = sameColor;
			}

			pageRow.onclick = pageSelectHandler(i);
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
	};

	var updatePageSelection = function(pageNo) {
		var tableBody = document.getElementById("page_list_table").getElementsByTagName("tbody")[0];

		for (var i = 0; i < tableBody.rows.length; i++) {
			var td = tableBody.rows[i].cells[0];
			var td_1 = tableBody.rows[i].cells[1];
			if (i == pageNo) {
				updateCellColor(td, true);
				updateCellColor(td_1, true);
			} else {
				updateCellColor(td, false);
				updateCellColor(td_1, false);
			}
		}
	}

	var updateCellColor = function(td, selected) {
		if (selected) {
			td.style.backgroundColor  = "lightgray";
			td.style.fontWeight  = "Bold";
			td.className += " selected";
		} else {
			td.style.backgroundColor = "rgb(238, 238, 238)";
			td.style.fontWeight  = "normal";
			td.classList.remove('selected');
		}
	}
	
	var pageSelectHandler = function(pageNo) {
		return function() {
			updatePageSelection(pageNo);
			
			page_view_paras["PageNo"] = pageNo;
			page_view_paras["DiffContent"] = null;

			if (pageNo < base_pdf_json_obj.pageCount) {
				page_view_paras["BasePageWidth"] = base_pdf_json_obj.pages[pageNo].width;
				page_view_paras["BasePageHeight"] = base_pdf_json_obj.pages[pageNo].height;
				page_view_paras["BaseIsBlank"] = false;
			} else {
				page_view_paras["BasePageWidth"] = 0;
				page_view_paras["BasePageHeight"] = 0;
				page_view_paras["BaseIsBlank"] = true;
			}

			if (pageNo < test_pdf_json_obj.pageCount) {
				page_view_paras["TestPageWidth"] = test_pdf_json_obj.pages[pageNo].width;
				page_view_paras["TestPageHeight"] = test_pdf_json_obj.pages[pageNo].height;
				page_view_paras["TestIsBlank"] = false;
			} else {
				page_view_paras["TestPageWidth"] = 0;
				page_view_paras["TestPageHeight"] = 0;
				page_view_paras["TestIsBlank"] = true;
			}

			drawTree(pageNo);
			updatePageView();
		};
	};
	
	var pageScale = "FitToPage";
	var baseCanvasScale = 1;
	var testCanvasScale = 1;

	var buildBaseCanvas = function(paras, cellW) {
		var pageNo = page_view_paras["PageNo"];
		
		var w = 0, h = 0;
		var baseCanvas = document.getElementById("base_page_canvas");
		var cell = document.getElementById("base_page_td");
			
		if (paras["BaseIsBlank"]) {
			w = toPixel(paras["TestPageWidth"]);
			h = toPixel(paras["TestPageHeight"]);
		} else {
			w = toPixel(paras["BasePageWidth"]);
			h = toPixel(paras["BasePageHeight"]);
		}

		if (pageScale == "FitToPage") {
			baseCanvasScale = cellW / w;
		}
		
		baseCanvas.width = w * baseCanvasScale;
		baseCanvas.height = h * baseCanvasScale;
			
		if (paras["BaseIsBlank"]) {
			drawBlankPage(baseCanvas);
		} else {
			var imageTag = base_pdf_json_obj.pages[pageNo].imageTag;
			drawPage(cell, imageTag, baseCanvas, baseCanvasScale);
			
			// addCanvasMouseListener(baseCanvas);
		}
	};

	var buildTestCanvas = function(paras, cellW) {
		var pageNo = page_view_paras["PageNo"];
		
		var w = 0, h = 0;
		var testCanvas = document.getElementById("test_page_canvas");
		var cell = document.getElementById("test_page_td");
		
		if (paras["TestIsBlank"]) {
			w = toPixel(paras["BasePageWidth"]);
			h = toPixel(paras["BasePageHeight"]);
		} else {
			w = toPixel(paras["TestPageWidth"]);
			h = toPixel(paras["TestPageHeight"]);
		}
		if (pageScale == "FitToPage") {
			testCanvasScale = cellW / w;
		}
		
		testCanvas.width = w * testCanvasScale;
		testCanvas.height = h * testCanvasScale;
		
		if (paras["TestIsBlank"]) {
			drawBlankPage(testCanvas);
		} else {
			var imageTag = test_pdf_json_obj.pages[pageNo].imageTag;
			drawPage(cell, imageTag, testCanvas, testCanvasScale);

			// addCanvasMouseListener(testCanvas);
		}
	}
	
	var updatePageView = function() {
		var t = document.getElementById("canvas_table");
		var tw = t.clientWidth;
		var canvasW = parseInt(tw / 2);

		buildBaseCanvas(page_view_paras, canvasW);
		buildTestCanvas(page_view_paras, canvasW);

		var item = page_view_paras["DiffContent"];
		$("#attribute_table tbody tr").remove();
		if ((typeof(item) !== 'undefined') && (item !== null)) {
			updateAttributeTable(item);
		}
	};

	var drawTree = function(pageNo) {
		tree[0].tags = ["Page " + (pageNo + 1)];
		initTreeData(pageNo);
		$(function() {
			var options = {
				bootstrap2: false, 
				showTags: true,
				levels: 5,
				data: tree};
				$('#treeview').treeview(options);

				$('#treeview').on('nodeSelected', function(e, node) {
					var parent = $('#treeview').treeview('getParent', node);
					if (parent !== undefined) {
						page_view_paras["text"] = parent['text'];
					}

					var item = node['item'];

					page_view_paras["DiffContent"] = item;
				// page_view_paras["text"] = node['text'];
				updatePageView();
			});
		});
	};

	var initTreeData = function(pageNo) {
		tree[0].nodes[0].nodes.length = 0;
		tree[0].nodes[0].tags = [0]; // Text
		tree[0].nodes[1].nodes.length = 0;
		tree[0].nodes[1].tags = [0]; // Image
		tree[0].nodes[2].nodes.length = 0;
		tree[0].nodes[2].tags = [0]; // Path
		tree[0].nodes[3].nodes.length = 0;
		tree[0].nodes[3].tags = [0]; // Annot
		
		for (var i = 0; i < diff_content_json_obj.length; i++) {
			var num = diff_content_json_obj[i].PageNo;
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
				
				// update Path node
				tree[0].nodes[2].tags = [result.Path.length];
				for (var j = 0; j < result.Path.length; j++) {
					var item = result.Path[j];
					var text = "Path-" + j;
					
					var pathNodes = tree[0].nodes[2].nodes;
					var newItem = {"text" : text, "item" : item};
					pathNodes.push(newItem);
				}
				
				// update Image node
				tree[0].nodes[3].tags = [result.Annot.length];
				for (var j = 0; j < result.Annot.length; j++) {
					var item = result.Annot[j];
					var text = "Annot-" + j;
					
					var annotNodes = tree[0].nodes[3].nodes;
					var newItem = {"text" : text, "item" : item};
					annotNodes.push(newItem);
				}
			}
		}
	};

	var drawBlankPage = function(canvas) {
		var ctx = canvas.getContext("2d");
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.save();
		ctx.fillStyle = "gray";
		ctx.fillRect(1, 1, canvas.width - 1, canvas.height - 1);
		ctx.rect(0, 0, canvas.width, canvas.height);
		ctx.strokeStyle = 'red';
		ctx.stroke();

		ctx.translate(72, canvas.height / 2);
		ctx.scale(3, 3);
		ctx.font = "16pt Calibri";
		ctx.fillStyle = 'red';
		ctx.fillText("NOT FOUND", 0, 0);
		
		ctx.restore();
	};

	var getMousePos = function(canvas, evt) {
		var rect = canvas.getBoundingClientRect();
		return {
			x: evt.clientX - rect.left,
			y: evt.clientY - rect.top
		};
	};

	var updateAttributeTable = function(item) {
		$("#attribute_table tbody tr").remove();
		var tableBody = document.getElementById("attribute_table").getElementsByTagName("tbody")[0];
		for (var i = 0; i < item.Attributes.length; i++) {
			var attr = item.Attributes[i];
			
			var attrRow = tableBody.insertRow(tableBody.rows.length);
			
			var cell = attrRow.insertCell(0);
			var text = document.createTextNode(attr.Key);
			cell.appendChild(text);
			cell.style.textAlign = "left";
			cell.style.fontWeight = "bold";
			if (!attr.Equals) {
				cell.style.color = "#FF0000";
			}
			
			var cell = attrRow.insertCell(1);
			var text = document.createTextNode(attr.Value[0]);
			cell.appendChild(text);
			cell.style.textAlign = "center";
			if (!attr.Equals) {
				cell.style.color = "#FF0000";
			}
			
			var cell = attrRow.insertCell(2);
			var text = document.createTextNode(attr.Value[1]);
			cell.appendChild(text);
			cell.style.textAlign = "center";
			if (!attr.Equals) {
				cell.style.color = "#FF0000";
			}
		}
	};

	var findShowText = function(attributes) {
		var text;
		for (var i = 0; i < attributes.length; i++) {
			if (attributes[i].Key == "Text") {
				text = attributes[i].Value[0];
				if (attributes[i].Value[0] == "") {
					text = attributes[i].Value[1];
				}
			}
		}
		return text;
	};


	var drawPage = function(cell, imageTag, canvas, scale) {
		ctx = canvas.getContext("2d");
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		drawPageImage(cell, imageTag, ctx, canvas, scale);
	};


	var drawPageImage = function(cell, imageTag, ctx, canvas, scale) {
		var img = new Image();
		img.src = "images/" + imageTag;

		img.onload = function() {
			var w = canvas.width;
			var h = canvas.height;

			var s = w / img.width;

			cell.style.backgroundImage = "url(" + "images/" + imageTag + ")";
			cell.style.backgroundSize = w + "px " + h + "px";
			cell.style.backgroundRepeat = "no-repeat";

			addCanvasMouseListener(canvas, img, scale);

			ctx.save();
			ctx.beginPath();
			ctx.lineWidth="1";
			ctx.strokeStyle="red";
			ctx.rect(1, 1, w - 1, h - 1);
			ctx.stroke();
			ctx.restore();
		
			var item = page_view_paras["DiffContent"];
			var category = page_view_paras["text"];
			if ((typeof(item) !== 'undefined') && (item !== null)) {
				drawDiffContentOutline(category, item.Outline, item.SubOutline);
			}
		}
	};

	var addCanvasMouseListener = function(canvas, img, scale) {
		canvas.style.cursor = 'crosshair';
		
		canvas.addEventListener('mousemove', function(evt) {
			var mousePos = getMousePos(canvas, evt);
			var zoomCtx = zoom.getContext("2d");
			zoomCtx.fillStyle = "white";
			zoomCtx.fillRect(0, 0, zoom.width, zoom.height);

			var s = scale / (Rendering_Resolution / dpi);
			var x = mousePos.x / s;
			var y = mousePos.y / s;
			var w =  zoom.width;
			var h = zoom.height;

			zoomCtx.drawImage(img, x, y, w, h, 0, 0, w, h);
			zoomCtx.drawImage(canvas, mousePos.x, mousePos.y, w, h, 0, 0, w / s, h / s);
			zoom.style.top = evt.pageY + 2 + "px";
			zoom.style.left = evt.pageX + 2 + "px";
			zoom.style.display = "block";
		}, false);

		canvas.addEventListener("mouseout", function() {
			zoom.style.display = "none";
		});
	};

	var drawDiffContentOutline = function(category, outlineArr, subOutlineArr) { // arr[base, test]
		var baseRect = outlineArr[0];
		var testRect = outlineArr[1];
		
		var baseSubRectArr = subOutlineArr[0];
		var testSubRectArr = subOutlineArr[1];
		
		if (baseRect.length > 0) {
			var baseCanvas = document.getElementById("base_page_canvas");
			var baseCtx = baseCanvas.getContext("2d");
			
			baseCtx.save();
			baseCtx.scale(baseCanvasScale, baseCanvasScale);
			drawContentOutline(category, baseRect, baseSubRectArr, baseCtx, 
					page_view_paras["BasePageWidth"], page_view_paras["BasePageHeight"], Base_Stroke_Color, Base_Fill_Color);
			baseCtx.restore();
		}

		if (testRect.length > 0) {
			var testCanvas = document.getElementById("test_page_canvas");
			var testCtx = testCanvas.getContext("2d");
			
			testCtx.save();
			testCtx.scale(testCanvasScale, testCanvasScale);
			drawContentOutline(category, testRect, testSubRectArr, testCtx, page_view_paras["TestPageWidth"], page_view_paras["TestPageHeight"], Test_Stroke_Color, Test_Fill_Color);
			testCtx.restore();
		}
	};

	var drawContentOutline = function(category, outline, subOutline, ctx, pageWidth, pageHeight, strokeColor, fillColor) {
		if (outline.length == 0) {
			return;
		}
		
		var x = toPixel(outline[0]);
		var y = toPixel(pageHeight - outline[1]);
		var h = toPixel(outline[3]);
		var w = toPixel(outline[2]);

		if (category === "Text") {
			var dh = parseInt(h / 4);
			y += dh;
			h += dh;
			w += 6;
		}
		
		ctx.save();
		ctx.setLineDash([4, 4]);
		ctx.beginPath();
		ctx.lineWidth = "1";
		ctx.strokeStyle = strokeColor;
		ctx.fillStyle = fillColor;
		
		if (category === "Path") {
			ctx.rect(x, y - h, w, h);
			
			if (subOutline.length > 0) {
				ctx.stroke();
				
				for (var i = 0; i < subOutline.length; i++) {
					var subRect = subOutline[i];
					var sx = toPixel(subRect[0]);
					var sy = toPixel(pageHeight - subRect[1]);
					var sh = toPixel(subRect[3]);
					var sw = toPixel(subRect[2]);
					ctx.save();
					ctx.rect(sx, sy - sh, sw, sh);
					ctx.stroke();
					ctx.restore();
				}
			}
			ctx.fill();	
		} else {
			if (strokeColor == "red") {
				ctx.rect(x, y - h, w, h);	
			} else {
				canvas_arrow(ctx, x - 40, y - 50, x, y - 10);	
			}
			
			ctx.stroke();
			ctx.fill();
		}

		ctx.moveTo(0, y);
		ctx.lineTo(toPixel(pageWidth), y);
		ctx.font = "16pt Calibri";
		ctx.fillStyle = 'red';
		ctx.fillText("x:" + Math.round(outline[0]) + " y:" + Math.round(outline[1]), 0, y);
		ctx.stroke();
		ctx.restore();
	};

	var canvas_arrow = function(context, fromx, fromy, tox, toy) {
	    var headlen = 20;   // length of head in pixels
	    var angle = Math.atan2(toy - fromy, tox - fromx);
	    context.moveTo(fromx, fromy);
	    context.lineTo(tox, toy);
	    context.lineTo(tox - headlen * Math.cos(angle - Math.PI/6),toy - headlen * Math.sin(angle - Math.PI / 6));
	    context.moveTo(tox, toy);
	    context.lineTo(tox - headlen * Math.cos(angle + Math.PI / 6),toy - headlen * Math.sin(angle + Math.PI / 6));
	};

	var toPixel = function (pt) {
		return parseInt((pt / 72.0) * dpi);
	};
};

PDF_DIFF.view = new PDF_DIFF.diff_report_view(PDF_DIFF.diff_report_data);

