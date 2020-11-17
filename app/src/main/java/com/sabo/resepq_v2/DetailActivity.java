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

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.resepq_v2.Helper.Common;
import com.sabo.resepq_v2.Helper.EventRefreshList;
import com.sabo.resepq_v2.RoomDB.LocalRecipeDataSource;
import com.sabo.resepq_v2.RoomDB.RecipeDataSource;
import com.sabo.resepq_v2.RoomDB.RecipeModel;
import com.sabo.resepq_v2.RoomDB.RoomDBHost;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private RecipeDataSource recipeDataSource;

    private Toolbar toolbar;
    private ImageView ivItemImage, ivMenu;
    private TextView tvItemName, tvItemIngredient, tvHowToMake, tvTime;

      /**
     * Dialog Insert
     */
    EditText etItemName, etItemIngredient, etHowToMake;
    Button btnCancel, btnConfirm;
    Uri resultImageUri = null, pickImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        ivItemImage = findViewById(R.id.ivItemImage);
        tvItemName = findViewById(R.id.tvItemName);
        tvItemIngredient = findViewById(R.id.tvItemIngredient);
        tvHowToMake = findViewById(R.id.tvHowToMake);
        tvTime = findViewById(R.id.tvTime);
        ivMenu = findViewById(R.id.ivMenu);

        setSupportActionBar(toolbar);
        loadDetailRecipe();

        recipeDataSource = new LocalRecipeDataSource(RoomDBHost.getInstance(this).recipeDAO());

        ivMenu.setOnClickListener(v -> {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_menu, null);
            LinearLayout edit, delete;
            edit = view.findViewById(R.id.edit);
            delete = view.findViewById(R.id.delete);

            SweetAlertDialog sweetMenu = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Action");
            sweetMenu.setCanceledOnTouchOutside(true);
            sweetMenu.setOnShowListener(dialog -> {
                edit.setOnClickListener(v1 -> {
                    sweetMenu.dismissWithAnimation();
                    updateRecipe();
                });

                delete.setOnClickListener(v1 -> {
                   deleteRecipe(sweetMenu);
                });
            });
            sweetMenu.show();
            LinearLayout linearLayout = sweetMenu.findViewById(R.id.loading);
            TextView title = sweetMenu.findViewById(R.id.title_text);
            Button confirm = sweetMenu.findViewById(R.id.confirm_button);
            linearLayout.setBackground(getResources().getDrawable(R.drawable.background_dialog_gray_dark));
            confirm.setVisibility(View.GONE);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(getResources().getColor(android.R.color.white));
            int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
            linearLayout.addView(view, index + 1);
        });
    }

    /** Load Detail Recipe */
    private void loadDetailRecipe() {
        Picasso.get().load(Common.selectedRecipe.getItemImage()).into(ivItemImage);
        tvItemName.setText(Common.selectedRecipe.getItemName());
        tvItemIngredient.setText(Common.selectedRecipe.getItemIngredient());
        tvHowToMake.setText(Common.selectedRecipe.getHowToMake());

        String date = Common.formatDateInDetail(Common.selectedRecipe.getDatetime());
        tvTime.setText("Edited - " + date);
    }

    /** Delete Recipe */
    private void deleteRecipe(SweetAlertDialog sweetMenu) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete!")
                .setContentText("Are you sure?")
                .showCancelButton(true)
                .setCancelText("No")
                .setCancelClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                })
                .setConfirmText("Yes")
                .setConfirmClickListener(sweetAlertDialog -> {
                    compositeDisposable.add(recipeDataSource.deleteRecipe(Common.selectedRecipe.getItemId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                EventBus.getDefault().postSticky(new EventRefreshList(true));
                                sweetMenu.dismissWithAnimation();
                                sweetAlertDialog.setTitleText("Success!")
                                        .setContentText("Recipe has been deleted")
                                        .showCancelButton(false)
                                        .setConfirmText("Close")
                                        .setConfirmClickListener(sweetAlertDialog1 -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            finish();
                                        })
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }));

                }).show();
    }

    /** Update Recipe */
    private void updateRecipe() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update, null);


        ivItemImage = view.findViewById(R.id.ivItemImage);
        etItemName = view.findViewById(R.id.etItemName);
        etItemIngredient = view.findViewById(R.id.etItemIngredient);
        etHowToMake = view.findViewById(R.id.etHowToMake);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        resultImageUri = null;

        SweetAlertDialog sweetDialogUpdate = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        sweetDialogUpdate.setCanceledOnTouchOutside(false);
        sweetDialogUpdate.setTitleText("Edit Recipe");
        sweetDialogUpdate.setOnShowListener(dialog -> {
            Picasso.get().load(Common.selectedRecipe.getItemImage()).into(ivItemImage);
            etItemName.setText(Common.selectedRecipe.getItemName());
            etItemIngredient.setText(Common.selectedRecipe.getItemIngredient());
            etHowToMake.setText(Common.selectedRecipe.getHowToMake());

            ivItemImage.setOnClickListener(v -> {
                checkPermissions();
            });

            btnConfirm.setOnClickListener(v -> {
                String name, ingredient, howToMake;
                name = etItemName.getText().toString();
                ingredient = etItemIngredient.getText().toString();
                howToMake = etHowToMake.getText().toString();
                if (checkFields(true, name, ingredient, howToMake)) {
                    /** Insert Data */
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    updateRecipe(sweetDialogUpdate, name, ingredient, howToMake, dateTime, resultImageUri);
                }
            });

            btnCancel.setOnClickListener(v -> {
                sweetDialogUpdate.dismissWithAnimation();
                pickImageUri = null;
                resultImageUri = null;
            });
        });
        sweetDialogUpdate.show();
        LinearLayout linearLayout = sweetDialogUpdate.findViewById(R.id.loading);
        TextView title = sweetDialogUpdate.findViewById(R.id.title_text);
        Button confirm = sweetDialogUpdate.findViewById(R.id.confirm_button);
        linearLayout.setPadding(16, 10, 16, 0);
        linearLayout.setBackground(getResources().getDrawable(R.drawable.background_dialog_gray_dark));
        confirm.setVisibility(View.GONE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(getResources().getColor(android.R.color.white));
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(view, index + 1);
    }

    /** Update Recipe */
    private void updateRecipe(SweetAlertDialog sweetDialogUpdate, String name, String ingredient, String howToMake, String dateTime, Uri resultImageUri) {
        RecipeModel recipeModel = new RecipeModel();
        recipeModel.setItemId(Common.selectedRecipe.getItemId());
        recipeModel.setItemName(name);
        recipeModel.setItemIngredient(ingredient);
        recipeModel.setHowToMake(howToMake);
        recipeModel.setDatetime(dateTime);

        if (resultImageUri != null)
            recipeModel.setItemImage(resultImageUri.toString());
        else
            recipeModel.setItemImage(Common.selectedRecipe.getItemImage());

        if (recipeDataSource.isExists(Common.selectedRecipe.getItemId()) == 1){
            compositeDisposable.add(recipeDataSource.insertOrUpdateRecipe(recipeModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        sweetDialogUpdate.dismissWithAnimation();
                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success!")
                                .setContentText("Recipe updated successfully")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    finish();
                                    sweetAlertDialog.dismissWithAnimation();
                                    EventBus.getDefault().postSticky(new EventRefreshList(true));
                                })
                                .show();
                    }, throwable -> {
                        sweetDialogUpdate.dismissWithAnimation();
                        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Oops!")
                                .setContentText(throwable.getMessage())
                                .show();
                    }));
        }
    }


    private boolean checkFields(boolean checked, String name, String ingredient, String howToMake) {
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
                    ivItemImage.setBackground(getResources().getDrawable(R.drawable.border_05dp));
                    uploading.dismissWithAnimation();
                    Log.d("imageResult", resultImageUri.toString());
                }, 1000);

            }
        } else
            pickImageUri = null;
    }
}