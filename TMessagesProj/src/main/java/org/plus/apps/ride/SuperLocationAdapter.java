package org.plus.apps.ride;

import android.location.Location;
import android.os.Build;
import android.util.Log;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.ride.data.RideObjects;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Adapters.BaseLocationAdapter;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public abstract class SuperLocationAdapter extends RecyclerListView.SelectionAdapter {


   private SearchProgressDelegate searchProgressDelegate;

    public void setDelegate(SearchProgressDelegate delegate) {
        this.searchProgressDelegate = delegate;
    }

    public interface SearchProgressDelegate {

        void onSearchStart();

        void onSearchStop();
    }

    public interface SuperLocationAdapterDelegate {
        void didLoadSearchResult(ArrayList<RideObjects.SearchLocation> places);
    }
    protected boolean searching;
    protected ArrayList<RideObjects.SearchLocation> places = new ArrayList<>();
    private Location lastSearchLocation;
    private String lastSearchQuery;
    private String lastFoundQuery;
    private SuperLocationAdapterDelegate delegate;
    private Runnable searchRunnable;
    private int currentRequestNum;
    private int currentAccount = UserConfig.selectedAccount;
    private boolean searchInProgress;

    public void destroy() {
        if (currentRequestNum != 0) {
            ShopDataController.getInstance(currentAccount).cancelRequest(currentRequestNum);
            currentRequestNum = 0;
        }
    }

    public void setDelegate(SuperLocationAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    public boolean isSearching() {
        return searchInProgress;
    }


    public String getLastSearchString() {
        return lastFoundQuery;
    }

    public void searchDelayed(final String query, final Location coordinate) {
        if (query == null || query.length() == 0) {
            places.clear();
            searchInProgress = false;
            notifyDataSetChanged();
        } else {
            if (searchRunnable != null) {
                Utilities.searchQueue.cancelRunnable(searchRunnable);
                searchRunnable = null;
            }
            if(searchProgressDelegate != null){
                searchProgressDelegate.onSearchStart();
            }
            searchInProgress = true;
            Utilities.searchQueue.postRunnable(searchRunnable = () -> AndroidUtilities.runOnUIThread(() -> {
                searchRunnable = null;
                lastSearchLocation = null;
                searchPlacesWithQuery(query, coordinate,false);
            }), 400);
        }
    }

    public void searchPlacesWithQuery(final String query, final Location coordinate, boolean animated) {
        if (coordinate == null || lastSearchLocation != null) {
            return;
        }
        //coordinate.distanceTo(lastSearchLocation) < 200
        Log.i("berhanzak", "searchPlacesWithQuery" + query );

        lastSearchLocation = new Location(coordinate);
        lastSearchQuery = query;
        if (searching) {
            searching = false;
            if (currentRequestNum != 0) {
                ShopDataController.getInstance(currentAccount).cancelRequest(currentRequestNum);
                currentRequestNum = 0;
            }
        }
        int oldItemCount = getItemCount();
        boolean wasSearching = searching;
        searching = true;


        Log.i("berhanzak", "search called for query = " + query );

        //plus
        currentRequestNum =  ShopDataController.getInstance(currentAccount).searchPlaces(query, coordinate, true, true, (response, error) -> AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                currentRequestNum = 0;
                searching = false;
                places.clear();
                searchInProgress = false;
                lastFoundQuery = query;
                if(error == null){
                     places =  (ArrayList<RideObjects.SearchLocation>)response;
                }
                if (delegate != null) {
                    delegate.didLoadSearchResult(places);
                }
                notifyDataSetChanged();
                if(searchProgressDelegate != null){
                    searchProgressDelegate.onSearchStop();
                }
            }
        }));

        if (animated && Build.VERSION.SDK_INT >= 19) {
            if (places.isEmpty() || wasSearching) {
                if (!wasSearching) {
                    notifyItemChanged(getItemCount() - 1);
                }
            } else {
                int placesCount = places.size() + 1;
                int offset = oldItemCount - placesCount;
                notifyItemInserted(offset);
                notifyItemRangeRemoved(offset, placesCount);
            }
        } else {
            notifyDataSetChanged();
        }

    }

}
