import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles.css'

const app = createApp(App)

// 任意图片加载失败时回退到本地占位图，避免出现破损图标
window.addEventListener(
  'error',
  (event) => {
    const target = event.target
    if (target && target.tagName === 'IMG' && !target.dataset.fallback) {
      target.dataset.fallback = '1'
      target.src = '/images/placeholder.svg'
    }
  },
  true
)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(ElementPlus)
app.use(router)
app.mount('#app')
