package com.sabo.resepq_v2.RoomDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface RecipeDAO {

    @Query("SELECT * FROM Recipe ORDER BY itemId ASC")
    Flowable<List<RecipeModel>> getAllRecipeASC();

    @Query("SELECT * FROM Recipe ORDER BY itemId DESC")
    Flowable<List<RecipeModel>> getAllRecipeDESC();

    @Query("SELECT COUNT(*) FROM Recipe")
    int listCount();

    @Query("SELECT EXISTS(SELECT 1 FROM Recipe WHERE itemId=:itemId)")
    int isExists(int itemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdateRecipe(RecipeModel... recipeModels);

    @Query("DELETE FROM Recipe WHERE itemId=:itemId")
    Completable deleteRecipe(int itemId);

    @Query("DELETE FROM Recipe")
    Completable clearAllRecipe();
}
