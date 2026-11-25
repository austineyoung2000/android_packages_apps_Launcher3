/*
 * Copyright (C) 2026 The RisingOS Project
 * Licensed under the Apache License, Version 2.0.
 */
package com.android.quickstep.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.launcher3.R;
import com.android.launcher3.LauncherPrefs;
import com.android.systemui.shared.recents.model.Task;

/** A task-card navigator. Each entry represents a card, not an application package. */
public class RecentsIconScrollView extends HorizontalScrollView {
    private RecentsView<?, ?> mRecents;
    private LinearLayout mRow;
    private View mSelected;
    private final Runnable mRefresh = this::rebuild;

    public RecentsIconScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bind(RecentsView<?, ?> recents) {
        mRecents = recents;
        mRow = findViewById(R.id.recents_icon_row);
        scheduleRefresh();
    }

    public void scheduleRefresh() {
        removeCallbacks(mRefresh);
        post(mRefresh);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h != oldh) scheduleRefresh();
    }

    private Drawable taskIcon(Task task) {
        PackageManager pm = getContext().getPackageManager();
        ComponentName component = task.key.getComponent();
        Drawable icon;
        try {
            icon = component == null ? pm.getDefaultActivityIcon() : pm.getActivityIcon(component);
        } catch (PackageManager.NameNotFoundException e) {
            icon = pm.getDefaultActivityIcon();
        }
        return pm.getUserBadgedIcon(icon, UserHandle.of(task.key.userId));
    }

    private void rebuild() {
        if (mRecents == null || mRow == null) return;
        mSelected = null;
        mRow.removeAllViews();
        for (int i = 0; i < mRecents.getChildCount(); i++) {
            TaskView card = mRecents.getTaskViewAt(i);
            if (card == null || card.getTaskContainers().isEmpty()) continue;
            LinearLayout entry = new LinearLayout(getContext());
            entry.setGravity(Gravity.CENTER);
            entry.setOrientation(LinearLayout.HORIZONTAL);
            entry.setPadding(dp(4), dp(4), dp(4), dp(4));
            entry.setTag(card);
            entry.setFocusable(true);
            entry.setClickable(true);
            StringBuilder label = new StringBuilder();
            int count = Math.min(card.getTaskContainers().size(), 2);
            int requestedSize = Math.max(20, Math.min(64,
                    LauncherPrefs.RECENTS_SHORTCUT_ICON_SIZE.get(getContext())));
            int singleSize = Math.min(dp(requestedSize), Math.max(dp(20), getHeight() - dp(8)));
            int iconIndex = 0;
            for (TaskContainer container : card.getTaskContainers()) {
                if (iconIndex++ >= count) break;
                Task task = container.getTask();
                if (label.length() > 0) label.append(", ");
                ComponentName component = task.key.getComponent();
                CharSequence name = task.titleDescription;
                label.append(name != null ? name : component != null
                        ? component.getPackageName() : getContext().getString(R.string.accessibility_recent_apps));
                ImageView icon = new ImageView(getContext());
                icon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                icon.setImageDrawable(taskIcon(task));
                int size = count > 1 ? Math.min(dp(32), singleSize * 2 / 3) : singleSize;
                entry.addView(icon, new LinearLayout.LayoutParams(size, size));
            }
            entry.setContentDescription(label);
            entry.setOnClickListener(v -> {
                int page = mRecents.indexOfChild(card);
                if (page >= 0) {
                    if (mRecents instanceof LauncherRecentsView) {
                        ((LauncherRecentsView) mRecents).showShortcutCard(card);
                    }
                    mRecents.snapToPage(page, 200);
                }
            });
            int entryWidth = Math.max(dp(48), count > 1
                    ? Math.min(dp(32), singleSize * 2 / 3) * 2 + dp(8)
                    : singleSize + dp(8));
            mRow.addView(entry, new LinearLayout.LayoutParams(entryWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }
        updateSelected();
    }

    public void updateSelected() {
        if (mRecents == null || mRow == null) return;
        TaskView card = mRecents.getTaskViewNearestToCenterOfScreen();
        View selected = null;
        for (int i = 0; i < mRow.getChildCount(); i++) {
            View entry = mRow.getChildAt(i);
            if (entry.getTag() == card) selected = entry;
        }
        if (selected == mSelected) return;
        if (mSelected != null) {
            mSelected.setBackground(null);
            mSelected.setSelected(false);
        }
        mSelected = selected;
        if (selected != null) {
            GradientDrawable background = new GradientDrawable();
            background.setColor(0x55ffffff);
            background.setCornerRadius(dp(24));
            selected.setBackground(background);
            selected.setSelected(true);
            final View target = selected;
            post(() -> smoothScrollTo(target.getLeft() - (getWidth() - target.getWidth()) / 2, 0));
        }
    }
}
