<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icon">HK</div>
        <h1>华宽通智能体平台</h1>
        <p>HK IoT Intelligence Platform</p>
      </div>

      <!-- 第一步：用户名密码登录 -->
      <a-form
        v-if="loginStep === 'credentials'"
        ref="formRef"
        :model="formData"
        :rules="rules"
        @finish="handleCredentialsSubmit"
        class="login-form"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formData.username"
            size="large"
            placeholder="请输入用户名"
            :prefix="h(UserOutlined)"
            allow-clear
          />
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="formData.password"
            size="large"
            placeholder="请输入密码"
            :prefix="h(LockOutlined)"
            @keyup.enter="handleCredentialsSubmit"
          />
        </a-form-item>

        <a-form-item name="tenantCode">
          <a-input
            v-model:value="formData.tenantCode"
            size="large"
            placeholder="请输入租户编码（可选）"
            :prefix="h(BankOutlined)"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="formData.rememberDevice">
            记住设备（30天内免MFA验证）
          </a-checkbox>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <!-- 第二步：MFA多因素认证 -->
      <div v-else-if="loginStep === 'mfa'" class="mfa-form">
        <a-alert
          type="info"
          show-icon
          style="margin-bottom: 24px"
        >
          <template #message>
            <div class="mfa-alert">
              <SafetyOutlined />
              <span>为了您的账户安全，请完成多因素认证</span>
            </div>
          </template>
        </a-alert>

        <!-- MFA类型选择 -->
        <div v-if="availableMfaTypes.length > 1" class="mfa-types">
          <a-radio-group v-model:value="selectedMfaType" button-style="solid">
            <a-radio-button
              v-for="type in availableMfaTypes"
              :key="type"
              :value="type"
            >
              <template #icon>
                <MobileOutlined v-if="type === 'SMS'" />
                <MailOutlined v-else-if="type === 'EMAIL'" />
                <SafetyOutlined v-else />
              </template>
              {{ getMfaTypeName(type) }}
            </a-radio-button>
          </a-radio-group>
        </div>

        <!-- TOTP认证 -->
        <div v-if="selectedMfaType === 'TOTP'" class="mfa-totp">
          <div class="qr-code" v-if="totpSetup && !totpSetup.verified">
            <a-qrcode :value="totpSetup.qrCodeUrl" :size="200" />
            <p class="qr-tip">请使用身份验证器应用扫描二维码</p>
          </div>
          <a-form
            ref="mfaFormRef"
            :model="mfaFormData"
            :rules="mfaRules"
            @finish="handleMfaVerify"
          >
            <a-form-item name="code" label="验证码">
              <a-input
                v-model:value="mfaFormData.code"
                size="large"
                placeholder="请输入6位验证码"
                maxlength="6"
                :prefix="h(SafetyOutlined)"
                allow-clear
              />
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                size="large"
                :loading="loading"
                block
              >
                验证
              </a-button>
            </a-form-item>

            <div v-if="totpSetup && !totpSetup.verified" class="backup-codes">
              <a-collapse>
                <a-collapse-panel key="1" header="备用恢复码（请妥善保存）">
                  <a-alert
                    type="warning"
                    message="这些恢复码只能在无法使用身份验证器时使用，请妥善保管！"
                    show-icon
                    closable
                    style="margin-bottom: 16px"
                  />
                  <p class="backup-codes-list">
                    <a-tag v-for="code in totpSetup.backupCodes" :key="code" color="blue">
                      {{ code }}
                    </a-tag>
                  </p>
                </a-collapse-panel>
              </a-collapse>
            </div>
          </a-form>
        </div>

        <!-- SMS认证 -->
        <div v-else-if="selectedMfaType === 'SMS'" class="mfa-sms">
          <a-form
            ref="mfaFormRef"
            :model="mfaFormData"
            :rules="mfaRules"
            @finish="handleMfaVerify"
          >
            <a-form-item>
              <a-alert
                type="info"
                message="验证码已发送至您的手机"
                show-icon
                closable
                style="margin-bottom: 16px"
              />
            </a-form-item>

            <a-form-item name="code" label="短信验证码">
              <a-row :gutter="8">
                <a-col :span="14">
                  <a-input
                    v-model:value="mfaFormData.code"
                    size="large"
                    placeholder="请输入6位验证码"
                    maxlength="6"
                    :prefix="h(SafetyOutlined)"
                    allow-clear
                  />
                </a-col>
                <a-col :span="10">
                  <a-button
                    :disabled="countdown > 0"
                    @click="handleSendSms"
                    :loading="sendingSms"
                  >
                    {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                  </a-button>
                </a-col>
              </a-row>
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                size="large"
                :loading="loading"
                block
              >
                验证
              </a-button>
            </a-form-item>
          </a-form>
        </div>

        <!-- Email认证 -->
        <div v-else-if="selectedMfaType === 'EMAIL'" class="mfa-email">
          <a-form
            ref="mfaFormRef"
            :model="mfaFormData"
            :rules="mfaRules"
            @finish="handleMfaVerify"
          >
            <a-form-item>
              <a-alert
                type="info"
                message="验证码已发送至您的邮箱"
                show-icon
                closable
                style="margin-bottom: 16px"
              />
            </a-form-item>

            <a-form-item name="code" label="邮箱验证码">
              <a-input
                v-model:value="mfaFormData.code"
                size="large"
                placeholder="请输入6位验证码"
                maxlength="6"
                :prefix="h(SafetyOutlined)"
                allow-clear
              />
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                size="large"
                :loading="loading"
                block
              >
                验证
              </a-button>
            </a-form-item>
          </a-form>
        </div>

        <div class="mfa-back">
          <a @click="handleBackToCredentials">
            <LeftOutlined /> 返回登录
          </a>
        </div>
      </div>

      <div class="login-footer">
        <p>默认账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  BankOutlined,
  SafetyOutlined,
  MobileOutlined,
  MailOutlined,
  LeftOutlined,
} from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue/es/form'
import type { Rule } from 'ant-design-vue/es/form'
import { authApi, type MfaDeviceType, getMfaDeviceTypeName } from '@/api/auth'
import { isValidTotpCode, isValidVerificationCode } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const mfaFormRef = ref<FormInstance>()
const loading = ref(false)

