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

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 450px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #8c8c8c;
}

.login-form {
  margin-bottom: 24px;
}

.mfa-form {
  margin-bottom: 24px;
}

.mfa-alert {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mfa-types {
  margin-bottom: 24px;
  text-align: center;
}

.mfa-types .ant-radio-group {
    display: flex;
    justify-content: center;
    gap: 8px;
  }

.mfa-totp .qr-code {
  text-align: center;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.qr-tip {
  font-size: 13px;
  color: #8c8c8c;
  margin-top: 8px;
}

.backup-codes {
  margin-top: 16px;
}

.backup-codes-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.backup-codes-list .ant-tag {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  padding: 4px 8px;
}

.mfa-sms,
.mfa-email {
  margin-bottom: 24px;
}

.mfa-back {
  text-align: center;
  margin-top: 16px;
}

.mfa-back a {
  color: #1890ff;
  text-decoration: none;
}

.login-footer {
  text-align: center;
  color: #8c8c8c;
  font-size: 13px;
}
</style>
