package com.example.lab3components;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 第 5 题：RecyclerView 列表。
 * 点名称 → 在原条目后面插一条同图同名的记录，只通知插入的那一格（notifyItemInserted），
 * 靠稳定 id + bindingAdapterPosition 保证插入后点击不串行、复用不错位。
 */
public class RecyclerActivity extends BaseDemoActivity {

    private final List<AnimalAdapter.Animal> data = new ArrayList<>();
    private AnimalAdapter adapter;
    private TextView tvCount;
    private long nextId = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        bindHeader(R.string.nav_05_title, "Adapter / ViewHolder / notifyItemInserted");

        seed();

        tvCount = findViewById(R.id.tvCount);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalAdapter(data, this::insertAfter);
        recycler.setAdapter(adapter);
        updateCount();
    }

    private void seed() {
        String[] names = getResources().getStringArray(R.array.animal_names);
        int[] photos = {
                R.drawable.lion, R.drawable.tiger, R.drawable.monkey,
                R.drawable.dog, R.drawable.cat, R.drawable.elephant
        };
        for (int i = 0; i < names.length; i++) {
            data.add(new AnimalAdapter.Animal(names[i], photos[i % photos.length], nextId++));
        }
    }

    private void insertAfter(int position) {
        AnimalAdapter.Animal origin = data.get(position);
        AnimalAdapter.Animal copy =
                new AnimalAdapter.Animal(origin.name, origin.photo, nextId++);
        data.add(position + 1, copy);
        adapter.notifyItemInserted(position + 1);
        // 序号徽标是按 position 算的，插入后它后面的行位置都变了，
        // 只 notifyItemInserted 不会让它们重新绑定，序号会停在旧值，这里补一次范围刷新。
        adapter.notifyItemRangeChanged(position + 1, data.size() - position - 1);
        updateCount();
        Toast.makeText(this, getString(R.string.added_fmt, origin.name, origin.name),
                Toast.LENGTH_SHORT).show();
    }

    private void updateCount() {
        tvCount.setText(getString(R.string.count_fmt, data.size()));
    }
}