// 登录步骤：credentials（凭据）或 mfa（多因素认证）
const loginStep = ref<'credentials' | 'mfa'>('credentials')

// 表单数据
const formData = reactive({
  username: 'admin',
  password: '123456',
  tenantCode: '',
  rememberDevice: false,
})

// MFA表单数据
const mfaFormData = reactive({
  code: '',
})

// MFA相关
const availableMfaTypes = ref<MfaDeviceType[]>([])
const selectedMfaType = ref<MfaDeviceType>('TOTP')
const totpSetup = ref<{ qrCodeUrl: string; backupCodes: string[]; verified: boolean } | null>(null)
const countdown = ref(0)
const sendingSms = ref(false)

// 表单验证规则
const rules: Record<string, Rule[]> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在6到32个字符', trigger: 'blur' },
  ],
}

const mfaRules: Record<string, Rule[]> = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (selectedMfaType.value === 'TOTP') {
          return isValidTotpCode(value) ? Promise.resolve() : Promise.reject('请输入6位数字验证码')
        }
        return isValidVerificationCode(value) ? Promise.resolve() : Promise.reject('请输入6位数字验证码')
      },
      trigger: 'blur',
    },
  ],
}

// 提交用户名密码
const handleCredentialsSubmit = async () => {
  loading.value = true
  try {
    const response = await authApi.login({
      username: formData.username,
      password: formData.password,
      tenantCode: formData.tenantCode || undefined,
      mfaTrustToken: userStore.mfaTrustToken || undefined,
    })

    if (response.data.requireMfa) {
      // 需要MFA验证
      loginStep.value = 'mfa'
      availableMfaTypes.value = response.data.mfaType ? [response.data.mfaType] : ['TOTP', 'SMS', 'EMAIL']
      selectedMfaType.value = response.data.mfaType || 'TOTP'

      // 如果是TOTP且需要设置，获取设置信息
      if (selectedMfaType.value === 'TOTP') {
        await handleTotpSetup()
      }

      message.info('请完成多因素认证')
    } else {
      // 不需要MFA或已验证
      await handleLoginSuccess(response.data)
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 处理TOTP设置
const handleTotpSetup = async () => {
  try {
    const response = await authApi.mfaApi?.enableTotp()
    if (response?.data) {
      totpSetup.value = {
        qrCodeUrl: response.data.qrCodeUrl,
        backupCodes: response.data.backupCodes,
        verified: false,
      }
    }
  } catch (error) {
    console.error('获取TOTP设置失败:', error)
  }
}

// MFA验证
const handleMfaVerify = async () => {
  loading.value = true
  try {
    const response = await authApi.mfaVerify({
      code: mfaFormData.code,
      deviceType: selectedMfaType.value,
      trustDevice: formData.rememberDevice,
    })

    if (response.data.success) {
      // 保存信任令牌
      if (response.data.trustToken) {
        userStore.setMfaTrustToken(response.data.trustToken)
      }

      // 如果返回了新的accessToken，说明验证成功
      if (response.data.accessToken) {
        await handleLoginSuccess({ ...response.data, user: response.data.user || (await authApi.getCurrentUser()).data })
      } else {
        // TOTP设置确认
        if (totpSetup.value) {
          totpSetup.value.verified = true
          message.success('TOTP设置成功！请保存备用恢复码')
        }
      }
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '验证失败')
  } finally {
    loading.value = false
  }
}

// 发送短信验证码
const handleSendSms = async () => {
  sendingSms.value = true
  try {
    await authApi.mfaApi?.sendSms()
    message.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error: any) {
    message.error(error.response?.data?.message || '发送失败')
  } finally {
    sendingSms.value = false
  }
}

// 返回登录页面
const handleBackToCredentials = () => {
  loginStep.value = 'credentials'
  mfaFormData.code = ''
  totpSetup.value = null
}

// 登录成功处理
const handleLoginSuccess = async (loginResponse: any) => {
  const { accessToken, user } = loginResponse

  // 保存token
  userStore.setToken(accessToken)
  userStore.setUserInfo(user)

  message.success('登录成功')
  router.push('/dashboard')
}

// 获取MFA类型名称
const getMfaTypeName = (type: MfaDeviceType) => {
  const names: Record<MfaDeviceType, string> = {
    TOTP: '身份验证器',
    SMS: '短信验证',
    EMAIL: '邮箱验证',
  }
  return names[type] || type
}
</script>

<style scoped lang="less">
// 品牌色变量
@primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
@primary-color: #1890ff;
@success-color: #52c41a;
@warning-color: #faad14;
@error-color: #ff4d4f;

// 动画
@transition-base: all 0.3s ease;
@transition-slow: all 0.5s ease;

.login-container {
  width: 100%;
  height: 100vh;
  background: @primary-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  // 动态背景装饰
  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    border-radius: 50%;
    top: -200px;
    left: -200px;
    animation: float 6s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
    border-radius: 50%;
    bottom: -150px;
    right: -150px;
    animation: float 8s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, 30px) scale(1.05); }
}

.login-box {
  width: 460px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 56px 48px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 32px 80px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;

  &:hover {
    box-shadow:
      0 12px 40px rgba(0, 0, 0, 0.15),
      0 40px 100px rgba(0, 0, 0, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo-icon {
    width: 72px;
    height: 72px;
    margin: 0 auto 20px;
    background: @primary-gradient;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
    transition: @transition-base;
    cursor: pointer;

    &:hover {
      transform: scale(1.08) rotate(5deg);
      box-shadow: 0 12px 32px rgba(102, 126, 234, 0.5);
    }
  }

  h1 {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    font-size: 14px;
    color: #8c8c8c;
    font-weight: 400;
  }
}

// 表单样式
.login-form,
.mfa-form {
  margin-bottom: 24px;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
  transition: @transition-base;

  &:hover {
    .ant-input-affix-wrapper {
      border-color: fade(@primary-color, 50%);
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
    }
  }
}

:deep(.ant-input-affix-wrapper) {
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  background: #fafafa;
  transition: @transition-base;
  font-size: 15px;

  &:focus,
  &:focus-within {
    background: #fff;
    border-color: @primary-color;
    box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
  }

  .ant-input {
    font-size: 15px;

    &::placeholder {
      color: #bfbfbf;
    }
  }

  .ant-input-prefix {
    color: #bfbfbf;
    margin-right: 12px;
    font-size: 16px;
    transition: @transition-base;
  }
}

:deep(.ant-form-item:hover .ant-input-affix-wrapper .ant-input-prefix) {
  color: @primary-color;
}

:deep(.ant-input-password) {
  .ant-input-suffix {
    color: #bfbfbf;
    transition: @transition-base;

    &:hover {
      color: @primary-color;
    }
  }
}

// 复选框
:deep(.ant-checkbox-wrapper) {
  font-size: 14px;
  color: #666;
  transition: @transition-base;

  &:hover {
    color: #333;
  }

  .ant-checkbox-checked {
    .ant-checkbox-inner {
      background-color: @primary-color;
      border-color: @primary-color;
    }
  }
}

// 提交按钮
:deep(.ant-btn-primary) {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: @primary-gradient;
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: @transition-base;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
    filter: brightness(1.05);
  }

  &:active {
    transform: translateY(0);
  }

  &[disabled] {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

// MFA 相关样式
.mfa-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(24, 144, 255, 0.08);
  border-radius: 12px;
  border: 1px solid rgba(24, 144, 255, 0.2);
  color: @primary-color;
  font-weight: 500;
}

.mfa-types {
  margin-bottom: 24px;
  text-align: center;

  :deep(.ant-radio-group) {
    display: flex;
    justify-content: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  :deep(.ant-radio-button-wrapper) {
    border-radius: 10px;
    padding: 10px 20px;
    height: auto;
    font-size: 14px;
    transition: @transition-base;

    &:hover {
      transform: translateY(-2px);
    }

    &.ant-radio-button-wrapper-checked {
      background: @primary-gradient;
      border-color: transparent;
      color: #fff;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);

      &::before {
        display: none;
      }
    }
  }
}

// TOTP QR 码
.mfa-totp .qr-code {
  text-align: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(255,255,255,0.9) 0%, rgba(250,250,250,0.95) 100%);
  border-radius: 16px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);

  :deep(.ant-qrcode) {
    padding: 8px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.qr-tip {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 16px;
  font-weight: 500;
}

// 备用码
.backup-codes {
  margin-top: 16px;

  :deep(.ant-collapse) {
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #e8e8e8;

    .ant-collapse-header {
      background: #fafafa;
      font-weight: 500;
      padding: 14px 16px !important;
    }

    .ant-collapse-content-box {
      padding: 20px;
      background: #fff;
    }
  }

  .backup-codes-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 12px;

    .ant-tag {
      font-family: 'Courier New', monospace;
      font-size: 14px;
      padding: 6px 12px;
      border-radius: 8px;
      background: linear-gradient(135deg, rgba(24, 144, 255, 0.1) 0%, rgba(24, 144, 255, 0.05) 100%);
      border: 1px solid rgba(24, 144, 255, 0.2);
      color: @primary-color;
      font-weight: 600;
      letter-spacing: 1px;
    }
  }
}

// SMS/Email 验证码
.mfa-sms,
.mfa-email {
  margin-bottom: 24px;

  :deep(.ant-alert) {
    border-radius: 12px;
    padding: 12px 16px;
    margin-bottom: 16px !important;
  }

  :deep(.ant-row) {
    align-items: center;

    .ant-input-affix-wrapper {
      width: 100%;
    }

    .ant-btn {
      height: 46px;
      border-radius: 12px;
      font-weight: 500;
      white-space: nowrap;

      &:disabled {
        opacity: 0.5;
      }
    }
  }
}

// 返回链接
.mfa-back {
  text-align: center;
  margin-top: 20px;

  a {
    color: #8c8c8c;
    text-decoration: none;
    font-size: 14px;
    transition: @transition-base;
    display: inline-flex;
    align-items: center;
    gap: 6px;

    &:hover {
      color: @primary-color;
      transform: translateX(-4px);
    }
  }
}

// 页脚
.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;

  p {
    color: #bfbfbf;
    font-size: 13px;

    code {
      background: #f5f5f5;
      padding: 2px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      color: @primary-color;
      font-weight: 600;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .login-box {
    width: calc(100% - 32px);
    padding: 40px 24px;
    margin: 16px;
  }

  .login-header {
    margin-bottom: 32px;

    .logo-icon {
      width: 60px;
      height: 60px;
      font-size: 24px;
    }

    h1 {
      font-size: 22px;
    }
  }

  .mfa-types {
    :deep(.ant-radio-button-wrapper) {
      flex: 1;
      text-align: center;
    }
  }
}
</style>
