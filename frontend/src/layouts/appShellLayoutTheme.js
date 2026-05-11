/** 与 {@link AppShellLayout} 配套的默认 Ant Design `theme`；可在业务里传入完整 `theme` 覆盖。 */
export const appShellLayoutDefaultTheme = {
  token: {
    colorPrimary: '#6d4bd6',
    fontFamily: 'var(--sans), system-ui, sans-serif',
  },
  components: {
    Menu: {
      itemSelectedBg: '#dbeafe',
      itemSelectedColor: '#1d4ed8',
      itemHoverBg: 'rgba(59, 130, 246, 0.12)',
      itemHoverColor: '#1e40af',
      itemActiveBg: '#bfdbfe',
    },
  },
}
