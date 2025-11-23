package com.gutierrez_rodriguez.burritosmedallas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    private String userRole = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. RECUPERAR ROL Y CONFIGURAR FAB
        if (getIntent().hasExtra("USER_ROLE")) {
            userRole = getIntent().getStringExtra("USER_ROLE");
        }
        if ("admin".equals(userRole)) {
            binding.fabAddProduct.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Modo Admin: Viendo todos los productos (activos e inactivos)", Toast.LENGTH_LONG).show();
        } else {
            binding.fabAddProduct.setVisibility(View.GONE);
        }

        binding.fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, AdminProductActivity.class);
            intent.putExtra("MODE", "ADD");
            startActivity(intent);
        });

        setupSignOutButton();

        // --- CONFIGURACIÓN DEL RECYCLERVIEW ÚNICO ---
        recyclerView = binding.rvProducts;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);


        // --- NUEVA LÓGICA DE CONSULTA SEGÚN ROL ---
        Query query;

        if ("admin".equals(userRole)) {
            // SI ES ADMIN: Traemos TODO (sin filtrar por 'available').
            // Solo ordenamos por categoría y nombre.
            query = db.collection("products")
                    .orderBy("category", Query.Direction.DESCENDING)
                    .orderBy("name");
        } else {
            // SI ES USER (o cualquier otro): Solo traemos los DISPONIBLES.
            // Mantenemos el filtro original.
            query = db.collection("products")
                    .whereEqualTo("available", true)
                    .orderBy("category", Query.Direction.DESCENDING)
                    .orderBy("name");
        }
        // -------------------------------------------


        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options);
        recyclerView.setAdapter(adapter);

        // 2. CONECTAR EL LISTENER DE CLICS
        if ("admin".equals(userRole)) {
            adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                    Product product = documentSnapshot.toObject(Product.class);
                    String productId = documentSnapshot.getId();
                    openEditScreen(productId, product);
                }
            });
        }
    }

    private void openEditScreen(String productId, Product product) {
        Intent intent = new Intent(MenuActivity.this, AdminProductActivity.class);
        intent.putExtra("MODE", "EDIT");
        intent.putExtra("PRODUCT_ID", productId);
        intent.putExtra("PROD_NAME", product.getName());
        intent.putExtra("PROD_DESC", product.getDescription());
        intent.putExtra("PROD_PRICE", product.getPrice());
        intent.putExtra("PROD_CAT", product.getCategory());
        intent.putExtra("PROD_AVAIL", product.isAvailable());
        startActivity(intent);
    }


    private void setupSignOutButton() {
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}