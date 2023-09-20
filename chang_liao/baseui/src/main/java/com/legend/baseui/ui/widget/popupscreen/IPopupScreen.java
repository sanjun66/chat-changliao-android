package com.legend.baseui.ui.widget.popupscreen;

import java.util.List;

public interface IPopupScreen {

    String getId();

    String getImageUrl();

    String getGotoUrl();

    List<? extends IClickArea> getClickableAreaList();
}
