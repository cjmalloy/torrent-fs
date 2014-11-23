package com.cjmalloy.torrentfs.model;



public enum Encoding
{
    BENCODE_BASE64("bencode"),
    MAGNET("magnet"),
    JSON("json");

    private String strValue;

    Encoding(String strValue)
    {
        this.strValue = strValue;
    }

    @Override
    public String toString()
    {
        return strValue;
    }

    public static Encoding getEncoding(String strValue)
    {
        for (Encoding enc : Encoding.values())
        {
            if (enc.toString().equalsIgnoreCase(strValue)) return enc;
        }
        return null;
    }
}
