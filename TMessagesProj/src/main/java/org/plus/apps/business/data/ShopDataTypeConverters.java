//package org.plus.apps.business.data;
//
//import androidx.room.TypeConverter;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//
//public class ShopDataTypeConverters {
//
//    @TypeConverter
//    public String convert(ArrayList<ShopTables.Field.Choice> choices){
//        Gson gson  = new Gson();
//        Type listType = new TypeToken<ArrayList<ShopTables.Field.Choice>>(){}.getType();
//        return gson.toJson(choices, listType);
//    }
//
//    @TypeConverter
//    public ArrayList<ShopTables.Field.Choice> convert(String string){
//        Gson gson  = new Gson();
//        Type listType = new TypeToken<ArrayList<ShopTables.Field.Choice>>(){}.getType();
//        return gson.fromJson(string, listType);
//    }
//
//}
