package vn.edu.dlu.ctk47.techmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CompareAdapter extends RecyclerView.Adapter<CompareAdapter.ViewHolder> {

    private List<Spec> list;

    public CompareAdapter(List<Spec> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compare, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Spec s = list.get(position);

        h.txtLabel.setText(s.getKey() != null ? s.getKey() : "");
        h.txtValue1.setText(s.getValue1() != null ? s.getValue1() : "N/A");
        h.txtValue2.setText(s.getValue2() != null ? s.getValue2() : "N/A");

        // Highlight difference
        LinearLayout row = h.itemView.findViewById(R.id.rowRoot);
        if (row != null) {
            if (s.getValue1() != null && !s.getValue1().equals(s.getValue2())) {
                row.setBackgroundColor(0xFFE0F2FE); // Light blue when different
            } else {
                row.setBackgroundColor(0x00000000); // Transparent when same
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLabel, txtValue1, txtValue2;

        public ViewHolder(@NonNull View v) {
            super(v);
            txtLabel = v.findViewById(R.id.txtLabel);
            txtValue1 = v.findViewById(R.id.txtValue1);
            txtValue2 = v.findViewById(R.id.txtValue2);
        }
    }
}
