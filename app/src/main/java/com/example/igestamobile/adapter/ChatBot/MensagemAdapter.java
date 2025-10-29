package com.example.igestamobile.adapter.ChatBot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.data.model.ChatBot.MensagemModel;

import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOT = 0;
    private static final int TYPE_USER = 1;

    private List<MensagemModel> mensagens;

    public MensagemAdapter(List<MensagemModel> mensagens) {
        this.mensagens = mensagens;
    }

    @Override
    public int getItemViewType(int position) {
     return mensagens.get(position).isFuncionario() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_funcionario, parent, false);
            return new UserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensagemModel msg = mensagens.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textMessage.setText(msg.getMensagem());
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).textMessage.setText(msg.getMensagem());
        }
    }

    @Override
    public int getItemCount() { return mensagens.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        UserViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.tvMensagemFuncionario);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        BotViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.tvMensagemBot);
        }
    }
}
