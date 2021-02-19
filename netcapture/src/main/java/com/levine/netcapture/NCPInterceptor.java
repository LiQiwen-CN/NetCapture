package com.levine.netcapture;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class NCPInterceptor implements Interceptor {

    private HCNetDataConvert mConvert;

    public NCPInterceptor() {
    }

    public NCPInterceptor(HCNetDataConvert convert) {
        mConvert = convert;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        return response(chain.proceed(request(chain.request())));
    }

    /**
     * 请求处理
     */
    private Request request(Request request) {
        Request requestTag = request.newBuilder().addHeader("REQUEST_TAG", request.hashCode() + "").build();
        LocalNetRecordIO.saveRequest(requestTag, mConvert);
        return requestTag;
    }

    /**
     * 响应处理
     */
    private Response response(Response response) {
        try {
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            String body = buffer.clone().readString(charset);
            LocalNetRecordIO.saveResponse(body, response.newBuilder().build(), mConvert);
        } catch (Exception e) {
        }
        return response;
    }
}
