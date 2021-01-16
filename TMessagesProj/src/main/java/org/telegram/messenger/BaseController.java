package org.telegram.messenger;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataStorage;
import org.plus.database.DataStorage;
import org.plus.net.RequestManager;
import org.plus.wallet.WalletController;
import org.telegram.tgnet.ConnectionsManager;

public class BaseController {

    protected int currentAccount;
    private AccountInstance parentAccountInstance;

    public BaseController(int num) {
        parentAccountInstance = AccountInstance.getInstance(num);
        currentAccount = num;
    }

    protected final AccountInstance getAccountInstance() {
        return parentAccountInstance;
    }

    protected final MessagesController getMessagesController() {
        return parentAccountInstance.getMessagesController();
    }

    protected final ContactsController getContactsController() {
        return parentAccountInstance.getContactsController();
    }

    protected final MediaDataController getMediaDataController() {
        return parentAccountInstance.getMediaDataController();
    }

    protected final ConnectionsManager getConnectionsManager() {
        return parentAccountInstance.getConnectionsManager();
    }

    protected final LocationController getLocationController() {
        return parentAccountInstance.getLocationController();
    }

    protected final NotificationsController getNotificationsController() {
        return parentAccountInstance.getNotificationsController();
    }

    protected final NotificationCenter getNotificationCenter() {
        return parentAccountInstance.getNotificationCenter();
    }

    protected final UserConfig getUserConfig() {
        return parentAccountInstance.getUserConfig();
    }

    protected final MessagesStorage getMessagesStorage() {
        return parentAccountInstance.getMessagesStorage();
    }

    protected final DownloadController getDownloadController() {
        return parentAccountInstance.getDownloadController();
    }

    protected final SendMessagesHelper getSendMessagesHelper() {
        return parentAccountInstance.getSendMessagesHelper();
    }

    protected final SecretChatHelper getSecretChatHelper() {
        return parentAccountInstance.getSecretChatHelper();
    }

    protected final StatsController getStatsController() {
        return parentAccountInstance.getStatsController();
    }

    protected final FileLoader getFileLoader() {
        return parentAccountInstance.getFileLoader();
    }

    protected final FileRefController getFileRefController() {
        return parentAccountInstance.getFileRefController();
    }


    //plus
    protected final ShopDataController getShopDataController(){
        return parentAccountInstance.getShopDataController();
    }

    protected final RequestManager getRequestManager(){
        return parentAccountInstance.getRequestManager();
    }

    protected final DataStorage getDataStorage(){
        return parentAccountInstance.getDataStorage();
    }


    protected final ShopDataStorage getShopDataStorage(){
        return parentAccountInstance.getShopDataStorage();
    }

    protected final WalletController getWalletController(){
        return parentAccountInstance.getWalletController();
    }
    //
}
