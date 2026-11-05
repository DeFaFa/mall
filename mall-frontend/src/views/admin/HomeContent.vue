<template>
  <el-card shadow="never" v-loading="loading">
    <el-tabs v-model="tab" @tab-change="loadCurrent">
      <el-tab-pane label="广告位" name="advertise">
        <div class="toolbar">
          <span class="muted">按位置区分轮播图（type=0）与推荐位（type=1）</span>
          <el-button type="danger" @click="openAdvertise()">新增广告</el-button>
        </div>
        <el-table :data="ads">
          <el-table-column label="图片" width="110">
            <template #default="{ row }"><img :src="row.pic" class="thumb" /></template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="180" />
          <el-table-column label="位置" width="90">
            <template #default="{ row }">{{ row.type === 0 ? '轮播' : '推荐' }}</template>
          </el-table-column>
          <el-table-column prop="url" label="跳转" min-width="140" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="70" />
          <el-table-column label="有效期" min-width="240">
            <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1" @change="(v) => toggleAdvertise(row, v)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button text type="primary" @click="openAdvertise(row)">编辑</el-button>
              <el-button text type="danger" @click="removeRow('/admin/home/advertise/delete', [row.id])">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="品牌推荐" name="brand">
        <div class="toolbar">
          <span class="muted">首页品牌精选区域</span>
          <el-button type="danger" @click="openBrand()">新增推荐品牌</el-button>
        </div>
        <el-table :data="homeBrands">
          <el-table-column prop="brandName" label="品牌" min-width="180" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="推荐" width="100">
            <template #default="{ row }">
              <el-tag size="small" type="success">推荐中</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button text type="danger" @click="removeRow('/admin/home/brand/delete', [row.id])">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="新品推荐" name="newProduct">
        <div class="toolbar">
          <span class="muted">首页新品首发区域</span>
          <el-button type="danger" @click="openHomeProduct('new')">新增新品推荐</el-button>
        </div>
        <el-table :data="newProducts">
          <el-table-column prop="productName" label="商品" min-width="240" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button text type="danger" @click="removeRow('/admin/home/newProduct/delete', [row.id])">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="人气推荐" name="recommendProduct">
        <div class="toolbar">
          <span class="muted">首页人气推荐区域</span>
          <el-button type="danger" @click="openHomeProduct('recommend')">新增人气推荐</el-button>
        </div>
        <el-table :data="recommendProducts">
          <el-table-column prop="productName" label="商品" min-width="240" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="操作" width="130">
            <template #default="{ row }">
              <el-button text type="danger" @click="removeRow('/admin/home/recommendProduct/delete', [row.id])">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="advertiseDialog" :title="advertiseForm.id ? '编辑广告' : '新增广告'" width="560px">
      <el-form :model="advertiseForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="advertiseForm.name" /></el-form-item>
        <el-form-item label="位置">
          <el-radio-group v-model="advertiseForm.type">
            <el-radio :value="0">轮播图</el-radio>
            <el-radio :value="1">推荐位</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="图片">
          <div class="upload-row">
            <el-input v-model="advertiseForm.pic" placeholder="图片 URL" />
            <el-upload :show-file-list="false" :http-request="uploadAd">
              <el-button>上传</el-button>
            </el-upload>
          </div>
          <img v-if="advertiseForm.pic" :src="advertiseForm.pic" class="preview" />
        </el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="advertiseForm.url" placeholder="/product/1" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="advertiseForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="advertiseForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="advertiseForm.sort" :min="0" /></el-form-item>
        <el-form-item label="上线"><el-switch v-model="advertiseForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="advertiseDialog = false">取消</el-button>
        <el-button type="danger" @click="saveAdvertise">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="brandDialog" title="新增推荐品牌" width="420px">
      <el-form label-width="90px">
        <el-form-item label="品牌">
          <el-select v-model="brandForm.brandId" style="width: 100%">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="brandForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="brandDialog = false">取消</el-button>
        <el-button type="danger" @click="saveBrand">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="productDialog" :title="homeProductMode === 'new' ? '新增新品推荐' : '新增人气推荐'" width="440px">
      <el-form label-width="90px">
        <el-form-item label="商品">
          <el-select v-model="homeProductForm.productId" filterable style="width: 100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="homeProductForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialog = false">取消</el-button>
        <el-button type="danger" @click="saveHomeProduct">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../utils/request'

