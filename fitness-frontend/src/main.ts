import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/theme.css'
import '@/styles/buttons.css'
import App from './App.vue'
import router from './router'

// ECharts & vue-echarts
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, RadarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import VueECharts from 'vue-echarts'

use([CanvasRenderer, LineChart, BarChart, RadarChart, GridComponent, TooltipComponent, LegendComponent, RadarComponent])

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.component('v-chart', VueECharts)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
