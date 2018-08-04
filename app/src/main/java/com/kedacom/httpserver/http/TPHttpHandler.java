package com.kedacom.httpserver.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.kedacom.httpserver.service.WebService;
import com.kedacom.httpserver.utils.BitUtil;
import com.kedacom.httpserver.utils.Constant;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.tplog.TPLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhanglei on 2017/3/8.
 */
public class TPHttpHandler implements HttpRequestHandler {

    private static final String TEMP_HTML = "temp.html";//模板html名称

    private static final String HTML_IMG_START_TAG = "<img class = \"content_img\" src=";

    private static final String HTML_IMG_END_TAG = "/>";

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {

        String target = URLDecoder.decode(httpRequest.getRequestLine().getUri(),
                Constant.ENCODING);

        TPLog.printKeyStatus("http request target = "+target);

        HttpEntity entity = new StringEntity("", Constant.ENCODING);

        String contentType = "text/html;charset=" + Constant.ENCODING;

        if(Constant.Config.Web_Root.equals(target))
        {
            TPLog.printKeyStatus("二维码扫描。。。。。。");
            httpResponse.setStatusCode(HttpStatus.SC_OK);

            String html = "";
            //读取模板网页
            html = readTempHtml(TEMP_HTML);
            //读取图片资源并拼接成html片段
            String imgHtml = mkImageListForHtml();


            //替换模板网页内对应的片段

            if(imgHtml!=null){
                html = html.replace("@#imglist#@",imgHtml);
            }

            html = html.replace("@#title#@",Constant.shareTitle);

            html = html.replace("@#subtitle#@",Constant.sharSubtitle);

            entity = new StringEntity(html, Constant.ENCODING);
            httpResponse.setHeader("Content-Type", contentType);
        }else{
            final File file = new File(target);
            if(!file.exists()){
                return;
            }
            //下载文件
            String mime = null;
            int dot = file.getCanonicalPath().lastIndexOf(".");
            if (dot >= 0)
            {
                mime = (String) Constant.theMimeTypes.get(file.getCanonicalPath()
                        .substring(dot + 1).toLowerCase(Locale.ENGLISH));
                if (TextUtils.isEmpty(mime))
                    mime = Constant.MIME_DEFAULT_BINARY;

                long fileLength = file.length();
                httpRequest.addHeader("Content-Length", "" + fileLength);
                httpResponse.setHeader("Content-Type", mime);
                httpResponse.addHeader("Content-Description", "File Transfer");
                httpResponse.addHeader("Content-Disposition", "attachment;filename="
                        + encodeFilename(file));
                httpResponse.setHeader("Content-Transfer-Encoding", "binary");

                entity = new EntityTemplate(new ContentProducer()
                {
                    @Override
                    public void writeTo(OutputStream outStream) throws IOException
                    {
                        write(file, outStream);
                    }
                });
            }
        }

        TPLog.printKeyStatus("响应请求！！！！");
        httpResponse.setEntity(entity);
    }


    private String mkImageListForHtml(){
        TPLog.printKeyStatus("创建图片html片段。。。。。。");
        List<String> listFiles = FileUtils.getAllSubFile(Constant.shareDir);

        if(listFiles==null||listFiles.isEmpty()){
            return null;
        }

        String imgHtml = "";
        for(String filePath:listFiles){
            imgHtml = imgHtml + HTML_IMG_START_TAG;
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            String bitmapStr = BitUtil.bitmaptoString(bitmap);

            imgHtml = imgHtml + "\"data:image/png;base64," +bitmapStr+ "\"";
            imgHtml = imgHtml + HTML_IMG_END_TAG;
        }
        return imgHtml;
    }


    private void write(File inputFile, OutputStream outStream) throws IOException
    {
        FileInputStream fis = new FileInputStream(inputFile);
        try
        {
            int count;
            byte[] buffer = new byte[Constant.BUFFER_LENGTH];
            while ((count = fis.read(buffer)) != -1)
            {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            fis.close();
            outStream.close();
        }
    }

    private String encodeFilename(File file) throws IOException
    {
        String filename = URLEncoder.encode(getFilename(file), Constant.ENCODING);
        return filename.replace("+", "%20");
    }

    private String getFilename(File file)
    {
        return file.isFile() ? file.getName() : file.getName() + ".zip";
    }



    //读取默认的html模板
    public String readTempHtml(String filePath){
        InputStream in = null;
        String html = "";
        try {
            in = WebService.getInstance().getResources().getAssets().open(filePath);

            int len = -1;
            byte buffer[] = new byte[1024];

            while((len = in.read(buffer))!=-1){
                html = html + new String(buffer,0,len);
            }
            return html;
        } catch (IOException e) {
            TPLog.printError("读取模板html失败：");
            TPLog.printError(e);
            e.printStackTrace();
        }finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
