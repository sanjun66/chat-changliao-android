package com.legend.baseui.ui.widget.slide;

import java.util.ArrayList;
import java.util.List;

public class SlideManager {
    private List<SlideLayout> mSlides;

    public SlideManager() {
        mSlides = new ArrayList<>();
    }

    public void onChange(SlideLayout layout, boolean isOpen) {
        if (isOpen) {
            mSlides.add(layout);
        } else {
            mSlides.remove(layout);
        }
    }

    public boolean closeAll(SlideLayout layout) {
        boolean ret = false;
        if (mSlides.size() <= 0) {
            return false;
        }
        for (int i = 0; i < mSlides.size(); i++) {
            SlideLayout slide = mSlides.get(i);
            if (slide != null && slide != layout) {
                slide.close();
                mSlides.remove(slide);
                ret = true;
                i--;
            }
        }
        return ret;
    }
}
