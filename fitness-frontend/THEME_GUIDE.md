# Fitness 前端主题色号规范

## 📋 目录
- [主色调](#主色调)
- [辅助色](#辅助色)
- [按钮渐变](#按钮渐变)
- [背景色](#背景色)
- [文本色](#文本色)
- [使用示例](#使用示例)

---

## 🎨 主色调

品牌核心颜色，用于主要操作和强调元素。

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `--color-primary` | `#ff6b35` | 主橙色，品牌色 |
| `--color-primary-light` | `#ff8f5a` | 浅橙色，悬停状态 |
| `--color-primary-dark` | `#e55a2b` | 深橙色，激活状态 |
| `--color-primary-hover` | `#ff7d4d` | 悬停过渡色 |
| `--color-primary-bg` | `rgba(255, 107, 53, 0.08)` | 浅色背景 |

---

## 🌈 辅助色

用于不同状态的反馈和分类。

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `--color-success` | `#52c41a` | 成功/完成状态 |
| `--color-success-light` | `#73d13d` | 成功状态浅色 |
| `--color-warning` | `#faad14` | 警告/注意状态 |
| `--color-error` | `#f5222d` | 错误/危险状态 |
| `--color-info` | `#1677ff` | 信息/提示状态 |
| `--color-info-light` | `#4096ff` | 信息状态浅色 |

---

## 🔘 按钮渐变

统一的按钮渐变色，确保视觉一致性。

### 主按钮（橙色）
```css
background: var(--btn-primary-gradient);
/* linear-gradient(135deg, #ff6b35 0%, #ff8f5a 100%) */
```

**悬停状态：**
```css
background: var(--btn-primary-gradient-hover);
/* linear-gradient(135deg, #ff7d4d 0%, #ffa070 100%) */
```

### 成功按钮（绿色）
```css
background: var(--btn-success-gradient);
/* linear-gradient(135deg, #52c41a 0%, #73d13d 100%) */
```

### 信息按钮（蓝色）
```css
background: var(--btn-info-gradient);
/* linear-gradient(135deg, #1677ff 0%, #4096ff 100%) */
```

### 紫色按钮
```css
background: var(--btn-purple-gradient);
/* linear-gradient(135deg, #722ed1 0%, #9254de 100%) */
```

---

## 🖼️ 背景色

### 页面背景
```css
--bg-page          /* 亮色: #ffffff, 暗色: #0a0a0a */
--bg-card          /* 卡片背景 */
--bg-sidebar       /* 侧边栏渐变 */
--bg-header        /* 顶部导航 */
--bg-banner        /* Banner 渐变背景 */
```

### Banner 渐变
```css
background: var(--bg-banner);
/* linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #2d1b4e 100%) */
```

---

## 📝 文本色

| 变量名 | 亮色模式 | 暗色模式 | 用途 |
|--------|---------|---------|------|
| `--text-primary` | `#1a1a1a` | `#f0f0f0` | 主要文本 |
| `--text-secondary` | `#555555` | `#bbbbbb` | 次要文本 |
| `--text-tertiary` | `#888888` | `#999999` | 辅助文本 |
| `--text-muted` | `#aaaaaa` | `#666666` | 禁用/提示文本 |
| `--text-inverse` | `#ffffff` | `#1a1a1a` | 反色文本 |

---

## 💡 使用示例

### 1. 在 Vue 组件中使用 CSS 变量

```vue
<style scoped>
.my-button {
  background: var(--btn-primary-gradient);
  color: #ffffff;
  border-radius: var(--radius-lg);
  padding: 10px 24px;
  transition: all 0.3s ease;
}

.my-button:hover {
  background: var(--btn-primary-gradient-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.3);
}

.highlight-text {
  background: var(--btn-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
```

### 2. Element Plus 按钮自动应用主题

所有 Element Plus 按钮已自动覆盖样式：

```vue
<!-- 主按钮 - 自动使用橙色渐变 -->
<el-button type="primary">主要按钮</el-button>

<!-- 成功按钮 - 自动使用绿色渐变 -->
<el-button type="success">成功按钮</el-button>

<!-- 信息按钮 - 自动使用蓝色渐变 -->
<el-button type="info">信息按钮</el-button>
```

### 3. 自定义渐变按钮类

```vue
<template>
  <button class="btn-gradient-primary">主要渐变按钮</button>
  <button class="btn-gradient-success">成功渐变按钮</button>
  <button class="btn-gradient-info">信息渐变按钮</button>
  <button class="btn-gradient-purple">紫色渐变按钮</button>
  <button class="btn-outline-primary">轮廓按钮</button>
</template>
```

### 4. 背景渐变

```vue
<style scoped>
.hero-section {
  background: var(--bg-banner);
  color: #ffffff;
}

.card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}
</style>
```

---

## ⚠️ 注意事项

### ❌ 避免硬编码颜色值

```css
/* 不要这样做 */
.button {
  background: linear-gradient(135deg, #ff6b35, #ff8f5a);
}

.text {
  color: #ff6b35;
}
```

### ✅ 使用 CSS 变量

```css
/* 推荐做法 */
.button {
  background: var(--btn-primary-gradient);
}

.text {
  color: var(--color-primary);
}
```

---

## 🎯 设计原则

1. **一致性**：所有页面使用相同的色号和渐变
2. **可维护性**：通过 CSS 变量集中管理颜色
3. **可扩展性**：支持亮色/暗色模式切换
4. **可访问性**：确保足够的对比度

---

## 📦 相关文件

- **主题变量**: `src/styles/theme.css`
- **按钮样式**: `src/styles/buttons.css`
- **入口文件**: `src/main.ts`（已引入上述样式）

---

## 🔄 更新记录

- **2026-04-23**: 创建统一主题规范，添加按钮渐变系统
- 统一登录页、注册页、个人主页的色号
- 添加 Element Plus 按钮主题覆盖
