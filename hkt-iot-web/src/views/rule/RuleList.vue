<template>
  <div class="rule-list-container">
    <div class="page-header">
      <h2 class="page-title">规则引擎</h2>
    </div>

    <a-card>
      <a-result
        status="info"
        title="规则引擎模块开发中"
        sub-title="该模块将在后续版本中实现"
      >
        <template #extra>
          <a-button type="primary" @click="$router.push('/device')">
            返回设备管理
          </a-button>
        </template>
      </a-result>
    </a-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.rule-list-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}
</style>
