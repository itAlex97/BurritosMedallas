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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variables para la lista ÚNICA
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
            Toast.makeText(this, "Modo Admin", Toast.LENGTH_SHORT).show();
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

        // TRUCO PARA EVITAR EL CRASH: Desactivar animaciones predictivas
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);

        // Consulta: Ordenamos por categoría DESCENDENTE (Z-A) para que 'C'omida salga antes que 'B'ebida
        Query query = db.collection("products")
                .whereEqualTo("available", true)
                .orderBy("category", Query.Direction.DESCENDING)
                .orderBy("name");

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options);
        recyclerView.setAdapter(adapter);
    }

    private void setupSignOutButton() {
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            finish();
        });
    }

    // --- CICLO DE VIDA (UN SOLO ADAPTADOR) ---
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