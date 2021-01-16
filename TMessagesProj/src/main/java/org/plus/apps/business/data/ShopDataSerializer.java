package org.plus.apps.business.data;

import com.google.gson.annotations.SerializedName;

import org.plus.net.SuperObject;
import org.telegram.tgnet.AbstractSerializedData;

import java.util.ArrayList;
import java.util.HashMap;

public class   ShopDataSerializer {

    public static class ConfigurationsObject extends SuperObject{

        public String section_id;

        public String fieldsets;

        public void serializeToStream(AbstractSerializedData stream) {
            section_id =  stream.readString(false);
            fieldsets =  stream.readString(false);
        }

        public void readParams(AbstractSerializedData stream, boolean exception) {
            stream.writeString(section_id);
            stream.writeString(fieldsets);
        }

        public ShopDataModels.ProductConfigurationObject toDatabaseModel(){
            ShopDataModels.ProductConfigurationObject configData = new ShopDataModels.ProductConfigurationObject();
            configData.section_id = section_id;
            configData.fieldsets = fieldsets;
            return configData;
        }
    }

    public static class ProductType{

        @SerializedName("key")
        public String key;

        @SerializedName("general")
        public String general;

        @SerializedName("display_name")
        public String display_name;

        @SerializedName("icon")
        public String icon;

        @SerializedName("order")
        public int order;


        @SerializedName("sort")
        public ArrayList<Sort> sorts;

        public static class Sort{

            @SerializedName("label")
            public String label;

            @SerializedName("sortby")
            public String sortby;

        }

    }

    public static class InstaShop{

        @SerializedName("title")
        public String title;

        @SerializedName("channel")
        public long channel;

        @SerializedName("description")
        public String description;

        @SerializedName("is_verified")
        public String verified;

        @SerializedName("total_products")
        public int count;

        @SerializedName("products")
        public ArrayList<Product> products;

        @SerializedName("profile_picture")
        public ProfilePicture  profilePicture;

        public static class ProfilePicture{

            public String photo;
        }

        public static class Product {

            @SerializedName("id")
            public int id;

            @SerializedName("title")
            public String title;

            @SerializedName("price")
            public double price;

            @SerializedName("picture")
            public PictureSnip picture;
        }

    }

    public static class Collection{

        @SerializedName("id")
        public int id;

        @SerializedName("order")
        public int order;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("updated_at")
        public String updated_at;

        @SerializedName("photo")
        public PictureSnip photo;

        @SerializedName("title")
        public String title;

        @SerializedName("is_active")
        public boolean is_active;


    }

    public static class PictureSnip{

        @SerializedName("id")
        public int id;

        @SerializedName("photo")
        public String photo;

    }


    public static class ShopSnip {

        @SerializedName("title")
        public String title;

        @SerializedName("profile_picture")
        public PictureSnip profile_picture;

        @SerializedName("channel")
        public long channel;

        @SerializedName("is_verified")
        public boolean is_verified;

        @SerializedName("total_count")
        public int count;

    }

    public static class FeaturedProduct{

        @SerializedName("id")
        public int id;

        @SerializedName("title")
        public String title;

        @SerializedName("price")
        public double price;

        @SerializedName("old_price")
        public double old_price;

        @SerializedName("shop")
        public ShopSnip shopSnip;

        @SerializedName("picture")
        public PictureSnip pictureSnip;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("is_favorite")
        public boolean is_favorite;
    }

    public static class ProductOffer{

        public static class ProductSnip{

            @SerializedName("id")
            public int id;

            @SerializedName("title")
            public String title;

            @SerializedName("price")
            public double price;

            @SerializedName("picture")
            public PictureSnip pictureSnip;

            public static class PictureSnip{

                @SerializedName("id")
                public int id;

                @SerializedName("photo")
                public String photo;
            }

        }


        @SerializedName("product")
        public ProductSnip productSnip;

        @SerializedName("id")
        public int id;


        @SerializedName("price")
        public double price;


        @SerializedName("note")
        public String note;


        @SerializedName("message_link")
        public String message_link;


        @SerializedName("status")
        public String status;


        @SerializedName("created_at")
        public String created_at;


        @SerializedName("shop")
        public int shop_id;


