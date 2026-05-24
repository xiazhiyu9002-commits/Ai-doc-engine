<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <img src="/logo.png" alt="AI Doc Engine Logo" class="auth-logo-icon" />
        </div>
        <h1 class="auth-title">AI Doc Engine</h1>
        <p class="auth-subtitle">重置密码</p>
      </div>
      <ResetPasswordForm />
    </div>
  </div>
</template>

<script setup lang="ts">
import ResetPasswordForm from '@/components/auth/ResetPasswordForm.vue'
</script>

<style scoped>
.auth-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 24px;
  background: #ffffff;
  overflow: hidden;
}

.auth-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 20px 60px rgba(17, 24, 39, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  backdrop-filter: blur(20px);
  animation: cardEntrance 0.4s ease-out;
}

@keyframes cardEntrance {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.auth-logo-icon {
  width: 32px;
  height: 32px;
  color: white;
}

.auth-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #111827;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.auth-subtitle {
  font-size: 16px;
  font-weight: 400;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .auth-container {
    padding: 20px;
    align-items: flex-start;
    padding-top: 60px;
  }

  .auth-card {
    padding: 40px 32px;
    border-radius: 18px;
  }

  .auth-title {
    font-size: 28px;
  }

  .auth-subtitle {
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .auth-container {
    padding: 16px;
    padding-top: 40px;
  }

  .auth-card {
    padding: 32px 24px;
    border-radius: 16px;
  }

  .auth-logo {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
  }

  .auth-logo-icon {
    width: 28px;
    height: 28px;
  }

  .auth-title {
    font-size: 26px;
  }

  .auth-subtitle {
    font-size: 14px;
  }

  .auth-header {
    margin-bottom: 28px;
  }
}

@media (min-width: 1440px) {
  .auth-card {
    max-width: 480px;
    padding: 56px 48px;
  }
}
</style>
