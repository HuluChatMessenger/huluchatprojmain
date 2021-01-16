package org.plus.apps.wallet;

import com.google.gson.annotations.SerializedName;

public class WalletDataSerializer{

    public static class Wallet{

        @SerializedName("payable")
        public double payable;

        @SerializedName("deductable")
        public double deductable;

        @SerializedName("deductable")
        public double fixable;

        @SerializedName("id")
        public int id;

        @SerializedName("amount")
        public double amount;

        @SerializedName("status")
        public boolean status;

        @SerializedName("updated_at")
        public String updated_at;

        @SerializedName("created_at")
        public String created_at;


    }

    public static class Transaction{


        @SerializedName("id")
        public int id;

        @SerializedName("tied_to")
        public Transaction tied_to;

        @SerializedName("tied_from")
        public Transaction tied_from;

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
