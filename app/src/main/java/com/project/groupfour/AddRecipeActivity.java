package com.project.groupfour;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.project.groupfour.models.RecipeModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int PICK_IMAGE_REQUEST = 1;
    Toolbar tb;

    private ImageView recipeImg;
    private String category;
    private String subCategory;

    private String c1;
    private String c2;
    private String cat_sub;

    private RatingBar recipeRating;
    private EditText recipeName;
    private EditText prepTime;
    private EditText ingredients;
    private EditText recipe;
    private ImageButton uploadPhoto;
    private Button saveRecipe;
    private Button deleteRecipe;

    private Button coffeeCategory;
    private Button iceBlendCategory;
    private Button teaCategory;
    private Button frappeCategory;

    private Uri imageUri;
    ProgressDialog pd;
    private Dialog dialog;
    private AlertDialog.Builder subCatDialogBuilder;
    private AlertDialog subCatDialog;

    //firebase stuff
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private ArrayList<String> recipeData = new ArrayList<>();
    private String recipeID;
    String prevActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        //setup toolbar
        tb = (Toolbar) findViewById(R.id.recipe_toolbar);
        setSupportActionBar(tb);

        //adding the back arrow in toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recipeImg = findViewById(R.id.recipe_image_view);
        recipeRating = findViewById(R.id.recipe_rating_view);
        recipeName = findViewById(R.id.txt_recipe_name);
        prepTime = findViewById(R.id.txt_prep_time);
        ingredients = findViewById(R.id.txt_ingredview);
        recipe = findViewById(R.id.txt_recipe_procedure);
        uploadPhoto = findViewById(R.id.upload_photo);
        saveRecipe = findViewById(R.id.edit_button);
        deleteRecipe = findViewById(R.id.delete_button);

        mStorageRef = FirebaseStorage.getInstance().getReference("Recipes");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Recipes");

        pd = new ProgressDialog(this);
        dialog = new Dialog(AddRecipeActivity.this);
        dialog.setContentView(R.layout.dialog_category);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.white_background));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //dialog.setCancelable(false);  //clicking outside the dialog box will not close the dialog
        coffeeCategory = dialog.findViewById(R.id.coffee_button);
        iceBlendCategory = dialog.findViewById(R.id.iceblend_button);
        teaCategory = dialog.findViewById(R.id.tea_button);
        frappeCategory = dialog.findViewById(R.id.frappe_button);

        coffeeCategory.setOnClickListener(this);
        iceBlendCategory.setOnClickListener(this);
        teaCategory.setOnClickListener(this);
        frappeCategory.setOnClickListener(this);

        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        saveRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uploadRecipe();
                dialog.show();
            }
        });

        deleteRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent i = new Intent(AddRecipeActivity.this, UserHome.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);*/
            }
        });

        Intent i = getIntent();
        recipeData = (ArrayList<String>) i.getSerializableExtra("RECIPE_DATA");
        prevActivity = i.getStringExtra("FROM_ACTIVITY");
        if(prevActivity.equals("RecipeActivity")){
            deleteRecipe.setVisibility(View.VISIBLE);
            deleteAndUpdateRecipe();
        }

        deleteRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Deleting Data");
                pd.show();
                mDatabaseRef.child(recipeID).removeValue();
                pd.dismiss();
                Intent i = new Intent(AddRecipeActivity.this, UserHome.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.coffee_button:
                category = "Coffee";
                c1 = "coffee";
                dialog.dismiss();
                tempSubCatDialog();
                //Toast.makeText(AddRecipeActivity.this, "Coffee Button", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iceblend_button:
                category = "Ice Blended";
                c1 = "ice";
                dialog.dismiss();
                baseSubCatDialog();
                //Toast.makeText(AddRecipeActivity.this, "Ice Blend Button", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tea_button:
                category = "Tea";
                c1 = "tea";
                dialog.dismiss();
                tempSubCatDialog();
                //Toast.makeText(AddRecipeActivity.this, "Tea Button", Toast.LENGTH_SHORT).show();
                break;
            case R.id.frappe_button:
                category = "Frappe";
                c1 = "frappe";
                dialog.dismiss();
                baseSubCatDialog();
                //Toast.makeText(AddRecipeActivity.this, "Frappe Button", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void baseSubCatDialog() {
        subCatDialogBuilder = new AlertDialog.Builder(AddRecipeActivity.this).setTitle("Select a Sub-category");
        subCatDialogBuilder.setCancelable(true);

        // Radio Button Starts here
        final String[] temp = {"Fruit-Based","Coffee-Based", "Milk-Based"};
        int checkedItem = -1;
        subCatDialogBuilder.setSingleChoiceItems(temp, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        subCategory = "Fruit-Based";
                        c2 = "_one";
                        //Toast.makeText(AddRecipeActivity.this, "Selected Fruit", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        subCategory = "Coffee-Based";
                        c2 = "_two";
                        //Toast.makeText(AddRecipeActivity.this, "Selected Coffee", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        subCategory = "Milk-Based";
                        c2 = "_three";
                        //Toast.makeText(AddRecipeActivity.this, "Selected Milk", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        subCatDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(prevActivity.equals("RecipeActivity")){
                            updateRecipe();
                        }else{
                            uploadRecipe();
                        }
                    }
                });
        subCatDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(AddRecipeActivity.this, "Cancel Culture Legoo", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

        subCatDialog = subCatDialogBuilder.create();
        subCatDialog.show();
    }

    private void tempSubCatDialog() {
        subCatDialogBuilder = new AlertDialog.Builder(AddRecipeActivity.this).setTitle("Select a Sub-category");
        subCatDialogBuilder.setCancelable(true);

        // Radio Button Starts here
        final String[] temp = {"Hot","Cold"};
        int checkedItem = -1;
        subCatDialogBuilder.setSingleChoiceItems(temp, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        subCategory = "Hot";
                        c2 = "_one";

                        //Toast.makeText(AddRecipeActivity.this, "Selected Hot", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        subCategory = "Cold";
                        c2 = "_two";

                        //Toast.makeText(AddRecipeActivity.this, "Selected Cold", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        subCatDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(prevActivity.equals("RecipeActivity")){
                            updateRecipe();
                        }else{
                            uploadRecipe();
                        }
                    }
                });
        subCatDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(AddRecipeActivity.this, "Upload Cancelled", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

        subCatDialog = subCatDialogBuilder.create();
        subCatDialog.show();
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    //upload recipe image to Firebase Storage
    private void uploadRecipe() {
        pd.setMessage("Uploading, please wait");
        pd.show();

        if(imageUri != null){
            //sets the name of images based on your computer time
            final StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            StorageTask uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();

                        pd.dismiss();

                        String rname = recipeName.getText().toString().trim();
                        String cat = category;
                        String subcat = subCategory;
                        String catsub = c1.concat(c2);
                        String rating = String.valueOf(recipeRating.getRating());
                        String ptime = prepTime.getText().toString().trim();
                        String ingred = ingredients.getText().toString();
                        String rec = recipe.getText().toString();

                        if(TextUtils.isEmpty(rname) || TextUtils.isEmpty(cat) || TextUtils.isEmpty(subcat) || TextUtils.isEmpty(rating) ||
                                TextUtils.isEmpty(ptime) || TextUtils.isEmpty(ingred) || TextUtils.isEmpty(rec)){
                            Toast.makeText(AddRecipeActivity.this, "Input contains empty field/s", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddRecipeActivity.this, "Uploaded Successfully in Database", Toast.LENGTH_LONG).show();
                            //add to Realtime DB in Firebase
                            RecipeModel uploads = new RecipeModel(rname, cat, subcat, catsub, rating, ptime, ingred, rec, downloadUri.toString());
                            mDatabaseRef.push().setValue(uploads);
                        }

                    } else {
                        pd.dismiss();
                        Toast.makeText(AddRecipeActivity.this, "Upload Failed", Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddRecipeActivity.this, "Upload Failed no. 2", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            //Picasso.get().load(imageUri).into(recipeImg);
            recipeImg.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteAndUpdateRecipe(){
        recipeID = recipeData.get(0);
        Picasso.get().load(recipeData.get(1)).into(recipeImg);
        recipeName.setText(recipeData.get(2));
        recipeRating.setRating(Float.parseFloat(recipeData.get(3)));
        prepTime.setText(recipeData.get(4));
        ingredients.setText(recipeData.get(5));
        recipe.setText(recipeData.get(6));
    }

    public void updateRecipe(){
        //Toast.makeText(this, "Hello! " + recipeData.get(2), Toast.LENGTH_SHORT).show();
        final String rname = recipeName.getText().toString().trim();
        final String cat = category;
        final String subcat = subCategory;
        final String catsub = c1.concat(c2);
        final String rating = String.valueOf(recipeRating.getRating());
        final String ptime = prepTime.getText().toString().trim();
        final String ingred = ingredients.getText().toString();
        final String rec = recipe.getText().toString();

        pd.setMessage("Updating, please wait");
        pd.show();

        if(imageUri != null) {
            final StorageReference srUpload = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            StorageTask uploadTask = srUpload.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return srUpload.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        final String updateImgTemp = downloadUri.toString();

                        mDatabaseRef.child(recipeID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Object> postValues = new HashMap<String, Object>();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    postValues.put(snapshot.getKey(), snapshot.getValue());
                                }
                                postValues.put("recipeName", rname);
                                postValues.put("ingredients", ingred);
                                postValues.put("prepTime", ptime);
                                postValues.put("recipeRating", rating);
                                postValues.put("recipe", rec);
                                postValues.put("category", cat);
                                postValues.put("subCategory", subcat);
                                postValues.put("cat_sub", catsub);
                                postValues.put("imageUrl", updateImgTemp);
                                mDatabaseRef.child(recipeID).updateChildren(postValues);
                                pd.dismiss();
                                Toast.makeText(AddRecipeActivity.this, "Updated Successfully", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else{
                        pd.dismiss();
                        Toast.makeText(AddRecipeActivity.this, "Sad", Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddRecipeActivity.this, "Update Failed no. 2", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            mDatabaseRef.child(recipeID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> postValues = new HashMap<String, Object>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        postValues.put(snapshot.getKey(), snapshot.getValue());
                    }
                    postValues.put("recipeName", rname);
                    postValues.put("ingredients", ingred);
                    postValues.put("prepTime", ptime);
                    postValues.put("recipeRating", rating);
                    postValues.put("recipe", rec);
                    postValues.put("category", cat);
                    postValues.put("subCategory", subcat);
                    postValues.put("cat_sub", catsub);
                    mDatabaseRef.child(recipeID).updateChildren(postValues);
                    pd.dismiss();
                    Toast.makeText(AddRecipeActivity.this, "Updated Successfully", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
