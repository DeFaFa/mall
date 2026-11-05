<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>
      <div class="head">
        <span>{{ id ? '编辑商品' : '新增商品' }}</span>
        <div>
          <el-button @click="$router.push('/admin/products')">返回列表</el-button>
          <el-button type="danger" :loading="saving" @click="save">保存商品</el-button>
        </div>
      </div>
    </template>

    <el-tabs v-model="tab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="form" label-width="110px" class="form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品名称" required><el-input v-model="form.name" /></el-form-item>
              <el-form-item label="副标题"><el-input v-model="form.subTitle" /></el-form-item>
              <el-form-item label="货号"><el-input v-model="form.productSn" placeholder="留空自动生成" /></el-form-item>
              <el-form-item label="商品分类" required>
                <el-select v-model="form.productCategoryId" style="width: 100%" @change="loadAttributes">
                  <el-option v-for="c in categories" :key="c.id" :label="c.label" :value="c.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="品牌">
                <el-select v-model="form.brandId" clearable style="width: 100%">
                  <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="售价"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
              <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计量单位"><el-input v-model="form.unit" /></el-form-item>
              <el-form-item label="重量(kg)"><el-input-number v-model="form.weight" :min="0" :precision="2" style="width: 100%" /></el-form-item>
              <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
              <el-form-item label="预警库存"><el-input-number v-model="form.lowStock" :min="0" style="width: 100%" /></el-form-item>
              <el-form-item label="关键词"><el-input v-model="form.keywords" /></el-form-item>
              <el-form-item label="上架状态"><el-switch v-model="form.publishStatus" :active-value="1" :inactive-value="0" /></el-form-item>
              <el-form-item label="新品"><el-switch v-model="form.newStatus" :active-value="1" :inactive-value="0" /></el-form-item>
              <el-form-item label="推荐"><el-switch v-model="form.recommandStatus" :active-value="1" :inactive-value="0" /></el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="主图">
            <div class="upload-row">
              <el-input v-model="form.pic" placeholder="图片 URL" />
              <el-upload :show-file-list="false" :http-request="(opt) => upload(opt, 'pic')">
                <el-button>上传图片</el-button>
              </el-upload>
            </div>
            <img v-if="form.pic" :src="form.pic" class="preview" />
          </el-form-item>
          <el-form-item label="图片集">
            <el-input v-model="form.albumPics" type="textarea" :rows="2" placeholder="多张图片 URL，逗号分隔" />
          </el-form-item>
          <el-form-item label="商品简介">
            <el-input v-model="form.description" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="详情标题"><el-input v-model="form.detailTitle" /></el-form-item>
          <el-form-item label="详情描述">
            <el-input v-model="form.detailDesc" type="textarea" :rows="5" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="SKU 规格" name="sku">
        <div class="tab-toolbar">
          <span class="muted">SKU 编码由系统按「日期 + 商品ID + 序号」自动生成</span>
          <el-button size="small" type="primary" @click="addSku">新增 SKU</el-button>
        </div>
        <el-table :data="form.skuStockList" size="small">
          <el-table-column label="SKU 编码" width="150">
            <template #default="{ row }">{{ row.skuCode || '保存后生成' }}</template>
          </el-table-column>
          <el-table-column label="规格 JSON" min-width="240">
            <template #default="{ row }">
              <el-input v-model="row.spData" size="small" placeholder='{"颜色":"雅川青","存储容量":"256G"}' />
            </template>
          </el-table-column>
          <el-table-column label="价格" width="130">
            <template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" /></template>
          </el-table-column>
          <el-table-column label="库存" width="110">
            <template #default="{ row }"><el-input-number v-model="row.stock" :min="0" size="small" /></template>
          </el-table-column>
          <el-table-column label="预警" width="100">
            <template #default="{ row }"><el-input-number v-model="row.lowStock" :min="0" size="small" /></template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button text type="danger" @click="form.skuStockList.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="商品属性" name="attr">
        <div class="tab-toolbar">
          <span class="muted">用于详情页展示的商品参数</span>
          <el-button size="small" type="primary" @click="addAttr">新增属性值</el-button>
        </div>
        <el-table :data="form.productAttributeValueList" size="small">
          <el-table-column label="属性" min-width="220">
            <template #default="{ row }">
              <el-select v-model="row.productAttributeId" filterable style="width: 100%">
                <el-option v-for="a in attributes" :key="a.id" :label="`${a.name}（${a.type === 0 ? '规格' : '参数'}）`" :value="a.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="属性值" min-width="260">
            <template #default="{ row }"><el-input v-model="row.value" /></template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button text type="danger" @click="form.productAttributeValueList.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="价格规则" name="price">
        <div class="rule-section">
          <div class="rule-head">
            <strong>会员价</strong>
            <el-button size="small" @click="form.memberPriceList.push({ memberLevelId: levels[1]?.id, memberPrice: 0 })">新增</el-button>
          </div>
          <el-table :data="form.memberPriceList" size="small">
            <el-table-column label="会员等级" min-width="160">
              <template #default="{ row }">
                <el-select v-model="row.memberLevelId" style="width: 100%">
                  <el-option v-for="l in levels" :key="l.id" :label="l.name" :value="l.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="会员价" width="160">
              <template #default="{ row }"><el-input-number v-model="row.memberPrice" :min="0" :precision="2" size="small" /></template>
            </el-table-column>
            <el-table-column width="70">
              <template #default="{ $index }"><el-button text type="danger" @click="form.memberPriceList.splice($index, 1)">删除</el-button></template>
            </el-table-column>
          </el-table>
        </div>
        <div class="rule-section">
          <div class="rule-head">
            <strong>阶梯价</strong>
            <el-button size="small" @click="form.productLadderList.push({ memberLevelId: 0, count: 2, price: 0 })">新增</el-button>
          </div>
          <el-table :data="form.productLadderList" size="small">
            <el-table-column label="会员等级(0=全部)" width="160">
              <template #default="{ row }">
                <el-select v-model="row.memberLevelId" style="width: 100%">
                  <el-option label="全部会员" :value="0" />
                  <el-option v-for="l in levels" :key="l.id" :label="l.name" :value="l.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="满件数" width="140">
              <template #default="{ row }"><el-input-number v-model="row.count" :min="1" size="small" /></template>
            </el-table-column>
            <el-table-column label="单价" width="150">
              <template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" /></template>
            </el-table-column>
            <el-table-column width="70">
              <template #default="{ $index }"><el-button text type="danger" @click="form.productLadderList.splice($index, 1)">删除</el-button></template>
            </el-table-column>
          </el-table>
        </div>
        <div class="rule-section">
          <div class="rule-head">
            <strong>满减规则</strong>
            <el-button size="small" @click="form.productFullReductionList.push({ fullPrice: 0, reducePrice: 0 })">新增</el-button>
          </div>
          <el-table :data="form.productFullReductionList" size="small">
            <el-table-column label="满金额" width="180">
              <template #default="{ row }"><el-input-number v-model="row.fullPrice" :min="0" :precision="2" size="small" /></template>
            </el-table-column>
            <el-table-column label="减金额" width="180">
              <template #default="{ row }"><el-input-number v-model="row.reducePrice" :min="0" :precision="2" size="small" /></template>
            </el-table-column>
            <el-table-column width="70">
              <template #default="{ $index }"><el-button text type="danger" @click="form.productFullReductionList.splice($index, 1)">删除</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="关联内容" name="relation">
        <el-form label-width="110px" class="form">
          <el-form-item label="关联专题">
            <el-select v-model="form.subjectIds" multiple style="width: 100%">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="优选专区">
            <el-select v-model="form.prefrenceAreaIds" multiple style="width: 100%">
              <el-option v-for="a in areas" :key="a.id" :label="a.name" :value="a.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../utils/request'

