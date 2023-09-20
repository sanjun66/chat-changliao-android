package com.legend.baseui.ui.widget.recyclerview;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class PageScrollListener extends  RecyclerView.OnScrollListener {
    public int currentPosition = RecyclerView.NO_POSITION;

    @Nullable
    protected SnapHelper snapHelper;

    @Override
    public final void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (snapHelper == null) {
            RecyclerView.OnFlingListener flingListener = recyclerView.getOnFlingListener();
            if (flingListener instanceof SnapHelper) {
                snapHelper = (SnapHelper) flingListener;
            }
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        View snapView = null;
        int position = RecyclerView.NO_POSITION;
        if (layoutManager != null) {
            snapView = snapHelper.findSnapView(layoutManager);
            if (snapView != null) {
                position = layoutManager.getPosition(snapView);
            }
        }
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (currentPosition != position) {
            currentPosition = position;
            onPageSelected(position);
        }

        int[] snapViewDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, snapView);
        float positionOffset = 0f;
        int positionOffsetPixels = 0;
        if (snapViewDistance != null) {
            if (layoutManager.canScrollHorizontally()) {
                positionOffsetPixels = snapViewDistance[0];
                positionOffset = (float) snapViewDistance[0] / snapView.getWidth();
            } else {
                positionOffsetPixels = snapViewDistance[1];
                positionOffset = (float) snapViewDistance[1] / snapView.getHeight();
            }
        }

        int targetPosition;
        float targetPositionOffset;
        int targetPositionOffsetPixels;

        if (positionOffset <= 0) {
            targetPosition = position;
            targetPositionOffset = Math.abs(positionOffset);
            targetPositionOffsetPixels = Math.abs(positionOffsetPixels);
        } else {
            targetPosition = position - 1;
            View lastView = layoutManager.findViewByPosition(targetPosition);
            int[] lastViewDistance = new int[2];
            if (lastView != null) {
                lastViewDistance =
                        snapHelper.calculateDistanceToFinalSnap(layoutManager, lastView);
            }
            float lastViewPositionOffset = 0f;
            int lastViewPositionOffsetPixels = 0;
            if (lastViewDistance != null) {
                if (layoutManager.canScrollHorizontally()) {
                    lastViewPositionOffsetPixels = lastViewDistance[0];
                    lastViewPositionOffset = (float) lastViewDistance[0] / snapView.getWidth();
                } else {
                    lastViewPositionOffsetPixels = lastViewDistance[1];
                    lastViewPositionOffset = (float) lastViewDistance[1] / snapView.getHeight();
                }
            }
            targetPositionOffset = Math.abs(lastViewPositionOffset);
            targetPositionOffsetPixels = Math.abs(lastViewPositionOffsetPixels);
        }

        onPageScrolled(targetPosition, targetPositionOffset, targetPositionOffsetPixels);
    }

    @CallSuper
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (currentPosition == RecyclerView.NO_POSITION) {
            return;
        }

        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onPageScrolled(currentPosition, 0, 0);
        }
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public void onPageSelected(int position) {

    }
}
