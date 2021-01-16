package org.plus.net;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


public class CountingRequestBody extends RequestBody {

    private final RequestBody requestBody;
    private final FileUploadTaskDelegate listener;

    public CountingRequestBody(RequestBody requestBody, FileUploadTaskDelegate listener) {
        this.requestBody = requestBody;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return requestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        requestBody.writeTo(bufferedSink);

        bufferedSink.flush();
    }

    final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;

        CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(@NonNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            AndroidUtilities.runOnUIThread(() -> listener.didChangedUploadProgress(bytesWritten, contentLength()));
        }

    }




    public interface  FileUploadTaskDelegate{

        void didFinishUploadingFile(long id);
        void didFailedUploadingFile();
        void didChangedUploadProgress(long uploadedSize, long totalSize);

    }

}