import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const isMobile = ref(false)

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setMobile = (value) => {
    isMobile.value = value
    if (value) {
      sidebarCollapsed.value = true
    }
  }

  const checkMobile = () => {
    setMobile(window.innerWidth < 768)
  }

  return {
    sidebarCollapsed,
    isMobile,
    toggleSidebar,
    setMobile,
    checkMobile
  }
})

