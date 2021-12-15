package me.desht.clicksort;

import me.desht.dhutils.CompatUtil;

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

    public String getInstruction() {
        return switch (this) {
            case SINGLE -> LanguageLoader.getMessage("instructionSingle");
            case DOUBLE -> LanguageLoader.getMessage("instructionDouble");
            case MIDDLE -> LanguageLoader.getMessage("instructionMiddle");
            case SWAP -> LanguageLoader.getMessage("instructionSwap");
            default -> LanguageLoader.getMessage("instructionDisabled");
        };
    }
}
