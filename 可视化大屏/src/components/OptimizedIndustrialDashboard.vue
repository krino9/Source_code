<template>
  <div class="tech-screen-container">
    <!-- 动态网格背景 -->
    <div class="grid-bg"></div>
    <div class="scanline"></div>
    
    <!-- 顶部状态栏 -->
    <header class="screen-header">
      <div class="flex items-center">
        <div class="header-glow"></div>
        <i class="fa fa-industry mr-2 text-cyan-400"></i>
        <h1 class="text-white">工业大数据智能监控平台</h1>
      </div>
      <div class="header-right">
        <!-- 系统状态指示灯 -->
        <div class="system-status flex items-center mr-4">
          <span class="status-dot online pulse"></span>
          <span class="ml-2 text-sm">系统正常运行中</span>
        </div>
        <!-- 实时通知 -->
        <div class="notification relative mr-4" @click="toggleNotification">
          <i class="fa fa-bell text-cyan-400"></i>
          <span class="notify-badge" v-if="unreadNotifyCount > 0">{{ unreadNotifyCount }}</span>
          <!-- 通知下拉框 -->
          <div class="notify-dropdown" v-show="showNotification">
            <div class="notify-header">
              <h4>系统通知</h4>
              <button class="mark-all" @click="markAllRead">全部已读</button>
            </div>
            <div class="notify-list">
              <div v-for="(notify, idx) in notifications" :key="idx" class="notify-item" :class="{ unread: !notify.read }">
                <i class="fa fa-exclamation-triangle text-yellow-400 mr-2"></i>
                <div class="notify-content">
                  <p class="notify-title">{{ notify.title }}</p>
                  <p class="notify-time">{{ notify.time }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <span class="time-info"><i class="fa fa-clock-o mr-1 text-cyan-400"></i>{{ currentTime }}</span>
        <button class="refresh-btn ml-4" @click="refreshData">
          <i class="fa fa-refresh text-cyan-400"></i> 刷新
        </button>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="screen-grid">
      <!-- 左侧区域：关键指标 -->
      <div class="col-left space-y-4">
        <!-- 设备总数 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>设备总数</h3>
            <span class="status-badge realtime">实时监控</span>
          </div>
          <div class="card-body">
            <div class="metric-value" v-count-to="totalDevices" :duration="1500">0</div>
            <div class="metric-trend">
              <span class="trend-up"><i class="fa fa-arrow-up mr-1"></i>48%</span>
            </div>
            <div class="chart-container" ref="deviceTrendChart"></div>
          </div>
        </div>

        <!-- 运行中设备 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>运行中设备</h3>
            <span class="status-badge normal">状态正常</span>
          </div>
          <div class="card-body">
            <div class="metric-value" v-count-to="runningDevices" :duration="1500">0</div>
            <div class="metric-trend">
              <span class="trend-up"><i class="fa fa-arrow-up mr-1"></i>5.1%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: runningDevicesPercentage + '%' }" 
                   v-animate-width="{ targetWidth: runningDevicesPercentage, duration: 1500 }"></div>
            </div>
            <div class="progress-text">{{ runningDevicesPercentage }}%</div>
          </div>
        </div>

        <!-- 告警设备 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300" 
             :class="{ 'alert-flash': alertDevices > 15 }">
          <div class="card-glow alert-glow"></div>
          <div class="card-header">
            <h3>告警设备</h3>
            <span class="status-badge warning">需要注意</span>
          </div>
          <div class="card-body">
            <div class="metric-value" v-count-to="alertDevices" :duration="1500">5</div>
            <div class="metric-trend">
              <span class="trend-down"><i class="fa fa-arrow-up mr-1"></i>12.3%</span>
            </div>
            <div class="alert-list">
              <div v-for="(alert, index) in recentAlerts" :key="index" class="alert-item" @click="showAlertDetail(alert)">
                <i class="fa fa-exclamation-circle text-yellow-400 mr-2"></i>
                <span class="truncate">{{ alert.device }}</span>
                <span class="alert-time">{{ alert.time }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 停机设备 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>停机设备</h3>
            <span class="status-badge abnormal">异常状态</span>
          </div>
          <div class="card-body">
            <div class="metric-value" v-count-to="stoppedDevices" :duration="1500">15</div>
            <div class="metric-trend">
              <span class="trend-up"><i class="fa fa-arrow-down mr-1"></i>3.7%</span>
            </div>
            <div class="stop-types">
              <div class="stop-type-item">
                <div class="stop-type-label">计划停机</div>
                <div class="stop-type-value">{{ plannedStops }}</div>
              </div>
              <div class="stop-type-item">
                <div class="stop-type-label">故障停机</div>
                <div class="stop-type-value danger">{{ unplannedStops }}</div>
              </div>
            </div>
            <div class="chart-container h-16 mt-2" ref="stopRatioChart"></div>
          </div>
        </div>
      </div>

      <!-- 中间区域：核心图表 -->
      <div class="col-middle space-y-4">
        <!-- 生产效率趋势 -->
        <div class="tech-card full-height hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>生产效率趋势分析</h3>
            <div class="time-filter">
              <button class="filter-btn active" @click="changeTimeRange('day')">日</button>
              <button class="filter-btn" @click="changeTimeRange('week')">周</button>
              <button class="filter-btn" @click="changeTimeRange('month')">月</button>
              <button class="filter-btn" @click="changeTimeRange('year')">年</button>
            </div>
          </div>
          <div class="card-body">
            <div class="chart-container full-chart chart-glow-container" ref="productionEfficiencyChart" @click="showEfficiencyDetail"></div>
            <!-- 数据钻取提示 -->
            <div class="drill-tip text-xs text-gray-400 mt-1">
              <i class="fa fa-info-circle mr-1"></i>点击图表查看时段详情
            </div>
          </div>
        </div>

        <!-- 设备分布与能耗分析 -->
        <div class="tech-card full-height hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>设备分布与能耗分析</h3>
            <div class="filter-group">
              <button class="filter-btn" @click="toggleDeviceFilter">
                <i class="fa fa-filter mr-1"></i>设备状态
              </button>
              <button class="filter-btn ml-2" @click="exportEnergyData">
                <i class="fa fa-download mr-1"></i>导出
              </button>
            </div>
          </div>
          <div class="card-body chart-grid">
            <div class="chart-item chart-glow-container" ref="equipmentDistributionChart"></div>
            <div class="chart-item chart-glow-container" ref="energyConsumptionChart"></div>
          </div>
        </div>
      </div>

      <!-- 右侧区域：详细信息 -->
      <div class="col-right space-y-4">
        <!-- 产量统计 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>产量统计</h3>
            <i class="fa fa-bar-chart text-cyan-400"></i>
          </div>
          <div class="card-body">
            <div class="chart-container chart-glow-container" ref="productionChart"></div>
            <div class="production-stats">
              <div class="stat-item">
                <div class="stat-label">今日产量</div>
                <div class="stat-value" v-count-to="productionData.todayProduction" :duration="1500">0</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">目标产量</div>
                <div class="stat-value">{{ productionData.targetProduction }}</div>
              </div>
            </div>
            <!-- 达成率进度 -->
            <div class="progress-bar mt-2">
              <div class="progress-fill bg-cyan-500" :style="{ width: productionRate + '%' }"></div>
            </div>
            <div class="progress-text text-xs mt-1">
              达成率：<span class="text-cyan-400">{{ productionRate }}%</span>
            </div>
          </div>
        </div>

        <!-- 质量监控 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>质量监控</h3>
            <i class="fa fa-check-square-o text-cyan-400"></i>
          </div>
          <div class="card-body">
            <div class="chart-container chart-glow-container" ref="qualityControlChart"></div>
            <div class="quality-stats">
              <div class="stat-item">
                <div class="stat-label">合格率</div>
                <div class="stat-value good">{{ qualityData.passRate }}%</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">不良率</div>
                <div class="stat-value danger">{{ qualityData.rejectRate }}%</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">CPK</div>
                <div class="stat-value" :class="{ good: qualityData.cpkValue >= 1.33, danger: qualityData.cpkValue < 1.33 }">
                  {{ qualityData.cpkValue }}
                </div>
              </div>
            </div>
            <!-- 不良原因提示 -->
            <div class="defect-tip text-xs mt-2" @click="showDefectDetail">
              <i class="fa fa-exclamation-circle text-yellow-400 mr-1"></i>
              查看不良原因分析 <i class="fa fa-angle-right ml-1"></i>
            </div>
          </div>
        </div>

        <!-- 设备健康度 -->
        <div class="tech-card hover:border-cyan-400 transition-all duration-300">
          <div class="card-glow"></div>
          <div class="card-header">
            <h3>设备健康度</h3>
            <i class="fa fa-heartbeat text-cyan-400"></i>
          </div>
          <div class="card-body">
            <div class="health-list">
              <div v-for="(device, index) in deviceHealthList" :key="index" class="health-item" @click="showDeviceHealthDetail(device)">
                <div class="health-info">
                  <span class="device-name truncate">{{ device.name }}</span>
                  <span :class="['health-status', device.healthClass]">{{ device.healthStatus }}</span>
                </div>
                <div class="health-bar">
                  <div :class="['health-fill', device.progressClass]" :style="{ width: device.healthScore + '%' }"></div>
                </div>
                <div class="health-extra flex justify-between text-xs mt-1">
                  <span class="health-score">{{ device.healthScore }}/100</span>
                  <span class="maintain-tip" v-if="device.healthScore < 80">
                    <i class="fa fa-wrench text-yellow-400 mr-1"></i>需维护
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 弹窗组件 -->
    <!-- 告警详情弹窗 -->
    <div class="modal" v-show="showAlertModal">
      <div class="modal-content">
        <div class="modal-glow"></div>
        <div class="modal-header">
          <h3>告警详情</h3>
          <button class="close-btn" @click="showAlertModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-item">
            <label>设备名称：</label>
            <span>{{ currentAlert.device }}</span>
          </div>
          <div class="detail-item">
            <label>告警时间：</label>
            <span>{{ currentAlert.time }}</span>
          </div>
          <div class="detail-item">
            <label>告警类型：</label>
            <span>{{ currentAlert.type || '温度异常' }}</span>
          </div>
          <div class="detail-item">
            <label>当前数值：</label>
            <span>{{ currentAlert.value || '85℃' }}</span>
          </div>
          <div class="detail-item">
            <label>处理建议：</label>
            <span>{{ currentAlert.suggestion || '检查设备散热系统，降低负载' }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel-btn" @click="showAlertModal = false">关闭</button>
          <button class="btn confirm-btn" @click="handleAlert()">标记已处理</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, onUnmounted, computed, getCurrentInstance, nextTick } from 'vue';
import * as echarts from 'echarts';
import axios from 'axios';

// 时间相关
const currentTime = ref('');
const lastUpdateTime = ref('');
const isLoading = ref(false);

// 图表容器与实例
const deviceTrendChart = ref(null);
const stopRatioChart = ref(null);
const productionEfficiencyChart = ref(null);
const equipmentDistributionChart = ref(null);
const energyConsumptionChart = ref(null);
const productionChart = ref(null);
const qualityControlChart = ref(null);
const chartInstances = ref({});

// 交互状态
const showNotification = ref(false);
const unreadNotifyCount = ref(3);
const notifications = ref([
  { title: '冲压机#A302温度异常', time: '10:24', read: false },
  { title: '包装机#C007维护到期', time: '09:15', read: false },
  { title: '今日产量达成85%', time: '08:30', read: true }
]);
const showAlertModal = ref(false);
const currentAlert = ref({});
const timeRange = ref('day'); // 时间范围：day/week/month/year

// 从JSON读取的数据
const productionEfficiencyData = ref({});
const equipmentEnergyData = ref({});
const productionData = ref({
  todayProduction: 0,
  targetProduction: 0,
  xData: [],
  productionData: []
});
const qualityData = ref({
  passRate: 0,
  rejectRate: 0,
  cpkValue: 0,
  xData: [],
  passRateData: [],
  cpkData: []
});

// 计算属性：产量达成率
const productionRate = computed(() => {
  if (!productionData.value.todayProduction || !productionData.value.targetProduction) return 0;
  return Math.round((productionData.value.todayProduction / productionData.value.targetProduction) * 100);
});

// 设备指标数据
const totalDevices = ref(248);
const runningDevices = ref(215);
const runningDevicesPercentage = ref(86.7);
const alertDevices = ref(18);
const stoppedDevices = ref(15);
const plannedStops = ref(7);
const unplannedStops = ref(8);

// 最近告警
const recentAlerts = ref([
  { device: '冲压机#A302', time: '10:24', type: '温度异常', value: '85℃' },
  { device: '传送带#B105', time: '09:15', type: '速度偏差', value: '12m/min' },
  { device: '包装机#C007', time: '08:42', type: '压力不足', value: '0.6MPa' },
  { device: '切割机#D211', time: '07:58', type: '电流过高', value: '18A' }
]);

// 设备健康度数据
const deviceHealthList = ref([
  { name: '数控机床#101', healthScore: 92, healthStatus: '优秀', healthClass: 'status-excellent', progressClass: 'progress-excellent' },
  { name: '机器人手臂#203', healthScore: 87, healthStatus: '良好', healthClass: 'status-good', progressClass: 'progress-good' },
  { name: '冲压机#A302', healthScore: 76, healthStatus: '一般', healthClass: 'status-average', progressClass: 'progress-average' },
  { name: '焊接机#B405', healthScore: 65, healthStatus: '较差', healthClass: 'status-poor', progressClass: 'progress-poor' },
  { name: '包装机#C007', healthScore: 58, healthStatus: '差', healthClass: 'status-bad', progressClass: 'progress-bad' }
]);

// 请求JSON数据
const fetchProductionEfficiencyData = async () => {
  try {
    // 实际项目中替换为真实API地址
    const res = await axios.get('/mock/productionEfficiency.json').catch(() => ({
      data: {
        "day": {
          "xData": ["00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"],
          "realData": [65, 68, 72, 89, 92, 87, 85, 88],
          "targetData": [80, 80, 80, 80, 80, 80, 80, 80]
        },
        "week": {
          "xData": ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
          "realData": [78, 82, 85, 88, 90, 86, 83],
          "targetData": [80, 80, 80, 80, 80, 80, 80]
        }
      }
    }));
    productionEfficiencyData.value = res.data;
  } catch (err) {
    console.error('请求生产效率数据失败：', err);
  }
};

const fetchEquipmentEnergyData = async () => {
  try {
    // 实际项目中替换为真实API地址
    const res = await axios.get('/mock/equipmentEnergy.json').catch(() => ({
      data: {
        "equipmentDistribution": [
          { "value": 35, "name": "加工设备" },
          { "value": 25, "name": "装配设备" },
          { "value": 15, "name": "输送设备" },
          { "value": 12, "name": "检测设备" },
          { "value": 13, "name": "包装设备" }
        ],
        "energyConsumption": {
          "xData": ["电力", "燃气", "水", "压缩空气"],
          "todayData": [680, 240, 95, 130],
          "yesterdayData": [720, 260, 105, 145]
        }
      }
    }));
    equipmentEnergyData.value = res.data;
  } catch (err) {
    console.error('请求设备能耗数据失败：', err);
  }
};

const fetchProductionData = async () => {
  try {
    // 实际项目中替换为真实API地址
    const res = await axios.get('/mock/production.json').catch(() => ({
      data: {
        "xData": ["8:00", "10:00", "12:00", "14:00", "16:00", "18:00"],
        "productionData": [1850, 2100, 2450, 2200, 2350, 1636],
        "todayProduction": 12586,
        "targetProduction": 15000
      }
    }));
    productionData.value = res.data;
  } catch (err) {
    console.error('请求产量数据失败：', err);
  }
};

const fetchQualityData = async () => {
  try {
    // 实际项目中替换为真实API地址
    const res = await axios.get('/mock/quality.json').catch(() => ({
      data: {
        "xData": ["批次1", "批次2", "批次3", "批次4", "批次5", "批次6"],
        "passRateData": [98.2, 98.5, 97.9, 99.1, 98.8, 98.7],
        "cpkData": [1.52, 1.60, 1.58, 1.72, 1.70, 1.68],
        "passRate": 98.7,
        "rejectRate": 1.3,
        "cpkValue": 1.68
      }
    }));
    qualityData.value = res.data;
  } catch (err) {
    console.error('请求质量数据失败：', err);
  }
};

// 批量请求所有数据
const fetchAllJsonData = async () => {
  isLoading.value = true;
  await Promise.all([
    fetchProductionEfficiencyData(),
    fetchEquipmentEnergyData(),
    fetchProductionData(),
    fetchQualityData()
  ]);
  isLoading.value = false;
};

// 时间更新
const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  if (now.getMinutes() % 5 === 0) {
    lastUpdateTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
};

// 自定义指令：数值滚动动画
const vCountTo = {
  mounted(el, binding) {
    const target = binding.value;
    const duration = binding.arg || 1000;
    let start = 0;
    const step = target / (duration / 16);
    const timer = setInterval(() => {
      start += step;
      if (start >= target) {
        clearInterval(timer);
        start = target;
      }
      el.textContent = Math.floor(start);
    }, 16);
  }
};

// 自定义指令：宽度动画
const vAnimateWidth = {
  mounted(el, binding) {
    // 修复：确保获取到正确的参数
    const { targetWidth, duration = 1000 } = binding.value || {};
    if (targetWidth === undefined) return;
    
    el.style.transition = `width ${duration}ms ease`;
    setTimeout(() => {
      el.style.width = `${targetWidth}%`;
    }, 100);
  }
};

// 图表初始化
const initCharts = () => {
  nextTick(() => {
    // 设备趋势小图表
    if (deviceTrendChart.value) {
      chartInstances.value.deviceTrend = echarts.init(deviceTrendChart.value);
      chartInstances.value.deviceTrend.setOption({
        tooltip: { trigger: 'none' },
        grid: { left: 0, right: 0, top: 0, bottom: 0 },
        xAxis: { type: 'category', data: Array(10).fill(''), show: false },
        yAxis: { type: 'value', show: false },
        series: [{
          data: [238, 240, 242, 241, 243, 245, 244, 246, 247, 248],
          type: 'line', smooth: true,
          lineStyle: { width: 2, color: '#06b6d4' },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(6, 182, 212, 0.3)' },
            { offset: 1, color: 'rgba(6, 182, 212, 0)' }
          ]) },
          symbol: 'none'
        }]
      });
    }

    // 停机占比小图表
    if (stopRatioChart.value) {
      chartInstances.value.stopRatio = echarts.init(stopRatioChart.value);
      chartInstances.value.stopRatio.setOption({
        tooltip: { trigger: 'item' },
        grid: { left: 0, right: 0, top: 0, bottom: 0 },
        series: [{
          type: 'pie',
          radius: ['60%', '80%'],
          center: ['50%', '50%'],
          data: [
            { value: plannedStops.value, name: '计划停机' },
            { value: unplannedStops.value, name: '故障停机' }
          ],
          itemStyle: {
            color: (params) => params.name === '计划停机' ? '#06b6d4' : '#ef4444',
            borderRadius: 3
          },
          label: { show: false },
          labelLine: { show: false }
        }]
      });
    }

    // 生产效率趋势
    if (productionEfficiencyChart.value && productionEfficiencyData.value[timeRange.value]) {
      const { xData, realData, targetData } = productionEfficiencyData.value[timeRange.value];
      chartInstances.value.productionEfficiency = echarts.init(productionEfficiencyChart.value);
      chartInstances.value.productionEfficiency.setOption({
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(10, 30, 60, 0.9)', borderColor: 'rgba(6, 182, 212, 0.5)' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { 
          type: 'category', 
          data: xData,
          axisLine: { color: 'rgba(6, 182, 212, 0.3)' },
          axisLabel: { color: '#b0c4de' }
        },
        yAxis: { 
          type: 'value',
          axisLine: { color: 'rgba(6, 182, 212, 0.3)' },
          axisLabel: { color: '#b0c4de' },
          splitLine: { color: 'rgba(6, 182, 212, 0.1)' }
        },
        series: [
          { 
            name: '实际效率', 
            data: realData,
            type: 'line',
            smooth: true,
            lineStyle: { width: 2, color: '#06b6d4' },
            areaStyle: { 
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(6, 182, 212, 0.3)' },
                { offset: 1, color: 'rgba(6, 182, 212, 0)' }
              ]) 
            },
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: { 
              color: '#06b6d4',
              shadowBlur: 10,
              shadowColor: 'rgba(6, 182, 212, 0.7)'
            }
          },
          { 
            name: '目标效率', 
            data: targetData,
            type: 'line',
            lineStyle: { 
              width: 1.5, 
              color: 'rgba(100, 116, 139, 0.6)',
              type: 'dashed'
            },
            symbol: 'none'
          }
        ]
      });
    }

    // 设备分布饼图
    if (equipmentDistributionChart.value && equipmentEnergyData.value.equipmentDistribution) {
      chartInstances.value.equipmentDistribution = echarts.init(equipmentDistributionChart.value);
      chartInstances.value.equipmentDistribution.setOption({
        tooltip: { trigger: 'item', backgroundColor: 'rgba(10, 30, 60, 0.9)', borderColor: 'rgba(6, 182, 212, 0.5)' },
        legend: { orient: 'vertical', bottom: 0, left: 'center', textStyle: { color: '#b0c4de', fontSize: 11 } },
        series: [{
          name: '设备类型', type: 'pie', radius: ['70%', '90%'], center: ['50%', '40%'],
          data: equipmentEnergyData.value.equipmentDistribution,
          itemStyle: { borderRadius: 4, borderColor: 'rgba(10, 30, 60, 0.8)', borderWidth: 2 },
          label: { show: false },
          emphasis: { 
            itemStyle: { 
              shadowBlur: 15, 
              shadowColor: 'rgba(6, 182, 212, 0.5)',
              borderColor: 'rgba(6, 182, 212, 0.8)'
            } 
          }
        }]
      });
    }

    // 能耗分析
    if (energyConsumptionChart.value && equipmentEnergyData.value.energyConsumption) {
      const { xData, todayData, yesterdayData } = equipmentEnergyData.value.energyConsumption;
      chartInstances.value.energyConsumption = echarts.init(energyConsumptionChart.value);
      chartInstances.value.energyConsumption.setOption({
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(10, 30, 60, 0.9)', borderColor: 'rgba(6, 182, 212, 0.5)' },
        legend: { data: ['今日能耗', '昨日能耗'], top: 0, textStyle: { color: '#b0c4de', fontSize: 11 } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: xData, axisLine: { color: 'rgba(6, 182, 212, 0.3)' }, axisLabel: { color: '#b0c4de' } },
        yAxis: { axisLine: { color: 'rgba(6, 182, 212, 0.3)' }, axisLabel: { color: '#b0c4de' }, splitLine: { color: 'rgba(6, 182, 212, 0.1)' } },
        series: [
          { name: '今日能耗', type: 'bar', data: todayData, itemStyle: { color: '#06b6d4', borderRadius: 4 }, barWidth: '30%' },
          { name: '昨日能耗', type: 'bar', data: yesterdayData, itemStyle: { color: 'rgba(100, 116, 139, 0.6)', borderRadius: 4 }, barWidth: '30%' }
        ]
      });
    }

    // 产量图表
    if (productionChart.value && productionData.value.xData) {
      chartInstances.value.production = echarts.init(productionChart.value);
      chartInstances.value.production.setOption({
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: productionData.value.xData, axisLine: { color: 'rgba(6, 182, 212, 0.3)' }, axisLabel: { color: '#b0c4de' } },
        yAxis: { axisLine: { color: 'rgba(6, 182, 212, 0.3)' }, axisLabel: { color: '#b0c4de' }, splitLine: { color: 'rgba(6, 182, 212, 0.1)' } },
        series: [{
          data: productionData.value.productionData,
          type: 'bar',
          itemStyle: { 
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#06b6d4' },
              { offset: 1, color: '#3b82f6' }
            ]), 
            borderRadius: 4 
          },
          barWidth: '60%',
          emphasis: { 
            itemStyle: { 
              shadowBlur: 10, 
              shadowColor: 'rgba(6, 182, 212, 0.5)' 
            } 
          }
        }]
      });
    }

    // 质量监控
    if (qualityControlChart.value && qualityData.value.xData) {
      chartInstances.value.qualityControl = echarts.init(qualityControlChart.value);
      chartInstances.value.qualityControl.setOption({
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(10, 30, 60, 0.9)', borderColor: 'rgba(6, 182, 212, 0.5)' },
        grid: { left: '3%', right: '15%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: qualityData.value.xData, axisLine: { color: 'rgba(6, 182, 212, 0.3)' }, axisLabel: { color: '#b0c4de' } },
        yAxis: [
          {
            type: 'value', name: '合格率(%)', nameTextStyle: { color: '#06b6d4' },
            axisLine: { color: 'rgba(6, 182, 212, 0.5)' }, axisLabel: { color: '#06b6d4', formatter: '{value}%' },
            splitLine: { color: 'rgba(6, 182, 212, 0.1)' }, min: 97, max: 100
          },
          {
            type: 'value', name: 'CPK', nameLocation: 'end', nameTextStyle: { color: '#3b82f6' },
            axisLine: { color: 'rgba(59, 130, 246, 0.5)' }, axisLabel: { color: '#3b82f6' },
            splitLine: { show: false }, min: 1.4, max: 1.8,
            splitLine: { show: true, lineStyle: { color: 'rgba(239, 68, 68, 0.2)', type: 'dashed' }, interval: 1.33 }
          }
        ],
        series: [
          {
            name: '合格率', type: 'line', data: qualityData.value.passRateData,
            smooth: true, lineStyle: { width: 2, color: '#06b6d4' },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(6, 182, 212, 0.3)' },
              { offset: 1, color: 'rgba(6, 182, 212, 0)' }
            ]) },
            symbol: 'circle', symbolSize: 6, itemStyle: { 
              color: '#06b6d4',
              shadowBlur: 10,
              shadowColor: 'rgba(6, 182, 212, 0.7)'
            }
          },
          {
            name: 'CPK', type: 'line', data: qualityData.value.cpkData,
            smooth: true, lineStyle: { width: 2, color: '#3b82f6' },
            symbol: 'circle', symbolSize: 6, itemStyle: { 
              color: '#3b82f6',
              shadowBlur: 10,
              shadowColor: 'rgba(59, 130, 246, 0.7)'
            },
            yAxisIndex: 1
          },
          {
            name: 'CPK标准线', type: 'line', data: [1.33, 1.33, 1.33, 1.33, 1.33, 1.33],
            lineStyle: { color: '#ef4444', type: 'dashed', width: 1.5 },
            symbol: 'none', yAxisIndex: 1
          }
        ]
      });
    }

    // 窗口resize适配
    window.addEventListener('resize', handleResize);
  });
};

