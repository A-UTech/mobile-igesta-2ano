package com.example.igestamobile.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igestamobile.R;
import com.example.igestamobile.adapter.CondenaAdapter;
import com.example.igestamobile.data.model.CondenaModel;

import java.util.ArrayList;
import java.util.List;

public class SelecionarCondenas extends AppCompatActivity {

    RecyclerView recyclerView;
    CondenaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_condenas);

        recyclerView = findViewById(R.id.condena_recyclerView);

        List<CondenaModel> mockList = new ArrayList<>();
        mockList.add(new CondenaModel(1, "Condena 1", "Tipo 1"));
        mockList.add(new CondenaModel(2, "Condena 2", "Tipo 2"));
        mockList.add(new CondenaModel(3, "Condena 3", "Tipo 3"));
        mockList.add(new CondenaModel(4, "Condena 4", "Tipo 4"));
        mockList.add(new CondenaModel(5, "Condena 5", "Tipo 5"));
        mockList.add(new CondenaModel(6, "Condena 6", "Tipo 6"));
        mockList.add(new CondenaModel(7, "Condena 7", "Tipo 7"));
        mockList.add(new CondenaModel(8, "Condena 8", "Tipo 8"));
        mockList.add(new CondenaModel(9, "Condena 9", "Tipo 9"));
        mockList.add(new CondenaModel(10, "Condena 10", "Tipo 10"));
        mockList.add(new CondenaModel(11, "Condena 11", "Tipo 11"));
        mockList.add(new CondenaModel(12, "Condena 12", "Tipo 12"));

        adapter = new CondenaAdapter(mockList);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}