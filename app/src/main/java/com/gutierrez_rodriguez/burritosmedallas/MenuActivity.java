package com.gutierrez_rodriguez.burritosmedallas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // --- AHORA TENEMOS DOS ADAPTADORES ---
    private ProductAdapter adapterComida;
    private ProductAdapter adapterBebida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupSignOutButton();

        // Configuramos las dos listas por separado
        setupComidaList();
        setupBebidaList();
    }

    private void setupSignOutButton() {
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            finish();
        });
    }

    // --- CONFIGURACIÓN LISTA DE COMIDA ---
    private void setupComidaList() {
        // 1. Consulta: Solo categoría 'comida' y disponibles, ordenados por nombre
        Query queryComida = db.collection("products")
                .whereEqualTo("available", true)
                .whereEqualTo("category", "comida") // <--- FILTRO CLAVE
                .orderBy("name");

        FirestoreRecyclerOptions<Product> optionsComida = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(queryComida, Product.class)
                .build();

        adapterComida = new ProductAdapter(optionsComida);

        // Usamos el ID nuevo rvComida
        binding.rvComida.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComida.setAdapter(adapterComida);
    }

    // --- CONFIGURACIÓN LISTA DE BEBIDA ---
    private void setupBebidaList() {
        // 1. Consulta: Solo categoría 'bebida' y disponibles, ordenados por nombre
        Query queryBebida = db.collection("products")
                .whereEqualTo("available", true)
                .whereEqualTo("category", "bebida") // <--- FILTRO CLAVE
                .orderBy("name");

        FirestoreRecyclerOptions<Product> optionsBebida = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(queryBebida, Product.class)
                .build();

        adapterBebida = new ProductAdapter(optionsBebida);

        // Usamos el ID nuevo rvBebida
        binding.rvBebida.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBebida.setAdapter(adapterBebida);
    }


    // --- CICLO DE VIDA (PARA LOS DOS ADAPTADORES) ---
    @Override
    protected void onStart() {
        super.onStart();
        if (adapterComida != null) adapterComida.startListening();
        if (adapterBebida != null) adapterBebida.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapterComida != null) adapterComida.stopListening();
        if (adapterBebida != null) adapterBebida.stopListening();
    }
}