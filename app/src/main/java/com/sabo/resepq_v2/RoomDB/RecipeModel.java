package com.sabo.resepq_v2.RoomDB;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Recipe")
public class RecipeModel {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "itemId")
    private int itemId;

    @ColumnInfo(name = "itemImage")
    private String itemImage;

    @ColumnInfo(name = "itemName")
    private String itemName;

    @ColumnInfo(name = "itemIngredient")
    private String itemIngredient;

    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "howToMake")
    private String howToMake;

    @NonNull
    public int getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull int itemId) {
        this.itemId = itemId;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemIngredient() {
        return itemIngredient;
    }

    public void setItemIngredient(String itemIngredient) {
        this.itemIngredient = itemIngredient;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getHowToMake() {
        return howToMake;
    }

    public void setHowToMake(String howToMake) {
        this.howToMake = howToMake;
    }
}
