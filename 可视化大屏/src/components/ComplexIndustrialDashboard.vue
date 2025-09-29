<template>
  <div class="complex-dashboard">
    <!-- 顶部状态栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <h1>工业大数据智能监控平台</h1>
      </div>
      <div class="header-right">
        <span class="time">{{ currentTime }}</span>
        <button class="refresh-btn" @click="refreshData">
          <i class="fa fa-refresh"></i> 刷新
        </button>
      </div>
    </header>

    <!-- 主内容区：网格布局承载多面板 -->
    <main class="dashboard-grid">
      <!-- 面板1：左上角 设备状态雷达图 + 小指标 -->
      <div class="panel" ref="deviceRadarPanel"></div>

      <!-- 面板2：中上部 生产趋势 + 设备分布 -->
      <div class="panel" ref="productionTrendPanel"></div>

      <!-- 面板3：右上角 能耗分析 + 预警趋势 -->
      <div class="panel" ref="energyWarnPanel"></div>

      <!-- 面板4：左中部 质量波动 + 工艺参数 -->
      <div class="panel" ref="qualityParamPanel"></div>

      <!-- 面板5：中下部 车间热力图 + 工单进度 -->
      <div class="panel" ref="workshopHeatPanel"></div>

      <!-- 面板6：右中部 设备健康度 + 维修预测 -->
      <div class="panel" ref="deviceHealthPanel"></div>

      <!-- 面板7：左下角 物料流转 + 库存预警 -->
      <div class="panel" ref="materialStockPanel"></div>

      <!-- 面板8：中下部 人员效率 + 排班分析 -->
      <div class="panel" ref="staffEfficiencyPanel"></div>

      <!-- 面板9：右下角 订单分布 + 交付预测 -->
      <div class="panel" ref="orderDeliveryPanel"></div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { useIntervalFn } from '@vueuse/core'; // 用于定时更新

// ---------- 1. 基础状态与引用 ----------
const deviceRadarPanel = ref(null);
const productionTrendPanel = ref(null);
const energyWarnPanel = ref(null);
const qualityParamPanel = ref(null);
const workshopHeatPanel = ref(null);
const deviceHealthPanel = ref(null);
const materialStockPanel = ref(null);
const staffEfficiencyPanel = ref(null);
const orderDeliveryPanel = ref(null);

const currentTime = ref('');
const chartInstances = ref({}); // 存储所有图表实例