const id = useRoute().params.id
const router = useRouter()
const tab = ref('basic')
const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const brands = ref([])
const levels = ref([])
const attributes = ref([])
const subjects = ref([])
const areas = ref([])

const form = reactive({
  name: '', subTitle: '', productSn: '', productCategoryId: null, brandId: null,
  price: 0, originalPrice: 0, lowStock: 10, unit: '件', weight: 0, sort: 0,
  pic: '', albumPics: '', description: '', detailTitle: '', detailDesc: '', keywords: '',
  publishStatus: 1, newStatus: 1, recommandStatus: 0, verifyStatus: 1,
  skuStockList: [], productAttributeValueList: [], memberPriceList: [],
  productLadderList: [], productFullReductionList: [], subjectIds: [], prefrenceAreaIds: []
})

function flatten(tree, level = 0, result = []) {
  for (const c of tree) {
    result.push({ id: c.id, label: (level ? '　'.repeat(level) : '') + c.name })
    if (c.children?.length) flatten(c.children, level + 1, result)
  }
  return result
}

async function loadAttributes() {
  if (!form.productCategoryId) {
    attributes.value = []
    return
  }
  const cats = await http.get('/admin/productAttribute/category/list')
  const cat = cats.find((c) => c.productCategoryId === form.productCategoryId)
  attributes.value = cat ? await http.get('/admin/productAttribute/list', { params: { categoryId: cat.id } }) : []
}

