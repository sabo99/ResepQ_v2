package com.sabo.resepq_v2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.ferfalk.simplesearchview.SimpleSearchViewListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.resepq_v2.Adapter.RecipeAdapter;
import com.sabo.resepq_v2.Helper.Common;
import com.sabo.resepq_v2.Helper.EventCloseSearchBar;
import com.sabo.resepq_v2.Helper.EventRefreshList;
import com.sabo.resepq_v2.RoomDB.LocalRecipeDataSource;
import com.sabo.resepq_v2.RoomDB.RecipeDataSource;
import com.sabo.resepq_v2.RoomDB.RecipeModel;
import com.sabo.resepq_v2.RoomDB.RoomDBHost;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecipeDataSource recipeDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FloatingActionButton fabAdd;
    private RecyclerView rvItems;
    private TextView tvEmptyItem, tvEmptySearch;
    private RecipeAdapter recipeAdapter, searchAdapter;
    private List<RecipeModel> searchList = new ArrayList<>();
    private List<RecipeModel> resultSearchList;

    private Toolbar toolbar;
    private SimpleSearchView simpleSearchView;
    private MenuItem itemSearch, itemClearAll;

    /**
     * Dialog Insert
     */
    ImageView ivGradient, ivItemImage;
    TextView tvImageNull;
    EditText etItemName, etItemIngredient, etHowToMake;
    Button btnCancel, btnConfirm;
    Uri resultImageUri = null, pickImageUri = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (simpleSearchView.onBackPressed())
            return;

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        itemSearch = menu.findItem(R.id.action_search);
        itemClearAll = menu.findItem(R.id.action_clear);
        simpleSearchView.setMenuItem(itemSearch);

        if (recipeDataSource.listCount() == 0)
            itemClearAll.setVisible(false);
        else
            itemClearAll.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear)
            clearAllRecipe();

        return super.onOptionsItemSelected(item);
    }

    private void clearAllRecipe() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Clear All Recipe")
                .setContentText("Are you sure?")
                .showCancelButton(true)
                .setCancelText("No")
                .setCancelClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                })
                .setConfirmText("Yes")
                .setConfirmClickListener(sweetAlertDialog -> {
                    compositeDisposable.add(recipeDataSource.clearAllRecipe()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                loadAllRecipe();
                                sweetAlertDialog.showCancelButton(false)
                                        .setTitleText("Success!")
                                        .setContentText("All recipes have been deleted")
                                        .setConfirmText("Close")
                                        .setConfirmClickListener(sweetAlertDialog1 -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                        })
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }, throwable -> {
                                sweetAlertDialog.showCancelButton(false)
                                        .setTitleText("Oops!")
                                        .setContentText("Something wrong \n" + throwable.getMessage())
                                        .setConfirmText("Close")
                                        .setConfirmClickListener(sweetAlertDialog1 -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                        })
                                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            }));
                })
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        simpleSearchView = findViewById(R.id.simpleSearchView);
        fabAdd = findViewById(R.id.fabAdd);
        rvItems = findViewById(R.id.rvItems);
        tvEmptyItem = findViewById(R.id.tvEmptyItem);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);

        setSupportActionBar(toolbar);
        fabAdd.setOnClickListener(this);

        recipeDataSource = new LocalRecipeDataSource(RoomDBHost.getInstance(this).recipeDAO());
        rvItems.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvItems.setHasFixedSize(true);

        loadAllRecipe();

        simpleSearchView.setOnSearchViewListener(new SimpleSearchViewListener() {
            @Override
            public void onSearchViewClosedAnimation() {
                loadAllRecipe();
                tvEmptySearch.setVisibility(View.GONE);
                super.onSearchViewClosedAnimation();
            }
        });

        simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                startSearchByQuery(newText);
                return true;
            }

            @Override
            public boolean onQueryTextCleared() {
                return false;
            }
        });
    }

    private void startSearchByQuery(String query) {
        resultSearchList = new ArrayList<>();
        for (RecipeModel items : searchList) {
            if (items.getItemName().contains(query) || items.getItemName().toLowerCase().contains(query) ||
                    items.getItemIngredient().contains(query) || items.getItemIngredient().toLowerCase().contains(query) ||
                    items.getDatetime().contains(query))
                resultSearchList.add(items);
        }

        if (resultSearchList.isEmpty())
            tvEmptySearch.setVisibility(View.VISIBLE);
        else
            tvEmptySearch.setVisibility(View.GONE);

        searchAdapter = new RecipeAdapter(this, resultSearchList);
        rvItems.setAdapter(searchAdapter);
    }

    /**
     * Load All Recipe
     */
    private void loadAllRecipe() {
        invalidateOptionsMenu();

        if (recipeDataSource.listCount() == 0) {
            tvEmptyItem.setVisibility(View.VISIBLE);
        } else {
            tvEmptyItem.setVisibility(View.GONE);
        }

        compositeDisposable.add(recipeDataSource.getAllRecipeDESC()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recipeModels -> {
                    if (recipeModels != null || !recipeModels.isEmpty() || recipeModels.size() > 0) {
                        recipeAdapter = new RecipeAdapter(this, recipeModels);
                        rvItems.setAdapter(recipeAdapter);

                        searchList.clear();
                        searchList = recipeModels;
                    }
                }, throwable -> {
                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Oops!")
                            .setContentText(throwable.getMessage())
                            .show();

                }));
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabAdd)
            openDialogInsert();
    }

    private void openDialogInsert() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_insert, null);

        ivGradient = view.findViewById(R.id.ivGradient);
        ivItemImage = view.findViewById(R.id.ivItemImage);
        tvImageNull = view.findViewById(R.id.tvImageNull);
        etItemName = view.findViewById(R.id.etItemName);
        etItemIngredient = view.findViewById(R.id.etItemIngredient);
        etHowToMake = view.findViewById(R.id.etHowToMake);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        resultImageUri = null;

        SweetAlertDialog sweetDialogInsert = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        sweetDialogInsert.setCanceledOnTouchOutside(false);
        sweetDialogInsert.setTitleText("Add New Recipe");
        sweetDialogInsert.setOnShowListener(dialog -> {
            ivItemImage.setOnClickListener(v -> {
                checkPermissions();
            });

            btnConfirm.setOnClickListener(v -> {
                String name, ingredient, howToMake;
                name = etItemName.getText().toString();
                ingredient = etItemIngredient.getText().toString();
                howToMake = etHowToMake.getText().toString();
                if (checkFields(true, name, ingredient, howToMake, resultImageUri)) {
                    /** Insert Data */
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    insertRecipe(sweetDialogInsert, name, ingredient, howToMake, dateTime, resultImageUri);
                }
            });

            btnCancel.setOnClickListener(v -> {
                sweetDialogInsert.dismissWithAnimation();
                pickImageUri = null;
                resultImageUri = null;
            });
        });
        sweetDialogInsert.show();
        LinearLayout linearLayout = sweetDialogInsert.findViewById(R.id.loading);
        TextView title = sweetDialogInsert.findViewById(R.id.title_text);
        Button confirm = sweetDialogInsert.findViewById(R.id.confirm_button);
        linearLayout.setPadding(16, 10, 16, 0);
        linearLayout.setBackground(getResources().getDrawable(R.drawable.background_dialog_gray_dark));
        confirm.setVisibility(View.GONE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(getResources().getColor(android.R.color.white));
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(view, index + 1);
    }

    /**
     * Insert Data Recipe
     */
    private void insertRecipe(SweetAlertDialog sweetDialogInsert, String name, String ingredient, String howToMake, String dateTime, Uri resultImageUri) {
        RecipeModel recipeModel = new RecipeModel();
        recipeModel.setItemImage(resultImageUri.toString());
        recipeModel.setItemName(name);
        recipeModel.setItemIngredient(ingredient);
        recipeModel.setHowToMake(howToMake);
        recipeModel.setDatetime(dateTime);

        compositeDisposable.add(recipeDataSource.insertOrUpdateRecipe(recipeModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    sweetDialogInsert.dismissWithAnimation();
                    loadAllRecipe();
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Success!")
                            .setContentText("Recipe inserted successfully")
                            .show();
                }, throwable -> {
                    sweetDialogInsert.dismissWithAnimation();
                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Oops!")
                            .setContentText(throwable.getMessage())
                            .show();
                }));

    }

    private boolean checkFields(boolean checked, String name, String ingredient, String howToMake, Uri resultImageUri) {
        String message = "Please fill out this field!";
        if (TextUtils.isEmpty(name) || name.equals("")) {
            checked = false;
            etItemName.setError(message);
        }
        if (TextUtils.isEmpty(ingredient) || ingredient.equals("")) {
            checked = false;
            etItemIngredient.setError(message);
        }
        if (TextUtils.isEmpty(howToMake) || howToMake.equals("")) {
            checked = false;
            etHowToMake.setError(message);
        }
        if (resultImageUri == null) {
            checked = false;
            tvImageNull.setVisibility(View.VISIBLE);
            ivItemImage.setBackground(getResources().getDrawable(R.drawable.border_error));
        }
        if (resultImageUri != null) {
            tvImageNull.setVisibility(View.GONE);
            ivItemImage.setBackground(getResources().getDrawable(R.drawable.border_05dp));
        }
        return checked;
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, Common.REQUEST_PERMISSION_UPLOAD);
        else
            openGallery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Common.REQUEST_PERMISSION_UPLOAD && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            openGallery();
    }

    private void openGallery() {
        CropImage.startPickImageActivity(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            pickImageUri = CropImage.getPickImageResultUri(this, data);
            CropImage.activity(pickImageUri)
                    .setAspectRatio(5, 3)
                    .setRequestedSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultImageUri = result.getUri();

                SweetAlertDialog uploading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                uploading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                uploading.setTitleText("Uploading...").show();

                new Handler().postDelayed(() -> {
                    ivItemImage.setImageURI(resultImageUri);
                    ivGradient.setVisibility(View.VISIBLE);
                    tvImageNull.setVisibility(View.GONE);
                    ivItemImage.setBackground(getResources().getDrawable(R.drawable.border_05dp));
                    uploading.dismissWithAnimation();
                    Log.d("imageResult", resultImageUri.toString());
                }, 1000);

            }
        } else
            pickImageUri = null;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRefreshList(EventRefreshList event) {
        if (event.isRefresh()) {
            loadAllRecipe();
            event.setRefresh(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCloseSearchBar(EventCloseSearchBar event) {
        if (event.isClosed()) {
            simpleSearchView.closeSearch();
            event.setClosed(false);
        }
    }
}