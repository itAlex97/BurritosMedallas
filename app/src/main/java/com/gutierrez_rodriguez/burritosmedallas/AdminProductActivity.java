package com.gutierrez_rodriguez.burritosmedallas;

import android.content.DialogInterface; // NUEVO (Para Borrar)
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // NUEVO (Para Borrar)
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ActivityAdminProductBinding;

import java.util.HashMap;
import java.util.Map;

public class AdminProductActivity extends AppCompatActivity {

    private ActivityAdminProductBinding binding;
    private FirebaseFirestore db;

    private String mode = "ADD";
    private String productId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("MODE")) {
            mode = getIntent().getStringExtra("MODE");
        }

        // Si es modo EDITAR, configuramos la pantalla y mostramos el botón de borrar
        if ("EDIT".equals(mode)) {
            setupForEditMode();
        }

        binding.btnSaveProduct.setOnClickListener(v -> saveOrUpdateProduct());
    }

    private void setupForEditMode() {
        binding.tvAdminTitle.setText("Editar Producto");
        binding.btnSaveProduct.setText("Actualizar Producto");

        // NUEVO (Para Borrar): ¡Hacemos visible el botón rojo!
        binding.btnDeleteProduct.setVisibility(View.VISIBLE);

        // NUEVO (Para Borrar): Configurar el clic del botón rojo
        binding.btnDeleteProduct.setOnClickListener(v -> {
            // En lugar de borrar directo, mostramos la confirmación
            showDeleteConfirmationDialog();
        });

        // Recuperar datos y rellenar el formulario...
        productId = getIntent().getStringExtra("PRODUCT_ID");
        binding.etProdName.setText(getIntent().getStringExtra("PROD_NAME"));
        binding.etProdDesc.setText(getIntent().getStringExtra("PROD_DESC"));
        binding.etProdPrice.setText(String.valueOf(getIntent().getDoubleExtra("PROD_PRICE", 0.0)));

        String category = getIntent().getStringExtra("PROD_CAT");
        if ("bebida".equals(category)) {
            binding.rbBebida.setChecked(true);
        } else {
            binding.rbComida.setChecked(true);
        }
        binding.switchAvailable.setChecked(getIntent().getBooleanExtra("PROD_AVAIL", true));
    }

    // NUEVO (Para Borrar): Método para mostrar el cuadro de diálogo de confirmación
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar producto?");
        builder.setMessage("Esta acción no se puede deshacer. ¿Estás seguro de que quieres eliminar este producto del menú?");

        // Botón Positivo (Sí, borrar)
        builder.setPositiveButton("Sí, borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Si dice que sí, ejecutamos el borrado en Firebase
                deleteProductFromFirebase();
            }
        });

        // Botón Negativo (Cancelar)
        builder.setNegativeButton("Cancelar", null); // null solo cierra el diálogo sin hacer nada

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // NUEVO (Para Borrar): La operación de borrado en Firebase (La 'D' del CRUD)
    private void deleteProductFromFirebase() {
        if (productId == null) return;

        binding.btnDeleteProduct.setEnabled(false); // Deshabilitar para evitar doble clic
        binding.btnDeleteProduct.setText("Borrando...");

        db.collection("products").document(productId)
                .delete() // <-- ¡Aquí sucede la magia destructiva!
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminProductActivity.this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cerramos la pantalla y volvemos al menú
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminProductActivity.this, "Error al borrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    binding.btnDeleteProduct.setEnabled(true);
                    binding.btnDeleteProduct.setText("Borrar Producto");
                });
    }


    // Este método sigue igual que antes (maneja Guardar y Actualizar)
    private void saveOrUpdateProduct() {
        String name = binding.etProdName.getText().toString().trim();
        String description = binding.etProdDesc.getText().toString().trim();
        String priceString = binding.etProdPrice.getText().toString().trim();
        String category = "comida";
        if (binding.rbBebida.isChecked()) {
            category = "bebida";
        }
        boolean isAvailable = binding.switchAvailable.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Por favor, llena todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0.0;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El precio no es válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("name", name);
        productMap.put("description", description);
        productMap.put("price", price);
        productMap.put("category", category);
        productMap.put("available", isAvailable);

        binding.btnSaveProduct.setEnabled(false);

        if ("EDIT".equals(mode) && productId != null) {
            // UPDATE
            binding.btnSaveProduct.setText("Actualizando...");
            db.collection("products").document(productId)
                    .update(productMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminProductActivity.this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(this::handleFailure);

        } else {
            // CREATE
            binding.btnSaveProduct.setText("Guardando...");
            db.collection("products")
                    .add(productMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AdminProductActivity.this, "Producto creado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(this::handleFailure);
        }
    }

    private void handleFailure(Exception e) {
        Toast.makeText(AdminProductActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        binding.btnSaveProduct.setEnabled(true);
        if ("EDIT".equals(mode)) {
            binding.btnSaveProduct.setText("Actualizar Producto");
        } else {
            binding.btnSaveProduct.setText("Guardar Producto");
        }
    }
}