// 会话存储工具
// 使用 sessionStorage 存储认证信息，关闭浏览器标签页/窗口后会话状态会被自动清除

const TOKEN_KEY = 'ai_doc_token'
const USER_KEY = 'ai_doc_user'

export const storage = {
  // Token 相关
  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY)
  },

  setToken(token: string): void {
    sessionStorage.setItem(TOKEN_KEY, token)
  },

  removeToken(): void {
    sessionStorage.removeItem(TOKEN_KEY)
  },

  // User 相关
  getUser(): any | null {
    const userStr = sessionStorage.getItem(USER_KEY)
    return userStr ? JSON.parse(userStr) : null
  },

  setUser(user: any): void {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  removeUser(): void {
    sessionStorage.removeItem(USER_KEY)
  },

  // 清空所有
  clear(): void {
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(USER_KEY)
  }
}
