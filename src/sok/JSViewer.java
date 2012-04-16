package sok;

import java.io.IOException;
import java.util.List;

import util.SNTUtil;

public class JSViewer {

	public JSViewer(List<String> urls) {

		StringBuilder sb = new StringBuilder();
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>SNT Viewer</title>\n<script type=\"text/javascript\">	\nvar curPage = 0;");
		sb.append("var size = ");
		sb.append(urls.size());
		sb.append("; var files = [");
		for (int i = 0; i < urls.size(); i++) {
			if (i == urls.size() - 1) {
				sb.append("'" + urls.get(i) + "'");
			} else {
				sb.append("'" + urls.get(i) + "',\n");
			}
		}
		sb.append("]; function setNumberOfLabelStartValue(){\n");
		sb.append("document.getElementById('numberOfLabel').innerHTML ='Viewing page: ' + (curPage+1) + '/' + size; }\n");
		sb.append("function initialiseVariables(){\n");
		sb.append("setNumberOfLabelStartValue();\n");
		sb.append("setFistPage(); }\n");
		sb.append("function setFistPage(){\n");
		sb.append("document.getElementById('vgFrame').src = files[curPage]; }\n");
		sb.append("function changeHtmlFile(direction) {\n");
		sb.append("if (direction == 'next') {\n");
		sb.append(" curPage++;\n");
		sb.append(" if (curPage == size)  curPage = 0;\n");
		sb.append(" } else { \n");
		sb.append("if(!curPage ==0 )  curPage--; } \n"); 
		sb.append(" document.getElementById('numberOfLabel').innerHTML ='Viewing page:' + (curPage +1) + '/' + size;\n");
		sb.append("document.getElementById('vgFrame').src = files[curPage]; }\n");
		sb.append("function jumpToHtmlFile(){\n");
		sb.append("var number = document.getElementById('jumpToNumber').value-1;\n");
		sb.append("if(number<size && number>-1){\n");
		sb.append("document.getElementById('numberOfLabel').innerHTML ='Viewing page:' + (number+1) + '/' + size;\n");
		sb.append(" document.getElementById('vgFrame').src = files[number];\n");
		sb.append("curPage = number; }} \n");
		sb.append("</script> </head>\n");
		sb.append("<body onload=\"initialiseVariables()\">\n");
		sb.append("<IFRAME id=\"vgFrame\" WIDTH=100% HEIGHT=97% FRAMEBORDER=1 SCROLLING=auto>\n");
		sb.append("<p>Your browser does not support iframes.</p> </iframe>\n");
		sb.append("<button onclick=\"changeHtmlFile('previous')\">Previous</button><button onclick=\"changeHtmlFile('next')\">Next</button>	\n<label id=\"numberOfLabel\"></label>	\n<label id=\"jumpToLabel\">| Jump to page number:</label>\n<input type=\"text\" id=\"jumpToNumber\" size=\"3\" maxlength =\"3\" />	\n<button onclick=\"jumpToHtmlFile()\">Go</button>\n");	
		sb.append("</body> </html>\n");
		
		try {
			SNTUtil.WriteToFileLineByLine("output" + System.getProperty("file.separator") + "index.html", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
