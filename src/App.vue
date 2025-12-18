<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: var(--nav-active, #333);
  background: var(--page-bg, #f6f7f9);
}

:root{
  --page-bg: #f6f7f9;
  --nav-bg: #f4f6f7;
  --nav-active: #2b3250;
  --primary: #2cc7b7;
  --accent: #fb8c4a;
  --muted: #7f8a97;
  --card-bg: #ffffff;
  --soft-shadow: 0 6px 20px rgba(43,50,80,0.06);
}

/* Global Element Plus overrides using project variables */
.el-button--primary {
  background-color: var(--primary) !important;
  border-color: var(--primary) !important;
  color: #fff !important;
}

.el-button[type="text"]:hover {
  color: var(--accent) !important;
}

.el-switch.is-checked .el-switch__core {
  background-color: var(--primary) !important;
  border-color: var(--primary) !important;
}

.el-radio__input.is-checked .el-radio__inner {
  border-color: var(--primary) !important;
  background: var(--primary) !important;
}

.el-input__inner:focus {
  border-color: var(--primary) !important;
  box-shadow: 0 6px 20px rgba(43,50,80,0.06);
}

.el-tag--success {
  background-color: rgba(44,199,183,0.08) !important;
  color: var(--primary) !important;
  border-color: rgba(44,199,183,0.12) !important;
}

.el-tag--warning {
  background-color: rgba(251,140,74,0.08) !important;
  color: var(--accent) !important;
  border-color: rgba(251,140,74,0.12) !important;
}

/* 额外卡片色系变量（用于仪表盘卡片样式） */
:root{
  --soft-purple: #efe9fb; /* 浅紫 */
  --soft-purple-600: #c9bff6;
  --soft-peach: #fbefe6;  /* 浅桃 */
  --soft-peach-600: #f6cdb0;
  --soft-green: #eef9ef;  /* 浅绿 */
  --soft-green-600: #cdeccd;
}

/* 通用卡片色系类，组件中可直接添加对应类名 */
.card-tone {
  border-radius: 12px;
  padding: 16px;
  color: var(--nav-active);
}

.card-tone--purple {
  background: linear-gradient(180deg, var(--soft-purple), rgba(232,223,250,0.6));
}

.card-tone--peach {
  background: linear-gradient(180deg, var(--soft-peach), rgba(252,240,228,0.6));
}

.card-tone--green {
  background: linear-gradient(180deg, var(--soft-green), rgba(236,250,236,0.6));
}

/* 辅助：色块内的阴影/分割 */
.card-tone .card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.04);
}
</style>
