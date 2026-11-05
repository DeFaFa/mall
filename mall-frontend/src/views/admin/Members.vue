<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="账号 / 昵称 / 手机号" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="username" label="账号" width="150" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="memberLevelId" label="等级 ID" width="90" />
      <el-table-column prop="integration" label="积分" width="90" />
      <el-table-column prop="growth" label="成长值" width="100" />
      <el-table-column prop="city" label="城市" width="110" />
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button text :type="row.status === 1 ? 'danger' : 'primary'" @click="toggle(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="load" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(true)

async function load(page = pageNum.value) {
  loading.value = true
  try {
    const data = await http.get('/admin/member/list', {
      params: { pageNum: page, pageSize: pageSize.value, keyword: keyword.value || undefined }
    })
    list.value = data.list
    total.value = data.total
    pageNum.value = page
  } finally {
    loading.value = false
  }
}

async function toggle(row) {
  await http.post('/admin/member/updateStatus', null, {
    params: { id: row.id, status: row.status === 1 ? 0 : 1 }
  })
  ElMessage.success('已更新')
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
