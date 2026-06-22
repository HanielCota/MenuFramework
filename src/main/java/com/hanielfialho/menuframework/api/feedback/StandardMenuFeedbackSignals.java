package com.hanielfialho.menuframework.api.feedback;

/** Standard feedback signals emitted by reusable framework components. */
public final class StandardMenuFeedbackSignals {

  public static final MenuFeedbackSignal BUTTON_CLICK = MenuFeedbackSignal.of("button.click");
  public static final MenuFeedbackSignal ACTION_SUCCESS = MenuFeedbackSignal.of("action.success");
  public static final MenuFeedbackSignal ACTION_FAILURE = MenuFeedbackSignal.of("action.failure");
  public static final MenuFeedbackSignal NAVIGATION_FORWARD =
      MenuFeedbackSignal.of("navigation.forward");
  public static final MenuFeedbackSignal NAVIGATION_BACK = MenuFeedbackSignal.of("navigation.back");
  public static final MenuFeedbackSignal MENU_CLOSE = MenuFeedbackSignal.of("menu.close");
  public static final MenuFeedbackSignal PAGE_PREVIOUS = MenuFeedbackSignal.of("page.previous");
  public static final MenuFeedbackSignal PAGE_NEXT = MenuFeedbackSignal.of("page.next");
  public static final MenuFeedbackSignal RETRY = MenuFeedbackSignal.of("retry");

  private StandardMenuFeedbackSignals() {}
}
