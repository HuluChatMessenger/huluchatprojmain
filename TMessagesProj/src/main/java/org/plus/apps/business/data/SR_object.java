package org.plus.apps.business.data;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SR_object{


    public static class ImageUpdateReq{

        @SerializedName("photo")
        public int id;


        @Override
        public String toString() {
            return "ImageUpdateReq{" +
                    "id=" + id +
                    '}';
        }
    }


    public static class post_review_request{

        @SerializedName("rating")
        public int rating;

        @SerializedName("comment")
        public String comment;

        @Override
        public String toString() {
            return "post_review_request{" +
                    "rating=" + rating +
                    ", comment='" + comment + '\'' +
                    '}';
        }
    }

    public static class create_offer_req{

        @SerializedName("price")
        public double price;

        @SerializedName("note")
        public String note;


        @SerializedName("message_link")
        public String message_link;

        @Override
        public String toString() {
            return "create_offer_req{" +
                    "price=" + price +
                    ", note='" + note + '\'' +
                    ", message_link='" + message_link + '\'' +
                    '}';
        }
    }

    public static class shop_create_res{

        @SerializedName("business")
        public String business;

        @SerializedName("address")
        public String address;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("channel")
        public long telegramID;

    }

    public static class Phone_number{

        @SerializedName("phonenumbers")
        public ArrayList<String> phonenumbers;

        @Override
        public String toString() {
            return "Phone_number{" +
                    "phonenumbers=" + phonenumbers +
                    '}';
        }
    }

    public static class create_shop_req{


        @SerializedName("telegram_channel")
        public long  channel_id;


        @SerializedName("telphone_contacts")
        public Phone_number phone_numbers;


        @SerializedName("closed")
        public boolean closed;


        @SerializedName("city")
        public String city;


        @SerializedName("address")
        public String address;


        @SerializedName("contact_username")
        public String admin_username;


        @SerializedName("website")
        public String website;


        @SerializedName("title")
        public String title;


        @SerializedName("description")
        public String description;


        @SerializedName("latitude")
        public double lat;


        @SerializedName("longtude")
        public double _long;

        @SerializedName("banner_picture")
        public long banner;


        @SerializedName("profile_picture")
        public long profile;

    }

    public static class shop_create_req {

        @SerializedName("telegram_channel")
        public long  channel_id;

        @SerializedName("address")
        public String  address;

        @SerializedName("latitude")
        public double lat;

         @SerializedName("longtude")
        public double _long;

         @SerializedName("city")
         public String city;

         @SerializedName("closed")
         public boolean closed;

        @SerializedName("title")
         public String title;

        @SerializedName("description")
        public String description;


        @SerializedName("contact_username")
        public String contact_username;


        @SerializedName("banner_picture")
        public String banner_picture;


        @SerializedName("profile_picture")
        public String profile_picture;

        @SerializedName("phonenumbers")
        public ArrayList<String> phones;

        @SerializedName("contact")
        public shop_contact_update_req contact;

        @Override
        public String toString() {
            return "shop_create_req{" +
                    "channel_id=" + channel_id +
                    ", address='" + address + '\'' +
                    ", lat=" + lat +
                    ", _long=" + _long +
                    ", city='" + city + '\'' +
                    ", closed=" + closed +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", contact_username='" + contact_username + '\'' +
                    ", banner_picture='" + banner_picture + '\'' +
                    ", profile_picture='" + profile_picture + '\'' +
                    '}';
        }
    }

    public static class shop_phone_update_req{

        @SerializedName("phonenumbers")
        public String phones;

        public long channel_id;

        @Override
        public String toString() {
            return "shop_phone_update_req{" +
                    "phones='" + phones + '\'' +
                    ", channel_id=" + channel_id +
                    '}';
        }
    }

    public static class shop_contact_update_req{

        @SerializedName("email")
        public String email;

        @SerializedName("website")
        public String website;

        @SerializedName("facebook")
        public String facebook;

        @SerializedName("instagram")
        public String instagram;


        @SerializedName("twitter")
        public String twitter;

        @SerializedName("youtube")
        public String youtube;

         @SerializedName("shop")
         public String shop_id;

        @Override
        public String toString() {
            return "shop_contact_update_req{" +
                    "email='" + email + '\'' +
                    ", website='" + website + '\'' +
                    ", facebook='" + facebook + '\'' +
                    ", instagram='" + instagram + '\'' +
                    ", twitter='" + twitter + '\'' +
                    ", youtube='" + youtube + '\'' +
                    ", shop_id='" + shop_id + '\'' +
                    '}';
        }
    }

}
