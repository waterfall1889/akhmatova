export const EMAIL_FORMAT_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export const emailFormatRule = {
  pattern: EMAIL_FORMAT_REGEX,
  message: 'Invalid email format',
}