// 更新生产效率图表
const updateProductionEfficiencyChart = (range) => {
  if (!chartInstances.value.productionEfficiency || !productionEfficiencyData.value[range]) return;
  const { xData, realData, targetData } = productionEfficiencyData.value[range];
  chartInstances.value.productionEfficiency.setOption({
    xAxis: { data: xData },
    series: [
      { name: '实际效率', data: realData },
      { name: '目标效率', data: targetData }
    ]
  });
};

// 窗口resize处理
const handleResize = () => {
  Object.values(chartInstances.value).forEach(instance => instance?.resize());
};

// 通知面板切换
const toggleNotification = () => {
  showNotification.value = !showNotification.value;
};

// 标记所有通知已读
const markAllRead = () => {
  notifications.value = notifications.value.map(notify => ({ ...notify, read: true }));
  unreadNotifyCount.value = 0;
};

// 时间范围切换
const changeTimeRange = (range) => {
  timeRange.value = range;
  updateProductionEfficiencyChart(range);
  document.querySelectorAll('.time-filter .filter-btn').forEach(btn => {
    btn.classList.remove('active');
    if (btn.textContent === (range === 'day' ? '日' : range === 'week' ? '周' : range === 'month' ? '月' : '年')) {
      btn.classList.add('active');
    }
  });
};

