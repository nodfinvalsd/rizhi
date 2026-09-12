import axios from 'axios'

export const BASE_URL = 'http://127.0.0.1:8900'

const api = axios.create({
  baseURL: BASE_URL + '/api',
  timeout: 30000,
})

let backendReady = false
let retried = 0

api.interceptors.response.use(
  (r) => {
    backendReady = true
    const d = r.data
    if (d && d.code !== 0) {
      return Promise.reject(new Error(d.message))
    }
    return d.data
  },
  async (err) => {
    // 后端刚启动未就绪时自动重试（最多 30 次 × 2 秒）
    if (!backendReady && !err.response && retried < 30) {
      retried++
      await new Promise((res) => setTimeout(res, 2000))
      return api.request(err.config)
    }
    const msg = err.response?.data?.message || err.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export default api
