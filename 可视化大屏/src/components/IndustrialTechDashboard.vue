<template>
  <div class="tech-screen-container">
    <!-- 顶部标题栏 -->
    <header class="screen-header">
      <div class="flex items-center justify-center">
        <i class="fa fa-industry mr-2"></i>
        <h1>工业大数据分析与挖掘平台</h1>
      </div>
      <div class="header-info">
        <span class="time-info"><i class="fa fa-clock-o mr-1"></i>{{ currentTime }}</span>
        <span class="update-info"><i class="fa fa-refresh mr-1"></i>数据更新于: {{ lastUpdateTime }}</span>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="screen-grid">
      <!-- 左侧区域 - 关键指标 -->
      <div class="col-left space-y-4">
        <!-- 设备总数 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>设备总数</h3>
            <span class="status-badge realtime">实时</span>
          </div>
          <div class="card-body">
            <div class="metric-value">{{ totalDevices }}</div>
            <div class="metric-trend">
              <span class="trend-up"><i class="fa fa-arrow-up mr-1"></i>2.4%</span>
            </div>
            <div class="chart-container" ref="deviceTrendChart"></div>
          </div>
        </div>

        <!-- 运行中设备 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>运行中设备</h3>
            <span class="status-badge normal">正常</span>
          </div>
          <div class="card-body">
            <div class="metric-value">{{ runningDevices }}</div>
            <div class="metric-trend">
              <span class="trend-up"><i class="fa fa-arrow-up mr-1"></i>5.1%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: runningDevicesPercentage + '%' }"></div>
            </div>
            <div class="progress-text">{{ runningDevicesPercentage }}%</div>
          </div>
        </div>

        <!-- 告警设备 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>告警设备</h3>
            <span class="status-badge warning">警告</span>
          </div>
          <div class="card-body">
            <div class="metric-value">{{ alertDevices }}</div>
            <div class="metric-trend">
              <span class="trend-down"><i class="fa fa-arrow-up mr-1"></i>12.3%</span>
            </div>
            <div class="alert-list">
              <div v-for="(alert, index) in recentAlerts" :key="index" class="alert-item">
                <i class="fa fa-exclamation-circle mr-2"></i>
                <span>{{ alert.device }}</span>
                <span class="alert-time">{{ alert.time }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 停机设备 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>停机设备</h3>
            <span class="status-badge abnormal">异常</span>
          </div>
          <div class="card-body">
            <div class="metric-value">{{ stoppedDevices }}</div>
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
          </div>
        </div>
      </div>

      <!-- 中间区域 - 主要图表 -->
      <div class="col-middle space-y-4">
        <!-- 生产效率趋势 -->
        <div class="tech-card full-height">
          <div class="card-header">
            <h3>生产效率趋势分析</h3>
            <div class="time-filter">
              <button class="filter-btn active">日</button>
              <button class="filter-btn">周</button>
              <button class="filter-btn">月</button>
              <button class="filter-btn">年</button>
            </div>
          </div>
          <div class="card-body">
            <div class="chart-container full-chart" ref="productionEfficiencyChart"></div>
          </div>
        </div>

        <!-- 设备分布与能耗分析 -->
        <div class="tech-card full-height">
          <div class="card-header">
            <h3>设备分布与能耗分析</h3>
            <button class="filter-btn"><i class="fa fa-filter mr-1"></i>筛选</button>
          </div>
          <div class="card-body chart-grid">
            <div class="chart-item" ref="equipmentDistributionChart"></div>
            <div class="chart-item" ref="energyConsumptionChart"></div>
          </div>
        </div>
      </div>

      <!-- 右侧区域 - 详细信息 -->
      <div class="col-right space-y-4">
        <!-- 产量统计 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>产量统计</h3>
            <i class="fa fa-bar-chart"></i>
          </div>
          <div class="card-body">
            <div class="chart-container" ref="productionChart"></div>
            <div class="production-stats">
              <div class="stat-item">
                <div class="stat-label">今日产量</div>
                <div class="stat-value">{{ todayProduction }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">目标产量</div>
                <div class="stat-value">{{ targetProduction }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 质量监控 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>质量监控</h3>
            <i class="fa fa-check-square-o"></i>
          </div>
          <div class="card-body">
            <div class="chart-container" ref="qualityControlChart"></div>
            <div class="quality-stats">
              <div class="stat-item">
                <div class="stat-label">合格率</div>
                <div class="stat-value good">{{ passRate }}%</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">不良率</div>
                <div class="stat-value danger">{{ rejectRate }}%</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">CPK</div>
                <div class="stat-value">{{ cpkValue }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 设备健康度 -->
        <div class="tech-card">
          <div class="card-header">
            <h3>设备健康度</h3>
            <i class="fa fa-heartbeat"></i>
          </div>
          <div class="card-body">
            <div class="health-list">
              <div v-for="(device, index) in deviceHealthList" :key="index" class="health-item">
                <div class="health-info">
                  <span class="device-name">{{ device.name }}</span>
                  <span :class="['health-status', device.healthClass]">{{ device.healthStatus }}</span>
                </div>
                <div class="health-bar">
                  <div :class="['health-fill', device.progressClass]" :style="{ width: device.healthScore + '%' }"></div>
                </div>
                <div class="health-score">{{ device.healthScore }}/100</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, onUnmounted } from 'vue';
import * as echarts from 'echarts';

// 时间相关
const currentTime = ref('');
const lastUpdateTime = ref('');

// 图表容器引用
const deviceTrendChart = ref(null);
const productionEfficiencyChart = ref(null);
const equipmentDistributionChart = ref(null);
const energyConsumptionChart = ref(null);
const productionChart = ref(null);
const qualityControlChart = ref(null);

// 图表实例
let chartInstances = {
  deviceTrend: null,
  productionEfficiency: null,
  equipmentDistribution: null,
  energyConsumption: null,
  production: null,
  qualityControl: null
};

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
  { device: '冲压机#A302', time: '10:24' },
  { device: '传送带#B105', time: '09:15' },
  { device: '包装机#C007', time: '08:42' },
  { device: '切割机#D211', time: '07:58' }
]);

// 产量数据
const todayProduction = ref(12586);
const targetProduction = ref(15000);

// 质量数据
const passRate = ref(98.7);
const rejectRate = ref(1.3);
const cpkValue = ref(1.68);

// 设备健康度数据
const deviceHealthList = ref([
  { name: '数控机床#101', healthScore: 92, healthStatus: '优秀', healthClass: 'status-excellent', progressClass: 'progress-excellent' },
  { name: '机器人手臂#203', healthScore: 87, healthStatus: '良好', healthClass: 'status-good', progressClass: 'progress-good' },
  { name: '冲压机#A302', healthScore: 76, healthStatus: '一般', healthClass: 'status-average', progressClass: 'progress-average' },
  { name: '焊接机#B405', healthScore: 65, healthStatus: '较差', healthClass: 'status-poor', progressClass: 'progress-poor' },
  { name: '包装机#C007', healthScore: 58, healthStatus: '差', healthClass: 'status-bad', progressClass: 'progress-bad' }
]);

// 更新时间
const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  
  // 每5分钟更新一次最后更新时间（模拟）
  if (now.getMinutes() % 5 === 0) {
    lastUpdateTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }
};

