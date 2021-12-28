package helpers;

import java.util.Locale;

public class SpeechLocale {

    public static Locale DecideLocale(String locale) {
        assert locale != null;
        switch (locale) {
            case "Chinese":
                return Locale.CHINESE;
            case "French":
                return Locale.FRENCH;
            case "German":
                return Locale.GERMAN;
            case "Italian":
                return Locale.ITALIAN;
            case "Japanese":
                return Locale.JAPANESE;
            case "Korean":
                return Locale.KOREAN;
            default:
                return Locale.ENGLISH;
        }
    }

}
