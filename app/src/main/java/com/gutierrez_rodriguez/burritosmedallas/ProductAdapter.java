package com.gutierrez_rodriguez.burritosmedallas;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ItemProductBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductViewHolder> {

    public ProductAdapter(@NonNull FirestoreRecyclerOptions<Product> options) {
        super(options);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getSnapshots().getSnapshot(position).getId().hashCode();
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product model) {
        // 1. Llenar datos normales
        holder.binding.tvProductName.setText(model.getName());
        holder.binding.tvProductDescription.setText(model.getDescription());
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        holder.binding.tvProductPrice.setText(format.format(model.getPrice()));

        // 2. Lógica de cabeceras
        String currentCategory = model.getCategory();
        String capitalizedCategory = currentCategory.substring(0, 1).toUpperCase() + currentCategory.substring(1);

        boolean showHeader = false;

        if (position == 0) {
            showHeader = true;
        } else {
            try {
                Product previousProduct = getItem(position - 1);
                // Agregamos un chequeo extra de nulidad por seguridad
                if (previousProduct != null && previousProduct.getCategory() != null) {
                    if (!currentCategory.equals(previousProduct.getCategory())) {
                        showHeader = true;
                    }
                }
            } catch (Exception e) {
                showHeader = false;
            }
        }

        if (showHeader) {
            holder.binding.tvSectionHeader.setVisibility(android.view.View.VISIBLE);
            holder.binding.tvSectionHeader.setText(capitalizedCategory + "s");
        } else {
            holder.binding.tvSectionHeader.setVisibility(android.view.View.GONE);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    // NUEVO: Este método se ejecuta cada vez que Firebase avisa de un cambio.
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // "Martillazo": Le decimos a la lista que se redibuje COMPLETA.
        // Esto asegura que la lógica de comparar con el anterior (position - 1)
        // se ejecute en orden correcto desde el principio hasta el final.
        notifyDataSetChanged();
    }
    // FIN DE LO NUEVO

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}