package org.plus.experment;

import android.util.SparseArray;

import org.telegram.messenger.BaseController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.UserConfig;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class DataLoader extends BaseController {


    public interface  DataLoaderDelegate{

        void fileUploadProgressChanged(String location, long uploadedSize, long totalSize);

        void fileDidFailedUpload(String location);

        void fileDidUploaded(String location, long totalFileSize);

        void fileDidUploaded(String location,long photo_id ,long totalFileSize);

        int getObserverTag();


    }

    public interface PhotoUploadDelegate{

        void onPhotoUploaded(String photo,long id);
    }

    private ConcurrentHashMap<String,DataLoaderDelegate> delegateConcurrentHashMap = new ConcurrentHashMap<>();
    private volatile static DispatchQueue dataLoaderQueue = new DispatchQueue("dataLoaderQueue");
    private LinkedList<FileUploadTask> uploadTaskQueue = new LinkedList<>();
    private ConcurrentHashMap<String, FileUploadTask> uploadTaskPaths = new ConcurrentHashMap<>();
    private int currentUploadTaskCount = 0;

    private SparseArray<String> observersByTag = new SparseArray<>();
    private boolean listenerInProgress = false;
    private HashMap<String, DataLoaderDelegate> addLaterArray = new HashMap<>();
    private ArrayList<DataLoaderDelegate> deleteLaterArray = new ArrayList<>();
    private int lastTag = 0;
    private HashMap<String, WeakReference<DataLoaderDelegate>> loadingFileObservers = new HashMap<>();


    private static volatile DataLoader[] Instance = new DataLoader[UserConfig.MAX_ACCOUNT_COUNT];
    public static DataLoader getInstance(int num) {
        DataLoader localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (DataLoader.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new DataLoader(num);
                }
            }
        }
        return localInstance;
    }


    public DataLoader(int instance) {
        super(instance);
    }

    public void cancelUploadFile(final String location){
        dataLoaderQueue.postRunnable(() -> {
            FileUploadTask fileUploadTask = uploadTaskPaths.get(location);
            if(fileUploadTask != null){
                uploadTaskQueue.remove(fileUploadTask);
                fileUploadTask.cancel();
            }
        });
    }


    public void uploadFile(final String location,int order,boolean isImage){
        if(location == null){
            return;
        }
        File file = new File(location);
        if(!file.exists()){
            return;
        }
        WeakReference<DataLoaderDelegate> reference = loadingFileObservers.get(location);
        if (reference != null) {
            reference.get().fileDidFailedUpload(location);
            observersByTag.remove(reference.get().getObserverTag());
        }

        dataLoaderQueue.postRunnable(() -> {
            if(uploadTaskPaths.containsKey(location)){
                return;
            }
            if(reference != null){
                reference.get().fileUploadProgressChanged(location,0,file.length());
            }


            FileUploadTask fileUploadTask = new FileUploadTask(currentAccount,location, order,isImage);
            fileUploadTask.setFileUploadTaskDelegate(new FileUploadTask.FileUploadTaskDelegate() {
                @Override
                public void didFinishUploadingFile(FileUploadTask fileUploadTask) {
                    dataLoaderQueue.postRunnable(() -> {
                        uploadTaskPaths.remove(location);
                        currentUploadTaskCount--;
                        if (currentUploadTaskCount < 1) {
                            FileUploadTask operation12 = uploadTaskQueue.poll();
                            if (operation12 != null) {
                                currentUploadTaskCount++;
                                operation12.start();
                            }
                        }
                        if(reference != null){
                            reference.get().fileDidUploaded(location,file.length());
                        }
                    });
                }
                @Override
                public void didFailedUploadingFile(FileUploadTask fileUploadTask) {

                    dataLoaderQueue.postRunnable(() -> {
                        uploadTaskPaths.remove(location);

                        if(reference != null){
                            reference.get().fileDidFailedUpload(location);
                        }
                        currentUploadTaskCount--;
                        if(currentUploadTaskCount < 1){
                          FileUploadTask fileUploadTask1 =  uploadTaskQueue.poll();
                          if(fileUploadTask1 != null){
                              currentUploadTaskCount++;
                              fileUploadTask1.start();
                          }
                        }
                    });
                }
                @Override
                public void didChangedUploadProgress(FileUploadTask fileUploadTask, long uploadedSize, long totalSize) {
                    if (reference != null) {
                        reference.get().fileUploadProgressChanged(location, uploadedSize, totalSize);
                    }
                }
            });
            if(currentUploadTaskCount < 1){
                currentUploadTaskCount++;
                fileUploadTask.start();
            }else{
                uploadTaskQueue.add(fileUploadTask);
            }
        });
    }

    public int generateObserverTag() {
        return lastTag++;
    }


    public void addLoadingFileObserver(String fileName, DataLoaderDelegate observer) {
        if (listenerInProgress) {
            addLaterArray.put(fileName, observer);
            return;
        }
        removeLoadingFileObserver(observer);
        WeakReference<DataLoaderDelegate> arrayList = loadingFileObservers.get(fileName);
        loadingFileObservers.put(fileName, arrayList);
        observersByTag.put(observer.getObserverTag(), fileName);
    }

    public void removeLoadingFileObserver(DataLoaderDelegate observer) {
        if (listenerInProgress) {
            deleteLaterArray.add(observer);
            return;
        }
        String fileName = observersByTag.get(observer.getObserverTag());

        if (fileName != null) {
           WeakReference<DataLoaderDelegate> arrayList = loadingFileObservers.get(fileName);

//            if (arrayList != null) {
//                for (int a = 0; a < arrayList.size(); a++) {
//                    WeakReference<DataLoaderDelegate> reference = arrayList.get(a);
//                    if (reference.get() == null || reference.get() == observer) {
//                        arrayList.remove(a);
//                        a--;
//                    }
//                }
//
//                if (arrayList.isEmpty()) {
//                    loadingFileObservers.remove(fileName);
//                }
//            }
            observersByTag.remove(observer.getObserverTag());
        }
    }

    private void processLaterArrays() {
        for (HashMap.Entry<String,DataLoaderDelegate> listener : addLaterArray.entrySet()) {
            addLoadingFileObserver(listener.getKey(), listener.getValue());
        }
        addLaterArray.clear();
        for (DataLoaderDelegate listener : deleteLaterArray) {
            removeLoadingFileObserver(listener);
        }
        deleteLaterArray.clear();
    }

}
