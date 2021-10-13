package edu.temple.convoy;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;


import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.util.CharsetUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class MultipartRequest extends Request<String> {
    MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    private static HttpEntity httpentity;
    private final Response.Listener<String> mListener;
    private final HashMap<String, byte[]> mFileParts;
    private final Map<String, String> mStringPart;
   // private final Map<String, String> header;
    public MultipartRequest(String url, Response.ErrorListener errorListener,
                            Response.Listener<String> listener, HashMap<String, byte[]> files,
                            Map<String, String> mStringPart, byte[] bytes) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mFileParts = files;
        this.mStringPart = mStringPart;
        //this.header = header;
        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            entity.setCharset(CharsetUtils.get("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        buildMultipartEntity();
        httpentity = entity.build();
    }
    private void buildMultipartEntity() {
        if (mFileParts != null) {
            for (Map.Entry<String, byte[]> entry : mFileParts.entrySet()) {
                Log.d("key", "did 2 " + entry.getKey());
                entity.addPart(entry.getKey(), new ByteArrayBody(entry.getValue(), ContentType.DEFAULT_BINARY, null)); //new FileBody(, .length);////ContentType.create("image/*"), entry.getValue().getName()));
            }
        }
        //entity.addPart(FILE_PART_NAME, new FileBody(mFilePart, ContentType.create("image/jpeg"), mFilePart.getName()));
        if (mStringPart != null) {
            for (Map.Entry<String, String> entry : mStringPart.entrySet()) {
                entity.addTextBody(entry.getKey(), entry.getValue());
            }
        }
    }

   /* @Override
    public byte[] getBody() throws AuthFailureError {
        return new byte[] {1, 2, 3, 4, 5};
    }*/

    @Override
    public String getBodyContentType() {
        return httpentity.getContentType().getValue();
    }
    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            httpentity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
      //  return null;
    }
    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            System.out.println("Network Response " + new String(response.data, "UTF-8"));
            return Response.success(new String(response.data, "UTF-8"),
                    getCacheEntry());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.success(new String(response.data), getCacheEntry());
        }
    }
   /* @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (header == null) {
            return super.getHeaders();
        } else {
            return header;
        }
    }*/
    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
/*8public class MultipartRequest extends Request<String> {

    private MultipartEntity entity = new MultipartEntity();



    private static final String FILE_PART_NAME = "file";
    private static final String STRING_PART_NAME = "text";

    private final Response.Listener<String> mListener;
    private final File mFilePart;
    private final String mStringPart;

    public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, File file, String stringPart) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mFilePart = file;
        mStringPart = stringPart;
        buildMultipartEntity();
    }

    private void buildMultipartEntity() {
        entity.addPart(FILE_PART_NAME, new FileBody(mFilePart));
        try {
            entity.addPart(STRING_PART_NAME, new StringBody(mStringPart));
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
    }

    @Override
    public String getBodyContentType() {
        return null;// entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
          entity.writeTo(bos);
        Log.d("run", "now");
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success("Uploaded", getCacheEntry());
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}*/
