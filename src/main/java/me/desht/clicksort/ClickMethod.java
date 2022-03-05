package me.desht.clicksort;

import me.desht.dhutils.CompatUtil;
import me.desht.dhutils.LogUtils;

public enum ClickMethod {
    MIDDLE, DOUBLE, SINGLE, SWAP, NONE;

    public ClickMethod next() {
        int o = (ordinal() + 1) % values().length;
        return values()[o];
    }

    public boolean isAvailable() {
        return switch (this) {
            case MIDDLE -> CompatUtil.isMiddleClickAllowed();
            case SWAP -> CompatUtil.isSwapKeyAvailable();
            default -> true;
        };
    }

    public boolean shouldCancelEvent() {
        return this == SWAP;
    }

    public String getInstruction() {
        return switch (this) {
            case SINGLE -> LanguageLoader.getMessage("instructionSingle");
            case DOUBLE -> LanguageLoader.getMessage("instructionDouble");
            case MIDDLE -> LanguageLoader.getMessage("instructionMiddle");
            case SWAP -> LanguageLoader.getMessage("instructionSwap");
            default -> LanguageLoader.getMessage("instructionDisabled");
        };
    }

    public static ClickMethod preferredDefault() {
        if (SWAP.isAvailable()) {
            return SWAP;
        } else if (MIDDLE.isAvailable()) {
            return MIDDLE;
        }
        return DOUBLE;
    }

    public static ClickMethod parse(String clickMethod) {
        return parse(clickMethod, ClickSortPlugin.getInstance().getDefaultClickMethod());
    }

    public static ClickMethod parse(String clickMethod, ClickMethod defaultMethod) {
        try {
            ClickMethod method = ClickMethod.valueOf(clickMethod);
            if (!method.isAvailable()) {
                LogUtils.warning("unavailable click method " + clickMethod + " - default to " + defaultMethod);
                method = defaultMethod;
            }
            return method;
        } catch (IllegalArgumentException e) {
            LogUtils.warning("invalid click method " + clickMethod + " - default to " + defaultMethod);
            return defaultMethod;
        }
    }
}
