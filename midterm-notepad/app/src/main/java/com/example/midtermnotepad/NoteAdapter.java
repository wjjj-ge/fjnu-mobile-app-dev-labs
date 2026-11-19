package com.example.midtermnotepad;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 列表适配器：卡片 + 分类色条 + 相对时间 + 搜索关键词高亮。 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.Holder> {

    public interface Listener {
        void onClick(NoteItem note);

        void onLongClick(NoteItem note, View anchor);
    }

    private final List<NoteItem> data = new ArrayList<>();
    private final Listener listener;
    private final boolean showSnippet;
    private String keyword = "";
    private int highlightColor = Color.parseColor("#FFE9A8");

    public NoteAdapter(Listener listener, boolean showSnippet) {
        this.listener = listener;
        this.showSnippet = showSnippet;
        setHasStableIds(true);
    }

    public void submit(List<NoteItem> notes, String keyword, int highlightColor) {
        this.data.clear();
        if (notes != null) {
            this.data.addAll(notes);
        }
        this.keyword = keyword == null ? "" : keyword.trim();
        this.highlightColor = highlightColor;
        notifyDataSetChanged();
    }

    public NoteItem get(int position) {
        return data.get(position);
    }

    public int size() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NoteItem note = data.get(position);
        int color = UI.categoryColor(note.category);

        holder.title.setText(highlight(note.displayTitle(), color));
        holder.strip.setBackgroundColor(color);

        GradientDrawable badge = new GradientDrawable();
        badge.setColor(color);
        badge.setCornerRadius(holder.itemView.getResources().getDisplayMetrics().density * 4);
        holder.category.setBackground(badge);
        holder.category.setText(UI.categoryLabel(note.category));

        if (showSnippet) {
            holder.snippet.setVisibility(View.VISIBLE);
            String snippet = UI.snippet(note.note, keyword);
            holder.snippet.setText(snippet.isEmpty()
                    ? holder.itemView.getContext().getString(R.string.editor_content_hint)
                    : highlight(snippet, color));
        } else {
            holder.snippet.setVisibility(View.GONE);
        }

        holder.time.setText(TimeUtil.relative(note.modified));

        holder.itemView.setOnClickListener(v -> listener.onClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(note, v);
            return true;
        });
    }

    /** 关键词命中处加底色 + 加粗，颜色跟分类条同色，读起来能对上。 */
    private CharSequence highlight(String text, int accent) {
        if (keyword.isEmpty() || text == null) {
            return text;
        }
        String lowerText = text.toLowerCase(Locale.CHINA);
        String lowerKey = keyword.toLowerCase(Locale.CHINA);
        int at = lowerText.indexOf(lowerKey);
        if (at < 0) {
            return text;
        }
        SpannableString span = new SpannableString(text);
        span.setSpan(new BackgroundColorSpan(highlightColor), at, at + keyword.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(accent), at, at + keyword.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new StyleSpan(Typeface.BOLD), at, at + keyword.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView snippet;
        final TextView category;
        final TextView time;
        final View strip;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            snippet = itemView.findViewById(R.id.tvSnippet);
            category = itemView.findViewById(R.id.tvCategory);
            time = itemView.findViewById(R.id.tvTime);
            strip = itemView.findViewById(R.id.colorStrip);
        }
    }
}
