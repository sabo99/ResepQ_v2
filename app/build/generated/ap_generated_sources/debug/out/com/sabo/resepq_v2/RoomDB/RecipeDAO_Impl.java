package com.sabo.resepq_v2.RoomDB;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.RxRoom;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.Void;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class RecipeDAO_Impl implements RecipeDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecipeModel> __insertionAdapterOfRecipeModel;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecipe;

  private final SharedSQLiteStatement __preparedStmtOfClearAllRecipe;

  public RecipeDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecipeModel = new EntityInsertionAdapter<RecipeModel>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `Recipe` (`itemId`,`itemImage`,`itemName`,`itemIngredient`,`datetime`,`howToMake`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, RecipeModel value) {
        stmt.bindLong(1, value.getItemId());
        if (value.getItemImage() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getItemImage());
        }
        if (value.getItemName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getItemName());
        }
        if (value.getItemIngredient() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getItemIngredient());
        }
        if (value.getDatetime() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getDatetime());
        }
        if (value.getHowToMake() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getHowToMake());
        }
      }
    };
    this.__preparedStmtOfDeleteRecipe = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM Recipe WHERE itemId=?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllRecipe = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM Recipe";
        return _query;
      }
    };
  }

  @Override
  public Completable insertOrUpdateRecipe(final RecipeModel... recipeModels) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecipeModel.insert(recipeModels);
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
        }
      }
    });
  }

  @Override
  public Completable deleteRecipe(final int itemId) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecipe.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, itemId);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
          __preparedStmtOfDeleteRecipe.release(_stmt);
        }
      }
    });
  }

  @Override
  public Completable clearAllRecipe() {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllRecipe.acquire();
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
          __preparedStmtOfClearAllRecipe.release(_stmt);
        }
      }
    });
  }

  @Override
  public Flowable<List<RecipeModel>> getAllRecipeDESC() {
    final String _sql = "SELECT * FROM Recipe ORDER BY itemId DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return RxRoom.createFlowable(__db, false, new String[]{"Recipe"}, new Callable<List<RecipeModel>>() {
      @Override
      public List<RecipeModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemId = CursorUtil.getColumnIndexOrThrow(_cursor, "itemId");
          final int _cursorIndexOfItemImage = CursorUtil.getColumnIndexOrThrow(_cursor, "itemImage");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfItemIngredient = CursorUtil.getColumnIndexOrThrow(_cursor, "itemIngredient");
          final int _cursorIndexOfDatetime = CursorUtil.getColumnIndexOrThrow(_cursor, "datetime");
          final int _cursorIndexOfHowToMake = CursorUtil.getColumnIndexOrThrow(_cursor, "howToMake");
          final List<RecipeModel> _result = new ArrayList<RecipeModel>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final RecipeModel _item;
            _item = new RecipeModel();
            final int _tmpItemId;
            _tmpItemId = _cursor.getInt(_cursorIndexOfItemId);
            _item.setItemId(_tmpItemId);
            final String _tmpItemImage;
            _tmpItemImage = _cursor.getString(_cursorIndexOfItemImage);
            _item.setItemImage(_tmpItemImage);
            final String _tmpItemName;
            _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
            _item.setItemName(_tmpItemName);
            final String _tmpItemIngredient;
            _tmpItemIngredient = _cursor.getString(_cursorIndexOfItemIngredient);
            _item.setItemIngredient(_tmpItemIngredient);
            final String _tmpDatetime;
            _tmpDatetime = _cursor.getString(_cursorIndexOfDatetime);
            _item.setDatetime(_tmpDatetime);
            final String _tmpHowToMake;
            _tmpHowToMake = _cursor.getString(_cursorIndexOfHowToMake);
            _item.setHowToMake(_tmpHowToMake);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public int listCount() {
    final String _sql = "SELECT COUNT(*) FROM Recipe";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if(_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int isExists(final int itemId) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM Recipe WHERE itemId=?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, itemId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if(_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