// 告警详情弹窗
const showAlertDetail = (alert) => {
  currentAlert.value = alert;
  showAlertModal.value = true;
};

// 处理告警
const handleAlert = () => {
  currentAlert.value.read = true;
  unreadNotifyCount.value = Math.max(0, unreadNotifyCount.value - 1);
  showAlertModal.value = false;
  alert('告警已标记为已处理');
};

// 刷新数据
const refreshData = async () => {
  isLoading.value = true;
  await fetchAllJsonData();
  initCharts();
  lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  isLoading.value = false;
  alert('数据已刷新');
};

// 未实现的功能占位
const showEfficiencyDetail = () => {
  alert('生产效率详情功能待实现');
};

const toggleDeviceFilter = () => {
  alert('设备状态筛选功能待实现');
};

const exportEnergyData = () => {
  alert('能耗数据导出功能待实现');
};

const showDefectDetail = () => {
  alert('不良原因分析功能待实现');
};

const showDeviceHealthDetail = (device) => {
  alert(`设备${device.name}健康详情功能待实现`);
};

// 生命周期与清理
onMounted(async () => {
  updateTime();
  setInterval(updateTime, 1000);
  await fetchAllJsonData();
  initCharts();
  simulateDataUpdates();
  
  // 注册自定义指令
  const instance = getCurrentInstance();
  if (instance) {
    const app = instance.appContext.app;
    app.directive('count-to', vCountTo);
    app.directive('animate-width', vAnimateWidth);
  }
});

