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
import com.google.firebase.firestore.DocumentSnapshot; // NUEVO
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
            Toast.makeText(this, "Modo Admin: Toca un producto para editarlo", Toast.LENGTH_LONG).show(); // Mensaje actualizado
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

        // Consulta: Ordenamos por categoría DESCENDENTE (Z-A) y luego por nombre
        Query query = db.collection("products")
                .whereEqualTo("available", true)
                .orderBy("category", Query.Direction.DESCENDING)
                .orderBy("name");

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options);
        recyclerView.setAdapter(adapter);

        // 2. NUEVO: CONECTAR EL LISTENER DE CLICS
        // Solo si es admin le hacemos caso a los clics en las tarjetas
        if ("admin".equals(userRole)) {
            adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                    // Aquí llega el documento de Firebase al que le dieron clic.
                    // Obtenemos el objeto Product y su ID único.
                    Product product = documentSnapshot.toObject(Product.class);
                    String productId = documentSnapshot.getId();

                    // Abrimos la pantalla de edición pasando los datos
                    openEditScreen(productId, product);
                }
            });
        }
    }

    // 3. NUEVO: Método auxiliar para abrir la pantalla de edición
    private void openEditScreen(String productId, Product product) {
        Intent intent = new Intent(MenuActivity.this, AdminProductActivity.class);
        // Le decimos que vamos en modo "EDIT"
        intent.putExtra("MODE", "EDIT");
        // Le pasamos el ID del producto a editar
        intent.putExtra("PRODUCT_ID", productId);

        // Pasamos los datos actuales para rellenar el formulario (Opcional, pero más rápido)
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