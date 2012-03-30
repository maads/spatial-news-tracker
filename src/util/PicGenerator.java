package util;
import com.badlogic.gdx.awesomium.RenderBuffer;
import com.badlogic.gdx.awesomium.WebCore;
import com.badlogic.gdx.awesomium.WebView;





public class PicGenerator{

	private WebView webView;
	private static WebCore webCore;
	public PicGenerator(String[] filePaths){
    webCore = new WebCore();
    takePicOfFiles(filePaths);
    webCore.dispose();	
	}


	public void takePicOfFiles(String[] FilePaths){
		for(String filePath : FilePaths){
			int lastSlash = filePath.lastIndexOf("\\");
			String path = filePath.substring(0, lastSlash);  
			String fileName = filePath.substring(lastSlash+1, filePath.length());
			webCore.setBaseDirectory(path);
		    webView = webCore.createWebView(1200, 15000);
		    webView.loadFile(fileName,"");
		    while(webView.isLoadingPage()) {
		            webCore.update();
		            try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    }     
		    RenderBuffer renderBuffer = webView.render();
		    if(renderBuffer != null) {
		    	String rawFilename = fileName.substring(0, fileName.lastIndexOf("."));
		    	renderBuffer.saveToJPEG(rawFilename+".jpeg",90);
		    }
		    webView.destroy();
		}
}
}