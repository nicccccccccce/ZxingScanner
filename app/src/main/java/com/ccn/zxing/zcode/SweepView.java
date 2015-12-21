package com.ccn.zxing.zcode;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SweepView extends FrameLayout {
    private SurfaceView sv;
    private ViewfinderView finder;

    public SweepView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public SweepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public SweepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        sv = new SurfaceView(context);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sv.setLayoutParams(params);
        addView(sv);
        finder = new ViewfinderView(context);
        finder.setLayoutParams(params);
        finder.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        addView(finder);
    }

    public SurfaceView getSurfaceView() {
        if (null != sv)
            return sv;
        return null;
    }

    public ViewfinderView getViewfinderView() {
        if (null != finder)
            return finder;
        return null;
    }
}
