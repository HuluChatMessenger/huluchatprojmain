package org.plus.features.transalte;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.util.HashMap;


public class Language {
    public static final String AFRIKAANS = "af";
    public static final String ALBANIAN = "sq";
    public static final String ARABIC = "ar";
    public static final String ARMENIAN = "hy";
    public static final String AZERBAIJANI = "az";
    public static final String BASQUE = "eu";
    public static final String BELARUSIAN = "be";
    public static final String BENGALI = "bn";
    public static final String BULGARIAN = "bg";
    public static final String CATALAN = "ca";
    public static final String CHINESE = "zh-CN";
    public static final String CROATIAN = "hr";
    public static final String CZECH = "cs";
    public static final String DANISH = "da";
    public static final String DUTCH = "nl";
    public static final String ENGLISH = "en";
    public static final String ESTONIAN = "et";
    public static final String FILIPINO = "tl";
    public static final String FINNISH = "fi";
    public static final String FRENCH = "fr";
    public static final String GALICIAN = "gl";
    public static final String GEORGIAN = "ka";
    public static final String GERMAN = "de";
    public static final String GREEK = "el";
    public static final String GUJARATI = "gu";
    public static final String HAITIAN_CREOLE = "ht";
    public static final String HEBREW = "iw";
    public static final String HINDI = "hi";
    public static final String HUNGARIAN = "hu";
    public static final String ICELANDIC = "is";
    public static final String INDONESIAN = "id";
    public static final String IRISH = "ga";
    public static final String ITALIAN = "it";
    public static final String JAPANESE = "ja";
    public static final String KANNADA = "kn";
    public static final String KOREAN = "ko";
    public static final String LATIN = "la";
    public static final String LATVIAN = "lv";
    public static final String LITHUANIAN = "lt";
    public static final String MACEDONIAN = "mk";
    public static final String MALAY = "ms";
    public static final String MALTESE = "mt";
    public static final String NORWEGIAN = "no";
    public static final String PERSIAN = "fa";
    public static final String POLISH = "pl";
    public static final String PORTUGUESE = "pt";
    public static final String ROMANIAN = "ro";
    public static final String RUSSIAN = "ru";
    public static final String SERBIAN = "sr";
    public static final String SLOVAK = "sk";
    public static final String SLOVENIAN = "sl";
    public static final String SPANISH = "es";
    public static final String SWAHILI = "sw";
    public static final String SWEDISH = "sv";
    public static final String TAMIL = "ta";
    public static final String TELUGU = "te";
    public static final String THAI = "th";
    public static final String TURKISH = "tr";
    public static final String UKRAINIAN = "uk";
    public static final String URDU = "ur";
    public static final String VIETNAMESE = "vi";
    public static final String WELSH = "cy";
    public static final String YIDDISH = "yi";
    public static final String CHINESE_SIMPLIFIED = "zh-CN";
    public static final String CHINESE_TRADITIONAL = "zh-TW";
    private static Language language;
    public HashMap<String, String> hashLanguage = new HashMap();


    private Language() {
        this.init();
    }

    public static synchronized Language getInstance() {
        if (language == null) {
            language = new Language();
        }

        return language;
    }

