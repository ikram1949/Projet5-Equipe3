package com.example.vipcinema.utils;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.*;
import java.util.*;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mHeaders = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // text params
            Map<String, String> params = getParams();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    buildTextPart(dos, entry.getKey(), entry.getValue());
                }
            }

            // file data
            Map<String, DataPart> data = getByteData();
            if (data != null) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    buildFilePart(dos, entry.getKey(), entry.getValue());
                }
            }

            // close
            dos.writeBytes("--" + boundary + "--\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private void buildTextPart(DataOutputStream dos, String paramName, String value) throws IOException {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n\r\n");
        dos.writeBytes(value + "\r\n");
    }

    private void buildFilePart(DataOutputStream dos, String paramName, DataPart dataFile) throws IOException {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + dataFile.getFileName() + "\"\r\n");
        dos.writeBytes("Content-Type: " + dataFile.getType() + "\r\n\r\n");

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }
        dos.writeBytes("\r\n");
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String name, byte[] data) {
            this(name, data, "application/octet-stream");
        }

        public DataPart(String name, byte[] data, String type) {
            this.fileName = name;
            this.content = data;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}