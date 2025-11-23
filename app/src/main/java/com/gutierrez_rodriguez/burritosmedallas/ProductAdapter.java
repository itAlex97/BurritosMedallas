package com.gutierrez_rodriguez.burritosmedallas;

import android.content.Context; // NUEVO: Para obtener colores del sistema
import android.graphics.Color;   // NUEVO: Para crear colores
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // NUEVO: Forma segura de obtener colores
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ItemProductBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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

        // --- NUEVA LÓGICA VISUAL PARA DISPONIBILIDAD ---
        Context context = holder.itemView.getContext(); // Obtenemos el contexto para usar colores
        if (model.isAvailable()) {
            // Si está disponible: Color de fondo BLANCO (normal)
            holder.binding.cardViewProduct.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            // Restauramos colores de texto originales
            holder.binding.tvProductName.setTextColor(ContextCompat.getColor(context, R.color.burritos_text));
            holder.binding.tvProductPrice.setTextColor(ContextCompat.getColor(context, R.color.burritos_orange));
        } else {
            // Si NO está disponible: Color de fondo GRIS CLARO y textos más apagados
            int disabledGrey = Color.parseColor("#F0F0F0"); // Un gris muy clarito
            int disabledTextGrey = Color.parseColor("#9E9E9E"); // Gris medio para texto
            holder.binding.cardViewProduct.setCardBackgroundColor(disabledGrey);
            // Cambiamos el color del texto para que se vea "inactivo"
            holder.binding.tvProductName.setTextColor(disabledTextGrey);
            holder.binding.tvProductPrice.setTextColor(disabledTextGrey);
        }
        // --------------------------------------------------


        // 2. Lógica de cabeceras (Se mantiene igual)
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

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
}