export const adminTokenKey = 'mall_admin_token'
export const adminInfoKey = 'mall_admin_info'
export const memberTokenKey = 'mall_member_token'
export const memberInfoKey = 'mall_member_info'

export const getAdminToken = () => localStorage.getItem(adminTokenKey)
export const getMemberToken = () => localStorage.getItem(memberTokenKey)

export function getAdminInfo() {
  try {
    return JSON.parse(localStorage.getItem(adminInfoKey) || 'null')
  } catch {
    return null
  }
}

export function getMemberInfo() {
  try {
    return JSON.parse(localStorage.getItem(memberInfoKey) || 'null')
  } catch {
    return null
  }
}

export function setAdminAuth(token, info) {
  localStorage.setItem(adminTokenKey, token)
  if (info) localStorage.setItem(adminInfoKey, JSON.stringify(info))
}

export function setMemberAuth(token, member) {
  localStorage.setItem(memberTokenKey, token)
  if (member) localStorage.setItem(memberInfoKey, JSON.stringify(member))
}

export function clearAdminAuth() {
  localStorage.removeItem(adminTokenKey)
  localStorage.removeItem(adminInfoKey)
}

export function clearMemberAuth() {
  localStorage.removeItem(memberTokenKey)
  localStorage.removeItem(memberInfoKey)
}
