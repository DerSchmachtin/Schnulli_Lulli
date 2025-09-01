package com.martin.love_application;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spacing;

    public SpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);

        // Add spacing to the top of each item
        outRect.top = spacing;

        // Add spacing to the sides
        outRect.left = spacing / 2;
        outRect.right = spacing / 2;

        // Add extra spacing to the bottom of the last item
        if (position == state.getItemCount() - 1) {
            outRect.bottom = spacing;
        }
    }
}