onUnmounted(() => {
  Object.values(chartInstances.value).forEach(instance => instance?.dispose());
  window.removeEventListener('resize', handleResize);
});

// 模拟数据更新
const simulateDataUpdates = () => {
  const updateInterval = setInterval(() => {
    // 1. 设备状态数据
    const runningChange = Math.random() > 0.7 ? (Math.random() > 0.5 ? 1 : -1) : 0;
    runningDevices.value = Math.max(180, Math.min(240, runningDevices.value + runningChange));
    runningDevicesPercentage.value = ((runningDevices.value / totalDevices.value) * 100).toFixed(1);

    // 2. 告警设备
    if (Math.random() > 0.85) {
      const alertChange = Math.random() > 0.5 ? 1 : -1;
      alertDevices.value = Math.max(0, Math.min(30, alertDevices.value + alertChange));
      
      if (alertChange > 0 && unreadNotifyCount.value < 10) {
        const alertDevicesList = ["冲压机", "传送带", "包装机", "切割机", "数控机床"];
        const alertTypes = ["温度异常", "速度偏差", "压力不足", "电流过高", "振动超标"];
        const randomDevice = alertDevicesList[Math.floor(Math.random() * alertDevicesList.length)];
        const randomType = alertTypes[Math.floor(Math.random() * alertTypes.length)];
        const randomNum = Math.floor(Math.random() * 20) + 1;
        const currentTime = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        
        recentAlerts.value.unshift({
          device: `${randomDevice}#${randomNum}`,
          time: currentTime,
          type: randomType,
          value: randomType === "温度异常" ? `${Math.floor(Math.random() * 20) + 70}℃` : 
                 randomType === "速度偏差" ? `${Math.floor(Math.random() * 5) + 10}m/min` :
                 randomType === "压力不足" ? `${(Math.random() * 0.3 + 0.4).toFixed(1)}MPa` :
                 `${Math.floor(Math.random() * 5) + 15}A`
        });
        
        if (recentAlerts.value.length > 5) recentAlerts.value.pop();
        unreadNotifyCount.value += 1;
      }
    }

    // 3. 产量数据
    if (productionData.value.todayProduction) {
      const productionIncrease = Math.floor(Math.random() * 10) + 5;
      productionData.value.todayProduction = Math.min(productionData.value.targetProduction, productionData.value.todayProduction + productionIncrease);
      
      // 更新产量图表
      if (chartInstances.value.production) {
        const lastData = chartInstances.value.production.getOption().series[0].data;
        const newData = [...lastData.slice(1), lastData[lastData.length - 1] + productionIncrease];
        chartInstances.value.production.setOption({ series: [{ data: newData }] });
      }
    }

    // 4. 质量指标
    if (qualityData.value.passRate) {
      qualityData.value.passRate = Math.max(97.5, Math.min(99.5, parseFloat(qualityData.value.passRate) + (Math.random() > 0.5 ? 0.1 : -0.1))).toFixed(1);
      qualityData.value.rejectRate = (100 - parseFloat(qualityData.value.passRate)).toFixed(1);
      qualityData.value.cpkValue = Math.max(1.3, Math.min(1.8, parseFloat(qualityData.value.cpkValue) + (Math.random() > 0.5 ? 0.01 : -0.01))).toFixed(2);
    }

    // 5. 停机占比更新
    if (Math.random() > 0.7 && chartInstances.value.stopRatio) {
      plannedStops.value = Math.max(0, Math.min(10, plannedStops.value + (Math.random() > 0.5 ? 1 : -1)));
      unplannedStops.value = Math.max(0, Math.min(10, unplannedStops.value + (Math.random() > 0.5 ? 1 : -1)));
      chartInstances.value.stopRatio.setOption({
        series: [{ data: [{ value: plannedStops.value, name: '计划停机' }, { value: unplannedStops.value, name: '故障停机' }] }]
      });
    }

  }, 3000);

  onUnmounted(() => {
    clearInterval(updateInterval);
  });
};
</script>

