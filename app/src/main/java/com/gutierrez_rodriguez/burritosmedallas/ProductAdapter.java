package com.gutierrez_rodriguez.burritosmedallas;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Importaciones de FirebaseUI y Firestore
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
// Importación del binding del diseño de item
import com.gutierrez_rodriguez.burritosmedallas.databinding.ItemProductBinding;

import java.text.NumberFormat;
import java.util.Locale;

// Esta clase extiende de FirestoreRecyclerAdapter, que hace la magia de Google.
// Le decimos que usará nuestro modelo 'Product' y nuestro 'ProductViewHolder'.
public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductViewHolder> {

    // Constructor: Recibe las opciones de configuración de Firebase
    public ProductAdapter(@NonNull FirestoreRecyclerOptions<Product> options) {
        super(options);
    }

    // MÉTODO 1: onBindViewHolder (Versión SIMPLE de nuevo)
    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product model) {
        // Solo llenamos los datos, sin lógica de cabeceras
        holder.binding.tvProductName.setText(model.getName());
        holder.binding.tvProductDescription.setText(model.getDescription());

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        holder.binding.tvProductPrice.setText(format.format(model.getPrice()));
    }

    // MÉTODO 2: onCreateViewHolder
    // Este método crea la tarjeta vacía la primera vez.
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos ViewBinding para "inflar" (crear) el diseño de item_product.xml
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }


    // CLASE INTERNA: ProductViewHolder
    // Esta clasecita es la que "sostiene" los elementos visuales de una sola tarjeta.
    // Usamos ViewBinding aquí para no usar findViewById nunca más.
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}