// 初始化图表
const initCharts = () => {
  // 设备趋势小图表
  chartInstances.deviceTrend = echarts.init(deviceTrendChart.value);
  chartInstances.deviceTrend.setOption({
    tooltip: { trigger: 'none' },
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: {
      type: 'category',
      data: ['', '', '', '', '', '', '', '', '', ''],
      show: false
    },
    yAxis: {
      type: 'value',
      show: false
    },
    series: [{
      data: [238, 240, 242, 241, 243, 245, 244, 246, 247, 248],
      type: 'line',
      smooth: true,
      lineStyle: { width: 2, color: '#1a53ff' },
      areaStyle: { color: 'rgba(26, 83, 255, 0.2)' },
      symbol: 'none'
    }]
  });
  
  // 生产效率趋势图
  chartInstances.productionEfficiency = echarts.init(productionEfficiencyChart.value);
  chartInstances.productionEfficiency.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 30, 60, 0.8)',
      borderColor: 'rgba(100, 150, 255, 0.3)',
      borderWidth: 1
    },
    legend: {
      data: ['实际效率', '目标效率'],
      top: 0,
      textStyle: { color: '#b0c4de' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['00:00', '03:00', '06:00', '09:00', '12:00', '15:00', '18:00', '21:00'],
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { 
        color: '#b0c4de',
        formatter: '{value}%'
      },
      splitLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.1)' } },
      min: 50,
      max: 100
    },
    series: [
      {
        name: '实际效率',
        type: 'line',
        data: [65, 68, 72, 89, 92, 87, 85, 88],
        smooth: true,
        lineStyle: { width: 2, color: '#1a53ff' },
        areaStyle: { color: 'rgba(26, 83, 255, 0.2)' },
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#1a53ff' }
      },
      {
        name: '目标效率',
        type: 'line',
        data: [80, 80, 80, 80, 80, 80, 80, 80],
        lineStyle: { 
          width: 1.5, 
          color: 'rgba(100, 116, 139, 0.7)',
          type: 'dashed'
        },
        symbol: 'none'
      }
    ]
  });
  
  // 设备分布饼图
  chartInstances.equipmentDistribution = echarts.init(equipmentDistributionChart.value);
  chartInstances.equipmentDistribution.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(10, 30, 60, 0.8)',
      borderColor: 'rgba(100, 150, 255, 0.3)',
      borderWidth: 1
    },
    legend: {
      orient: 'vertical',
      bottom: 0,
      left: 'center',
      textStyle: { color: '#b0c4de', fontSize: 11 },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 15
    },
    series: [{
      name: '设备类型',
      type: 'pie',
      radius: ['70%', '90%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: 'rgba(10, 30, 60, 0.8)',
        borderWidth: 2
      },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: false }
      },
      labelLine: { show: false },
      data: [
        { value: 35, name: '加工设备', itemStyle: { color: '#1a53ff' } },
        { value: 25, name: '装配设备', itemStyle: { color: '#00cfd5' } },
        { value: 15, name: '输送设备', itemStyle: { color: '#7cb305' } },
        { value: 12, name: '检测设备', itemStyle: { color: '#ff7d00' } },
        { value: 13, name: '包装设备', itemStyle: { color: '#8b5cf6' } }
      ]
    }]
  });
  
  // 能耗分析图
  chartInstances.energyConsumption = echarts.init(energyConsumptionChart.value);
  chartInstances.energyConsumption.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 30, 60, 0.8)',
      borderColor: 'rgba(100, 150, 255, 0.3)',
      borderWidth: 1
    },
    legend: {
      data: ['今日能耗', '昨日能耗'],
      top: 0,
      textStyle: { color: '#b0c4de', fontSize: 11 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['电力', '燃气', '水', '压缩空气'],
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.1)' } }
    },
    series: [
      {
        name: '今日能耗',
        type: 'bar',
        data: [680, 240, 95, 130],
        itemStyle: { color: '#1a53ff', borderRadius: 4 },
        barWidth: '30%'
      },
      {
        name: '昨日能耗',
        type: 'bar',
        data: [720, 260, 105, 145],
        itemStyle: { color: 'rgba(100, 116, 139, 0.7)', borderRadius: 4 },
        barWidth: '30%'
      }
    ]
  });
  
  // 产量图表
  chartInstances.production = echarts.init(productionChart.value);
  chartInstances.production.setOption({
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00'],
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de', fontSize: 10 },
      splitLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.1)' } }
    },
    series: [{
      data: [1850, 2100, 2450, 2200, 2350, 1636],
      type: 'bar',
      itemStyle: { 
        color: new echarts.graphic.LinearGradient(
          0, 0, 0, 1,
          [{ offset: 0, color: '#1a53ff' }, { offset: 1, color: '#00cfd5' }]
        ), 
        borderRadius: 4 
      },
      barWidth: '60%'
    }]
  });
  
  // 质量监控图表
  chartInstances.qualityControl = echarts.init(qualityControlChart.value);
  chartInstances.qualityControl.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 30, 60, 0.8)',
      borderColor: 'rgba(100, 150, 255, 0.3)',
      borderWidth: 1
    },
    grid: {
      left: '3%',
      right: '15%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['批次1', '批次2', '批次3', '批次4', '批次5', '批次6'],
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de', fontSize: 10 }
    },
    yAxis: [
      {
        type: 'value',
        name: '合格率(%)',
        nameTextStyle: { color: '#00cfd5', fontSize: 10 },
        axisLine: { lineStyle: { color: 'rgba(0, 207, 213, 0.5)' } },
        axisLabel: { 
          color: '#00cfd5', 
          fontSize: 10,
          formatter: '{value}%'
        },
        splitLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.1)' } },
        min: 97,
        max: 100
      },
      {
        type: 'value',
        name: 'CPK',
        nameLocation: 'end',
        nameTextStyle: { color: '#1a53ff', fontSize: 10 },
        axisLine: { lineStyle: { color: 'rgba(26, 83, 255, 0.5)' } },
        axisLabel: { 
          color: '#1a53ff', 
          fontSize: 10
        },
        splitLine: { show: false },
        min: 1.4,
        max: 1.8
      }
    ],
    series: [
      {
        name: '合格率',
        type: 'line',
        data: [98.2, 98.5, 97.9, 99.1, 98.8, 98.7],
        smooth: true,
        lineStyle: { width: 2, color: '#00cfd5' },
        areaStyle: { color: 'rgba(0, 207, 213, 0.2)' },
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#00cfd5' }
      },
      {
        name: 'CPK',
        type: 'line',
        data: [1.52, 1.60, 1.58, 1.72, 1.70, 1.68],
        smooth: true,
        lineStyle: { width: 2, color: '#1a53ff' },
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: { color: '#1a53ff' },
        yAxisIndex: 1
      }
    ]
  });
  
  // 监听窗口大小变化，调整图表尺寸
  window.addEventListener('resize', handleResize);
};