        @SerializedName("product")
        public int product_id;


        @SerializedName("offered_by")
        public String offered_by;

    }

    public static class FilterModel {

        @SerializedName("key")
        public String key;

        @SerializedName("choices")
        public ArrayList<String[]> choices;

        @SerializedName("label")
        public String label;

        @SerializedName("default")
        public String _default;

        @SerializedName("ui_type")
        public String ui_type;

        @SerializedName("placeholder")
        public String placeholder;

    }

    public static class Sort{

        @SerializedName("label")
        public String label;

        @SerializedName("sortby")
        public String key;

    }

    public static class ImageUploadResult{

        @SerializedName("id")
        public long id;

        @SerializedName("caption")
        public String caption;

        @SerializedName("order")
        public String order;

        @SerializedName("photo")
        public String link;
    }

    public static class FieldSet{


        @SerializedName("key")
        public String key;

        @SerializedName("remote")
        public String remote;

        @SerializedName("more")
        public String more;

        @SerializedName("info")
        public String info;

        @SerializedName("header")
        public String header;

        @SerializedName("description")
        public String description;

        @SerializedName("order")
        public int order;

        @SerializedName("ui_type")
        public String type;

        @SerializedName("field_s")
        public ArrayList<Field> fields;
    }

    public static class Field{

        @SerializedName("key")
        public String key;

        @SerializedName("default")
        public String _default;

        @SerializedName("ui_type")
        public String ui_type;

        @SerializedName("min_len")
        public int min_len;

        @SerializedName("max_len")
        public int max_len;

        @SerializedName("required")
        public  boolean required;

        @SerializedName("suffix")
        public String suffix;

        @SerializedName("prefix")
        public String prefix;


        @SerializedName("placeholder")
        public String placeholder;


        @SerializedName("header")
        public String label;


        @SerializedName("meta")
        public Meta meta;

        @SerializedName("order")
        public int order;

        @SerializedName("choices")
        public ArrayList<String[]> choices;

        public static class Meta{

            @SerializedName("icon")
            public String icon;
        }

    }

    public static class ProductTag{

        @SerializedName("title")
        public String title;

        @SerializedName("type")
        public String type; //date,choose,input,select,location,phone

        @SerializedName("values")
        public ArrayList<String[]> values = new ArrayList<>();

        @SerializedName("required")
        public boolean required;

    }

    public static class TelephoneContact{

        @SerializedName("id")
        public int id;

        @SerializedName("phonenumber")
        public String phonenumber;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("update_at")
        public String update_at;

        @SerializedName("shop")
        public String shop_id;

    }

    public static class Contact extends SuperObject{

        @SerializedName("id")
        public String id;


        @SerializedName("username")
        public String username;

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

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("update_at")
        public String update_at;

        @SerializedName("shop")
        public String shop_id;
    }




    public static class ProfilePic{

        @SerializedName("id")
        public int id;

        @SerializedName("caption")
        public String caption;


        @SerializedName("order")
        public int order;


        @SerializedName("photo")
        public String photo;


        @SerializedName("created_at")
        public String created;

    }

    public static class User{


        @SerializedName("id")
        public int id;


        @SerializedName("username")
        public String username;

        @SerializedName("first_name")
        public String first_name;


        @SerializedName("last_name")
        public String last_name;

        @SerializedName("gender")
        public String gender;

        @SerializedName("phoneNumber")
        public String phoneNumber;


