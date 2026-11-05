<template>
  <div class="mall-card panel" v-loading="loading">
    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="商品收藏" name="collection">
        <div class="grid">
          <div v-for="item in collections" :key="item.id" class="item" @click="$router.push(`/product/${item.productId}`)">
            <img :src="item.productPic" />
            <div class="name">{{ item.productName }}</div>
            <div class="foot">
              <span class="price">{{ item.productPrice }}</span>
              <el-button text type="danger" @click.stop="removeCollection(item)">取消收藏</el-button>
            </div>
          </div>
        </div>
        <div v-if="!collections.length" class="empty-block">还没有收藏商品</div>
      </el-tab-pane>
      <el-tab-pane label="品牌关注" name="attention">
        <div class="brand-list">
          <div v-for="item in attentions" :key="item.id" class="brand-item">
            <div>
              <div class="brand-name">{{ item.brandName }}</div>
              <div class="muted">关注于 {{ item.createTime }}</div>
            </div>
            <el-button text type="danger" @click="removeAttention(item)">取消关注</el-button>
          </div>
        </div>
        <div v-if="!attentions.length" class="empty-block">还没有关注品牌</div>
      </el-tab-pane>
      <el-tab-pane label="浏览足迹" name="history">
        <div class="history-head">
          <span class="muted">最近 {{ histories.length }} 条记录</span>
          <el-button text type="danger" @click="clearHistory" :disabled="!histories.length">清空足迹</el-button>
        </div>
        <div class="grid">
          <div v-for="item in histories" :key="item.id" class="item" @click="$router.push(`/product/${item.productId}`)">
            <img :src="item.productPic" />
            <div class="name">{{ item.productName }}</div>
            <div class="foot">
              <span class="price">{{ item.productPrice }}</span>
              <span class="muted">{{ item.createTime?.slice(0, 10) }}</span>
            </div>
          </div>
        </div>
        <div v-if="!histories.length" class="empty-block">暂无浏览记录</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const tab = ref('collection')
const collections = ref([])
const attentions = ref([])
const histories = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    if (tab.value === 'collection') collections.value = await http.get('/portal/collection/list')
    if (tab.value === 'attention') attentions.value = await http.get('/portal/attention/list')
    if (tab.value === 'history') histories.value = await http.get('/portal/history/list')
  } finally {
    loading.value = false
  }
}

async function removeCollection(item) {
  await http.post(`/portal/collection/delete/${item.productId}`)
  ElMessage.success('已取消收藏')
  load()
}

async function removeAttention(item) {
  await http.post(`/portal/attention/delete/${item.brandId}`)
  ElMessage.success('已取消关注')
  load()
}

async function clearHistory() {
  await ElMessageBox.confirm('确定清空浏览足迹吗？', '提示', { type: 'warning' })
  await http.post('/portal/history/clear')
  load()
}

onMounted(load)
</script>

<style scoped>
.panel {
  padding: 18px 24px 24px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.item {
  border: 1px solid #f0f1f4;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
}
.item img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  background: #f4f5f7;
}
.name {
  font-size: 13px;
  margin: 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.brand-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.brand-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #f0f1f4;
  border-radius: 10px;
}
.brand-name {
  font-weight: 600;
}
.history-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
@media (max-width: 800px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