// 处理窗口大小变化
const handleResize = () => {
  Object.values(chartInstances).forEach(instance => {
    if (instance) instance.resize();
  });
};

// 更新生产效率图表数据
const updateProductionEfficiencyChart = (newData) => {
  if (chartInstances.productionEfficiency) {
    const option = chartInstances.productionEfficiency.getOption();
    option.series[0].data = newData;
    chartInstances.productionEfficiency.setOption(option);
  }
};

// 更新产量图表数据
const updateProductionChart = (newValue) => {
  if (chartInstances.production) {
    const option = chartInstances.production.getOption();
    // 移除第一个数据点，添加新数据点
    option.series[0].data.shift();
    option.series[0].data.push(newValue);
    chartInstances.production.setOption(option);
  }
};

// 模拟数据更新
const simulateDataUpdates = () => {
  setInterval(() => {
    // 随机小幅波动设备数量
    runningDevices.value = Math.max(200, Math.min(230, runningDevices.value + (Math.random() > 0.5 ? 1 : -1)));
    runningDevicesPercentage.value = ((runningDevices.value / totalDevices.value) * 100).toFixed(1);
    
    // 随机更新告警数量
    if (Math.random() > 0.8) {
      alertDevices.value = Math.max(10, Math.min(25, alertDevices.value + (Math.random() > 0.5 ? 1 : -1)));
    }
    
    // 更新产量
    const newProduction = Math.floor(Math.random() * 10) + 1;
    todayProduction.value += newProduction;
    
    // 更新产量图表
    updateProductionChart(newProduction + 1600); // 模拟新增产量数据点
    
    // 随机更新生产效率数据
    if (Math.random() > 0.7) {
      const baseData = [65, 68, 72, 89, 92, 87, 85, 88];
      const newEfficiencyData = baseData.map(val => {
        return Math.max(60, Math.min(95, val + (Math.random() > 0.5 ? 1 : -1) * Math.random() * 3));
      });
      updateProductionEfficiencyChart(newEfficiencyData);
    }
    
    // 随机更新设备健康度
    if (Math.random() > 0.7) {
      const idx = Math.floor(Math.random() * deviceHealthList.value.length);
      const change = Math.random() > 0.5 ? 1 : -1;
      deviceHealthList.value[idx].healthScore = Math.max(50, Math.min(100, deviceHealthList.value[idx].healthScore + change));
      
      // 更新健康状态
      const score = deviceHealthList.value[idx].healthScore;
      if (score >= 90) {
        deviceHealthList.value[idx].healthStatus = '优秀';
        deviceHealthList.value[idx].healthClass = 'status-excellent';
        deviceHealthList.value[idx].progressClass = 'progress-excellent';
      } else if (score >= 80) {
        deviceHealthList.value[idx].healthStatus = '良好';
        deviceHealthList.value[idx].healthClass = 'status-good';
        deviceHealthList.value[idx].progressClass = 'progress-good';
      } else if (score >= 70) {
        deviceHealthList.value[idx].healthStatus = '一般';
        deviceHealthList.value[idx].healthClass = 'status-average';
        deviceHealthList.value[idx].progressClass = 'progress-average';
      } else if (score >= 60) {
        deviceHealthList.value[idx].healthStatus = '较差';
        deviceHealthList.value[idx].healthClass = 'status-poor';
        deviceHealthList.value[idx].progressClass = 'progress-poor';
      } else {
        deviceHealthList.value[idx].healthStatus = '差';
        deviceHealthList.value[idx].healthClass = 'status-bad';
        deviceHealthList.value[idx].progressClass = 'progress-bad';
      }
    }
  }, 5000);
};