// ---------- 2. 初始化图表 ----------
const initCharts = () => {
  // 面板1：设备状态雷达图
  chartInstances.value.deviceRadar = echarts.init(deviceRadarPanel.value);
  chartInstances.value.deviceRadar.setOption({
    title: { text: '设备健康雷达', left: 'center', textStyle: { color: '#fff' } },
    tooltip: { trigger: 'item' },
    radar: {
      indicator: [
        { name: '温度', max: 100 },
        { name: '振动', max: 100 },
        { name: '能耗', max: 100 },
        { name: '精度', max: 100 },
        { name: '负载', max: 100 },
        { name: '噪声', max: 100 }
      ],
      splitLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.5)' } },
      axisLabel: { color: '#b0c4de' }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [85, 78, 92, 88, 75, 82],
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#1a53ff' },
          { offset: 1, color: '#00cfd5' }
        ]) },
        areaStyle: { color: 'rgba(26, 83, 255, 0.2)' }
      }]
    }]
  });

  // 面板2：生产趋势 + 设备分布
  chartInstances.value.productionTrend = echarts.init(productionTrendPanel.value);
  chartInstances.value.productionTrend.setOption({
    title: { text: '生产趋势与设备分布', left: 'center', textStyle: { color: '#fff' } },
    tooltip: { trigger: 'axis' },
    grid: { top: '30%', left: '5%', right: '5%', bottom: '5%' },
    xAxis: {
      type: 'category',
      data: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00'],
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(100, 150, 255, 0.3)' } },
      axisLabel: { color: '#b0c4de' }
    },
    series: [
      {
        name: '产量',
        type: 'line',
        data: [1850, 2100, 2450, 2200, 2350, 1636],
        smooth: true,
        lineStyle: { color: '#1a53ff' },
        areaStyle: { color: 'rgba(26, 83, 255, 0.2)' }
      },
      {
        name: '设备分布',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['50%', '15%'],
        data: [
          { value: 35, name: '加工设备' },
          { value: 25, name: '装配设备' },
          { value: 15, name: '输送设备' },
          { value: 12, name: '检测设备' },
          { value: 13, name: '包装设备' }
        ],
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#00cfd5' },
          { offset: 1, color: '#7cb305' }
        ]) }
      }
    ]
  });

  // 其他面板图表初始化（类似上述结构，根据需求扩展...）
  // 面板3：能耗分析 + 预警趋势
  chartInstances.value.energyWarn = echarts.init(energyWarnPanel.value);
  // 面板4：质量波动 + 工艺参数
  chartInstances.value.qualityParam = echarts.init(qualityParamPanel.value);
  // 面板5：车间热力图 + 工单进度
  chartInstances.value.workshopHeat = echarts.init(workshopHeatPanel.value);
  // 面板6：设备健康度 + 维修预测
  chartInstances.value.deviceHealth = echarts.init(deviceHealthPanel.value);
  // 面板7：物料流转 + 库存预警
  chartInstances.value.materialStock = echarts.init(materialStockPanel.value);
  // 面板8：人员效率 + 排班分析
  chartInstances.value.staffEfficiency = echarts.init(staffEfficiencyPanel.value);
  // 面板9：订单分布 + 交付预测
  chartInstances.value.orderDelivery = echarts.init(orderDeliveryPanel.value);
};

// ---------- 3. 定时更新时间与数据 ----------
const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const refreshData = () => {
  // 模拟数据刷新，实际可调用接口
  Object.values(chartInstances.value).forEach(instance => {
    if (instance) {
      const option = instance.getOption();
      // 随机更新系列数据（示例）
      if (option.series) {
        option.series.forEach(series => {
          if (series.data && Array.isArray(series.data)) {
            series.data = series.data.map(item => 
              typeof item === 'number' ? item + (Math.random() * 100 - 50) : item
            );
          }
        });
        instance.setOption(option);
      }
    }
  });
};

// 每秒更新时间，每30秒刷新数据
useIntervalFn(updateTime, 1000);
useIntervalFn(refreshData, 30000);

// ---------- 4. 生命周期与清理 ----------
onMounted(() => {
  updateTime();
  initCharts();
  // 窗口resize时自适应
  window.addEventListener('resize', () => {
    Object.values(chartInstances.value).forEach(instance => {
      instance && instance.resize();
    });
  });
});

onUnmounted(() => {
  // 销毁所有图表实例
  Object.values(chartInstances.value).forEach(instance => {
    instance && instance.dispose();
  });
  window.removeEventListener('resize', () => {});
});
</script>

<style scoped>
.complex-dashboard {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(to bottom, #0a1931, #0f2a4d);
  color: #e0e0e0;
  font-family: 'Arial', sans-serif;
  overflow: hidden;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(to right, #1a53ff, #0f3ad0);
  padding: 12px 24px;
  border-bottom: 1px solid rgba(100, 150, 255, 0.3);
}

.header-left h1 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.time {
  font-size: 14px;
  color: #b0c4de;
}

.refresh-btn {
  background: rgba(100, 150, 255, 0.2);
  border: 1px solid rgba(100, 150, 255, 0.3);
  color: #b0c4de;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: rgba(100, 150, 255, 0.4);
  color: #fff;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
  gap: 16px;
  padding: 16px;
  height: calc(100vh - 60px);
}

.panel {
  background: rgba(10, 50, 100, 0.5);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(100, 150, 255, 0.3);
  border-radius: 8px;
  padding: 8px;
}
</style>