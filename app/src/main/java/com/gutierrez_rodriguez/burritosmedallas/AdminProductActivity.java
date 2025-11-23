package com.gutierrez_rodriguez.burritosmedallas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ActivityAdminProductBinding;

import java.util.HashMap;
import java.util.Map;

public class AdminProductActivity extends AppCompatActivity {

    private ActivityAdminProductBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.btnSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

        // Botón para cancelar/cerrar (opcional, por si quieres agregar una 'X' en el diseño después)
        // binding.btnClose.setOnClickListener(v -> finish());
    }

    private void saveProduct() {
        // 1. LEER LOS DATOS DE LOS CAMPOS
        String name = binding.etProdName.getText().toString().trim();
        String description = binding.etProdDesc.getText().toString().trim();
        String priceString = binding.etProdPrice.getText().toString().trim();

        // Leer la categoría seleccionada (RadioGroup)
        String category = "comida"; // Valor por defecto
        if (binding.rbBebida.isChecked()) {
            category = "bebida";
        }

        // Leer la disponibilidad (Switch)
        boolean isAvailable = binding.switchAvailable.isChecked();

        // 2. VALIDACIONES BÁSICAS
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Por favor, llena todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir el precio de texto a número (double)
        double price = 0.0;
        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El precio no es válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. CREAR EL PAQUETE DE DATOS PARA FIREBASE (Map)
        // Las claves deben ser IDÉNTICAS a las que usaste en tu clase 'Product.java'
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("name", name);
        productMap.put("description", description);
        productMap.put("price", price);
        productMap.put("category", category);
        productMap.put("available", isAvailable);

        // Deshabilitamos el botón para evitar dobles clics mientras se guarda
        binding.btnSaveProduct.setEnabled(false);
        binding.btnSaveProduct.setText("Guardando...");

        // 4. ENVIAR A FIRESTORE (Operación de CREAR 'C' en el CRUD)
        // Usamos .add() para que Firebase genere un ID único automáticamente para el nuevo producto.
        db.collection("products")
                .add(productMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // ¡Éxito!
                        Toast.makeText(AdminProductActivity.this, "Producto guardado correctamente", Toast.LENGTH_SHORT).show();
                        finish(); // Cerramos la pantalla y volvemos al menú
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error
                        Toast.makeText(AdminProductActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // Rehabilitamos el botón para que pueda intentar de nuevo
                        binding.btnSaveProduct.setEnabled(true);
                        binding.btnSaveProduct.setText("Guardar Producto");
                    }
                });
    }
}