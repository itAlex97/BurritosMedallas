package com.gutierrez_rodriguez.burritosmedallas;

import android.view.LayoutInflater;
import android.view.View; // <-- NUEVO: Importar View
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot; // <-- NUEVO: Importar para obtener el ID
import com.gutierrez_rodriguez.burritosmedallas.databinding.ItemProductBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductViewHolder> {

    // 1. NUEVO: Definimos la "Interfaz" (El contrato para escuchar clics)
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    // Método para que la Actividad se suscriba a los clics
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // --- FIN DE LO NUEVO 1 ---


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
        // ... (Todo el código de llenar datos y cabeceras SIGUE IGUAL aquí) ...
        holder.binding.tvProductName.setText(model.getName());
        holder.binding.tvProductDescription.setText(model.getDescription());
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        holder.binding.tvProductPrice.setText(format.format(model.getPrice()));

        String currentCategory = model.getCategory();
        String capitalizedCategory = currentCategory.substring(0, 1).toUpperCase() + currentCategory.substring(1);
        boolean showHeader = false;
        if (position == 0) {
            showHeader = true;
        } else {
            try {
                Product previousProduct = getItem(position - 1);
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

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        notifyDataSetChanged();
    }

    // 2. NUEVO: Modificamos el ViewHolder para detectar el clic en la tarjeta
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Configurar el clic en toda la tarjeta (el root)
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Verificamos si alguien está escuchando y si la posición es válida
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        // ¡Avisamos que hubo clic y mandamos el documento de Firebase!
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
    // --- FIN DE LO NUEVO 2 ---
}