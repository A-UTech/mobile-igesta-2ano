package com.example.igestamobile.adapter.ChatBot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.igestamobile.R;
import com.example.igestamobile.data.model.ChatBot.MensagemModel;

import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOT = 0;
    private static final int TYPE_USER = 1;

    private static final int TYPE_LOADING = 2;

    private List<MensagemModel> mensagens;
    private Context context;


    public MensagemAdapter(List<MensagemModel> mensagens,Context context) {
        this.mensagens = mensagens;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        MensagemModel mensagem = mensagens.get(position);
        if (mensagem.isLoading()) {
            return TYPE_LOADING;
        } else if (mensagem.isFuncionario()) {
            return TYPE_USER;
        } else {
            return TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_funcionario, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == TYPE_BOT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_bot, parent, false);
            return new BotViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_carregando, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensagemModel msg = mensagens.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textMessage.setText(msg.getMensagem());
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).textMessage.setText(msg.getMensagem());
        }else if (holder instanceof LoadingViewHolder){
            Glide.with(holder.itemView.getContext())
                    .asGif()
                    .load(R.mipmap.loading_dots)
                    .into(((LoadingViewHolder) holder).gif);
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

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ImageView gif;
        LoadingViewHolder(View itemView) {
            super(itemView);
            gif = itemView.findViewById(R.id.loading_gif);
        }
    }
}