function addSku() {
  form.skuStockList.push({ id: null, skuCode: '', spData: '', price: form.price, stock: 0, lowStock: 0 })
}

function addAttr() {
  form.productAttributeValueList.push({ productAttributeId: attributes.value[0]?.id, value: '' })
}

async function upload(options, field) {
  const fd = new FormData()
  fd.append('file', options.file)
  form[field] = await http.post('/admin/oss/upload', fd)
  options.onSuccess?.({})
}

async function load() {
  categories.value = flatten(await http.get('/admin/productCategory/tree'))
  brands.value = await http.get('/admin/brand/all')
  levels.value = await http.get('/admin/memberLevel/list')
  subjects.value = (await http.get('/admin/subject/list', { params: { pageNum: 1, pageSize: 100 } })).list
  areas.value = await http.get('/admin/prefrenceArea/list')
  if (!id) {
    addSku()
    return
  }
  loading.value = true
  try {
    const detail = await http.get(`/admin/product/${id}`)
    const p = detail.product
    for (const key of ['name', 'subTitle', 'productSn', 'productCategoryId', 'brandId', 'price', 'originalPrice',
      'lowStock', 'unit', 'weight', 'sort', 'pic', 'albumPics', 'description', 'detailTitle', 'detailDesc', 'keywords',
      'publishStatus', 'newStatus', 'recommandStatus', 'verifyStatus']) {
      form[key] = p[key]
    }
    form.skuStockList = detail.skuStockList || []
    form.productAttributeValueList = detail.attributeValueList || []
    form.memberPriceList = detail.memberPriceList || []
    form.productLadderList = detail.ladderList || []
    form.productFullReductionList = detail.fullReductionList || []
    form.subjectIds = detail.subjectIds || []
    form.prefrenceAreaIds = detail.prefrenceAreaIds || []
    await loadAttributes()
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.name || !form.productCategoryId) {
    ElMessage.warning('请填写商品名称并选择分类')
    return
  }
  if (!form.skuStockList.length) {
    ElMessage.warning('至少需要一个 SKU')
    return
  }
  saving.value = true
  try {
    if (id) {
      await http.post(`/admin/product/update/${id}`, form)
    } else {
      await http.post('/admin/product/create', form)
    }
    ElMessage.success('保存成功')
    router.push('/admin/products')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form {
  max-width: 900px;
}
.upload-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.preview {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  margin-top: 8px;
}
.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.muted {
  color: #9aa0aa;
  font-size: 12px;
}
.rule-section {
  margin-bottom: 22px;
}
.rule-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
</style>