const tab = ref('advertise')
const loading = ref(true)
const ads = ref([])
const homeBrands = ref([])
const newProducts = ref([])
const recommendProducts = ref([])
const brands = ref([])
const products = ref([])
const advertiseDialog = ref(false)
const brandDialog = ref(false)
const productDialog = ref(false)
const homeProductMode = ref('new')
const advertiseForm = reactive({
  id: null, name: '', type: 0, pic: '', url: '', startTime: '', endTime: '', sort: 0, status: 1
})
const brandForm = reactive({ brandId: null, sort: 0 })
const homeProductForm = reactive({ productId: null, sort: 0 })

async function loadAds() {
  const data = await http.get('/admin/home/advertise/list', { params: { pageNum: 1, pageSize: 100 } })
  ads.value = data.list
}

async function loadBrands() {
  const data = await http.get('/admin/home/brand/list', { params: { pageNum: 1, pageSize: 100 } })
  homeBrands.value = data.list
}

async function loadNew() {
  const data = await http.get('/admin/home/newProduct/list', { params: { pageNum: 1, pageSize: 100 } })
  newProducts.value = data.list
}

async function loadRecommend() {
  const data = await http.get('/admin/home/recommendProduct/list', { params: { pageNum: 1, pageSize: 100 } })
  recommendProducts.value = data.list
}

function loadCurrent() {
  if (tab.value === 'advertise') loadAds()
  if (tab.value === 'brand') loadBrands()
  if (tab.value === 'newProduct') loadNew()
  if (tab.value === 'recommendProduct') loadRecommend()
}

function openAdvertise(row) {
  Object.assign(advertiseForm, {
    id: null, name: '', type: 0, pic: '', url: '', startTime: '', endTime: '', sort: 0, status: 1
  })
  if (row) Object.assign(advertiseForm, row)
  advertiseDialog.value = true
}

async function uploadAd(options) {
  const fd = new FormData()
  fd.append('file', options.file)
  advertiseForm.pic = await http.post('/admin/oss/upload', fd)
  options.onSuccess?.({})
}

async function saveAdvertise() {
  if (!advertiseForm.name || !advertiseForm.pic) {
    ElMessage.warning('请填写名称并上传图片')
    return
  }
  await http.post(advertiseForm.id ? '/admin/home/advertise/update' : '/admin/home/advertise/create', advertiseForm)
  ElMessage.success('保存成功')
  advertiseDialog.value = false
  loadAds()
}

async function toggleAdvertise(row, value) {
  await http.post('/admin/home/advertise/update', { ...row, status: value ? 1 : 0 })
  loadAds()
}

function openBrand() {
  Object.assign(brandForm, { brandId: null, sort: 0 })
  brandDialog.value = true
}

async function saveBrand() {
  if (!brandForm.brandId) {
    ElMessage.warning('请选择品牌')
    return
  }
  await http.post('/admin/home/brand/create', brandForm)
  ElMessage.success('添加成功')
  brandDialog.value = false
  loadBrands()
}

function openHomeProduct(mode) {
  homeProductMode.value = mode
  Object.assign(homeProductForm, { productId: null, sort: 0 })
  productDialog.value = true
}

async function saveHomeProduct() {
  if (!homeProductForm.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  const url = homeProductMode.value === 'new' ? '/admin/home/newProduct/create' : '/admin/home/recommendProduct/create'
  await http.post(url, homeProductForm)
  ElMessage.success('添加成功')
  productDialog.value = false
  loadCurrent()
}

async function removeRow(url, ids) {
  await ElMessageBox.confirm('确定移除该推荐吗？', '提示', { type: 'warning' })
  await http.post(url, ids)
  ElMessage.success('已移除')
  loadCurrent()
}

onMounted(async () => {
  try {
    brands.value = await http.get('/admin/brand/all')
    products.value = await http.get('/admin/product/simpleList')
    await Promise.all([loadAds(), loadBrands(), loadNew(), loadRecommend()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
.thumb {
  width: 70px;
  height: 44px;
  object-fit: cover;
  border-radius: 6px;
}
.upload-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.preview {
  width: 120px;
  height: 70px;
  object-fit: cover;
  border-radius: 8px;
  margin-top: 8px;
}
</style>