<style scoped>
/* 基础样式优化 */
.tech-screen-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #030712 0%, #0b162c 50%, #1e3a8a 100%);
  color: #e0e0e0;
  overflow: hidden;
  font-family: 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  position: relative;
}

/* 动态网格背景 - 增强科技感 */
.grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(6, 182, 212, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(6, 182, 212, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.8;
  z-index: 0;
  animation: gridMove 60s linear infinite;
}

/* 扫描线效果 */
.scanline {
  position: absolute;
  top: -100%;
  left: 0;
  width: 100%;
  height: 5px;
  background: rgba(6, 182, 212, 0.1);
  z-index: 1;
  animation: scan 10s linear infinite;
}

@keyframes scan {
  0% { top: -10%; }
  100% { top: 100%; }
}

@keyframes gridMove {
  0% { background-position: 0 0; }
  100% { background-position: 30px 30px; }
}

/* 顶部状态栏优化 */
.screen-header {
  background: linear-gradient(to right, #0f172a, #1e40af);
  padding: 0.75rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(6, 182, 212, 0.3);
  box-shadow: 0 2px 15px rgba(6, 182, 212, 0.1);
  position: relative;
  z-index: 10;
}

.header-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(6, 182, 212, 0) 0%, rgba(6, 182, 212, 0.1) 50%, rgba(6, 182, 212, 0) 100%);
  z-index: -1;
  animation: headerPulse 4s infinite alternate;
}

