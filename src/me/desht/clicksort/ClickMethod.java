package me.desht.clicksort;

public enum ClickMethod {
    MIDDLE, DOUBLE, SINGLE, NONE;

    public ClickMethod next() {
        int o = (ordinal() + 1) % values().length;
        return values()[o];
    }

    public String getInstruction() {
        switch (this) {
            case SINGLE:
                return LanguageLoader.getMessage("instructionSingle");
            case DOUBLE:
                return LanguageLoader.getMessage("instructionDouble");
            case MIDDLE:
                return LanguageLoader.getMessage("instructionMiddle");
            default:
                return LanguageLoader.getMessage("instructionDisabled");
        }
    }
}
