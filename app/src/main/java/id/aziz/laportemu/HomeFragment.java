package id.aziz.laportemu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private BarangAdapter adapter;
    private MaterialButton btnSemua, btnHilang, btnKetemu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rvBarang = view.findViewById(R.id.rv_barang);
        SearchView searchView = view.findViewById(R.id.search_view);
        btnSemua = view.findViewById(R.id.btn_filter_semua);
        btnHilang = view.findViewById(R.id.btn_filter_hilang);
        btnKetemu = view.findViewById(R.id.btn_filter_ketemu);

        DataStore.loadData(requireContext());

        adapter = new BarangAdapter(getContext(), DataStore.barangList);
        rvBarang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBarang.setAdapter(adapter);

        // Initial state
        setActiveFilter(btnSemua);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        btnSemua.setOnClickListener(v -> {
            adapter.updateData(DataStore.barangList);
            setActiveFilter(btnSemua);
        });

        btnHilang.setOnClickListener(v -> {
            List<Barang> filtered = new ArrayList<>();
            for (Barang b : DataStore.barangList) {
                if ("Hilang".equalsIgnoreCase(b.getStatus())) {
                    filtered.add(b);
                }
            }
            adapter.updateData(filtered);
            setActiveFilter(btnHilang);
        });

        btnKetemu.setOnClickListener(v -> {
            List<Barang> filtered = new ArrayList<>();
            for (Barang b : DataStore.barangList) {
                if ("Ditemukan".equalsIgnoreCase(b.getStatus())) {
                    filtered.add(b);
                }
            }
            adapter.updateData(filtered);
            setActiveFilter(btnKetemu);
        });

        return view;
    }

    private void setActiveFilter(MaterialButton activeBtn) {
        MaterialButton[] buttons = {btnSemua, btnHilang, btnKetemu};
        for (MaterialButton btn : buttons) {
            if (btn == activeBtn) {
                // Active Style
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary)));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                btn.setStrokeWidth(0);
            } else {
                // Inactive Style
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_main));
                btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.border_color)));
                btn.setStrokeWidth(2);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.updateData(DataStore.barangList);
        }
    }
}