@keyframes headerPulse {
  from { opacity: 0.3; }
  to { opacity: 0.8; }
}

.header-right {
  display: flex;
  align-items: center;
}

/* 系统状态指示灯 */
.system-status {
  display: flex;
  align-items: center;
  color: #e0e0e0;
  font-size: 0.875rem;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.online {
  background-color: #06b6d4;
  box-shadow: 0 0 10px rgba(6, 182, 212, 0.7);
}

/* 脉冲动画 */
.pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); box-shadow: 0 0 10px rgba(6, 182, 212, 0.7); }
  50% { transform: scale(1.1); box-shadow: 0 0 20px rgba(6, 182, 212, 0.9); }
  100% { transform: scale(1); box-shadow: 0 0 10px rgba(6, 182, 212, 0.7); }
}

/* 通知组件 */
.notification {
  position: relative;
  cursor: pointer;
}

.notify-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #ef4444;
  color: white;
  font-size: 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 10px rgba(239, 68, 68, 0.7);
}

.notify-dropdown {
  position: absolute;
  top: 24px;
  right: 0;
  width: 300px;
  background-color: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(6, 182, 212, 0.3);
  border-radius: 8px;
  padding: 0.75rem;
  box-shadow: 0 4px 20px rgba(6, 182, 212, 0.15);
  z-index: 100;
}

