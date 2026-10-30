package cl.ipt.siga.enums;

public enum StudentStatus {
    REGULAR, // student academically enabled to continue studying.
    POSTPONEMENT, // student who has postponed studies.
    TEMPORARY_WITHDRAWAL, // student who temporarily interrupted studies.
    GRADUATED, // student who has approved every subject in their study plan.
    ELIMINATED // student who lost their status after failing the same subject a second time.
}