        @SerializedName("telegramID")
        public int telegramId;

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", first_name='" + first_name + '\'' +
                    ", last_name='" + last_name + '\'' +
                    ", gender='" + gender + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", telegramId=" + telegramId +
                    '}';
        }
    }


    public static class Shop{

        @SerializedName("id")
        public int  id;

        @SerializedName("profile_picture")
        public ProfilePic profilePic;

        @SerializedName("admins")
        public ArrayList<User> admins;

        @SerializedName("telphone_contacts")
        public ArrayList<TelephoneContact> phoneNumbers;

        @SerializedName("created_by")
        public User created_by;

        @SerializedName("rating_count")
        public int rating_count;

        @SerializedName("rating_average")
        public double rating_average;

        @SerializedName("is_verified")
        public boolean verified;

        @SerializedName("closed")
        public boolean closed;

        @SerializedName("city")
        public String city;

        @SerializedName("address")
        public String address;

        @SerializedName("contact_username")
        public String contact_username;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("latitude")
        public double lat;

        @SerializedName("longtude")
        public double _long;

        @SerializedName("updated_at")
        public String updated_at;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("channel")
        public long channel_id;

        @SerializedName("is_admin")
        public boolean isAdmin;

        @SerializedName("is_creator")
        public boolean is_creator;


        @SerializedName("can_review")
        public boolean can_review;

        @SerializedName("total_products")
        public int count;

        @SerializedName("website")
        public String website;

    }


    public  static class DetailShop extends SuperObject{

        @SerializedName("id")
        public int id;

        @SerializedName("phonenumbers")
        public ArrayList<TelephoneContact> phone_contacts;

//        @SerializedName("profile_picture")
//        public ProfilePic profilePic;
//

//        @SerializedName("banner_picture")
//        public ProfilePic bannerPic;

        @SerializedName("verified")
        public boolean isVerified;

        @SerializedName("closed")
        public boolean closed;

        @SerializedName("city")
        public String city;

        @SerializedName("address")
        public String address;

        @SerializedName("contact_username")
        public String contact_username;

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

        @SerializedName("channel")
        public long channel;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("updated_at")
        public String updated_at;


        @SerializedName("rating_count")
        public int rating_count;


        @SerializedName("rating_average")
        public double rating_average;

    }

    public static class Business{

        @SerializedName("name")
        public String name;

        @SerializedName("id")
        public int id;

        @SerializedName("photo")
        public String photo;


        @SerializedName("created_at")
        public String created_at;

        @SerializedName("business_type")
        public String business_type;

//
//        public ShopDataSerializer.ProductType toDatabaseModel(){
//            ShopDataSerializer.ProductType business = new ShopDataSerializer.ProductType();
//            business.photo = photo;
//            business.name = name;
//            business.business_type = business_type;
//            return business;
//        }

    }

    public static class ProductFull {

        @SerializedName("id")
        public int product_id;

        @SerializedName("price")
        public  double price;

        @SerializedName("title")
        public  String title;

        @SerializedName("pictures")
        public  ArrayList<ProductImage> images;

        @SerializedName("product_type")
        public String product_type;

//        @SerializedName("shop")
//        public Shop shop;

        @SerializedName("latitude")
        public double lat;

        @SerializedName("longtude")
        public double _long;

        @SerializedName("old_price")
        public double old_price;

        @SerializedName("instock")
        public boolean instock;

        @SerializedName("description")
        public String description;

        @SerializedName("usage_condition")
        public String usage_condition;

        @SerializedName("created_at")
        public String created_at;

        public HashMap<String,Object> otherFields;

    }

    public static class ProductImage{

         @SerializedName("photo")
         public String photo;

         @SerializedName("order")
         public int order;

        @SerializedName("id")
        public int id;
    }

    public static class Product{

        public static class ShopSnipt{

            @SerializedName("channel")
            public long chanel;

            @SerializedName("id")
            public int id;
        }


        @SerializedName("id")
        public int id;

        @SerializedName("shop")
        public ShopSnipt shop;


        @SerializedName("pictures")
        public ArrayList<ProductImage> images;


        @SerializedName("title")
        public String title;


        @SerializedName("price")
        public double price;


        @SerializedName("old_price")
        public double old_price;

        @SerializedName("usage_condition")
        public String usage_condition;


        @SerializedName("description")
        public String description;


        @SerializedName("instock")
        public boolean in_stock;

        @SerializedName("is_favorite")
        public boolean is_favorite;


        @SerializedName("created_at")
        public String created_at;


        @SerializedName("business_type")
        public String business_type;
    }

    public static class Review{

        @SerializedName("id")
        public int id;

        @SerializedName("comment")
        public String comment;

        @SerializedName("rating")
        public int rating;

        @SerializedName("shop")
        public int shop_id;

        @SerializedName("product")
        public int product_id;

        @SerializedName("reviewed_by")
        public ShopDataSerializer.User user;


        @SerializedName("created_at")
        public String created_at;

    }

}
