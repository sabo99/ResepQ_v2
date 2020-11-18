package com.sabo.resepq_v2.RoomDB;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class LocalRecipeDataSource implements RecipeDataSource {

    private RecipeDAO recipeDAO;

    public LocalRecipeDataSource(RecipeDAO recipeDAO) {
        this.recipeDAO = recipeDAO;
    }

    @Override
    public Flowable<List<RecipeModel>> getAllRecipeDESC() {
        return recipeDAO.getAllRecipeDESC();
    }

    @Override
    public int listCount() {
        return recipeDAO.listCount();
    }

    @Override
    public int isExists(int itemId) {
        return recipeDAO.isExists(itemId);
    }

    @Override
    public Completable insertOrUpdateRecipe(RecipeModel... recipeModels) {
        return recipeDAO.insertOrUpdateRecipe(recipeModels);
    }

    @Override
    public Completable deleteRecipe(int itemId) {
        return recipeDAO.deleteRecipe(itemId);
    }

    @Override
    public Completable clearAllRecipe() {
        return recipeDAO.clearAllRecipe();
    }
}
