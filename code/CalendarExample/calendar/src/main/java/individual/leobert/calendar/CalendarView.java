/*
 * Copyright 2016,leobert,The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package individual.leobert.calendar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

public class CalendarView extends ViewGroup {

    private static final String TAG = "CalendarView";

    private int selectPosition = -1;

    private CalendarAdapter adapter;
    private List<CalendarBean> data;
    private OnItemClickListener onItemClickListener;

    private int row = 6;
    private int column = 7;
    private int itemWidth;
    private int itemHeight;

    private boolean isToday;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, CalendarBean bean);
    }

    public CalendarView(Context context, int row) {
        super(context);
        this.row = row;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public int getItemHeight() {
        return itemHeight;
    }


    public int getItemWidth() {
        return itemWidth;
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public void setAdapter(CalendarAdapter adapter) {
        this.adapter = adapter;
    }

    public void setData(List<CalendarBean> data, boolean isToday) {
        this.data = data;
        this.isToday = isToday;
        setItem();
        requestLayout();
    }

    private void setItem() {

        selectPosition = -1;
        if (adapter == null) {
            throw new RuntimeException("adapter is null,please set adapter");
        }

        for (int i = 0; i < data.size(); i++) {
            CalendarBean bean = data.get(i);
            View view = getChildAt(i);
            View childView = adapter.getView(view, this, bean);

            if (view == null || view != childView) {
                addViewInLayout(childView, i, childView.getLayoutParams(), true);
            }

            if (isToday && selectPosition == -1) {
                int[] date = CalendarUtil.getYMD(new Date());
                if (bean.year == date[0] && bean.month == date[1] && bean.day == date[2]) {
                    selectPosition = i;
                }
            } else {
                if (selectPosition == -1 && bean.day == 1) {
                    selectPosition = i;
                }
            }
            childView.setSelected(selectPosition == i);
            setItemClick(childView, i, bean);
        }
    }

    public Object[] getSelect() {
        return new Object[]{getChildAt(selectPosition), selectPosition, data.get(selectPosition)};
    }

    public void setItemClick(final View view, final int position, final CalendarBean bean) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectPosition != -1) {
                    getChildAt(selectPosition).setSelected(false);
                    getChildAt(position).setSelected(true);
                }
                selectPosition = position;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, position, bean);
                }
            }
        });
    }

    public int[] getselectPosition() {
        Rect rect = new Rect();
        try {
            getChildAt(selectPosition).getHitRect(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{rect.left, rect.top, rect.right, rect.top};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY));

        itemWidth = parentWidth / column;
        itemHeight = itemWidth;

        View view = getChildAt(0);
        if (view == null) {
            return;
        }
        LayoutParams params = view.getLayoutParams();
        if (params != null && params.height > 0) {
            itemHeight = params.height;
        }
        setMeasuredDimension(parentWidth, itemHeight * row);


        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.measure(MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY));
        }

        Log.i(TAG, "onMeasure() called with: itemHeight = [" + itemHeight + "], itemWidth = [" + itemWidth + "]");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            layoutChild(getChildAt(i), i, l, t, r, b);
        }
    }

    private void layoutChild(View view, int postion, int l, int t, int r, int b) {

        int cc = postion % column;
        int cr = postion / column;

        int itemWidth = view.getMeasuredWidth();
        int itemHeight = view.getMeasuredHeight();

        l = cc * itemWidth;
        t = cr * itemHeight;
        r = l + itemWidth;
        b = t + itemHeight;
        view.layout(l, t, r, b);

    }
}
