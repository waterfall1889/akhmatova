import { Outlet } from 'react-router-dom'
import { ConfigProvider, Layout, Menu, theme as antdTheme } from 'antd'
import { appShellLayoutDefaultTheme } from './appShellLayoutTheme.js'
import './AppShellLayout.css'

const { Header, Content, Sider } = Layout

/**
 * 通用应用壳：紫顶栏 + 左侧菜单 + 右侧主区（嵌套路由用 {@link Outlet}）。
 * 在 `createBrowserRouter` 中作为父级 `element` 使用，子路由在 `children` 里渲染到主内容区。
 *
 * @param {object} props
 * @param {string} [props.brand='AKHMATOVA'] 顶栏左侧品牌文案
 * @param {import('antd').MenuProps['items']} props.menuItems 侧栏菜单项
 * @param {string[]} props.selectedKeys 当前选中的菜单 key（与 items 中 key 一致）
 * @param {import('antd').MenuProps['onClick']} props.onMenuClick 菜单点击
 * @param {number} [props.siderWidth=200] 侧栏宽度
 * @param {import('antd').ThemeConfig} [props.theme] 覆盖默认 {@link appShellLayoutDefaultTheme}
 */
export function AppShellLayout({
  brand = 'AKHMATOVA',
  menuItems,
  selectedKeys,
  onMenuClick,
  siderWidth = 200,
  theme: themeOverride,
}) {
  return (
    <ConfigProvider theme={themeOverride ?? appShellLayoutDefaultTheme}>
      <AppShellLayoutInner
        brand={brand}
        menuItems={menuItems}
        selectedKeys={selectedKeys}
        onMenuClick={onMenuClick}
        siderWidth={siderWidth}
      />
    </ConfigProvider>
  )
}

function AppShellLayoutInner({ brand, menuItems, selectedKeys, onMenuClick, siderWidth }) {
  const {
    token: { colorBgContainer, colorSplit },
  } = antdTheme.useToken()

  return (
    <Layout className="app-shell">
      <Header className="app-shell__header">
        <div className="app-shell__logo">{brand}</div>
      </Header>
      <div className="app-shell__wrap">
        <Layout
          className="app-shell__main"
          style={{
            padding: '24px 0',
            background: colorBgContainer,
            borderRadius: 0,
          }}
        >
          <Sider
            className="app-shell__sider"
            style={{
              background: colorBgContainer,
              borderInlineEnd: `1px solid ${colorSplit}`,
            }}
            width={siderWidth}
          >
            <Menu
              mode="inline"
              selectedKeys={selectedKeys}
              style={{ height: '100%', borderRight: 0 }}
              className="app-shell__menu"
              items={menuItems}
              onClick={onMenuClick}
            />
          </Sider>
          <Content className="app-shell__content">
            <Outlet />
          </Content>
        </Layout>
      </div>
    </Layout>
  )
}
