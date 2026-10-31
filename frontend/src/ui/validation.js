/**
 * Basic RUN format: 7 or 8 digits, a hyphen and a check digit (0-9 or K).
 * It intentionally does not validate the modulo-11 check digit.
 */
export const RUN_PATTERN = /^\d{7,8}-[0-9Kk]$/;

export function isValidRun(run) {
  return RUN_PATTERN.test(run);
}
