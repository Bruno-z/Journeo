import { test, expect } from '../fixtures/auth';
import { test as base } from '@playwright/test';

// ─── Login ───────────────────────────────────────────────────────────────────

test.describe('Login', () => {
  test('admin can login and lands on /dashboard', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Se connecter' }).first().click();
    await page.getByRole('dialog').waitFor({ state: 'visible' });

    await page.locator('#modal-email').fill('admin@hws.com');
    await page.locator('#modal-password').fill('admin123');
    await page.getByRole('button', { name: 'Se connecter' }).last().click();

    await page.waitForURL('**/dashboard**');
    await expect(page).toHaveURL(/dashboard/);
  });

  test('regular user can login and lands on /dashboard', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Se connecter' }).first().click();
    await page.getByRole('dialog').waitFor({ state: 'visible' });

    await page.locator('#modal-email').fill('user1@hws.com');
    await page.locator('#modal-password').fill('user123');
    await page.getByRole('button', { name: 'Se connecter' }).last().click();

    await page.waitForURL('**/dashboard**');
    await expect(page).toHaveURL(/dashboard/);
  });

  test('wrong password shows an error message', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Se connecter' }).first().click();
    await page.getByRole('dialog').waitFor({ state: 'visible' });

    await page.locator('#modal-email').fill('admin@hws.com');
    await page.locator('#modal-password').fill('wrongpassword');
    await page.getByRole('button', { name: 'Se connecter' }).last().click();

    // Should stay on landing page with an error visible
    await expect(page.locator('.alert--error')).toBeVisible({ timeout: 5_000 });
    await expect(page).not.toHaveURL(/dashboard/);
  });

  test('admin sees "Admin" badge in navbar', async ({ adminPage: page }) => {
    await expect(page.locator('.navbar__role-badge')).toHaveText('Admin');
  });

  test('regular user does NOT see the Admin badge', async ({ userPage: page }) => {
    await expect(page.locator('.navbar__role-badge')).not.toBeVisible();
  });

  test('display name appears in navbar after login', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Se connecter' }).first().click();
    await page.getByRole('dialog').waitFor({ state: 'visible' });

    await page.locator('#modal-email').fill('admin@hws.com');
    await page.locator('#modal-password').fill('admin123');
    await page.getByRole('button', { name: 'Se connecter' }).last().click();

    await page.waitForURL('**/dashboard**');
    // Navbar should show the user's name (not just email)
    const namePill = page.locator('.navbar__avatar-name');
    await expect(namePill).not.toHaveText('admin@hws.com');
    await expect(namePill).toBeVisible();
  });
});

// ─── Logout ──────────────────────────────────────────────────────────────────

test.describe('Logout', () => {
  test('logout button redirects to landing page', async ({ adminPage: page }) => {
    await page.getByRole('button', { name: 'Se déconnecter' }).click();
    await expect(page).toHaveURL('/');
  });

  test('after logout, navigating to /dashboard redirects to /auth/login', async ({
    adminPage: page,
  }) => {
    await page.getByRole('button', { name: 'Se déconnecter' }).click();
    await page.goto('/dashboard/guides');
    await expect(page).toHaveURL(/auth\/login/);
  });
});

// ─── Unauthenticated access ───────────────────────────────────────────────────

base.describe('Unauthenticated access', () => {
  base.test('visiting /dashboard when not logged in redirects to /auth/login', async ({
    page,
  }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/auth\/login/);
  });

  base.test('visiting /dashboard/guides when not logged in redirects to /auth/login', async ({
    page,
  }) => {
    await page.goto('/dashboard/guides');
    await expect(page).toHaveURL(/auth\/login/);
  });

  base.test('visiting / when logged out shows the landing page', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('button', { name: 'Se connecter' })).toBeVisible();
  });

  base.test('visiting / while logged in redirects to /dashboard', async ({ page }) => {
    // First, login via localStorage injection (faster than UI)
    await page.goto('/');
    await page.evaluate(() => {
      localStorage.setItem('jrn_token', 'fake-but-present');
      localStorage.setItem('jrn_role', 'USER');
      localStorage.setItem('jrn_email', 'user1@hws.com');
    });
    await page.goto('/');
    // publicGuard should redirect logged-in users away from landing page
    await expect(page).toHaveURL(/dashboard/);
  });
});
