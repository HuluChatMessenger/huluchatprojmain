//package org.plus.apps.business.data;
//
//import androidx.annotation.NonNull;
//import androidx.room.ColumnInfo;
//import androidx.room.Embedded;
//import androidx.room.Entity;
//import androidx.room.Ignore;
//import androidx.room.PrimaryKey;
//import androidx.room.Relation;
//
//import com.google.gson.annotations.SerializedName;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ShopTables{
//
//    @Entity(tableName = "product_type_table")
//    public static class ProductType{
//
//        @NonNull
//        @ColumnInfo(name = "key")
//        @PrimaryKey
//        public  String key;
//
//        @ColumnInfo(name = "display_name")
//        public String display_name;
//
//        @ColumnInfo(name = "icon")
//        public String icon;
//
//        @ColumnInfo(name = "order")
//        public int order;
//
//        @SerializedName("sort")
//        private ArrayList<ProductTypeSort> sorts;
//
//        @Override
//        public String toString() {
//            return "ProductType{" +
//                    "key='" + key + '\'' +
//                    ", display_name='" + display_name + '\'' +
//                    ", icon='" + icon + '\'' +
//                    ", order=" + order +
//                    '}';
//        }
//    }
//
//    @Entity(tableName = "product_type_sort_table")
//    public static class ProductTypeSort{
//
//
//        @Exclude
//        @PrimaryKey(autoGenerate = true)
//        public int id;
//
//        @Exclude
//        @ColumnInfo(name = "product_type_id")
//        public long product_type_id;
//
//
//        @ColumnInfo(name = "label")
//        public String label;
//
//        @ColumnInfo(name = "sortby")
//        public String sortby;
//
//    }
//
//    public static class ProductTypeWithSort {
//        @Embedded
//        public ProductType productType;
//        @Relation(
//                parentColumn = "key",
//                entityColumn = "product_type_id"
//        )
//        public List<ProductTypeSort> productTypeSortList;
//    }
//
//    @Entity(tableName = "field_table")
//    public static class Field{
//
//        @PrimaryKey(autoGenerate = true)
//        public long id;
//
//        @ColumnInfo(name = "field_id")
//        public long field_id;
//
//        @ColumnInfo(name = "key")
//        public String key;
//
//        @ColumnInfo(name = "ui_type")
//        public String ui_type;
//
//        @ColumnInfo(name = "header")
//        public String header;
//
//        @ColumnInfo(name = "placeholder")
//        public String placeholder;
//
//        @ColumnInfo(name = "prefix")
//        public String prefix;
//
//        @ColumnInfo(name = "suffix")
//        public String suffix;
//
//        @ColumnInfo(name = "_default")
//        public String _default;
//
//        @ColumnInfo(name = "max_len")
//        public int max_len;
//
//        @ColumnInfo(name = "min_len")
//        public int min_len;
//
//        @ColumnInfo(name = "required")
//        public boolean required;
//
//        @ColumnInfo(name = "order")
//        public int order;
//
//        @ColumnInfo(name = "choices")
//        public ArrayList<Choice> choices;
//
//        public static class Choice{
//
//            public String key;
//            public String value;
//        }
//
//    }
//
//    @Entity(tableName = "field_sets_table")
//    public static class FieldSet{
//
//        @PrimaryKey(autoGenerate = true)
//        public long fields_set_id;
//
//        @ColumnInfo(name = "section_id")
//        public String section_id;
//
//        @ColumnInfo(name = "key")
//        public String key;
//
//        @ColumnInfo(name = "header")
//        public String header;
//
//        @ColumnInfo(name = "description")
//        public String description;
//
//        @ColumnInfo(name = "info")
//        public String info;
//
//        @ColumnInfo(name = "order")
//        public int order;
//
//        @ColumnInfo(name = "remote")
//        public String remote;
//
//        @ColumnInfo(name = "more")
//        public String more;
//
//        @ColumnInfo(name = "ui_type")
//        public String ui_type;
//    }
//
//    public static class FieldSetWithFields{
//
//        @Embedded
//        public  FieldSet fieldSet;
//        @Relation(
//                parentColumn = "fields_set_id",
//                entityColumn = "field_id"
//        )
//        public List<Field> fieldList;
//    }
//
//}
