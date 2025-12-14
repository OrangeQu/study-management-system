module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended'
  ],
  parserOptions: {
    parser: '@babel/eslint-parser'
  },
  rules: {
    'vue/multi-word-component-names': 'off' // 关闭多单词组件名规则
  },
  // 添加这个 globals 配置
  globals: {
    defineProps: 'readonly',
    defineEmits: 'readonly', 
    defineExpose: 'readonly',
    withDefaults: 'readonly'
  }
}