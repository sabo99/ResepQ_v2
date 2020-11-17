package com.sabo.resepq_v2.RoomDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(version = 1, entities = RecipeModel.class, exportSchema = false)
public abstract class RoomDBHost extends RoomDatabase {

    public abstract RecipeDAO recipeDAO();
    public static RoomDBHost instance;

    public static RoomDBHost getInstance(Context context){
        if (instance == null)
            instance = Room.databaseBuilder(context, RoomDBHost.class, "Recipe")
                    .allowMainThreadQueries()
                    .build();
        return instance;
    }
}