.notify-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(6, 182, 212, 0.1);
}

.notify-header h4 {
  margin: 0;
  font-size: 0.875rem;
  color: #fff;
}

.mark-all {
  background: transparent;
  border: none;
  color: #06b6d4;
  font-size: 0.75rem;
  cursor: pointer;
}

.notify-list {
  max-height: 200px;
  overflow-y: auto;
}

.notify-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 0;
  font-size: 0.75rem;
  border-bottom: 1px solid rgba(6, 182, 212, 0.1);
}

.notify-item:last-child {
  border-bottom: none;
}

.notify-item.unread {
  background-color: rgba(6, 182, 212, 0.1);
  padding: 0.5rem;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.notify-title {
  margin: 0;
  font-weight: 500;
  color: #fff;
}

.notify-time {
  margin: 0;
  font-size: 0.7rem;
  color: #94a3b8;
}

/* 刷新按钮 */
.refresh-btn {
  background-color: rgba(6, 182, 212, 0.1);
  border: 1px solid rgba(6, 182, 212, 0.3);
  color: #e0e0e0;
  border-radius: 4px;
  padding: 0.25rem 0.75rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  background-color: rgba(6, 182, 212, 0.2);
  color: #fff;
  box-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
}

/* 卡片样式优化 - 增强科技感 */
.tech-card {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(6, 182, 212, 0.2);
  border-radius: 8px;
  padding: 0.75rem;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
  z-index: 2;
}

/* 卡片发光效果 */
.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(0deg, rgba(6, 182, 212, 0) 0%, rgba(6, 182, 212, 0.05) 100%);
  z-index: -1;
  transition: all 0.3s ease;
}

.alert-glow {
  background: linear-gradient(0deg, rgba(245, 158, 11, 0) 0%, rgba(245, 158, 11, 0.1) 100%);
}

.tech-card:hover {
  border-color: #06b6d4;
  box-shadow: 0 0 20px rgba(6, 182, 212, 0.2);
  transform: translateY(-2px);
}

.tech-card:hover .card-glow {
  background: linear-gradient(0deg, rgba(6, 182, 212, 0) 0%, rgba(6, 182, 212, 0.15) 100%);
}

/* 告警闪烁动画 */
.alert-flash {
  animation: alertFlash 2s infinite alternate;
}

@keyframes alertFlash {
  from { 
    border-color: rgba(245, 158, 11, 0.3); 
    box-shadow: 0 0 15px rgba(245, 158, 11, 0.1);
  }
  to { 
    border-color: #f59e0b; 
    box-shadow: 0 0 25px rgba(245, 158, 11, 0.3);
  }
}

/* 图表容器发光效果 */
.chart-glow-container {
  position: relative;
}

.chart-glow-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.1) 0%, rgba(6, 182, 212, 0) 70%);
  pointer-events: none;
  z-index: 1;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(6, 182, 212, 0.3);
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 0 30px rgba(6, 182, 212, 0.2);
  position: relative;
  overflow: hidden;
}

.modal-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(0deg, rgba(6, 182, 212, 0) 0%, rgba(6, 182, 212, 0.05) 100%);
  z-index: -1;
}

.modal-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid rgba(6, 182, 212, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #fff;
  font-size: 0.9375rem;
}

.close-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 1.25rem;
  cursor: pointer;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #fff;
}

.modal-body {
  padding: 1rem;
}

.detail-item {
  display: flex;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
}

.detail-item label {
  width: 80px;
  color: #94a3b8;
}

.detail-item span {
  color: #e0e0e0;
}

