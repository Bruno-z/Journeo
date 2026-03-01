import { test as base, type Page } from '@playwright/test';

/**
 * Opens the auth modal and logs in via the landing page.
 * Waits until the dashboard is visible before returning.
 */
export async function loginAs(page: Page, email: string, password: string) {
  await page.goto('/');

  // Open the login modal
  await page.getByRole('button', { name: 'Se connecter' }).first().click();
  await page.getByRole('dialog').waitFor({ state: 'visible' });

  // Fill in the login form
  await page.locator('#modal-email').fill(email);
  await page.locator('#modal-password').fill(password);
  await page.getByRole('button', { name: 'Se connecter' }).last().click();

  // Wait for redirect to dashboard
  await page.waitForURL('**/dashboard**', { timeout: 10_000 });
}

// ── Extended test fixtures ────────────────────────────────────────────────────

type AuthFixtures = {
  adminPage: Page;
  userPage: Page;
};

export const test = base.extend<AuthFixtures>({
  adminPage: async ({ page }, use) => {
    await loginAs(page, 'admin@hws.com', 'admin123');
    await use(page);
  },

  userPage: async ({ page }, use) => {
    await loginAs(page, 'user1@hws.com', 'user123');
    await use(page);
  },
});

export { expect } from '@playwright/test';
