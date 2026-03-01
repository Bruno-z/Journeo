import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,      // tests run sequentially (shared backend state)
  retries: 1,                // retry once on CI to absorb network flakiness
  timeout: 30_000,           // 30s per test
  expect: { timeout: 8_000 },

  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',   // captures trace on failure for debugging
    screenshot: 'only-on-failure',
    video: 'off',
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],

  // HTML report saved in playwright-report/
  reporter: [['html', { open: 'never' }], ['list']],
});