    private void init() {
        this.hashLanguage.put("af", "Afrikaans");
        this.hashLanguage.put("sq", "Albanian");
        this.hashLanguage.put("ar", "Arabic");
        this.hashLanguage.put("hy", "Armenian");
        this.hashLanguage.put("am","Amharic");
        this.hashLanguage.put("az", "Azerbaijani");
        this.hashLanguage.put("eu", "Basque");
        this.hashLanguage.put("be", "Belorussian");
        this.hashLanguage.put("bn", "Bengali");
        this.hashLanguage.put("bg", "Bulgarian");
        this.hashLanguage.put("ca", "Catalan");
        this.hashLanguage.put("zh-CN", "Chinese");
        this.hashLanguage.put("hr", "Croatian");
        this.hashLanguage.put("cs", "Czech");
        this.hashLanguage.put("da", "Danish");
        this.hashLanguage.put("nl", "Dutch");
        this.hashLanguage.put("en", "English");
        this.hashLanguage.put("et", "Estonian");
        this.hashLanguage.put("tl", "Filipino");
        this.hashLanguage.put("fi", "Finnish");
        this.hashLanguage.put("fr", "French");
        this.hashLanguage.put("gl", "Galician");
        this.hashLanguage.put("ka", "Georgian");
        this.hashLanguage.put("de", "German");
        this.hashLanguage.put("el", "Greek");
        this.hashLanguage.put("gu", "Gujarati");
        this.hashLanguage.put("ht", "Haitian_Creole");
        this.hashLanguage.put("iw", "Hebrew");
        this.hashLanguage.put("hi", "Hindi");
        this.hashLanguage.put("hu", "Hungarian");
        this.hashLanguage.put("is", "Icelandic");
        this.hashLanguage.put("id", "Indonesian");
        this.hashLanguage.put("ga", "Irish");
        this.hashLanguage.put("it", "Italian");
        this.hashLanguage.put("ja", "Japanese");
        this.hashLanguage.put("kn", "Kannada");
        this.hashLanguage.put("ko", "Korean");
        this.hashLanguage.put("la", "Latin");
        this.hashLanguage.put("lv", "Latvian");
        this.hashLanguage.put("lt", "Lithuanian");
        this.hashLanguage.put("mk", "Macedonian");
        this.hashLanguage.put("ms", "Malay");
        this.hashLanguage.put("mt", "Maltese");
        this.hashLanguage.put("no", "Norwegian");
        this.hashLanguage.put("fa", "Persian");
        this.hashLanguage.put("pl", "Polish");
        this.hashLanguage.put("pt", "Portuguese");
        this.hashLanguage.put("ro", "Romanian");
        this.hashLanguage.put("ru", "Russian");
        this.hashLanguage.put("sr", "Serbian");
        this.hashLanguage.put("sk", "Slovak");
        this.hashLanguage.put("sl", "Slovenian");
        this.hashLanguage.put("es", "Spanish");
        this.hashLanguage.put("sw", "Swahili");
        this.hashLanguage.put("sv", "Swedish");
        this.hashLanguage.put("ta", "Tamil");
        this.hashLanguage.put("te", "Telugu");
        this.hashLanguage.put("th", "Thai");
        this.hashLanguage.put("tr", "Turkish");
        this.hashLanguage.put("uk", "Ukrainian");
        this.hashLanguage.put("ur", "Urdu");
        this.hashLanguage.put("vi", "Vietnamese");
        this.hashLanguage.put("cy", "Welsh");
        this.hashLanguage.put("yi", "Yiddish");
        this.hashLanguage.put("ar", "Arabic");
        this.hashLanguage.put("hy", "Armenian");
        this.hashLanguage.put("az", "Azerbaijani");
        this.hashLanguage.put("eu", "Basque");
        this.hashLanguage.put("be", "Belarusian");
        this.hashLanguage.put("bn", "Bengali");
        this.hashLanguage.put("bg", "Bulgarian");
        this.hashLanguage.put("ca", "Catalan");
        this.hashLanguage.put("zh-CN", "Chinese_Simplified");
        this.hashLanguage.put("zh-TW", "Chinese_Traditional");
        this.hashLanguage.put("hr", "Croatian");
        this.hashLanguage.put("cs", "Czech");
        this.hashLanguage.put("da", "Danish");
        this.hashLanguage.put("nl", "Dutch");
        this.hashLanguage.put("et", "Estonian");
        this.hashLanguage.put("tl", "Filipino");
        this.hashLanguage.put("fi", "Finnish");
        this.hashLanguage.put("fr", "French");
        this.hashLanguage.put("gl", "Galician");
        this.hashLanguage.put("ka", "Georgian");
        this.hashLanguage.put("de", "German");
        this.hashLanguage.put("el", "Greek");
        this.hashLanguage.put("gu", "Gujarati");
        this.hashLanguage.put("ht", "Haitian_creole");
        this.hashLanguage.put("iw", "Hebrew");
        this.hashLanguage.put("hi", "Hindi");
        this.hashLanguage.put("hu", "Hungarian");
        this.hashLanguage.put("is", "Icelandic");
        this.hashLanguage.put("id", "Indonesian");
        this.hashLanguage.put("ga", "Irish");
        this.hashLanguage.put("it", "Italian");
        this.hashLanguage.put("ja", "Japanese");
        this.hashLanguage.put("kn", "Kannada");
        this.hashLanguage.put("ko", "Korean");
        this.hashLanguage.put("la", "Latin");
        this.hashLanguage.put("lv", "Latvian");
        this.hashLanguage.put("lt", "Lithuanian");
        this.hashLanguage.put("mk", "Macedonian");
        this.hashLanguage.put("ms", "Malay");
        this.hashLanguage.put("mt", "Maltese");
        this.hashLanguage.put("no", "Norwegian");
        this.hashLanguage.put("fa", "Persian");
        this.hashLanguage.put("pl", "Polish");
        this.hashLanguage.put("pt", "Portuguse");
        this.hashLanguage.put("ro", "Romanian");
        this.hashLanguage.put("ru", "Russian");
        this.hashLanguage.put("sr", "Serbian");
        this.hashLanguage.put("sk", "Slovak");
        this.hashLanguage.put("sl", "Slovenian");
        this.hashLanguage.put("es", "Spanish");
        this.hashLanguage.put("sw", "Swahili");
        this.hashLanguage.put("sv", "Swedish");
        this.hashLanguage.put("ta", "Tamil");
        this.hashLanguage.put("te", "Telugu");
        this.hashLanguage.put("th", "Thai");
        this.hashLanguage.put("tr", "Turkish");
        this.hashLanguage.put("uk", "Ukrainian");
        this.hashLanguage.put("ur", "Urdu");
        this.hashLanguage.put("uz","Uzbek");
        this.hashLanguage.put("vi", "Vietnamese");
        this.hashLanguage.put("cy", "Welsh");
        this.hashLanguage.put("yi", "Yiddish");
    }

    public String getNameLanguage(String prefixLanguage) {
        return (String)this.hashLanguage.get(prefixLanguage);
    }

    public HashMap getLang() {
        return hashLanguage;
    }
}
