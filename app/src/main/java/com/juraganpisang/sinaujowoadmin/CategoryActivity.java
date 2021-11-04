package com.juraganpisang.sinaujowoadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.juraganpisang.sinaujowoadmin.Adapter.CategoryAdapter;
import com.juraganpisang.sinaujowoadmin.Model.CategoryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView cat_recycler_view;
    private Toolbar toolbar;
    private Button addCatB, dialogAddB;
    public static List<CategoryModel> catList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private Dialog progressDialog, addCatDialog;
    private TextView dialogText;
    private EditText dialogCatName;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kategori");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        cat_recycler_view = findViewById(R.id.catRecyclerView);
        addCatB = findViewById(R.id.addCatB);

        progressDialog = new Dialog(CategoryActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Proses...");

//        tambah

        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category_dialog);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogCatName = addCatDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addCatDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        addCatB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCatName.getText().clear();
                addCatDialog.show();
            }
        });

        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dialogCatName.getText().toString().isEmpty()){
                    dialogCatName.setError("Masukan Kategori");

                    return;
                }

                addNewCategory(dialogCatName.getText().toString());
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycler_view.setLayoutManager(layoutManager);

        loadData();
    }

    private void addNewCategory(String title) {

        addCatDialog.dismiss();
        progressDialog.show();

        String doc_id = firestore.collection("QUIZ").document().getId();

        Map<String, Object> catData = new ArrayMap<>();
        catData.put("CAT_ID", doc_id);
        catData.put("NAME", title);
        catData.put("NO_OF_TESTS", 0);
        catData.put("COUNTER", "1");

        firestore.collection("QUIZ").document(doc_id)
                .set(catData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Map<String, Object> catDoc = new ArrayMap<>();
//                        catDoc.put("CAT"+String.valueOf(catList.size() + 1)+ "_NAME", title);
                        catDoc.put("CAT"+String.valueOf(catList.size() + 1)+ "_ID", doc_id);
                        catDoc.put("COUNT", catList.size()+1);

                        firestore.collection("QUIZ").document("Categories")
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(CategoryActivity.this, "Kategori berhasil ditambah", Toast.LENGTH_SHORT).show();

                                        catList.add(new CategoryModel(doc_id, title, 0));

                                        adapter.notifyItemInserted(catList.size());

                                        progressDialog.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                        progressDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void loadData() {

        progressDialog.show();

        catList.clear();
        firestore.collection("QUIZ").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            docList.put(doc.getId(), doc);
                        }

                        QueryDocumentSnapshot catListDooc = docList.get("Categories");

                        long catCount = catListDooc.getLong("COUNT");

                        for (int i = 1; i <= catCount; i++) {
                            String catID = catListDooc.getString("CAT" + String.valueOf(i) + "_ID");

                            QueryDocumentSnapshot catDoc = docList.get(catID);

                            String catId = catDoc.getString("CAT_ID");
                            String catName = catDoc.getString("NAME");

                            catList.add(new CategoryModel(catId, catName, 0));;
                        }
                        adapter = new CategoryAdapter(catList);
                        cat_recycler_view.setAdapter(adapter);

                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                });
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                        if(task.isSuccessful()){
//
//                            DocumentSnapshot doc = task.getResult();
//
//                            if(doc.exists()){
//                                long count = (long)doc.get("COUNT");
//
//                                for(int i=1; i <= count; i++){
//                                    String catID = doc.getString("CAT" + String.valueOf(i) + "_ID");
//
//                                    DocumentSnapshot catDoc = (DocumentSnapshot) doc.get(catID);
//
//                                    String catName = catDoc.getString("NAME");
//                                    catList.add(catName);
//                                }
//
//                                CategoryAdapter adapter = new CategoryAdapter(catList);
//                                cat_recycler_view.setAdapter(adapter);
//
//                            }else{
//                                Toast.makeText(CategoryActivity.this, "No Category Document", Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
//                        }else{
//                            Toast.makeText(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//
//                        progressDialog.dismiss();
//                    }
//                });
    }
}