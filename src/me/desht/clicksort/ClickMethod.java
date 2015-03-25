package me.desht.clicksort;

public enum ClickMethod
{
    MIDDLE, DOUBLE, SINGLE, NONE;
    
    public ClickMethod next()
    {
        int o = (ordinal() + 1) % values().length;
        return values()[o];
    }
    
    public String getInstruction()
    {
        switch (this)
        {
            case SINGLE:
                return "Single-click an empty inventory slot to sort.";
            case DOUBLE:
                return "Double-click to sort.";
            case MIDDLE:
                return "Middle-click to sort.";
            default:
                return "Click-sorting has been disabled.";
        }
    }
}
