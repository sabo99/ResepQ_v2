package com.sabo.resepq_v2.RoomDB;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface RecipeDataSource {
    Flowable<List<RecipeModel>> getAllRecipeDESC();
    int listCount();
    int isExists(int itemId);
    Completable insertOrUpdateRecipe(RecipeModel... recipeModels);
    Completable deleteRecipe(int itemId);
    Completable clearAllRecipe();
}