// 清理图表实例
const cleanUpCharts = () => {
  Object.values(chartInstances).forEach(instance => {
    if (instance) {
      instance.dispose();
    }
  });
  window.removeEventListener('resize', handleResize);
};

onMounted(() => {
  // 初始化时间
  updateTime();
  setInterval(updateTime, 1000);
  
  // 初始化图表
  initCharts();
  
  // 模拟数据更新
  simulateDataUpdates();
});

onUnmounted(() => {
  cleanUpCharts();
});
</script>

<style scoped>
/* 基础样式 */
.tech-screen-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(to bottom, #0a1931, #0f2a4d);
  color: #e0e0e0;
  overflow: hidden;
  font-family: 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
}

/* 顶部标题栏 */
.screen-header {
  background: linear-gradient(to right, #1a53ff, #0f3ad0);
  padding: 0.75rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(100, 150, 255, 0.3);
}

.screen-header h1 {
  margin: 0;
  font-size: 1.25rem;
  color: #fff;
  display: flex;
  align-items: center;
}

.header-info {
  display: flex;
  gap: 1.5rem;
  font-size: 0.875rem;
}

.time-info, .update-info {
  display: flex;
  align-items: center;
}

/* 主网格布局 */
.screen-grid {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 1rem;
  padding: 1rem;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

.col-left, .col-middle, .col-right {
  display: flex;
  flex-direction: column;
}

/* 卡片样式 */
.tech-card {
  background: rgba(10, 50, 100, 0.5);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(100, 150, 255, 0.3);
  border-radius: 8px;
  padding: 0.75rem;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.full-height {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(100, 150, 255, 0.1);
}

.card-header h3 {
  margin: 0;
  font-size: 0.875rem;
  color: #b0c4de;
}

/* 状态标签 */
.status-badge {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  text-transform: uppercase;
  font-weight: 500;
}

.realtime {
  background-color: rgba(26, 83, 255, 0.2);
  color: #1a53ff;
  border: 1px solid rgba(26, 83, 255, 0.3);
}

.normal {
  background-color: rgba(0, 207, 213, 0.2);
  color: #00cfd5;
  border: 1px solid rgba(0, 207, 213, 0.3);
}

.warning {
  background-color: rgba(255, 125, 0, 0.2);
  color: #ff7d00;
  border: 1px solid rgba(255, 125, 0, 0.3);
}

.abnormal {
  background-color: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

/* 卡片内容区 */
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 指标值样式 */
.metric-value {
  font-size: 1.875rem;
  font-weight: bold;
  color: #fff;
  margin-bottom: 0.25rem;
  line-height: 1;
}

/* 趋势指示器 */
.metric-trend {
  margin-bottom: 0.75rem;
  font-size: 0.75rem;
}

.trend-up {
  color: #00cfd5;
}

.trend-down {
  color: #ff7d00;
}

/* 图表容器 */
.chart-container {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.full-chart {
  height: 100%;
}

/* 进度条 */
.progress-bar {
  height: 6px;
  background-color: rgba(100, 150, 255, 0.1);
  border-radius: 3px;
  margin-bottom: 0.25rem;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #00cfd5;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 0.75rem;
  color: #b0c4de;
  text-align: right;
}

/* 告警列表 */
.alert-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 0.5rem;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 0;
  font-size: 0.75rem;
  border-bottom: 1px solid rgba(100, 150, 255, 0.1);
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-item i {
  color: #ff7d00;
  font-size: 0.875rem;
}

.alert-time {
  margin-left: auto;
  color: #b0c4de;
}

/* 停机类型 */
.stop-types {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.stop-type-item {
  background-color: rgba(100, 150, 255, 0.1);
  border-radius: 4px;
  padding: 0.5rem;
}

.stop-type-label {
  font-size: 0.75rem;
  color: #b0c4de;
  margin-bottom: 0.25rem;
}

.stop-type-value {
  font-size: 1rem;
  font-weight: bold;
}

/* 时间筛选按钮 */
.time-filter {
  display: flex;
  gap: 0.5rem;
}

.filter-btn {
  background-color: rgba(100, 150, 255, 0.1);
  border: 1px solid rgba(100, 150, 255, 0.3);
  color: #b0c4de;
  border-radius: 4px;
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-btn.active {
  background-color: rgba(26, 83, 255, 0.2);
  color: #1a53ff;
  border-color: rgba(26, 83, 255, 0.3);
}

.filter-btn:hover:not(.active) {
  background-color: rgba(100, 150, 255, 0.2);
}

/* 图表网格 */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  flex: 1;
}

.chart-item {
  width: 100%;
  height: 100%;
}

/* 产量统计 */
.production-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.stat-item {
  background-color: rgba(100, 150, 255, 0.1);
  border-radius: 4px;
  padding: 0.5rem;
}

.stat-label {
  font-size: 0.75rem;
  color: #b0c4de;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 1rem;
  font-weight: bold;
}

/* 质量统计 */
.quality-stats {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

/* 设备健康度列表 */
.health-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.health-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.health-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
}

.device-name {
  font-weight: 500;
}

.health-status {
  font-size: 0.75rem;
  padding: 0.125rem 0.5rem;
  border-radius: 4px;
}

.status-excellent {
  background-color: rgba(0, 207, 213, 0.2);
  color: #00cfd5;
}

.status-good {
  background-color: rgba(26, 83, 255, 0.2);
  color: #1a53ff;
}

.status-average {
  background-color: rgba(124, 180, 5, 0.2);
  color: #7cb305;
}

.status-poor {
  background-color: rgba(255, 125, 0, 0.2);
  color: #ff7d00;
}

.status-bad {
  background-color: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.health-bar {
  height: 4px;
  background-color: rgba(100, 150, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.health-fill {
  height: 100%;
  transition: width 0.5s ease;
}

.progress-excellent {
  background-color: #00cfd5;
}

.progress-good {
  background-color: #1a53ff;
}

.progress-average {
  background-color: #7cb305;
}

.progress-poor {
  background-color: #ff7d00;
}

.progress-bad {
  background-color: #ef4444;
}

.health-score {
  font-size: 0.75rem;
  color: #b0c4de;
  text-align: right;
}

/* 状态颜色 */
.good {
  color: #00cfd5;
}

.danger {
  color: #ef4444;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

::-webkit-scrollbar-track {
  background: rgba(100, 150, 255, 0.1);
  border-radius: 2px;
}

::-webkit-scrollbar-thumb {
  background: rgba(100, 150, 255, 0.5);
  border-radius: 2px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 150, 255, 0.8);
}
</style>
