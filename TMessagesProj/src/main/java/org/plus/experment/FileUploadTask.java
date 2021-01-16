package org.plus.experment;
import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import org.plus.net.RequestManager;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Utilities;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


public class FileUploadTask {

    /**
     * state 1 = started
     * state 2 = cancled
     * state  = 3 finished
     */

    public interface  FileUploadTaskDelegate{
        void didFinishUploadingFile(FileUploadTask fileUploadTask);
        void didFailedUploadingFile(FileUploadTask fileUploadTask);
        void didChangedUploadProgress(FileUploadTask fileUploadTask,long uploadedSize, long totalSize);
    }

    private FileUploadTaskDelegate fileUploadTaskDelegate;

    private int currentAccount;
    private SharedPreferences preferences;
    private int state;
    private boolean isPhoto;
    private int order;
    private String mimeType;
    private String uploadFileLocation;

    public void setFileUploadTaskDelegate(FileUploadTaskDelegate fileUploadTaskDelegate) {
        this.fileUploadTaskDelegate = fileUploadTaskDelegate;
    }

    public FileUploadTask(int instance, String upload_file_location,int pos,boolean isPhoto){
        currentAccount = instance;
        uploadFileLocation = upload_file_location;
        order = pos;
        this.isPhoto = isPhoto;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if(TextUtils.isEmpty(type)){
            type = "image/*";
        }
        return type;
    }


    public void start(){
        if (state != 0) {
            return;
        }
        state = 1;
        Utilities.stageQueue.postRunnable(() -> {
            preferences = ApplicationLoader.applicationContext.getSharedPreferences("uploadTaskInfo", Activity.MODE_PRIVATE);
            mimeType = getMimeType(uploadFileLocation);
            startUploadRequest();
        });
    }

    private void startUploadRequest(){
        if (state != 1) {
            return;
        }
        File file  = new File(uploadFileLocation);
        if(!file.exists()){
            return;
        }
        FileUploadTaskDelegate delegate = new FileUploadTaskDelegate() {
            @Override
            public void didFinishUploadingFile(FileUploadTask fileUploadTask) {
                state = 3;
                if (fileUploadTaskDelegate != null) {
                    fileUploadTaskDelegate.didFinishUploadingFile(fileUploadTask);

                }
            }
            @Override
            public void didFailedUploadingFile(FileUploadTask fileUploadTask) {
                state = 2;
                if (fileUploadTaskDelegate != null) {
                    fileUploadTaskDelegate.didFinishUploadingFile(fileUploadTask);

                }
            }


            @Override
            public void didChangedUploadProgress(FileUploadTask fileUploadTask, long uploadedSize, long totalSize) {
                double progress = (1.0 * uploadedSize) / totalSize;
                if (fileUploadTaskDelegate != null) {
                    fileUploadTaskDelegate.didChangedUploadProgress(fileUploadTask,uploadedSize,totalSize);
                }
                Log.i("totalpercent",progress + "%");
              //  df2.setRoundingMode(RoundingMode.UP);

                //  private static DecimalFormat df2 = new DecimalFormat("#.##");


            }
        };
        RequestBody uploadRequestBody = CountingRequestBody.create( MediaType.parse(getMimeType(mimeType)),file);
        CountingRequestBody countingRequestBody = new CountingRequestBody(uploadRequestBody,delegate,this);
        String name  = "";
        if(isPhoto){
            name = "photo";
        }
        MultipartBody.Part body = MultipartBody.Part.createFormData(name, file.getName(), countingRequestBody);
        RequestBody description = RequestBody.create(MultipartBody.FORM, String.valueOf(order));
        try {
           RequestManager.getInstance(currentAccount).getShopInterface().uploadPhoto(body,description).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        if (state == 3) {
            return;
        }
        state = 2;
        fileUploadTaskDelegate.didFailedUploadingFile(this);
    }


    private static class CountingRequestBody extends RequestBody {

        private final RequestBody delegate;
        private final FileUploadTaskDelegate listener;
        private FileUploadTask fileUploadTask;

        public CountingRequestBody(RequestBody delegate, FileUploadTaskDelegate listener,FileUploadTask uploadTask) {
            this.delegate = delegate;
            this.listener = listener;
            this.fileUploadTask = uploadTask;
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return delegate.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
           CountingSink countingSink = new CountingSink(sink);
            BufferedSink bufferedSink = Okio.buffer(countingSink);

            delegate.writeTo(bufferedSink);

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
                listener.didChangedUploadProgress(fileUploadTask,bytesWritten, contentLength());

            }

        }
    }
}
