<template>
  <div class="space-list-container">
    <div class="page-header">
      <h2 class="page-title">空间管理</h2>
    </div>

    <a-card>
      <a-result
        status="info"
        title="空间管理模块开发中"
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
.space-list-container {
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
