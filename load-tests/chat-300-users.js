import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 300,
  duration: '60s',
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.05']
  }
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const suffix = `${__VU}_${__ITER}`;
  const username = `load_user_${suffix}`;
  const password = 'LoadTest123!';

  const registerResponse = http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email: `${username}@load.test`,
    username,
    password
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  check(registerResponse, {
    'register or already exists': (r) => r.status === 200 || r.status === 400
  });

  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    username,
    password
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  const loginOk = check(loginResponse, {
    'login ok': (r) => r.status === 200
  });

  if (!loginOk) {
    return;
  }

  const token = loginResponse.json('token');

  const unreadResponse = http.get(`${BASE_URL}/api/messages/unread-count`, {
    headers: { Authorization: `Bearer ${token}` }
  });

  check(unreadResponse, {
    'unread count <= 3s and 200': (r) => r.status === 200 && r.timings.duration <= 3000
  });

  sleep(1);
}
