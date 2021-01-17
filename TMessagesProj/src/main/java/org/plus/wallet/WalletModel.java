package org.plus.wallet;

import com.google.gson.annotations.SerializedName;

import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.wallet.WalletDataSerializer;

public class WalletModel {

    public static class Provider{
        public String name;
        public int id;
        public int res;
        public boolean requireOtp;
        public String key;
    }




    public static class Wallet{

        @SerializedName("payable")
        public String payable;

        @SerializedName("deductable")
        public String deductable;

        @SerializedName("fixable")
        public String fixable;

        @SerializedName("id")
        public int id;

        @SerializedName("balance")
        public double balance;

        @SerializedName("status")
        public boolean status;//active,suspended

        @SerializedName("updated_at")
        public String updated_at;

        @SerializedName("created_at")
        public String created_at;


    }

    public static class PayUser{

        public int tg_user_id;

        public String first_name;
    }

    public static class Transaction{

        public PayUser from_user;

        public PayUser to_user;

        @SerializedName("id")
        public int id;

        @SerializedName("tied_to")
        public WalletDataSerializer.Transaction tied_to;

        @SerializedName("tied_from")
        public WalletDataSerializer.Transaction tied_from;

        @SerializedName("attribute")
        public String attribute;


        @SerializedName("description")
        public String description;


        @SerializedName("access")
        public String access;


        @SerializedName("previous_balance")
        public double previous_balance;

        @SerializedName("amount")
        public double amount;

        @SerializedName("reason")
        public String reason;

        @SerializedName("status")
        public String status;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("updated_at")
        public String updated_at;

        @SerializedName("wallet")
        public int  wallet;

        @SerializedName("transaction_type")
        public String transaction_type;

    }
}
