<template>
  <div class="login-wrapper">
    <div class="container">
      <h2>Вход</h2>

      <form @submit.prevent="handleLogin">
        <input
            type="text"
            v-model="username"
            placeholder="Имя пользователя"
            required
            :class="{ 'error-border': errorMessage }"
        />
        <input
            type="password"
            v-model="password"
            placeholder="Пароль"
            required
            :class="{ 'error-border': errorMessage }"
        />
        <button type="submit">Войти</button>
      </form>
    </div>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import config from '../config.js';

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

const handleLogin = async () => {
  errorMessage.value = '';

  try {
    const res = await fetch(`http://${config.apiBaseUrl}/auth/sign-in`, {
      method: 'POST',
      credentials: 'include',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    });

    if (res.ok) {
      await router.push('/');
    } else {
      let errorText = '';
      try {
        const errorData = await res.json();
        errorText = errorData.message || errorData.error || 'Неверный логин или пароль';
      } catch {
        errorText = await res.text();
        if (!errorText || errorText.trim() === '') {
          errorText = 'Неверный логин или пароль';
        }
      }

      errorMessage.value = errorText;

      password.value = '';
    }
  } catch (err) {
    errorMessage.value = 'Неверный логин или пароль';
  }
};
</script>

<style scoped>
:root {
  --bg: #e6f2ff; /* голубой фон */
  --card: #ffffff; /* белая форма */
  --brand: #1e40af;
  --ink: #0f172a;
  --line: #cbd5e1;
  --border-radius: 12px;
  --error: #dc2626;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, Helvetica, Arial, sans-serif;
  background-color: #e6f2ff;
}

.login-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #e6f2ff;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 0;
}

.container {
  background-color: var(--card);
  padding: 40px 30px;
  border-radius: var(--border-radius);
  width: 340px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 25px;
  color: var(--ink);
  font-size: 24px;
  font-weight: 700;
}

.login-wrapper {
  background-image: url('../assets/images/background.png');
  background-size: cover;
}

.error-message {
  background-color: #fee2e2;
  color: var(--error);
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  border-left: 4px solid var(--error);
  text-align: left;
  top: 75%;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

input[type='text'],
input[type='password'] {
  width: 100%;
  padding: 14px 16px;
  margin: 10px 0 20px 0;
  border: 1px solid var(--line);
  border-radius: var(--border-radius);
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}

.error-border {
  border-color: var(--error) !important;
}

.error-border:focus {
  border-color: var(--error) !important;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important;
}

button {
  width: 100%;
  padding: 14px;
  background-color: var(--brand);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: background 0.2s;
}

button:hover {
  background-color: #1c3aaf;
}
</style>
