package cl.ipt.siga.enums;

public enum EnrollmentResult {
    PENDING, // final grade not registered yet.
    APPROVED, // final grade >= 4.0.
    FAILED // final grade < 4.0.
}