.modal-footer {
  padding: 0.75rem 1rem;
  border-top: 1px solid rgba(6, 182, 212, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.btn {
  padding: 0.375rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background-color: rgba(100, 116, 139, 0.2);
  border: 1px solid rgba(100, 116, 139, 0.3);
  color: #e0e0e0;
}

.cancel-btn:hover {
  background-color: rgba(100, 116, 139, 0.3);
}

.confirm-btn {
  background-color: rgba(6, 182, 212, 0.3);
  border: 1px solid rgba(6, 182, 212, 0.5);
  color: #fff;
}

.confirm-btn:hover {
  background-color: rgba(6, 182, 212, 0.5);
  box-shadow: 0 0 10px rgba(6, 182, 212, 0.3);
}

/* 其他细节优化 */
.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.drill-tip, .defect-tip {
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s ease;
}

.defect-tip:hover {
  color: #06b6d4;
}

.filter-group {
  display: flex;
  align-items: center;
}

.maintain-tip {
  color: #f59e0b;
}

/* 进度条优化 */
.progress-fill {
  transition: width 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 布局样式 */
.time-info, .update-info { display: flex; align-items: center; }
.screen-grid { display: grid; grid-template-columns: 1fr 2fr 1fr; gap: 1rem; padding: 1rem; height: calc(100vh - 60px); box-sizing: border-box; position: relative; z-index: 2; }
.col-left, .col-middle, .col-right { display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; padding-bottom: 0.5rem; border-bottom: 1px solid rgba(6, 182, 212, 0.1); }
.card-header h3 { margin: 0; font-size: 0.875rem; color: #b0c4de; }
.status-badge { font-size: 0.75rem; padding: 0.25rem 0.5rem; border-radius: 4px; text-transform: uppercase; font-weight: 500; }
.realtime { background-color: rgba(6, 182, 212, 0.1); color: #06b6d4; border: 1px solid rgba(6, 182, 212, 0.3); }
.normal { background-color: rgba(6, 182, 212, 0.1); color: #06b6d4; border: 1px solid rgba(6, 182, 212, 0.3); }
.warning { background-color: rgba(245, 158, 11, 0.1); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.3); }
.abnormal { background-color: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3); }
.card-body { flex: 1; display: flex; flex-direction: column; min-height: 0; }
.metric-value { font-size: 1.875rem; font-weight: bold; color: #fff; margin-bottom: 0.25rem; line-height: 1; text-shadow: 0 0 10px rgba(6, 182, 212, 0.3); }
.metric-trend { margin-bottom: 0.75rem; font-size: 0.75rem; }
.trend-up { color: #06b6d4; }
.trend-down { color: #f59e0b; }
.chart-container { flex: 1; min-height: 0; width: 100%; height: 100%; }
.full-chart { height: 100%; }
.progress-bar { height: 6px; background-color: rgba(6, 182, 212, 0.1); border-radius: 3px; margin-bottom: 0.25rem; overflow: hidden; }
.progress-text { font-size: 0.75rem; color: #b0c4de; text-align: right; }
.alert-list { flex: 1; overflow-y: auto; margin-top: 0.5rem; }
.alert-item { display: flex; align-items: center; padding: 0.5rem 0; font-size: 0.75rem; border-bottom: 1px solid rgba(6, 182, 212, 0.1); cursor: pointer; transition: background-color 0.2s ease; }
.alert-item:hover { background-color: rgba(6, 182, 212, 0.1); }
.alert-item:last-child { border-bottom: none; }
.alert-item i { color: #f59e0b; font-size: 0.875rem; }
.alert-time { margin-left: auto; color: #94a3b8; }
.stop-types { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-top: 0.5rem; }
.stop-type-item { background-color: rgba(6, 182, 212, 0.05); border-radius: 4px; padding: 0.5rem; }
.stop-type-label { font-size: 0.75rem; color: #94a3b8; margin-bottom: 0.25rem; }
.stop-type-value { font-size: 1rem; font-weight: bold; }
.time-filter { display: flex; gap: 0.5rem; }
.filter-btn { background-color: rgba(6, 182, 212, 0.05); border: 1px solid rgba(6, 182, 212, 0.2); color: #b0c4de; border-radius: 4px; padding: 0.25rem 0.75rem; font-size: 0.75rem; cursor: pointer; transition: all 0.2s ease; }
.filter-btn.active { background-color: rgba(6, 182, 212, 0.15); color: #06b6d4; border-color: rgba(6, 182, 212, 0.3); }
.filter-btn:hover:not(.active) { background-color: rgba(6, 182, 212, 0.1); }
.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; flex: 1; }
.chart-item { width: 100%; height: 100%; }
.production-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-top: 0.5rem; }
.stat-item { background-color: rgba(6, 182, 212, 0.05); border-radius: 4px; padding: 0.5rem; }
.stat-label { font-size: 0.75rem; color: #94a3b8; margin-bottom: 0.25rem; }
.stat-value { font-size: 1rem; font-weight: bold; }
.quality-stats { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.5rem; margin-top: 0.5rem; }
.health-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 0.75rem; margin-top: 0.5rem; }
.health-item { display: flex; flex-direction: column; gap: 0.25rem; cursor: pointer; padding: 0.25rem; border-radius: 4px; transition: background-color 0.2s ease; }
.health-item:hover { background-color: rgba(6, 182, 212, 0.1); }
.health-info { display: flex; justify-content: space-between; font-size: 0.875rem; }
.device-name { font-weight: 500; }
.health-status { font-size: 0.75rem; padding: 0.125rem 0.5rem; border-radius: 4px; }
.status-excellent { background-color: rgba(6, 182, 212, 0.2); color: #06b6d4; }
.status-good { background-color: rgba(59, 130, 246, 0.2); color: #3b82f6; }
.status-average { background-color: rgba(132, 204, 22, 0.2); color: #84cc16; }
.status-poor { background-color: rgba(245, 158, 11, 0.2); color: #f59e0b; }
.status-bad { background-color: rgba(239, 68, 68, 0.2); color: #ef4444; }
.health-bar { height: 4px; background-color: rgba(6, 182, 212, 0.1); border-radius: 2px; overflow: hidden; }
.health-fill { height: 100%; transition: width 0.5s ease; }
.progress-excellent { background-color: #06b6d4; box-shadow: 0 0 5px rgba(6, 182, 212, 0.5); }
.progress-good { background-color: #3b82f6; box-shadow: 0 0 5px rgba(59, 130, 246, 0.5); }
.progress-average { background-color: #84cc16; box-shadow: 0 0 5px rgba(132, 204, 22, 0.5); }
.progress-poor { background-color: #f59e0b; box-shadow: 0 0 5px rgba(245, 158, 11, 0.5); }
.progress-bad { background-color: #ef4444; box-shadow: 0 0 5px rgba(239, 68, 68, 0.5); }
.health-score { font-size: 0.75rem; color: #94a3b8; text-align: right; }
.good { color: #06b6d4; }
.danger { color: #ef4444; }
::-webkit-scrollbar { width: 4px; height: 4px; }
::-webkit-scrollbar-track { background: rgba(6, 182, 212, 0.1); border-radius: 2px; }
::-webkit-scrollbar-thumb { background: rgba(6, 182, 212, 0.5); border-radius: 2px; }
::-webkit-scrollbar-thumb:hover { background: rgba(6, 182, 212, 0.8); }
</style>
