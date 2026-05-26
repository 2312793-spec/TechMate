package vn.edu.dlu.ctk47.ai.model;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.dlu.ctk47.techmate.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.txtMessage.setText(message.getText());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.cardMessage.getLayoutParams();

        if (message.isAi()) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            holder.cardMessage.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            holder.txtMessage.setTextColor(Color.parseColor("#333333"));
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            holder.cardMessage.setCardBackgroundColor(Color.parseColor("#B2EBF2"));
            holder.txtMessage.setTextColor(Color.parseColor("#006064"));
        }
        holder.cardMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        CardView cardMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.tv_message_text);
            cardMessage = itemView.findViewById(R.id.cardMessage);
        }
    }
}