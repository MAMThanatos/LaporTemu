package id.aziz.laportemu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class BarangAdapter extends RecyclerView.Adapter<BarangAdapter.BarangViewHolder> {

    private List<Barang> barangList;
    private List<Barang> barangListFull;
    private Context context;

    public BarangAdapter(Context context, List<Barang> barangList) {
        this.context = context;
        this.barangList = barangList;
        this.barangListFull = new ArrayList<>(barangList);
    }

    @NonNull
    @Override
    public BarangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barang, parent, false);
        return new BarangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarangViewHolder holder, int position) {
        Barang barang = barangList.get(position);
        holder.tvNama.setText(barang.getNama());
        holder.tvLokasi.setText(barang.getLokasi());
        holder.tvWaktu.setText(barang.getWaktu());
        holder.tvStatus.setText(barang.getStatus());

        if (barang.getImageUriString() != null) {
            try {
                holder.imgBarang.setImageURI(Uri.parse(barang.getImageUriString()));
            } catch (Exception e) {
                e.printStackTrace();
                holder.imgBarang.setImageResource(R.mipmap.ic_launcher);
            }
        } else if (barang.getImageResId() != 0) {
            holder.imgBarang.setImageResource(barang.getImageResId());
        } else {
            // Default image
            holder.imgBarang.setImageResource(R.mipmap.ic_launcher);
        }

        // Setup Badge Color
        if ("Hilang".equalsIgnoreCase(barang.getStatus())) {
            holder.cvBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.lost_bg));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.lost_text));
        } else {
            holder.cvBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.found_bg));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.found_text));
        }

        // Handle Item Click
        holder.itemView.setOnClickListener(v -> {
            int originalIndex = DataStore.barangList.indexOf(barang);
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("ITEM_INDEX", originalIndex);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return barangList.size();
    }

    public void filter(String text) {
        barangList.clear();
        if (text.isEmpty()) {
            barangList.addAll(barangListFull);
        } else {
            text = text.toLowerCase();
            for (Barang item : barangListFull) {
                if (item.getNama().toLowerCase().contains(text) || 
                    item.getStatus().toLowerCase().contains(text)) {
                    barangList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    public void updateData(List<Barang> newList) {
        this.barangList = newList;
        this.barangListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public static class BarangViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvLokasi, tvWaktu, tvStatus;
        ImageView imgBarang;
        CardView cvBadge;

        public BarangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_nama_barang);
            tvLokasi = itemView.findViewById(R.id.tv_lokasi_barang);
            tvWaktu = itemView.findViewById(R.id.tv_waktu_barang);
            tvStatus = itemView.findViewById(R.id.tv_status_badge);
            imgBarang = itemView.findViewById(R.id.img_barang);
            cvBadge = itemView.findViewById(R.id.cv_badge);
        }
    }
}