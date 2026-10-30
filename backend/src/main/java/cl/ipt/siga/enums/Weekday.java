package cl.ipt.siga.enums;

/**
 * Weekdays with the codes used by SIGA IPT: L (Monday), M (Tuesday),
 * W (Wednesday), J (Thursday) and V (Friday).
 */
public enum Weekday {
    MONDAY("L"),
    TUESDAY("M"),
    WEDNESDAY("W"),
    THURSDAY("J"),
    FRIDAY("V");

    private final String code;

    Weekday(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
