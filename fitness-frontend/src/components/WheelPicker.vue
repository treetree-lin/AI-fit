<template>
  <div class="wheel-picker">
    <div class="wheel-wrapper">
      <div ref="wheelRef" class="wheel-list" @scroll="onScroll">
        <div
          v-for="item in displayItems"
          :key="item.value"
          class="wheel-item"
          :class="{ active: modelValue === item.value }"
          :data-value="item.value"
          @click="select(item.value)"
        >
          {{ item.label }}
        </div>
      </div>
      <div class="wheel-highlight" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick, computed } from 'vue'

const props = defineProps<{
  modelValue?: number
  min: number
  max: number
  step?: number
  unit?: string
  precision?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
}>()

const wheelRef = ref<HTMLDivElement>()
const ITEM_HEIGHT = 40

const displayItems = computed(() => {
  const items = []
  const step = props.step || 1
  const precision = props.precision || 0
  for (let v = props.min; v <= props.max; v += step) {
    const val = precision > 0 ? parseFloat(v.toFixed(precision)) : v
    items.push({
      value: val,
      label: val + (props.unit || '')
    })
  }
  return items
})

const onScroll = () => {
  if (!wheelRef.value) return
  const scrollTop = wheelRef.value.scrollTop
  const index = Math.round(scrollTop / ITEM_HEIGHT)
  const item = displayItems.value[index]
  if (item && item.value !== props.modelValue) {
    emit('update:modelValue', item.value)
  }
}

const select = (val: number) => {
  emit('update:modelValue', val)
  scrollToValue(val)
}

const scrollToValue = (val?: number) => {
  nextTick(() => {
    if (!wheelRef.value || val === undefined) return
    const index = displayItems.value.findIndex(i => i.value === val)
    if (index >= 0) {
      wheelRef.value.scrollTo({ top: index * ITEM_HEIGHT, behavior: 'smooth' })
    }
  })
}

onMounted(() => scrollToValue(props.modelValue))
watch(() => props.modelValue, scrollToValue)
</script>

<style scoped>
.wheel-picker {
  width: 100%;
}

.wheel-wrapper {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: var(--bg-input);
  border-radius: 12px;
}

.wheel-list {
  height: 100%;
  overflow-y: auto;
  scroll-snap-type: y mandatory;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding: 60px 0;
}

.wheel-list::-webkit-scrollbar {
  display: none;
}

.wheel-item {
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 15px;
  color: var(--text-secondary);
  scroll-snap-align: center;
  transition: all 0.2s;
  cursor: pointer;
}

.wheel-item.active {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

.wheel-highlight {
  position: absolute;
  top: 50%;
  left: 10%;
  right: 10%;
  transform: translateY(-50%);
  height: 40px;
  border-top: 1px solid var(--color-primary);
  border-bottom: 1px solid var(--color-primary);
  pointer-events: none;
  opacity: 0.3;
}
</style>
