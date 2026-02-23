import { defineStore } from 'pinia'
import { get } from '@/utils/request'

export const useAppStore = defineStore('app', {
  state: () => ({
    currentCampusId: null,
    currentCampusName: '',
    campusList: [],
    categoryList: []
  }),
  actions: {
    setCampus(id, name) {
      this.currentCampusId = id
      this.currentCampusName = name || ''
      uni.setStorageSync('currentCampusId', id)
      uni.setStorageSync('currentCampusName', name || '')
    },
    async loadCampusList() {
      const data = await get('/mini/campus/list', {}, { showLoading: false })
      this.campusList = Array.isArray(data) ? data : []
      const savedCampusId = uni.getStorageSync('currentCampusId')
      const savedCampusName = uni.getStorageSync('currentCampusName')
      if (savedCampusId && savedCampusName) {
        this.setCampus(savedCampusId, savedCampusName)
        return
      }
      if (!this.currentCampusId && this.campusList.length > 0) {
        const campus = this.campusList[0]
        this.setCampus(campus.id, campus.name)
      }
    },
    async loadCategoryList() {
      const data = await get('/mini/category/list', {}, { showLoading: false })
      this.categoryList = Array.isArray(data) ? data : []
    }
  }
})
